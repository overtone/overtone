(ns overtone.studio.core
  (:use
    [overtone util event time-utils deps]
    [overtone.sc.ugen.defaults]
    [overtone.sc core synth ugen envelope node synthdef bus]
    [overtone.music rhythm]))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments*  (ref {}))
(defonce inst-group*   (ref nil))
(defonce mixer-group*  (ref nil))
(defonce mixer-id*     (ref nil))
(defonce fx-group*     (ref nil))
(defonce record-group* (ref nil))

(defonce MIXER-BUS 10)

; A mixer synth for volume, pan, and limiting
; TODO: Add basic EQ
(defsynth mixer [in-bus 10 out-bus 0
                 volume 0.5 pan 0.0
                 threshold 0.7
                 slope-below 1 slope-above 0.1
                 clamp-time 0.005 relax-time 0.005]
  (let [source  (in in-bus)
        limited (compander source source threshold
                           slope-below slope-above
                           clamp-time relax-time)]
    (out out-bus (pan2 limited pan volume))))

(defn volume
  "Master volume control on the mixer."
  [vol]
  (ctl @mixer-id* :volume vol))

(defn pan
  "Master pan control on the mixer."
  [pan]
  (ctl @mixer-id* :pan pan))

(defn inst-volume
  "Control the volume for a single instrument."
  [inst vol]
  (ctl inst :volume vol))

(defn inst-out-bus
  "Set an instruments downstream bus."
  [inst bus]
  (let [ins-name (:name inst)]
    (ctl inst :out-bus bus)
    (dosync
      (alter instruments* assoc-in [ins-name :out-bus] bus))))

(defn inst-fx
  "Append an effect to an instrument channel."
  [inst fx]
  (let [ins-name (:name inst)
        fx-chain (:fx-chain (get @instruments* ins-name))
        bus (audio-bus)
        fx-id (fx :tgt @fx-group* :pos :tail :in-bus bus :out-bus MIXER-BUS)
        src  (if (empty? fx-chain)
               inst
               (:fx-id (last fx-chain)))
        entry {:fx fx
               :fx-id fx-id
               :bus bus
               :src src}
        fx-chain (conj fx-chain entry)]
    (if (= src inst)
      (inst-out-bus inst bus)
      (ctl src :out-bus bus))
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] fx-chain))
    entry))

(comment defn remove-fx
  [inst fx]
  (let [ins-name (:name inst)]
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] fx-chain))))

(defn clear-fx
  [inst]
  (inst-out-bus inst MIXER-BUS)
  (let [ins-name (:name inst)
        fx-chain (:fx-chain (get @instruments* ins-name))]
    (doseq [id (map :fx-id fx-chain)]
      (kill id))
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] [])))
  :clear)

(defn start-mixer []
  (Thread/sleep 2000)
  (let [mix (mixer :tgt @mixer-group*)]
    (dosync (ref-set mixer-id* mix))))

(on-deps :studio-setup-completed ::start-mixer start-mixer)

(defn setup-studio []
  (let [g (group :head ROOT-GROUP)
        f (group :after g)
        m (group :tail ROOT-GROUP)
        r (group :tail ROOT-GROUP)]
    (dosync
      (ref-set inst-group* g)
      (ref-set fx-group* g)
      (ref-set mixer-group* m)
      (ref-set record-group* r)
      (ref-set instruments* (map-vals #(assoc % :group (group :tail g))
                                      @instruments*)))
    (satisfy-deps :studio-setup-completed)))

(on-deps :connected ::setup-studio setup-studio)

;; Clear and re-create the instrument groups after a reset
;; TODO: re-create the instrument groups
(defn reset-inst-groups
  "Frees all synth notes for each of the current instruments"
  []
  (doseq [[name inst] @instruments*]
    (group-clear (:group inst))))

(on-sync-event :reset :reset-instruments reset-inst-groups)

; Add instruments to the session when defined
(defn add-instrument [inst]
  (let [i-name (:name inst)]
    (dosync (alter instruments* assoc i-name inst))
    i-name))

(defn remove-instrument [i-name]
  (dosync (alter instruments* dissoc i-name)))

(defn clear-instruments []
  (dosync (ref-set instruments* {})))

