(ns overtone.studio
  (:use [overtone.core sc synth ugen envelope util event time-utils]
        [overtone.music rhythm]))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments* (ref {}))
(defonce inst-group* (ref nil))

(defn create-inst-groups []
  (let [g (group :tail ROOT-GROUP)]
    (dosync
      (ref-set inst-group* g)
      (map-vals #(assoc % :group (group :tail g)) @instruments*))))

(on-sync-event :connected :create-instrument-groups create-inst-groups)

;; Clear and re-create the instrument groups after a reset
;; TODO: re-create the instrument groups
(defn reset-inst-groups []
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

(defn inst-prefix [ugens]
  (if (and (ugen? ugens)
           (or (= 0 (:n-outputs ugens))
               (OUTPUT-UGENS (:name ugens))
               (= :kr (get REVERSE-RATES (:rate ugens)))))
    ugens
    (out 0 (pan2 ugens))))

(defmacro inst [sname & args]
  ;(println "inst: " sname "\nargs: " args)
  `(let [[sname# params# ugens#] (pre-synth ~sname ~@args)
         ugens# (inst-prefix ugens#)
         sdef# (synthdef sname# params# ugens#)
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

     (if (connected?)
       (load-synthdef sdef#))
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

(defmethod overtone.core.sc/kill :overtone.studio/instrument
  [& args]
  (doseq [inst args]
    (group-clear (:group inst))))

(if (and (nil? @inst-group*)
         (connected?))
  (dosync (ref-set inst-group* (group :head ROOT-GROUP))))

(def session* (ref {:metro (metronome 120)
                    :tracks {}
                    :playing false}))

(defn track [tname inst]
  (let [t {:type :track
           :name tname
           :inst inst
           :note-fn nil}]
    (dosync (alter session* assoc-in [:tracks tname] t))))

(defn notes [tname f]
  (dosync (alter session* assoc-in [:tracks tname :note-fn] f)))

(defn session-metro [m]
  (dosync (alter session* assoc :metro m)))

(defn- session-player [b]
  (when (:playing @session*)
    (let [{:keys [metro tracks]} @session*]
      (doseq [[_ {:keys [inst note-fn]}] tracks]
        (if note-fn
          (if-let [n (note-fn)]
            (inst n))))
      (apply-at #'session-player (metro (inc b)) (inc b)))))

(defn session-play []
  (dosync (alter session* assoc :playing true))
  (session-player ((:metro @session*))))

(defn session-stop []
  (dosync (alter session* assoc :playing false)))

(def _ nil)
(def X 440)
(def x 220)

;(definst foo [freq 440]
;  (* 0.1
;     (env-gen (perc 0.1 0.4) 1 1 0 1 :free)
;     (rlpf (saw [freq (* 0.98 freq)])
;           (mul-add (sin-osc:kr 30) 100 (* 1.8 freq)) 0.2)))
;
;(definst kick [freq 100]
;  (* 0.1
;     (env-gen (perc 0.01 0.3) 1 1 0 1 :free)
;     (sin-osc freq)))
;
;(defn test-session []
;  (track :kick #'kick)
;  (notes :kick (fn [] 80))
;
;  (track :foo #'foo)
;  (notes :foo #(if (> (rand) 0.7) (+ 300 (rand-int 500))))
;  (session-play))
;
