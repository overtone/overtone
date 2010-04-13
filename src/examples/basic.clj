(ns examples.basic
  (:use overtone.live))

; Some of the examples gathered here were found on this page:
; http://en.wikibooks.org/wiki/Designing_Sound_in_SuperCollider/Print_version
; which come originally from the book Designing Sound by Andy Farnell.

(refer-ugens)

; When a multiplication is done involving UGen objects, then
; multiply UGens will be produced with the operands as their
; inputs.  (Note that synthdefs can have doc strings too.)
(defsynth pedestrian-crossing 
  "Street crossing in Britain."
  []
  (* 0.2 (sin-osc 2500) (lf-pulse 5)))

; You can mix signals by adding them together.  The soundcard can take audio
; data between -1 and 1, so if you add up signals remember to multiply
; by a fractional number or else you will have clipping distortion.
(defsynth trancy-waves []
  (* 0.2
     (+ (sin-osc 200) (saw 200) (saw 203) (sin-osc 400))))

; The functions representing UGens support what's called multi-channel
; expansion.  What this means is that if pass a collection of N arguments
; where a single value is expected, then N instances of the UGen will
; be created, each using the successive values.
(defsynth dial-tone [freq-a 350 freq-b 440]
  (apply + (* (sin-osc [freq-a freq-b]) 0.2)))

; Takes an input signal coming in from a selectable bus, and plays it out
; through a series of filters..
(defsynth transmission-interference [in-bus 10]
  (let [sig (clip2 (in in-bus) 0.9)
        sig (bpf sig 2000 1/12)
        sig (+ (bpf (* 0.5 sig) 400 1/3)
               (* (clip2 sig 0.4) 0.15))]
    (* (hpf (hpf sig 90) 90) 100)))

; Synths can also communicate back to us.  Here we use the send-trig
; UGen, which sends a "/tr" trigger message every time it gets an
; input trigger.  The message includes an id number, and the current
; input value of its last input.
(on "/tr" #(println "trigger: " %))

(defsynth trigger-finger []
  (send-trig:kr (impulse:kr 0.2) 200 (sin-osc:kr 1)))

(defn wah-wah [freq depth]
  (* depth (sin-osc:kr freq)))

(defsynth ticker [freq 2]
  (* (sin-osc 440) (env-gen (perc 0.1 0.2) (sin-osc:kr freq))))

(defsynth sizzle [bus 0 amp 0.4 depth 10 freq 220 lfo 8] 
  (out bus (* amp (saw (+ freq (wah-wah lfo depth))))))

; It's typical to use a pulse as a sort of on off switch like this.
(defsynth line-two []
  (* (sin-osc [480 440]) (lf-pulse 1/6 0 1/3)))

(defsynth busy-signal []
  (let [on-off (lpf (lf-pulse 2) 100)]
    (* 0.2
       (apply + (* (sin-osc [480 620]) on-off)))))

; Need to make a call?
(def DTMF-TONES {1 [697, 1209]
                 2 [770, 1209]
                 3 [852, 1209]
                 4 [697, 1336]
                 5 [770, 1336]
                 6 [852, 1336]
                 7 [697, 1477]
                 8 [770, 1477]
                 9 [852, 1477]
                 \* [697, 1633]
                 0 [770, 1633]
                 \# [852, 1633]})

(defsynth dtmf [freq-a 770 freq-b 1633 gate 1]
  (let [sig (* 0.2 (+ (sin-osc freq-a) (sin-osc freq-b)))
        env (env-gen (asr 0.001 1 0.001) gate 1 0 1 :free)]
    (* sig env)))

(defn dial-number [num-seq]
  (loop [t (now)
         nums num-seq]
    (when nums
      (let [t-on  (+ t 200 (rand-int 200))
            t-off (+ t-on 200 (rand-int 80))
            [a b] (get DTMF-TONES (first nums))]
        (at t-on (dtmf a b))
        (at t-off (dtmf :ctl :gate 0))
        (recur t-off (next nums))))))

; Try this:
;  (dial-number [2 5 9 3 3 7 7])

;(defsynth two-tone-alarm []
;  (let [t1 (sin-osc 600)
;        t2 (sin-osc 800)
;        ctl (lpf:kr (lf-pulse:kr 2) 70)]
;           // We switch between the tones using LFPulse, but soften the crossfade with the low-pass:
;           var control = LPF.kr(LFPulse.kr(2), 70);
;           var out = SelectX.ar(control, [tone1, tone2]);
;           Pan2.ar(out * 0.1)

;(defsynth alarm []
;  (let [freq (duty:kr 0.05 0 
;                freq = Duty.kr(0.05, 0, Dseq([723, 932, 1012], inf));
;                freq = LPF.kr(freq, 70);
;                out = SinOsc.ar(freq);
;                operations = [out, (out * pi).sin, (out * pi).cos, ((out+0.25) * pi).cos];
;                out = Select.ar(MouseX.kr(0,4).poll, operations);
;                Pan2.ar(out * 0.1)
;              }).play


