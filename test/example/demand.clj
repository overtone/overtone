(ns example.demand
  (:use overtone.core))
; Play a sequence of notes, where the demand ugen pulls them
; each time it gets a trigger.
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dseq [440 880 220] Float/POSITIVE_INFINITY)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; Randomize the sequence of notes
(demo 2
      (let [trig (impulse:kr 8)
            freqs (drand [440 880 220] Float/POSITIVE_INFINITY)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; Randomize the sequence, but don't repeat already played notes
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dxrand [440 880 220] Float/POSITIVE_INFINITY)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; TODO: Fixme
; Generate a geometric sequence
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dgeom 1 1.2 10)
            note-gen (+ 340 (* 30 (demand:kr trig 0 freqs)))
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; Another way to generate a sequence...?
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dser [440 880 220] Float/POSITIVE_INFINITY)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; TODO: Fixme
; Demanding noise...
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dwhite 0 20 Float/POSITIVE_INFINITY)
            note-gen (+ 340 (* 30 (demand:kr trig 0 freqs)))
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

