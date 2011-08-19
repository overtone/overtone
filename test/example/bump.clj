(ns example.bump
  (:use overtone.live)
  (:use [overtone.music.instrument synth drum]))

(definst beep [note 60 vol 0.2]
  (let [freq (midicps note)
        src (sin-osc freq)
        env (env-gen (perc 0.3 2) :action FREE)]
    (* vol src env)))

(defn play [synth pitch-classes & args]
  (doall (map #(apply synth % args) pitch-classes)))

(defn play-seq [count synth notes vels durs time odds]
  (when (and notes durs)
    (let [dur   (- (/ (first durs) 1.2) 10 (rand-int 20))
          pitch (first notes)
          vel (first vels)
          n-time (+ time dur)]
      (at time
          (kick)

          (when (zero? count)
            (bass (midi->hz (first pitch)) (* 4 (/ dur 1000.0))))

          (when (#{1 3} count)
            (if (> (rand) (- 1 odds))
              (bass (midi->hz (first pitch)) (* 4 (/ dur 1000.0 2)) 0.1))
            (snare))

          (play synth pitch :amp vel))
      (at (+ time (* 0.5 dur))
          (c-hat 0.5))
      (apply-at n-time #'play-seq (mod (inc count) 4)
                synth
                (next notes) (next vels) (next durs)
                n-time odds []))))

; TODO: Strum the chord

(def bump-chords
  [:i  :major
   :i  :major
   :i  :major7
   :i  :major
   :i  :major
   :i  :major
   :i  :major
   :i  :major
   :i  :major
   :i  :major
   :i  :major
   :v  :major])

; Bass note on the one
(def bass-line (map first (partition 4 blues-chords)))

(defn progression [chord-seq key-note octave scale]
  (for [[roman-numeral chord-type] (partition 2 chord-seq)]
    (chord (+ (key-note NOTE)
              (degree->interval scale roman-numeral))
           chord-type
           octave)))

; Be sure to try moving the mouse around...
(defn bump []
  (play-seq 0 ks1-demo
            (cycle (map sort (progression bump-chords :a 2 :ionian)))
            (cycle [0.6])
            (cycle [1000])
            (now)
            0.5))
(bump)
