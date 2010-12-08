(ns examples.basic
  (:use overtone.live))

(defsynth foo [freq 200 dur 0.5]
  (let [src (saw [freq (* freq 1.01) (* 0.99 freq)])
        low (sin-osc (/ freq 2))
        filt (lpf src (line:kr (* 10 freq) freq 10))
        env (env-gen (perc 0.1 dur) :action :free)]
    (out 0 (pan2 (* 0.1 low env filt)))))

;(dotimes [i 10]
;  (foo (* i 220) 2)
;  (Thread/sleep 800))
;
;(reset)

; Some of the examples gathered here were found on this page:
; http://en.wikibooks.org/wiki/Designing_Sound_in_SuperCollider/Print_version
; which come originally from the book Designing Sound by Andy Farnell.

(definst overpad [out-bus 0 note 60 amp 0.7 a 0.001 rel 0.5]
  (let [freq (midicps note)
        env (env-gen (perc a rel) 1 1 0 1 :free)
        f-env (+ freq (* 3 freq (env-gen (perc 0.12 (- rel 0.1)))))
        bfreq (/ freq 2)
        sig (apply +
                   (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)]))
                           (lpf (saw [freq (* freq 1.01)]) f-env)))
        audio (* amp env sig)]
    (out out-bus audio)))

;(overpad 0 62 0.5 5)

(def metro (metronome 128))

(definst kick []
  (let [src (sin-osc 80)
        env (env-gen (perc 0.001 0.02) :action :free)]
    (* 0.7 src env)))

