(ns overtone.studio.core
  (:use
    [overtone util event time-utils]
    [overtone.sc.ugen.defaults]
    [overtone.sc core synth ugen envelope node synthdef]
    [overtone.music rhythm]))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments*  (ref {}))
(defonce inst-group*   (ref nil))
(defonce mixer-group*  (ref nil))
(defonce mixer-id*     (ref nil))
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

(defn start-mixer []
  (let [mix (mixer :tgt @mixer-group*)]
    (dosync (ref-set mixer-id* mix))))

(defn setup-studio []
  (let [g (group :tail ROOT-GROUP)
        m (group :tail ROOT-GROUP)
        r (group :tail ROOT-GROUP)]
    (dosync
      (ref-set inst-group* g)
      (ref-set mixer-group* m)
      (ref-set record-group* r)
      (ref-set instruments* (map-vals #(assoc % :group (group :tail g)) 
                                      @instruments*)))
    (start-mixer)))

(on-sync-event :connected :studio-setup setup-studio)

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

(defn inst-prefix [ugens constants]
  (let [root (last ugens)]
        (if (and (ugen? root)
                 (or (= 0 (:n-outputs root))
                     (OUTPUT-UGENS (:name root))
                     (= :kr (get REVERSE-RATES (:rate root)))))
          [ugens constants]
          (let [pan-chans (pan2 root)
                pan (:ugen (first pan-chans))]
            [(conj ugens pan (out MIXER-BUS pan-chans)) (set (floatify (conj constants MIXER-BUS 1)))]))))

(defmacro inst [sname & args]
  `(let [[sname# params# ugens# constants#] (pre-synth ~sname ~@args)
         [ugens# constants#] (inst-prefix ugens# constants#)
         sdef# (synthdef sname# params# ugens# constants#)
         sgroup# (or (:group (get @instruments* sname#))
                     (if (connected?)
                       (group :tail @inst-group*)
                       nil))
         param-names# (map first (partition 2 params#))
         s-player# (synth-player sname# param-names#)
         player# (fn [& play-args#]
                   (apply s-player# :tgt (:group (get @instruments* sname#)) play-args#))
         inst# (callable-map {:type ::instrument
                              :name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :doc "This is a test."
                              :group sgroup#
                              :player player#}
                             player#)]

     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defn inst? [o]
  (and (associative? o)
       (= ::instrument (:type o))))

(defmacro definst [i-name & inst-form]
  (let [[md params ugen-form] (synth-form i-name inst-form)
        md (assoc md :type ::instrument)]
    (list 'def i-name ;(with-meta i-name md)
       `(inst ~i-name ~params ~ugen-form))))

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

(defonce session* (ref {:metro (metronome 120)
                    :tracks {}
                    :playing false}))

(defn track [tname inst]
  (let [t {:type :track
           :name tname
           :inst inst
           :note-fn nil}]
    (dosync (alter session* assoc-in [:tracks tname] t))))

(defn track-fn [tname f]
  (dosync (alter session* assoc-in [:tracks tname :note-fn] f)))

(defn remove-track-fn [tname]
  (dosync (alter session* dissoc-in [:tracks tname :note-fn])))

(defn session-metro [m]
  (dosync (alter session* assoc :metro m)))

(defn- session-player [beat]
  (when (:playing @session*)
    (let [{:keys [metro tracks]} @session*
          tick (metro beat)
          next-beat (inc beat)
          next-tick (metro next-beat)]
      (format "tick: %f\nnext: %d\nnext-tick: %f" tick next-beat next-tick)
      (at tick
        (doseq [[_ {:keys [inst note-fn]}] tracks]
          (if note-fn
            (if-let [args (note-fn)]
              (apply inst args)))))
      (apply-at next-tick #'session-player [next-beat]))))

(defn session-play []
  (dosync (alter session* assoc :playing true))
  (session-player ((:metro @session*))))

(defn session-stop []
  (dosync (alter session* assoc :playing false)))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    ;(println "loading synth: " (:name synth))
    (load-synthdef synth)))

