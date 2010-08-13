(ns overtone.lib.inst
  (:use (overtone.core sc synth envelope)
        (overtone.music pitch)))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(def instruments* (ref {}))

(defn load-instruments []
  (doseq [[sname sdef] @instruments*]
    (load-synthdef sdef)))

(add-boot-handler load-instruments :load-instruments)

(defn load-inst [sdef]
  (let [inst {:name (keyword (:name sdef))
              :sdef sdef
              :group (group :tail 0)}]
  (dosync (alter instruments* assoc (:name sdef) sdef))
  (if (connected?)
    (load-synthdef sdef))))

(defmacro inst [& args]
  `(load-inst (synth ~@args)))

