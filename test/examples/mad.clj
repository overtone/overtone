(ns examples.mad
  (:use overtone.live
        overtone.inst.synth))

; Adapted from the music as data project, cool stuff!

(definst tone [note 60 amp 0.3 dur 0.4]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc 0.01 dur) :action :free)]
    (* env snd amp)))

(defrecord note
  [synth vol pitch dur data])

(defn p
  ([elements] 
   (p elements (now)))
  ([[{:keys [synth vol pitch dur data]} & elements] t]
   (let [next-t (+ t (int (* 1000 dur)))]
     (at t
         (synth pitch vol dur))
     (when elements
       (apply-at next-t #'p elements [next-t])))))

(declare calc-duration)

(defn pattern
  ([m-element] (pattern m-element 1))
  ([m-element duration]
   (if (= (type []) (type m-element))
     (flatten
       (calc-duration m-element duration (count m-element)))
     (assoc m-element :dur (float duration)))))

(defn calc-duration 
  [elements duration count]
  (map #(pattern % (/ duration count))
       elements))

(defn def-notes 
  "Define vars for all notes."
  []
  (doseq [octave (range 8)]
    (doseq [n (range 7)] 
      (let [n-char (char (+ 65 n))
            n-sym (symbol (str n-char octave))
            note (octave-note octave (get NOTE (keyword (str n-char))))]
      (intern *ns* n-sym
              {:synth tone
               :vol 0.2
               :pitch note 
               :dur 0.1
               :data []})))))

(def-notes)

; run this to play the pattern
;(p (pattern [[E4 G4 E4] [E5 B4 G4 D4 A4 E4 G4 A4]] 2))

; or this to play it forever
;(p (cycle (pattern [[E4 G4 E4] [E5 B4 G4 D4 A4 E4 G4 A4]], 2)))

; call stop to kill the loop
;(stop)
