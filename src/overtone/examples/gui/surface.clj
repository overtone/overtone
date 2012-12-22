(ns overtone.examples.gui.surface
  (:use overtone.live
        overtone.gui.sequencer
        overtone.gui.surface
        overtone.inst.drum))

(def m (metronome 128))

(defn a []
  (step-sequencer m 11 [kick closed-hat open-hat snare])

  (surface-grid [(synth-param snare "freq")
                 (synth-param kick "freq")
                 (synth-param snare "sustain")]

                [(synth-param closed-hat "low")
                 (synth-param closed-hat "hi")
                 (synth-param closed-hat "t")]

                [(synth-param open-hat "low")
                 (synth-param open-hat "hi")
                 (synth-param open-hat "t")]

                [(synth-param kick "freq-decay")
                 (synth-param kick "amp-decay")
                 (synth-param snare "decay")]))

(defsynth harmony [freq1 {:default 440 :min 40 :max 1800 :step 1}
                   freq2 {:default 880 :min 40 :max 1800 :step 1}
                   amp   {:default 0.1 :min 0.1 :max 1 :step 0.01} ]
  (out 0 (* amp (sin-osc [freq1 freq2]))))

(defn b []
  (def h (harmony))

  (surface (synth-param harmony h "freq1")
           (synth-param harmony h "freq2")
           (synth-param harmony h "amp")))
