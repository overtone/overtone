(ns overtone.lib.drums
  (:use overtone.live))

;* Pattern based rhythms
; - define piano rolls of triggers and assign instruments to each channel
;
;; Using 1/4 notes
;(def house-beat {:kick  [O _ _ _ O _ _ _ O _ _ _ O _ _ _]
;                 :o-hat [_ _ O _ _ _ O _ _ _ O _ _ _ O _]
;                 :clap  [_ _ _ _ O _ _ _ _ _ _ _ O _ _ _]})
;
;(make-beat house-beat {:base "kick.wav" 
;                       :o-hat "hat.wav"
;                       :clap "clap.wav"})
;
;* Archaeopteryx style rhythm queue, so you can push no rhythms on to start playing, but then pop off to go back to where you were before.
;
;* Keep a ref var pointing to the current rhythm function(s)
;-- returns true if it should play on the next beat?
;-- modify the ref and you get a new beat on the fly

(def *drums (ref []))
(def *drum-count (ref 0))

(defn drum [voice pattern]
  (dosync (alter *drums conj [voice pattern])))

(defn clear-drums []
  (dosync (ref-set *drums [])))

(defn play-drums [tempo beat-count]
  (periodic (fn []
              (let [num (rand)
                    i   @*drum-count]
                (doseq [[voice pattern] @*drums]
                  (if (< num (nth pattern i))
                    (hit voice :pitch 50 :dur 200)))
                (dosync (ref-set *drum-count (mod (inc @*drum-count) beat-count)))))
            tempo))

