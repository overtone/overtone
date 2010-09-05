(ns overtone.studio
  (:use (overtone.core sc synth ugen envelope util event)))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments* (ref {}))
(defonce inst-group* (ref nil))

; Re-create the instrument groups after a reset
(defn- create-inst-groups []
  (println "resetting groups: " @inst-group*)
  (println "groups: " (map :group (vals @instruments*)))
  (println "all-ids: " (all-ids :node))
  (println "status: " (status))
  (dosync 
    (ref-set inst-group* (group :head ROOT-GROUP))
    (ref-set instruments* (doall 
                            (zipmap (keys @instruments*)
                                    (map #(assoc % :group (group :tail @inst-group*))
                                         (vals @instruments*))))))
  (println "all-ids: " (all-ids :node))
  (println "status: " (status))
  (println "groups: " (map :group (vals @instruments*))))

(defonce _reset_inst (on :reset #'create-inst-groups))

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
(defn inst-prefix [ugens]
  (if (and (ugen? ugens)
           (or (= 0 (:n-outputs ugens))
               (OUTPUT-UGENS (:name ugens))
               (= :kr (get REVERSE-RATES (:rate ugens)))))
    ugens
    (overtone.ugens/out 0 (overtone.ugens/pan2 ugens))))

(defmacro inst [sname & args]
  (println "inst: " sname "\nargs: " args)
  `(let [[sname# param-map# ugens#] (pre-synth ~sname ~@args)
         ugens# (inst-prefix ugens#)
         sdef# (synthdef sname# param-map# ugens#)
         sgroup# (or (:group (get @instruments* sname#))
                     (group :tail @inst-group*))
         param-names# (keys param-map#)
         player# (partial (synth-player sname# param-names#) :tgt sgroup#)
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
  (let [md (if (string? (first inst-form)) {:doc (first inst-form)} {})
        nsym (with-meta i-name md)]
    `(def ~nsym
       (inst ~i-name ~@inst-form))))

(if (and (nil? @inst-group*) 
         (connected?))
  (dosync (ref-set inst-group* (group :head ROOT-GROUP))))
