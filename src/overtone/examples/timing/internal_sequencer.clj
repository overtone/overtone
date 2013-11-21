(ns overtone.examples.timing.internal-sequencer
  (:use [overtone.live]))

;; A fully server-side sample sequencer.
;; =====================================

;; This example demonstrates some of the benefits of moving all synth
;; triggers inside the server itself. For example, it allows you to
;; modify the synthesis with *immediate* effect (rather than waiting for
;; the next bar/chunk to be scheduled) and you can use a global pulse to
;; drive both the timing and to also modulate aspects of the synthesis
;; so that the modulations are sympathetic to the rhythms being played.


;; First, let's create some sequencer buffers for specifying which beat
;; to trigger a sample. This will be our core data structure for a basic
;; emulation of an 8-step sequencer. A buffer is like a Clojure vector,
;; except it lives on the server and may only contain floats. Buffers
;; are initialised to have all values be 0.0
(defonce buf-0 (buffer 8))
(defonce buf-1 (buffer 8))
(defonce buf-2 (buffer 8))
(defonce buf-3 (buffer 8))

;; Next let's create some timing buses. These can be visualised as
;; 'patch cables' - wires that carry pulse signals that may be
;; arbitrarily forked and fed into any synth that wants to be aware of
;; the pulses. We have two types of information being conveyed here -
;; firstly the trg buses contain a stream of 0s with an intermittant 1
;; every time there is a tick. Secondly we have the cnt buses which
;; contain a stream of the current tick count. We then have two of each
;; type of bus - one for a high resolution global metronome, and another
;; for a division of the global metronome for our beats.
(defonce root-trg-bus (control-bus)) ;; global metronome pulse
(defonce root-cnt-bus (control-bus)) ;; global metronome count
(defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
(defonce beat-cnt-bus (control-bus)) ;; beat count

(def BEAT-FRACTION "Number of global pulses per beat" 30)

;; Here we design synths that will drive our pulse buses.
(defsynth root-trg [rate 100]
  (out:kr root-trg-bus (impulse:kr rate)))

(defsynth root-cnt []
  (out:kr root-cnt-bus (pulse-count:kr (in:kr root-trg-bus))))

(defsynth beat-trg [div BEAT-FRACTION]
  (out:kr beat-trg-bus (pulse-divider (in:kr root-trg-bus) div))  )

(defsynth beat-cnt []
  (out:kr beat-cnt-bus (pulse-count (in:kr beat-trg-bus))))

;; Now we get a little close to the sounds. Here's four nice sounding
;; samples from Freesound.org
(def kick-s (freesound 777))
(def click-s (freesound 406))
(def boom-s (freesound 33637))
(def subby-s (freesound 25649))

;; Here's a synth for playing back the samples with a bit of modulation
;; to keep things interesting.
(defsynth mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 sequencer 0 amp 1]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 sequencer cnt)
                      (= beat-num (mod cnt 8))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out
     out-bus (* vol
                amp
                (pan2
                 (rlpf
                  (scaled-play-buf 1 buf rate bar-trg)
                  (demand bar-trg 0 (dbrown 200 20000 50 INF))
                  (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

;; Here's Dan Stowell's dubstep synth modified to work with the global
;; pulses
(definst dubstep [note 40 wobble BEAT-FRACTION hi-man 0 lo-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 sweep-max-freq 3000 hi-man-max 1000 lo-man-max 500 beat-vol 0 lag-delay 0.5]
  (let [bpm     300
        wob     (pulse-divider (in:kr root-trg-bus) wobble)
        sweep   (lin-lin:kr (lag-ud wob 0.01 lag-delay) 0 1 400 sweep-max-freq)
        snd     (mix (saw (* (midicps note) [0.99 1.01])))
        snd     (lpf snd sweep)
        snd     (normalizer snd)

        snd     (bpf snd 1500 2)
        ;;special flavours
        ;;hi manster
        snd     (select (> hi-man 0.05) [snd (* 4 (hpf snd hi-man-max))])

        ;;sweep manster
        snd     (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;;lo manster
        snd     (select (> lo-man 0.05) [snd (lpf snd lo-man-max)])

        ;;decimate
        snd     (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;;crunch
        snd     (select (> tan-man 0.05) [snd (tanh (* snd 5))])

        snd     (* 0.5 (+ (* 0.8 snd) (* 0.3 (g-verb snd 100 0.7 0.7))))
        ]
    (normalizer snd)))

;; Here's a nice supersaw synth
(definst supersaw2 [freq 440 amp 1 fil-mul 2 rq 0.3]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1)
                  (- input comp2)
                  (- input comp3)
                  (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))
        output (normalizer (rlpf output (* freq fil-mul) rq))]

    (* amp output (line 1 0 10 FREE))))


;; OK, let's make some noise!

;; Now, let's start up all the synths:
(do
  (def r-trg (root-trg))
  (def r-cnt (root-cnt [:after r-trg]))
  (def b-trg (beat-trg [:after r-trg]))
  (def b-cnt (beat-cnt [:after b-trg]))


  (def kicks (doall
              (for [x (range 8)]
                (mono-sequencer :buf kick-s :beat-num x :sequencer buf-0))))

  (def clicks (doall
               (for [x (range 8)]
                 (mono-sequencer :buf click-s :beat-num x :sequencer buf-1))))

  (def booms (doall
              (for [x (range 8)]
                (mono-sequencer :buf boom-s :beat-num x :sequencer buf-2))))

  (def subbies (doall
                (for [x (range 8)]
                  (mono-sequencer :buf subby-s :beat-num x :sequencer buf-3)))))

;; An empty palatte to play with:
(do
  (buffer-write! buf-0 [1 0 1 1 0 0 1 0])  ;; kick
  (buffer-write! buf-1 [0 0 0 0 1 0 0 0])  ;; click
  (buffer-write! buf-2 [0 0 0 0 0 0 1 0])  ;; boom
  (buffer-write! buf-3 [0 0 0 0 0 0 0 0])) ;; subby

;; try mixing up the sequences. Evaluate this a few times:
(do
  (buffer-write! buf-0 (repeatedly 8 #(choose [0 1])))
  (buffer-write! buf-1 (repeatedly 8 #(choose [0 1])))
  (buffer-write! buf-2 (repeatedly 8 #(choose [0 1])))
  (buffer-write! buf-3 (repeatedly 8 #(choose [0 1]))))

;; and then to something interesting
(do
  (buffer-write! buf-0 [1 1 1 1 1 1 1 1])
  (buffer-write! buf-1 [1 0 1 0 0 1 1 0])
  (buffer-write! buf-2 [1 1 0 1 0 1 1 0])
  (buffer-write! buf-3 [1 0 0 0 0 0 1 0]))

;; try changing the rate of the global pulse (everything else will
;; follow suit):
(ctl r-trg :rate 75)
(ctl r-trg :rate 300)
(ctl r-trg :rate 150)

;; get the dubstep bass involved:
(dubstep :note 28
         :wobble (* BEAT-FRACTION 1)
         :lo-man 1)

;; go crazy - especially with the deci-man
(ctl dubstep
     :note 40
     :wobble (* BEAT-FRACTION 0.1)
     :lag-delay 0.05
     :hi-man 0
     :lo-man 0
     :deci-man 0)


;; Bring in the supersaws!

(def ssaw-rq 0.9)
(def ssaw-fil-mul 2)

;; Fire at will...
(supersaw2 (midi->hz 28) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 40) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 45) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 48) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 52) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 55) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 57) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 64) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 67) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 69) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)

;; modify saw params on the fly too...
;;(ctl supersaw2 :fil-mul 4 :rq 0.2)
;;(stop)
