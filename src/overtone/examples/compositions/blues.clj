(ns overtone.examples.compositions.blues
  (:use overtone.live)
  (:use [overtone.inst synth drum]))

(definst beep [note 60 amp 0.2]
  (let [freq (midicps note)
        src (sin-osc freq)
        env (env-gen (perc 0.3 2) :action FREE)]
    (* amp src env)))

(def ps (atom []))

(defn play-blues [instr pitch-classes]
  (doseq [pitch pitch-classes]
    (swap! ps conj pitch)
    (instr pitch)))

(defn play-seq [count instr notes durs time odds]
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

          (play-blues instr pitch))
      (at (+ time (* 0.5 dur))
          (closed-hat 0.1))
      (apply-by n-time #'play-seq
                [(mod (inc count) 4) instr (next notes) (next durs) n-time odds]))))

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
    (chord (+ (note (str (name key-note) octave))
              (degree->interval roman-numeral scale))
           chord-type)))

(defn blue-beep []
  (play-seq 0 beep
            (cycle (mapcat #(repeat 4 %) (map sort (progression blues-chords :a 3 :ionian))))
            (cycle [1200 1204 1195 1206])
            (now)
            0.2))

;;(blue-beep)
(stop)

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
;(blue-ks1-demo)
;(stop)
