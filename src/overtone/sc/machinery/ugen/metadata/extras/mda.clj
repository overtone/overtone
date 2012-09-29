(ns overtone.sc.machinery.ugen.metadata.extras.mda
  (:use [overtone.sc.machinery.ugen common check]))

; NOTE: You will need to have the sc3 plugins installed for this ugen to be available.

(def specs
  [
   {:name "MdaPiano"
    :summary "Synthesised piano"
    :args [{:name "freq"
            :default 440.0
            :doc "Frequency of the note."}

           {:name "gate"
            :default 1.0
            :doc "Note-on occurs when gate goes from nonpositive to
                  positive; note-off occurs when it goes from positive
                  to nonpositive. Most of the other controls are only
                  updated when a new note-on occurs." }

           {:name "vel"
            :default 100.0
            :doc "Velocity (range is 0 to 127)." }

           {:name "decay"
            :default 0.8
            :doc "The time for notes to decay after the initial strike."}

           {:name "release"
            :default 0.8
            :doc "The time for notes to decay after the key is
                  released."}

           {:name "hard"
            :default 0.8}

           {:name "velhard"
            :default 0.8}

           {:name "muffle"
            :default 0.8}

           {:name "velmuff"
            :default 0.8}

           {:name "velcurve"
            :default 0.8}

           {:name "stereo"
            :default 0.2
            :doc "Width of the stereo effect (which makes low notes
                  sound towards the left, high notes towards the
                  right). 0 to 1."}

           {:name "tune"
            :default 0.5
            :doc "Overall tuning."}

           {:name "random"
            :default 0.1
            :doc "Randomness in note tuning."}

           {:name "stretch"
            :default 0.1
            :doc "Stretches the tuning out (higher notes pushed
                  higher)."}

           {:name "sustain"
            :default 0.1
            :doc "If positive, act as if the piano's sustain pedal is
                  pressed."}]
    :rates #{:ar}
    :num-outs 2
    :doc "A piano synthesiser (originally a VST plugin by Paul Kellett,
          ported to SC by Dan Stowell). This UGen is not polyphonic (but
          can be retriggered to play notes in sequence). Note: This UGen
          is stereo - it returns two channels, with a stereo 'wideness'
          effect controlled by the stereo argument."}])
