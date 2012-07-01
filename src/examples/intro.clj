(ns examples.intro
  (:use overtone.live))

;; ## Outputting sound
;;
;; The "out" ugen is used to send data to a bus, and if passed multiple
;; channels it will automatically start at the first bus and send to successive
;; busses.  The first bus goes to your left audio channel on the sound card.
;;
;; Output a 440 hz sin wave to the left channel:
(defsynth beep [] (out 0 (* 0.1 (sin-osc 440))))

;; (beep) plays synth and returns an integer ID of a synth instance
;; (kill <id>) kills a specific synth instance
;; (stop) kills all synths

;; ## Controlling Amplitude
;;
;; The amplitude of a signal can be controlled using a multiply ugen, as is
;; used above.  Notice how we can pass arguments to synths too.
;; Try passing different values:
(defsynth beep2 [freq 440 amp 0.1]
  (out 0 (* amp (sin-osc freq))))

;; (beep2)
;; (beep2 220 0.4)
;; (beep2 80 0.8)
;; (stop)

;; ## Multi-channel output

;; The first N audio busses correspond to your soundcard output channels, so
;; outputting in stereo means sending audio data to busses 0 and 1, while 4
;; channel surround would require sending audio to busses 0 through 3.

;; We can make two parallel sin wave oscillators to output in stereo:
(defsynth beep3 [freq 440 amp 0.1]
  (let [a (* amp (sin-osc freq))
        b (* amp (sin-osc freq))]
    (out 0 [a b])))

;; (beep3)
;; (stop)

;; It would be annoying to have to duplicate everything anytime we want multi-channel
;; output, so Overtone also supports something called multi-channel expansion, which
;; is borrowed from sclang.  Expansion lets you pass a seq of arguments to any ugen
;; where a value is expected, and then it will create a new instance of the ugen
;; for each value in the seq.
;;
;; This is equivalent to the previous synth:

(defsynth beep4 [freq 440 amp 0.1]
  (out 0 (* amp (sin-osc [freq freq]))))

;; (beep4)
;; (stop)

;; This can be used in all sorts of ways, for example, here we slightly offset
;; the frequency in one channel:

(defsynth beep5 [freq 440 amp 0.1 offset 7]
  (out 0 (* amp (sin-osc [freq (+ offset freq)]))))

;; Try adjusting the offset to hear what it sounds like at different distances
;; from the main frequency.  You can adjust parameters of a running synth instance
;; using the (ctl <id> <:param> <val>) function like this:
;;
;; (def b (beep5))
;; (ctl b :offset 3)
;; (ctl b :offset 30)
;; (ctl b :offset 300)
;; (stop)

;; ## Delay
(defsynth beep6 [freq 440 amp 0.1 offset 7 delay 0.4]
  (let [src (* (env-gen (perc) :action FREE) (sin-osc [freq (+ offset freq)]))
        del (delay-n src delay delay)]
    (out 0 (* amp (+ src del)))))

;; (beep6)

;; ## Flanger
;;
;; A flanger is created by a signal added to a delayed copy of itself, where
;; the amount of the delay is varied over time.

(defsynth beep7 [freq 440 amp 0.1 offset 3 rate 4 depth 0.2 delay 0.3]
  (let [src (* (env-gen (perc 0.2 0.4) (dust 1)) (sin-osc [freq (+ offset freq)]))
        lfo (* depth (abs (sin-osc rate)))
        del (delay-n src 2 (* lfo delay))]
    (out 0 (distort (* amp (+ src del))))))

;; (beep7)
;; (stop)

;; ## Chorus
;;
;; A chorus effect is created by adding multiple delayed copies of a signal
;; together, possibly modifying the amplitude and other factors of each
;; voice to give them unique sounds.
;;

;; ## Overdrive and Distortion
;;
;; Basic distortion and overdrive can be created by clipping a signal when it goes
;; above a certain threshold.
;;

;; Random experiment...

(defsynth voices []
  (let [n 8
        src (* 0.6 (dust (repeat n 2)))
        del (comb-l src 0.1 (repeatedly n #(+ (rand 0.004) 0.003)) 4)]
    (out 0 (splay del :spread 0.8))))

;; (voices)
;; (stop)
