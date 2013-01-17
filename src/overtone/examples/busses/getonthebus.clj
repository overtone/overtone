(ns overtone.examples.busses.getonthebus
  (:use overtone.live))

;; Here's some code to help get you going with busses.
;; (Audio busses to be precise, but I'm fuzzy enough on the
;; difference between audio and control busses that I'm not
;; going to attempt to explain it here).
;;
;; Busses are like wires or pipes that you can use to connect the output
;; of one synth to the inputs of one or more other synths.  (I haven't
;; tried connecting the outputs of more than one synth to the same
;; bus so I'm not sure how well that would work).

;; First here are some busses to carry some signals
(def tri-bus (audio-bus))
(def sin-bus (audio-bus))

;; These are synths created to send data down the busses.
;; They are set up so that you can use the ctl function after
;; they're triggered to change the bus that they output on
;; or the frequency at which their ugen is operating.
(defsynth tri-synth [out-bus 0 freq 5]
  (out out-bus (lf-tri:ar freq)))

(defsynth sin-synth [out-bus 0 freq 5]
  (out out-bus (sin-osc:ar freq)))

;; Evaluate these to start sending signals to the busses
;; Note that if any of the subsequent instantiations of synths end up with
;; synths that are not being modulated (i.e. that are just outputting a
;; single frequency at an unchanging volume, or that sound like they
;; aren't outputting anything at all) you probably need to re-evaluate
;; one or both of these, and then possibly you'll need to kill and
;; re-create the synths (I'm not sure why; seems like that shouldn't
;; be necessary but that's how it goes).
(comment
  (def tri-synth-inst (tri-synth tri-bus))
  (def sin-synth-inst (sin-synth sin-bus))
  )

;; These synths are what actually make sound (because their outputs
;; are going to the 0 and 1 busses, a.k.a. the left and right channels
;; of the audio output).

;; For each sample, this one reads the value off the bus (at audio rate)
;; and multiplies it with the lf-tri ugen's sample value.  The overall
;; result is then sent to two busses: 0 and the one above it. (pan2 duplicates
;; a single channel signal to two channels; this is documented in more detail
;; in some of the getting-started examples).
(defsynth modulated-vol-tri [vol-bus 0 freq 220]
  (out 0 (pan2 (* (in:ar vol-bus) (lf-tri freq)))))

;; This one is a little trickier.  It calculates the frequency
;; by taking the sample value from the bus, multipling it by
;; the frequency amplitude, and then adding the result to the midpoint
;; or median frequency.  Therefore, if you hook it up to a bus carrying
;; a sine wave signal and use the defalt mid-freq and freq-amp values
;; you'll get an lf-tri ugen that oscillates between 165 and 275 Hz
;; (165 is 55 below 220, and 275 is 55 above 220)
(defsynth modulated-freq-tri [freq-bus 0 mid-freq 220 freq-amp 55]
  (let [freq (+ mid-freq (* (in:ar freq-bus) freq-amp))]
    (out 0 (pan2 (lf-tri freq)))))


;; One of the nifty things about busses is that you can have multiple synths reading
;; them from the same time.

;; Evaluate these to use the signals on the busses to modulate synth parameters
(comment
  (def mvt (modulated-vol-tri sin-bus))
  (def mft (modulated-freq-tri sin-bus))
  )

;; Fun fact: These two examples are key features
;; of AM and FM radio transmitters, respectively.

;; Change the frequency of the triangle wave on the tri-bus
;; This causes the modulation of the volume to happen more slowly
(comment
  (ctl tri-synth-inst :freq 0.5)
  )

;; Switch the bus that is modulating the frequency and volume
;; to be the triangle bus.
;;
;; Note that we have to use its id because OSC (or maybe supercollider)
;; is expecting a number, not a map (at least I think that's what's going on;
;; feel free to correct this if it's something else happening).
(comment
  (ctl mft :freq-bus (:id tri-bus))
  (ctl mvt :vol-bus (:id tri-bus))
  )

;; Kill the two things that are making noise
(comment
  (do
    (kill mft)
    (kill mvt))
  )

;; At this point, the busses are still carrying data from the tri-synth and sin-synth; you'll have to kill them as well explicitly or invoke (stop) if you want them to stop.

;; Or can re-use them!
(comment
  (def mvt-2 (modulated-vol-tri sin-bus 110))
  (kill mvt-2)
  )

;; Wacky heterodyning stuff!
(comment
  (do
    (ctl tri-synth-inst :freq 5)
    (ctl sin-synth-inst :freq 5)
    (def mft-2 (modulated-freq-tri sin-bus 220 55))
    (def mft-3 (modulated-freq-tri tri-bus 220 55)))
  (ctl sin-synth-inst :freq 4)
  (kill mft-2 mft-3)
  )

(comment
  "For when you're ready to stop all the things"
  (stop))
