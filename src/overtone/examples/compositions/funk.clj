(ns overtone.examples.compositions.funk
    "This example creates a simple drum and bass pattern, based off of
    the James Brown classic 'Licking Stick', with Bootsy Collins on bass,
    and John Jab'o Starks on drums"
    (:use [overtone.live]))

; model a plucked string, we'll use this for our bass
(definst string [note 60 amp 1.0 dur 0.5 decay 30 coef 0.3 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly   (/ 1.0 freq)
        plk   (pluck noize gate dly dly decay coef)
        dist  (distort plk)
        filt  (rlpf dist (* 12 freq) 0.6)
        clp   (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur) :action 0) reverb)))

; define a simple drumkit using freesound samples
(def snare (sample (freesound-path 26903)))
(def kick (sample (freesound-path 2086)))
(def close-hihat (sample (freesound-path 802)))
(def open-hihat (sample (freesound-path 26657)))


(defn subdivide
    "subdivide two time intervals by 4, and return the time interval
    at position. this is a close-hihateap hack to sclose-hihatedule 16th notes without
    defining the whole pattern with the metronome firing every 16th note."
    [a b position]
    (+ a (* position (/ (- b a) 4) )))

(defn drums [nome]
    (let [beat (nome)]
        ; hi-hat pattern
        (at (nome beat) (close-hihat))
        (at (nome (+ 1 beat)) (open-hihat))
        (at (nome (+ 2 beat)) (close-hihat))
        (at (nome (+ 3 beat)) (close-hihat))
        (at (nome (+ 4 beat)) (close-hihat))
        (at (nome (+ 5 beat)) (open-hihat))
        (at (nome (+ 6 beat)) (close-hihat))
        (at (nome (+ 7 beat)) (close-hihat))

        ; snare pattern
        (at (nome (+ 2 beat)) (snare))
        (at (subdivide (nome (+ 2 beat)) (nome (+ 4 beat)) 3) (snare))
        (at (subdivide (nome (+ 4 beat)) (nome (+ 6 beat)) 1) (snare))
        (at (nome (+ 6 beat)) (snare))
        (at (subdivide (nome (+ 6 beat)) (nome (+ 8 beat)) 3) (snare))

        ; kick drum pattern
        (at (nome beat) (kick))
        (at (nome (+ 5 beat)) (kick))
        (at (nome (+ 7 beat)) (kick))
        (apply-by (nome (+ 8 beat)) drums nome [])))

(defn bass [nome]
    (let [beat (nome)]
    (at (nome beat) (string 51))
    (at (subdivide (nome beat) (nome (+ 2 beat)) 1) (string 51))
    (at (subdivide (nome beat) (nome (+ 2 beat)) 3) (string 51))
    (at (subdivide (nome (+ beat 1)) (nome (+ 3 beat)) 1) (string 51))
    (at (subdivide (nome (+ beat 1)) (nome (+ 3 beat)) 3) (string 51))
    (at (nome (+ 4 beat)) (string 51))
    (at (subdivide (nome (+ 4 beat)) (nome (+ 6 beat)) 1) (string 49))
    (at (nome (+ 5 beat)) (string 46))
    (at (nome (+ 6 beat)) (string 51))
    (at (subdivide (nome (+ 6 beat)) (nome (+ 8 beat)) 1) (string 49))
    (at (nome (+ 7 beat)) (string 46))
    (at (nome (+ 8 beat)) (string 51))
    (at (nome (+ 12 beat)) (string 51))
    (at (subdivide (nome (+ 12 beat)) (nome (+ 14 beat)) 1) (string 51))
    (apply-by (nome (+ 16 beat)) bass nome [])))

(defn section [nome]
    (drums nome)
    (bass nome))

;; define a metronome that will fire every eighth note
;; at 100 bpm

(def met (metronome (* 100 2)))
;; to play the beat, just run
;; (section met)
;; (stop)
