(ns example.fm-synthesis
  (:use overtone.live))

(refer-ugens)

(defsynth fm [carrier 440 modulator 440 depth 0.1]
  (let [mod-env (env-gen (perc 0.3 0.6))
        amp-env (env-gen (perc 0.01 0.8) 1 1 0 1 FREE)]
    (* amp-env
       (sin-osc (+ carrier
                 (* depth mod-env
                    (sin-osc modulator)))))))

(fm 440 660 880)
(reset)

; Play this one, and then try moving your mouse around and clicking
(defsynth fm-synth [gate 1]
  (let [modulator (+ 0.1 (* (mouse-button:kr 0 1) (sin-osc (mouse-x:kr 20 4000))))
        carrier (sin-osc (* (mouse-y:kr 4000, 0) modulator))]
    (out 0 (pan2 carrier))))

(fm-synth)
(fm-synth :ctl :gate 0)

(fm-synth 220)
