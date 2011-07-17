(ns example.demand
  (:use overtone.core))

; Unlike the audio and control rate ugens, which produce a constant
; stream of values, the demand rate ugens only produce a value
; when it is "demanded" of them.  This is normally done using the
; demand ugen, which will pull from its arguments when triggered.

; Play a sequence of notes, where the demand ugen pulls them
; each time it gets a trigger.
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dseq [440 880 220] INF)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (* [0.1 0.1] src)))

; Randomize the sequence of notes
(demo 2
      (let [trig (impulse:kr 8)
            freqs (drand [440 880 220] INF)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; Shuffle the notes, but then repeat them in the same order
(demo 4
      (let [trig (impulse:kr 3)
            freqs (dseq (dshuf [440 880 220] 2) INF)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc [(* 1.01 note-gen) note-gen])]
        (* 0.2 src)))

; Randomize the sequence, but don't repeat already played notes until all
; the others have played.
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dxrand [440 880 220] INF)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (* [0.2 0.2] src)))

; generate n elements in total from a sequence, unlike dseq which takes
; a repeat number rather than the total number of values generated
(demo 10
      (let [trig (impulse:kr 2.5)
            n 15
            freqs (dser [440 880 660 1760] n)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

(def buf (buffer 8))
(overtone.sc.buffer/buffer-write buf 0 8
                                 (map #(+ 12 %) [50 50 54 50 57 50 45 49]))

(demo 20
      (let [trig (impulse:kr 8)
            indexes (dseq (range 8) inf)
            freqs (dbufrd buf indexes)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc (midicps note-gen))]
        (* [0.1 0.1] src)))

; Now while it's playing you can set buffer elements to change the notes:
(overtone.sc.buffer/buffer-set buf 3 45)
(overtone.sc.buffer/buffer-set buf 3 80)
(overtone.sc.buffer/buffer-set buf 7 50)

; TODO: Fixme
(demo 4
      (let [freq (duty (drand [0.2 0.4 0.8 0.6] inf)
                       0
                       (dseq [440 880 1200 600] 2))
            src (saw freq)]
        (* [0.2 0.2] src)))

; TODO: Fixme
; Generate a series of values, incrementing linearly.
(demo 8
      (let [trig (impulse:kr 2)
            freqs (dseries 100 50 2)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.2 src))))

; TODO: Fixme
; Generate a geometric sequence
(demo 2
      (let [trig (impulse:kr 8)
            freqs (dgeom 1 1.2 10)
            note-gen (+ 340 (* 30 (demand:kr trig 0 freqs)))
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))

; TODO: Fixme
; Demanding noise...
(demo 2
      (let [trig (impulse:kr 2)
            freqs (dwhite 0 20 INF)
            note-gen (+ 340 (* 30 (demand:kr trig 0 freqs)))
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))
