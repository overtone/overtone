(ns overtone.sc.ugen.mda
  (:use (overtone.sc.ugen common)))

; NOTE: You will need to have the sc3 plugins installed for this ugen to be available.

(def specs
  [
   {:name "MdaPiano",
    :args [{:name "freq", :default 440.0 :doc "Frequency of the note."}
           {:name "gate", :default 1.0 :doc "Note-on occurs when gate goes from nonpositive to positive; note-off occurs when it goes from positive to nonpositive. Most of the other controls are only updated when a new note-on occurs."}
           {:name "vel", :default 100.0 :doc "Velocity (range is 0 to 127)."}
           {:name "decay", :default 0.8 :doc "The time for notes to decay after the initial strike."}
           {:name "release", :default 0.8 :doc "The time for notes to decay after the key is released."}
           {:name "hard", :default 0.8}
           {:name "velhard", :default 0.8}
           {:name "muffle", :default 0.8}
           {:name "velmuff", :default 0.8}
           {:name "velcurve", :default 0.8}
           {:name "stereo", :default 0.2 :doc "Width of the stereo effect (which makes low notes sound towards the left, high notes towards the right). 0 to 1."}
           {:name "tune", :default 0.5 :doc "Overall tuning."}
           {:name "random", :default 0.1 :doc "Randomness in note tuning."}
           {:name "stretch", :default 0.1 :doc "Stretches the tuning out (higher notes pushed higher)."}
           {:name "sustain", :default 0.1 :doc "If positive, act as if the piano's sustain pedal is pressed."}],
    :rates #{:ar}
    :num-outs 2
    :doc "Piano ugen"}
   ])