(defn player [beat notes]
  (let [notes (if (empty? notes)
                [50 55 53 50]
                notes)]
    (at (metro beat)
        (kick))
    (at (metro (+ 0.5 beat))
        (overpad 0 (choose notes) 0.5 0.5))
  (apply-at #'player (metro (inc beat)) (inc beat) [(next notes)])))

;(player (metro) [])













;(overpad 0 60 0.5 5)

(def BEAT 425) ; ms per beat
(def TICK (- BEAT 100))

(defn play-notes [t notes durs]
  (when notes
    (at t (overpad 0 (first notes) 0.5 (first durs)))
    (apply-at #'play-notes (+ t TICK) (+ t BEAT) (next notes) durs)))

;(play-notes (now) [40 42 44 45 47 49 51 52] (repeat 0.4))
;(play-notes (now) (scale :c :major) (repeat 0.05))

;(play-notes (now) (take 50 (cycle [40 42 44 45 47 49 51 52])) (repeat 0.3))

;(play-notes (now) (take 24 (drop 36 (scale :a :minor))) (repeat 0.4))

; Inspired by "How do I play a chord" from Impromptu website
(defn chord-notes []
 [(choose [58 60 60 62])
  (choose [62 63 63 65])
  (choose [65 67 68 70])])

(def metro (metronome 120))

(defn play-chords [t]
  (let [tick (* 2 (choose [125 500 250 250 500 250 500 250]))]
    (at t (doseq [note (map #(- %  12) (chord-notes))]
            (overpad 0 note 0.3 (/ tick 1020))))
    (apply-at #'play-chords (+ t (- tick 50)) (+ t BEAT))))

;(play-chords (now))

(def kick (sample "/home/rosejn/studio/samples/kit/boom.wav"))
;(kick)

(defn looper [t dur notes]
  (at t (kick))
  (at (+ t 350) (doseq [note (chord-notes)] (overpad 0 (first notes) 0.3 0.1)))
  (at t (overpad (- (first notes) 36) 0.3 (/ dur 1000)))
  (apply-at #'looper (+ t (* 0.5 dur)) (+ t dur) dur (next notes)))

;(looper (now) 500 (cycle [60 67 65 72 75 70]))

; When a multiplication is done involving UGen objects, then
; multiply UGens will be produced with the operands as their
; inputs.  (Note that synthdefs can have doc strings too.)
(definst pedestrian-crossing
  "Street crossing in Britain."
  []
  (* 0.2 (sin-osc 2500) (lf-pulse 5)))

;(pedestrian-crossing)

; You can mix signals by adding them together.  The soundcard can take audio
; data between -1 and 1, so if you add up signals remember to multiply
; by a fractional number or else you will have clipping distortion.
(definst trancy-waves []
  (* 0.2
     (+ (sin-osc 200) (saw 200) (saw 203) (sin-osc 400))))

;(trancy-waves)
;(reset)

(definst foo [gate 1]
  (let [env (env-gen:kr (adsr 0.2 0.8 0.2) gate 1 0 1 :free)
        mod (* 500 (sin-osc:kr 1))]
    (out 0 (lpf (white-noise env) (+ 500 mod)))))

; The functions representing UGens support what's called multi-channel
; expansion.  What this means is that if pass a collection of N arguments
; where a single value is expected, then N instances of the UGen will
; be created, each using the successive values.
(definst dial-tone [freq-a 350 freq-b 440]
  (apply + (* (sin-osc [freq-a freq-b]) 0.2)))

;(dial-tone)
;(reset)

; Takes an input signal coming in from a selectable bus, and plays it out
; through a series of filters..
(definst transmission-interference [in-bus 10]
  (let [sig (clip2 (in in-bus) 0.9)
        sig (bpf sig 2000 1/12)
        sig (+ (bpf (* 0.5 sig) 400 1/3)
               (* (clip2 sig 0.4) 0.15))]
    (* (hpf (hpf sig 90) 90) 100)))

; Synths can also communicate back to us.  Here we use the send-trig
; UGen, which sends a "/tr" trigger message every time it gets an
; input trigger.  The message includes an id number, and the current
; input value of its last input.
(on-event "/tr" :trigger-test #(println "trigger: " %))

(defsynth trigger-finger []
  (send-trig:kr (impulse:kr 0.2) 200 (num-output-buses)))

(defsynth dtest []
         (send-trig:kr (impulse:kr 2) 1 (demand:kr (impulse 0.5) 1 (dwhite))))

;(defsynth demander-tone [rate 3]
;  (let [trig (impulse:kr rate)
;        a (dwhite 0 15 1000)
;        freq (+ 340 (* (demand:kr trig 0 a) 30))]
;    (out 0 (pan2 (* 0.1 (sin-osc freq))))))
;
;ynthDef(\demander,
;            { arg rate = 3;
;               var a, freq, trig;
;               a = Dwhite(0, 15, inf);
;               trig = Impulse.kr(rate);
;               freq = Demand.kr(trig, 0, a) * 30 + 340;
;               Out.ar(0, Pan2.ar( SinOsc.ar(freq) * 0.1));
;               }).store;
;)
;
;

(defsynth adder [a 1 b 2]
  (let [v1 (- a b)
        v2 (- b a)
        sum (+ a b)
        product (* a b)]
    (send-trig:kr v1 201 sum)
    (send-trig:kr v2 201 product)))

; You can read audio data in from your sound card using the regular (in <bus-num>) ugen,
; but you need to know where your input buses start.  The output buses start at number 0,
; and then the input buses begin, so you need to know how many outputs you have to know
; the right bus to read from.
(defsynth external-input [out-bus 0]
  (out out-bus (in (num-output-buses:ir))))

(defn wah-wah [freq depth]
  (with-ugens
    (* depth (sin-osc:kr freq))))

(defsynth ticker [freq 2]
  (* (sin-osc 440) (env-gen (perc 0.1 0.2) (sin-osc:kr freq))))

(defsynth sizzle [bus 0 amp 0.4 depth 10 freq 220 lfo 8]
  (out bus (* amp (saw (+ freq (wah-wah lfo depth))))))

; It's typical to use a pulse as a sort of on off switch like this.
(defsynth line-two [bus 0]
  (out bus (* (sin-osc [480 440]) (lf-pulse 1/6 0 1/3))))

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
    (out 0 (pan2 (* sig env)))))

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
;(dial-number [0 6 2 1 2 2 4 2 9 8])

; The done ugen can act as a flag for the completion of envelopes and other ugens that
; have a done action.  Listen to the noise come on after the 2 second sine wave.
(defsynth done-trigger []
  (let [line (line:kr 1 0 2)]
    (* 0.1 (+ (* line (sin-osc 440)) (* (done line) (white-noise))))))

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

