(ns examples.basic
  (:use overtone.live))

; Some of the examples gathered here were found on this page:
; http://en.wikibooks.org/wiki/Designing_Sound_in_SuperCollider/Print_version
; which come originally from the book Designing Sound by Andy Farnell.

(defsynth foo [freq 200 dur 0.5]
  (let [src (saw [freq (* freq 1.01) (* 0.99 freq)])
        low (sin-osc (/ freq 2))
        filt (lpf src (line:kr (* 10 freq) freq 10))
        env (env-gen (perc 0.1 dur) :action FREE)]
    (out 0 (pan2 (* 0.8 low env filt)))))

;(foo 440)

(defn foo-pause
  []
  (dotimes [i 10]
    (foo (* i 220) 1)
    (Thread/sleep 300)))

;(foo-pause)

; Using Thread/sleep like above can result in JVM pauses with unknown
; wakeup times, so in order to make sure sounds are triggered exactly
; when you want them to you can wrap any call to a synthesizer function
; in the (at <timestamp> ...) macro.  This will schedule all enclosed
; synthesizer calls to play at the specified time.
(defn foo-timed
  []
  (let [n (now)]
    (dotimes [i 10]
      (at (+ n (* i 300))
          (foo (* i 220) 1)))))

;(foo-timed)

; A simple pad sound using definst rather than defsynth, which will
; automatically take the enclosing synth and send it to a bus.
; (Note how in comparison to foo above it doesn't use the out and pan ugens.)
(definst overpad [note 60 amp 0.7 attack 0.001 release 2]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)]))
                             (lpf (saw [freq (* freq 1.01)]) f-env)))
        audio (* amp env sig)]
    audio))

;(overpad 64 :attack 2 :release 10)

(def metro (metronome 128))

(definst kick []
  (let [src (sin-osc 80)
        env (env-gen (perc 0.001 0.3) :action FREE)]
    (* 0.7 src env)))

;(kick)

