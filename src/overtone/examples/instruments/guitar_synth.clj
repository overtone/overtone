(ns overtone.examples.instruments.guitar-synth
  ^{:doc "Guitar from overtone.synth.stringed usage examples"
    :author "Roger Allen"}
  (:use [overtone.live]
        [overtone.synth.stringed]))

;; ======================================================================
;; make a guitar
(def g (guitar))
;; strum it on your own
(guitar-strum g :E :down 0.25)
(guitar-strum g :E :up 0.75)
(guitar-strum g :B :down 0.25)
(guitar-strum g :A :up 0.5)
;; bow down to the power chord!
(ctl g :pre-amp 4.0 :distort 0.99)
(guitar-strum g [0 2 2 -1 -1 -1])
(guitar-strum g [3 5 5 -1 -1 -1])
;; mute all strings
(guitar-strum g [-1 -1 -1 -1 -1 -1])

;; ======================================================================
;; try out a bit of rhythmic accompanyment
;; http://www.youtube.com/watch?v=DV1ANPOYuH8
;; http://www.guitar.gg/strumming.html
(defn pattern-to-beat-strum-seq
  "given a string describing a one-measure up/down strum pattern like
  'ud-udu-', return a sequence of vector [beats :up/:down] pairs"
  [cur-pattern]
  (let [strums-per-measure (count cur-pattern)
        beats-per-measure 4.0
        beats-per-strum (/ beats-per-measure strums-per-measure)
        ud-keywords {\u :up, \d :down}]
    (for [[i s] (map-indexed vector cur-pattern)]
      (when (contains? ud-keywords s)
        [(* i beats-per-strum) (ud-keywords s)]))))
(defn strum-pattern [the-guitar metro cur-measure cur-chord cur-pattern]
  (let [cur-beat (* 4 cur-measure)]
    (doall
     (doseq [[b d] (pattern-to-beat-strum-seq cur-pattern)]
       (when-not (= b nil)
         (guitar-strum the-guitar cur-chord d 0.07 (metro (+ b cur-beat))))))))

;; play a variety of different rhythm patterns.
(ctl g :pre-amp 10.0 :amp 1.0 :distort 0.0)
(do ;; strumming practice
  (let [metro (metronome 100)]
    (doall
     (doseq [[i c] (map-indexed vector [:Gadd5 :Gadd5 :Cadd9 :Cadd9
                                        :Dsus4 :Dsus4 :Gadd5 :Cadd9
                                        :Gadd5 :Cadd9])]
       (strum-pattern g metro i c "d-du-ud-")))))
(do ;; knocking on heaven's door
  (let [metro (metronome 100)]
    (doall
     (doseq [[i c] (map-indexed vector [:Gadd5 :Dsus4 :Am :Am
                                        :Gadd5 :Dsus4 :Am :Am
                                        :Gadd5 :Dsus4 :Cadd9 :Cadd9])]
       (strum-pattern g metro i c "d-du-udu")))))
(do ;; 16th notes.
  (let [metro (metronome 90)]
    (doall
     (doseq [[i c] (map-indexed vector [:Gadd5 :Cadd9 :Gadd5 :Cadd9])]
       (strum-pattern g metro i c "d---d---dudu-ud-")))))

;; ======================================================================
;; ac/dc's highway to hell intro.  turn it up!
(defn ddd0 []
  (let [t (now) dt 250]
    (guitar-strum g [-1  0  2  2  2 -1] :down 0.01 (+ t (* 0 dt)))
    (guitar-strum g [-1  0  2  2  2 -1] :up   0.01 (+ t (* 1 dt)))
    (guitar-strum g [-1  0  2  2  2 -1] :down 0.01 (+ t (* 2 dt) 50))
    (guitar-strum g [-1 -1 -1 -1 -1 -1] :down 0.01 (+ t (* 3.5 dt)))))
(defn ddd1 []
  (let [t (now) dt 250]
    (guitar-strum g [ 2 -1  0  2  3 -1] :down 0.01 (+ t (* 0 dt)))
    (guitar-strum g [ 2 -1  0  2  3 -1] :up   0.01 (+ t (* 1 dt)))
    (guitar-strum g [ 3 -1  0  0  3 -1] :down 0.01 (+ t (* 2 dt) 50))
    (guitar-strum g [-1 -1 -1 -1 -1 -1] :down 0.01 (+ t (* 3.5 dt)))))
(defn ddd2 []
  (let [t (now) dt 250]
    (guitar-strum g [ 2 -1  0  2  3 -1] :down 0.01 (+ t (* 0 dt)))
    (guitar-strum g [-1 -1 -1 -1 -1 -1] :down 0.01 (+ t (* 1.5 dt)))
    (guitar-strum g [-1  0  2  2  2 -1] :down 0.01 (+ t (* 2 dt)))
    (guitar-strum g [-1  0  2  2  2 -1] :up   0.01 (+ t (* 3 dt)))
    (guitar-strum g [-1 -1 -1 -1 -1 -1] :down 0.01 (+ t (* 4.5 dt)))))
;; give us a good, crunchy sound
(ctl g :pre-amp 5.0 :distort 0.96
     :lp-freq 5000 :lp-rq 0.25
     :rvb-mix 0.5 :rvb-room 0.7 :rvb-damp 0.4)
(ddd0) ;; play once
(ddd1) ;; repeat 3 times
(ddd2) ;; play once

;; ======================================================================
;; play with the one chord progression to rule them all
;; The I - V - vi - IV
;; (or C - G - Am - F)
(ctl g :pre-amp 4.0 :distort 0.5 :noise-amp 1.0
     :lp-freq 4000 :lp-rq 2.0
     :rvb-mix 0.45 :rvb-room 0.4 :rvb-damp 0.9)
(defn play1 [metro k N chord-list]
   (dotimes [n N]
     (doseq [[i cur-chord] (map-indexed vector chord-list)]
       (let [cur-dir (choose [:up :down])
             cur-pattern (choose ["d-du-ud-"
                                  "d-du-udu"
                                  "d-d--udu"])]
         (strum-pattern g metro (+ k (* 4 n) i) cur-chord cur-pattern)))))
;; every pop song ever written.  :^)
(doall
 (let [metro (metronome 100)]
   (play1 metro 0 4 [:C :G :Am :F])))
;; okay, change it up a bit
(doall
 (let [metro (metronome 132)]
   (play1 metro 0 1 [:C :G :Am :F])
   (play1 metro 4 1 [:Am :F :C :G])
   (play1 metro 8 1 [:C :G :Am :F])
   (play1 metro 12 1 [:C :G :Em :C])
   ))
