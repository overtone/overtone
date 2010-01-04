(ns overtone.inst
  (:use (overtone synth envelope pitch)))

; An instrument abstracts the more basic concept of a synthesizer used by 
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the 
; instances of that group.  (Likewise for controlling them...)

(def instruments* (ref {}))

(defn load-instruments []
  (doseq [[sname sdef] @instruments*]
    (overtone.sc/load-synthdef sdef)))

(overtone.sc/add-boot-handler load-instruments :load-instruments)

(defn load-inst [sdef]
  (let [inst {:name (keyword (:name sdef))
              :sdef sdef
              :group (overtone.sc/group :tail 0)}]
  (dosync (alter instruments* assoc (:name sdef) sdef))
  (if (overtone.sc/connected?)
    (overtone.sc/load-synthdef sdef))))

(defmacro inst [& args]
  `(load-inst (synth ~@args)))