(defn player [beat notes]
  (let [notes (if (empty? notes)
                [50 55 53 50]
                notes)]
    (at (metro beat)
        (kick))
    (at (metro beat)
        (if (zero? (mod beat 5))
          (overpad (+ 24 (choose notes)) 0.2 0.75 0.005)))
    (at (metro (+ 0.5 beat))
        (if (zero? (mod beat 6))
          (overpad (+ 12 (choose notes)) 0.5 0.15 0.1)
          (overpad (choose notes) 0.5 0.15 0.1)))
  (apply-at (metro (inc beat)) #'player (inc beat) (next notes) [])))

;(player (metro) [])
;(stop)

(defn play-notes [t beat-dur notes attacks]
  (when notes
    (let [note      (+ 12 (first notes))
          attack    (first attacks)
          amp       0.5
          release   0.1
          next-beat (+ t beat-dur)]
      (at t (overpad note amp attack release))
      (apply-at next-beat #'play-notes next-beat beat-dur (next notes) (next attacks) []))))

;(play-notes (now) 425 (cycle [40 42 44 45 47 49 51 52]) (repeat 0.4))
;(play-notes (now) 300 (scale :c4 :major) (repeat 0.05))
;(play-notes (now) 300 (take 15 (cycle [40 42 44 45 47 49 51 52])) (repeat 0.3))
;(play-notes (now) 100 (take 50 (cycle (scale :a4 :minor))) (repeat 0.4))
;(stop)

; Inspired by "How do I play a chord" from Impromptu website
(defn chord-notes []
 [(choose [58 60 60 62])
  (choose [62 63 63 65])
  (choose [65 67 68 70])])

(def metro (metronome 70))

(defn play-chords [b]
  (let [tick (* 2 (choose [125 500 250 250 500 250 500 250]))
        next-beat (inc b)]
    (at (metro b)
        (doseq [note (map #(- %  12) (chord-notes))]
            (overpad note 0.3 (/ tick 1020))))
    (apply-at (metro next-beat) #'play-chords [next-beat])))

;(play-chords (metro))
;(metro-bpm metro 70)
;(stop)

; You can load samples from freesound.org using their ID number:
(def kick-d (sample (freesound-path 41155)))
;(kick-d)

(defn looper [t dur notes]
  (at t (kick-d))
  (at (+ t 350) (doseq [note (chord-notes)] (overpad (first notes) 0.3 0.1)))
  (at t (overpad (- (first notes) 36) 0.3 (/ dur 1000)))
  (apply-at (+ t dur) #'looper (+ t dur) dur (next notes) []))

;(looper (now) 500 (cycle [60 67 65 72 75 70]))
;(stop)

; When a multiplication is done involving UGen objects, then
; multiply UGens will be produced with the operands as their
; inputs.  (Note that synthdefs can have doc strings too.)
(definst pedestrian-crossing
  "Street crossing in Britain."
  []
  (* 0.2 (sin-osc 2500) (lf-pulse 5)))

;(pedestrian-crossing)
;(stop)

; You can mix signals by adding them together.  The soundcard can take audio
; data between -1 and 1, so if you add up signals remember to multiply
; by a fractional number or else you will have clipping distortion.
(definst trancy-waves []
  (* 0.2
     (+ (sin-osc 200) (saw 200) (saw 203) (sin-osc 400))))

; (trancy-waves)
; (stop)

;; A noise filter, using the mouse to control the bandpass frequency and bandwidth
(defn noise-demo
  []
  (demo 10 (bpf (* [0.5 0.5] (pink-noise))
                (mouse-y 10 10000)
                (mouse-x 0.0001 0.9999))))

;(noise-demo)

; Move your mouse around to hear the random sine waves moving around
(defsynth roaming-sines
  []
  (let [freqs (take 5 (repeatedly #(ranged-rand 40 2000)))
        ampmod [(mouse-x 0 1) (mouse-y 1 0)]
        snd (splay (* 0.5 (sin-osc freqs)))
        snd (* (sin-osc ampmod) snd)]
    (out 0 snd)))
;(roaming-sines)
;(stop)

; Gangsta scratch
(defsynth scratch-pendulum []
  (let [kon (sin-osc:kr (* 10 (mouse-x)))
        k2 (sin-osc:kr (* 5 (mouse-x)))
        lpk (lin-lin:kr kon -1 1 0 1000)
        foo (poll:kr (impulse:kr 20) lpk)
        src (lpf (white-noise) lpk)
        src (pan2 src k2)
        bak (* 0.5 (lpf (white-noise 500)))]
    (out 0 (+ src [bak bak]))))
;(scratch-pendulum)
;(stop)


; The functions representing UGens support what's called multi-channel
; expansion.  What this means is that if pass a collection of N arguments
; where a single value is expected, then N instances of the UGen will
; be created, each using the successive values.
(definst dial-tone [freq-a 350 freq-b 440]
  (apply + (* (sin-osc [freq-a freq-b]) 0.2)))

;(dial-tone)
;(stop)

;; Synths can also communicate back to us.  Here we use the send-trig
;; UGen, which sends a "/tr" trigger message every time it gets an
;; input trigger.  The message includes an id number, and the current
;; input value of its last input.

(defsynth trigger-finger []
  (send-trig:kr (impulse:kr 0.2) 200 (num-output-buses)))

;(on-event "/tr" #(println "trigger: " %) ::trigger-test)
;(trigger-finger)
;(stop)

(defsynth dtest []
  (send-trig:kr (impulse:kr 2) 1 (demand:kr (impulse:kr 0.5) 1 (dwhite))))

; (dtest)
; (stop)

(defsynth adder [a 1 b 2]
  (let [v1 (- a b)
        v2 (- b a)
        sum (+ a b)
        product (* a b)]
    (send-trig:kr v1 201 sum)
    (send-trig:kr v2 202 product)))

;(adder)

; You can read audio data in from your sound card using the regular (in <bus-num>) ugen,
; but you need to know where your input buses start.  The output buses start at number 0,
; and then the input buses begin, so you need to know how many outputs you have to know
; the right bus to read from.
(defsynth external-input [out-bus 0]
  (out out-bus (in (num-output-buses:ir))))

(definst ticker [freq 2]
  (* (sin-osc 440) (env-gen (perc 0.1 0.2) (sin-osc freq))))
;(ticker)

(definst sizzle [amp 0.4 depth 10 freq 220 lfo 8]
  (* amp (saw (+ freq (* depth (sin-osc:kr lfo))))))

;(sizzle)
;(ctl sizzle :depth 100 :lfo 0.5)
;(stop)

; It's typical to use a pulse as a sort of on off switch like this.
(defsynth line-two [bus 0]
  (let [sig (lf-pulse 1/6 0 0.25)]
    (out 0 (* 0.5 (sin-osc [480 440]) (lag sig)))))

; (line-two)
; (stop)

(definst busy-signal []
  (let [on-off (lag (lf-pulse 2) 0.1)]
    (* 0.2
       (apply + (* (sin-osc [480 620]) on-off)))))

;;(busy-signal)
;;(stop)

; Need to make a call?
(def DTMF-TONES {1  [697, 1209]
                 2  [770, 1209]
                 3  [852, 1209]
                 4  [697, 1336]
                 5  [770, 1336]
                 6  [852, 1336]
                 7  [697, 1477]
                 8  [770, 1477]
                 9  [852, 1477]
                 \* [697, 1633]
                 0  [770, 1633]
                 \# [852, 1633]})

(definst dtmf [freq-a 770 freq-b 1633 gate 1]
  (let [sig (* 0.2 (+ (sin-osc freq-a) (sin-osc freq-b)))
        env (env-gen (asr 0.001 1 0.001) gate 1 0 1 FREE)]
    (* sig env)))

(defn dial-number [num-seq]
  (loop [t (now)
         nums num-seq]
    (when nums
      (let [t-on  (+ t 160 (rand-int 200))
            t-off (+ t-on 160 (rand-int 80))
            [a b] (get DTMF-TONES (first nums))]
        (at t-on (dtmf a b))
        (at t-off (ctl dtmf :gate 0))
        (recur t-off (next nums))))))

; Try this:
;(dial-number [0 6 2 1 2 2 4 2 9 8])

; The done ugen can act as a flag for the completion of envelopes and other ugens that
; have a done action.  Listen to the noise come on after the 2 second sine wave.
(definst done-trigger []
  (let [line (line:kr 1 0 2)]
    (* 0.1 (+ (* line (sin-osc 440)) (* (done line) (white-noise))))))

;;(done-trigger)
;;(stop)
