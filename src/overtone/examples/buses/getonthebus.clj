(ns overtone.examples.buses.getonthebus
  (:use overtone.live))

;; Buses are like wires or pipes that you can use to connect the output
;; of one synth to the inputs of one or more other synths.
;; There are two types of buses - control buses and audio buses.

;; Control buses are designed to carry control signals - values changing
;; at a human rate (i.e. the speed you may turn a dial or slide a slider).

;; Audio buses are designed to carry audio signals - values changing at
;; a rate that makes them audible.

;; Audio buses can carry both audio and control rate signals. However,
;; they will use more computational resources. Therefore, consider using
;; a control bus if you're signal doesn't need to change more than, say,
;; 60 times a second.

;; You can create many new control and audio buses. However, your system
;; will start with one audio bus per audio input and one audio bus per audio
;; output. For example, your left speaker is represented by audio bus 0
;; and your right speaker is represented by audio bus 1

;; Let's create some buses to carry some control rate signals

;; We use the defonce construct to avoid new buses being created and
;; assigned accidentally, if the forms get re-evaluated.
(defonce tri-bus (audio-bus))
(defonce sin-bus (audio-bus))

;; These are synths created to send data down the buses.
;; They are set up so that you can modify both the bus they output on and
;; their frequency whilst they're running via standard ctl messages.
;;
;; Note that we use the :kr variant of the out ugen. This tells the synth
;; to output to a control bus rather than an audio bus which is the default.
(defsynth tri-synth [out-bus 0 freq 5]
  (out:kr out-bus (lf-tri:kr freq)))

(defsynth sin-synth [out-bus 0 freq 5]
  (out:kr out-bus (sin-osc:kr freq)))

;; Probably the most important lesson about using buses is to understand that
;; the execution of the synthesis on the server is strictly ordered. Running
;; synths are placed in a node tree which is evaluated in a depth-first order.
;; This is important to know because if you want synth instance A to be able to
;; communicate with synth instance B via a bus, A needs to be *before* B in the
;; synthesis node tree.

;; The way to gain control over the order of execution within the synthesis tree
;; is to use groups. Let's create some now:

(defonce main-g (group "get-on-the-bus main"))
(defonce early-g (group "early birds" :head main-g))
(defonce later-g (group "latecomers" :after early-g))

;; Let's create some source synths that will send signals on our buses. Let's
;; also put them in the early group to ensure that their signals get sent first.

(comment
  (def tri-synth-inst (tri-synth [:tail early-g] tri-bus))
  (def sin-synth-inst (sin-synth [:tail early-g] sin-bus))
  )

;; Notice how these synths aren't making or controlling any sound. This is because
;; they're control rate synths and also because their output is going to the buses
;; we created which aren't connected to anything. The signals are therefore ignored.

;; We can verify that they're running by viewing the node tree. We can do this
;; easily with the following fn:
(pp-node-tree)

;; This will print the current synthesis node-tree to the REPL. This can get pretty
;; hairy and large, but if you've only evaluated this tutorial since you started
;; Overtone, it should be pretty manageable. You should be able to see the tri-synth
;; and sin-synth within the early birds group, which itself is within the
;; get-on-the-bus main group which itself is within the Overtone Default group.

;; Now, let's use these signals to make actual noise!

;; First, let's define a synth that we'll use to receive the signal from the bus to
;; make some sound:

(defsynth modulated-vol-tri [vol-bus 0 freq 220]
  (out 0 (pan2 (* (in:kr vol-bus) (lf-tri freq)))))

;; Notice how this synth is using the default audio rate version of the out
;; ugen to output a signal to the left speaker of your computer. It is also
;; possible to use out:ar to achieve the same result.

;; This synth reads the value off the bus (at control rate)
;; and multiplies it with the lf-tri ugen's sample value.  The overall
;; result is then sent to two consecutive buses: 0 and 1. (pan2 duplicates
;; a single channel signal to two channels; this is documented in more detail
;; in some of the getting-started examples).


;; This synth is a little trickier.  It calculates the frequency
;; by taking the sample value from the bus, multipling it by
;; the frequency amplitude, and then adding the result to the midpoint
;; or median frequency.  Therefore, if you hook it up to a bus carrying
;; a sine wave signal and use the defalt mid-freq and freq-amp values
;; you'll get an lf-tri ugen that oscillates between 165 and 275 Hz
;; (165 is 55 below 220, and 275 is 55 above 220)
(defsynth modulated-freq-tri [freq-bus 0 mid-freq 220 freq-amp 55]
  (let [freq (+ mid-freq (* (in:kr freq-bus) freq-amp))]
    (out 0 (pan2 (lf-tri freq)))))


;; One of the nifty things about buses is that you can have multiple synths reading
;; them from the same time.

;; Evaluate these to use the signals on the buses to modulate synth parameters
(comment
  (def mvt (modulated-vol-tri [:tail later-g] sin-bus))
  (def mft (modulated-freq-tri [:tail later-g] sin-bus))
  )

;; Fun fact: These two examples are key features
;; of AM and FM radio transmitters, respectively.

;; Switch the bus that is modulating the frequency
;; to be the triangle bus.
;;
(comment
  (ctl mft :freq-bus tri-bus)
  )

;; Change the frequency of the triangle wave on the tri-bus
;; This causes the modulation of the volume to happen more slowly
(comment
  (ctl tri-synth-inst :freq 0.5)
  )

;; Switch the modulated-vol-tri instance to be modulated by the triangle
;; bus as well.
(comment
  (ctl mvt :vol-bus tri-bus)
  )

;; Kill the two things that are making noise
(comment
  (do
    (kill mft)
    (kill mvt))
  )

;; At this point, the buses are still carrying data from the tri-synth and sin-synth;
;; you'll have to kill them as well explicitly or invoke (stop) if you want them to stop.

;; Or can re-use them!
(comment
  (def mvt-2 (modulated-vol-tri [:tail later-g] sin-bus 110))
  (kill mvt-2)
  )

;; Wacky heterodyning stuff!
(comment
  (do
    (ctl tri-synth-inst :freq 5)
    (ctl sin-synth-inst :freq 5)
    (def mft-2 (modulated-freq-tri [:tail later-g] sin-bus 220 55))
    (def mft-3 (modulated-freq-tri [:tail later-g] tri-bus 220 55)))
  (ctl sin-synth-inst :freq 4)
  (kill mft-2 mft-3)
  )

(comment
  "For when you're ready to stop all the things"
  (stop)
  )
