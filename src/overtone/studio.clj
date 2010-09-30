(ns overtone.studio
  (:use [overtone.core sc synth ugen envelope util event time-utils]
        [overtone.music rhythm]))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments* (ref {}))
(defonce inst-group* (ref nil))

(defn create-inst-group []
  (let [g (group :tail ROOT-GROUP)]
    (dosync (ref-set inst-group* g))))

(defonce _on-connect_ (on :connected create-inst-group))

; Clear and re-create the instrument groups after a reset
(defn reset-inst-groups []
  (doseq [inst @instruments*]
    (group-clear (:group inst))))

(defonce _reset_inst (on :reset #'reset-inst-groups))

; Add instruments to the session when defined
(defn add-instrument [inst]
  (let [i-name (:name inst)]
    (dosync (alter instruments* assoc i-name inst))
    i-name))

(defn remove-instrument [i-name]
  (dosync (alter instruments* dissoc i-name)))

(defn clear-instruments []
  (dosync (ref-set instruments* {})))

;(def synth-prefix* (ref #(out 0 (pan2 %))))
;
;(defn set-synth-prefix [prefix-fn]
;  (dosync (ref-set synth-prefix* prefix-fn)))

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
                     (group :tail @inst-group*))
         param-names# (map first (partition 2 params#))
         s-player# (synth-player sname# param-names#)
         player# (fn [& play-args#]
                   (apply s-player# :tgt (:group (get @instruments* sname#)) play-args#))
         inst# (callable-map {:name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :doc nil
                              :group sgroup#
                              :player player#}
                             player#)]
     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defmacro definst [i-name & inst-form]
  (let [[md params ugen-form] (synth-form i-name inst-form)]
    (list 'def (with-meta i-name md)
       `(inst ~i-name ~params ~ugen-form))))

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
