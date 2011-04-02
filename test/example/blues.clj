(ns example.blues
  (:use overtone.live)
  (:use [overtone.inst synth drum]))

(definst beep [note 60 vol 0.2]
  (let [freq (midicps note)
        src (sin-osc freq)
        env (env-gen (perc 0.3 2) :action :free)]
    (* vol src env)))

(defn play [synth pitch-classes]
  (doseq [pitch pitch-classes]
    (synth pitch)))

(defn play-seq [count synth notes durs time odds]
  (when (and notes durs)
    (let [dur   (- (/ (first durs) 1.2) 10 (rand-int 20))
          pitch (first notes)
          n-time (+ time dur)]
      (at time
          (when (> (rand) (- 1 odds))
            (tom))

          (when (zero? count)
            (kick)
            (bass (midi->hz (first pitch)) (* 4 (/ dur 1000.0))))

          (when (#{1 3} count)
            (if (> (rand) (- 1 odds))
              (bass (midi->hz (first pitch)) (* 4 (/ dur 1000.0 2)) 0.1))
            (snare))

          (when (= 2 count)
            (kick))

          (play synth pitch))
      (at (+ time (* 0.5 dur))
          (c-hat 0.1))
      (apply-at n-time #'play-seq 
                [(mod (inc count) 4) synth (next notes) (next durs) n-time odds]))))

; TODO: Strum the chord

(def blues-chords
  [:i  :major
   :iv :major
   :i  :major7
   :i  :7
   :iv :major
   :iv :7
   :i  :major
   :i  :major
   :v  :major
   :v  :7
   :i  :major
   :v  :7])

; Bass note on the one
(def bass-line (map first (partition 4 blues-chords)))

(defn progression [chord-seq key-note octave scale]
  (for [[roman-numeral chord-type] (partition 2 chord-seq)]
    (chord (+ (key-note NOTE)
              (degree->interval scale roman-numeral))
           chord-type
           octave)))

(defn blue-beep []
  (play-seq beep
            (cycle (map sort (progression blues-chords :a 3 :ionian)))
            (cycle [1200 1204 1195 1206])
            (now)
            0.2))

;(blue-beep)

; Be sure to try moving the mouse around...
(defn blue-ks1 []
  (play-seq 0 ks1
            (cycle (map sort (progression blues-chords :a 2 :ionian)))
            (take 80 (map #(* 1.5 %) (cycle [530 524 532 528])))
            (now)
            0.5))

(defn blue-ks1-demo []
  (play-seq 0 ks1-demo
            (cycle (map sort (progression blues-chords :a 2 :ionian)))
            (take 80 (map #(* 1.5 %) (cycle [530 524 532 528])))
            (now)
            0.5))

;(blue-ks1)