; When there is a single channel audio output add pan2 and out ugens
; to make all instruments stereo by default.
(def OUTPUT-UGENS #{"Out" "RecordBuf" "DiskOut" "LocalOut" "OffsetOut" "ReplaceOut" "SharedOut" "XOut"})

(def DEFAULT-INST-VOLUME 0.6)

(defn inst-prefix
  "Wraps the patch with an out ugen and a volume control, routing it to the master mixer.
  (inst (sin-osc 440))
  becomes:
  (out MIXER-BUS (pan2 (sin-osc 440)))
  "
  [params ugens constants]
  (let [root (last ugens)
        out-bus (control-proxy "out-bus" MIXER-BUS)
        volume (control-proxy "volume" DEFAULT-INST-VOLUME)
        vol-ugen (with-ugens (* volume root))
        out-ugen (with-ugens (out out-bus vol-ugen))]
    [(concat params
             ["out-bus" MIXER-BUS "volume" DEFAULT-INST-VOLUME])
     (concat ugens
             [vol-ugen out-ugen])
     (set (floatify (conj constants MIXER-BUS 1 0)))]))

(defmacro inst [sname & args]
  `(let [[sname# params# ugens# constants#] (pre-synth ~sname ~@args)
         [params# ugens# constants#] (inst-prefix params# ugens# constants#)
         sdef# (synthdef sname# params# ugens# constants#)
         sgroup# (or (:group (get @instruments* sname#))
                     (if (connected?)
                       (group :tail @inst-group*)
                       nil))
         param-names# (map first (partition 2 params#))
         s-player# (synth-player sname# param-names#)
         player# (fn [& play-args#]
                   (let [ins# (get @instruments* sname#)
                         play-args# (if (and
                                          (= 1 (count play-args#))
                                          (map? (first play-args#)))
                                      (flatten (seq (first play-args#)))
                                      play-args#)
                         pargs# (concat play-args# [:out-bus (:out-bus ins#)])]
                     (apply s-player#
                            :tgt (:group ins#)
                            pargs#)))
         inst# (callable-map {:type ::instrument
                              :name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :doc "This is a test."
                              :group sgroup#
                              :out-bus MIXER-BUS
                              :fx-chain []
                              :player player#
                              :args param-names#}
                             player#)]

     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defn inst? [o]
  (and (associative? o)
       (= ::instrument (:type o))))

(defmacro definst [i-name & inst-form]
  (let [i-name (with-meta i-name {:type ::instrument})
        [i-name params ugen-form] (synth-form i-name inst-form)]
    `(def ~i-name (inst ~i-name ~params ~ugen-form))))

(defmethod print-method ::instrument [ins w]
  (let [info (meta ins)]
    (.write w (format "#<instrument: %s>" (:name info)))))

(defmethod overtone.sc.node/kill :overtone.studio.core/instrument
  [& args]
  (doseq [inst args]
    (group-clear (:group inst))))

(defmethod overtone.sc.node/ctl :overtone.studio.core/instrument
  [inst & ctls]
  (apply node-control (:group inst) ctls))

(if (and (nil? @inst-group*)
         (connected?))
  (dosync (ref-set inst-group* (group :head ROOT-GROUP))))

(defonce session* (ref
                    {:metro (metronome 120)
                     :tracks {}
                     :playing false}))

(defn track [tname inst]
  (let [t {:type :track
           :name tname
           :inst inst
           :note-fn nil}]
    (dosync (alter session* assoc-in [:tracks tname] t))))

(defn remove-track
  [tname]
  (dosync (alter session* dissoc-in [:tracks] tname)))

(defn track-fn [tname f]
  (dosync (alter session* assoc-in [:tracks tname :note-fn] f)))

(defn remove-track-fn [tname]
  (dosync (alter session* dissoc-in [:tracks tname] :note-fn)))

(defn session-metro [m]
  (dosync (alter session* assoc :metro m)))

(defn track-start
  [t]
  )

(defn track-stop
  [t]
  )

;(def m (:metro @session*))
;(f m (m) kick)

(defn playing?
  []
  (:playing @session*))

(defn session-play
  "Call the player functions for all tracks with the session metronome,
  and the appropriate track instrument."
  []
  (let [metro (:metro @session*)
        beat (inc (metro))]
    (dosync (alter session* assoc :playing true))
    (doseq [[_ t] (:tracks @session*)]
      ((:note-fn t) metro beat (:inst t)))))

(defn session-stop []
  (dosync (alter session* assoc :playing false)))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    ;(println "loading synth: " (:name synth))
    (load-synthdef synth)))

