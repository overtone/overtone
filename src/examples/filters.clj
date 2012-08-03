(ns examples.filters
  (:use overtone.live))

; Filter examples, mostly ported from SC docs

; You might want to bring up the scope while trying the filters out,
; because in at least some instances it can help to understand what
; effect the filter is having.
; (scope)

; Lowpass
(demo 10
  (lpf (* 0.5 (saw [339 440]))
       (mouse-x 10 10000))) ; cutoff frequency

; Resonant Lowpass
(demo 10
  (rlpf (* 0.5 (saw [338 440]))
       (mouse-x 10 10000)
       (mouse-y 0.0001 0.9999))) ; cutoff frequency

; Highpass
(demo 10
  (hpf (* 0.5 (saw [203 200]))
       (mouse-x 100 10000))) ; cutoff frequency

; Bandpass
; - only let a band of frequencies pass through
(demo 10
  (bpf (* 0.5 (saw 200))
       (mouse-x 100 10000) ; center frequency
       (mouse-y 0 1)))     ; rq => bandwidth/cutoff

; Band reject
; - the inverse of the bandpass
(demo 10
  (brf (* 0.5 (saw 200))
       (mouse-x 100 10000) ; center frequency
       (mouse-y 0 1)))     ; rq => bandwidth/cutoff

; Limiter
; - limit the amplitude
(demo 10
  (limiter (* 0.5 (saw 440))
           (mouse-y 0.01 0.5)))


; Clip
; - limit the amplitude with hi and low thresholds
(demo 10
  (clip:ar (* (sin-osc 440) 0.4)
        -0.01
        0.01))

; Linear to linear
; - converte from one range to another
(demo 10
  (let [freq (lin-lin:kr (mouse-x 0.1 1)
                   0 1                ; source range
                   110 880)]          ; destination range
  (* 0.3 (saw freq))))

; Linear to exponential
; - convert from a linear range to an exponential range
(demo 10
  (let [freq (lin-exp:kr (mouse-x 0.1 1)
                   0 1                ; linear range
                   10 10000)]         ; exponential range
  (* 0.3 (saw freq))))


; Lag
; - smooths a transition by adjusting input value exponentially
;   over the specified lag time
; - used to smooth out control signals
(demo 10
  (* 0.2 (saw (lag:kr
                     (mouse-x 80 10000) ; frequency value
                     3))))             ; lag time

; Lag Up Down
; - allows for different up and down lag times
(demo 10
  (* 0.2 (saw (lag-ud:kr
                     (mouse-x 80 10000) ; frequency value
                     4 2))))             ; lag time


; Ramp
; - like lag, but transitions linearly
(demo 10
  (* 0.2 (saw (ramp:kr
                     (mouse-x 80 10000) ; frequency value
                     3))))             ; lag time

;; FIXME
;; (demo 10
;;   (klank [(repeatedly 12 #(+ 200.0 (rand 4000)))
;;           (repeat 12 0.8)
;;           (repeatedly 12 #(+ 0.1 (rand 2)))]
;;          (* (clip-noise) 0.01 (decay (impulse 4) 0.03))))

; Amplitude compensation
; - to account for higher pitches seeming louder
; - move the mouse up and down
;
; First a normal saw wave.  Careful, the high pitches can be painful
; at high volume so turn your headphones down!
(demo 10
  (let [f (mouse-y 300 15000)]
    (* (saw f) 0.1)))

; notice how with the compensation the higher pitches are less piercing
(demo 10
  (let [f (mouse-y 300 15000)]
    (* (saw f) 0.1 (amp-comp f 300))))
