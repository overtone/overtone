(ns overtone.examples.gui.control
  (:use overtone.live
        overtone.gui.control))

;; An example synth with lots of controls you can play with via the
;; overtone.gui.control/live-synth-controller.  This makes for a nice
;; way to explore the synth design space interactively.

;; For more explanation, see the series of examples in:
;; https://github.com/rogerallen/explore_overtone/blob/master/src/explore_overtone/sawbble.clj
;; or ask questions on the overtone google group.

;; NOTE - to get this working with the GUI controller, I remove an
;; important bit of code from the env-gen call: :action FREE.  For a
;; "real" synth, you will want it to disappear when the envelope goes
;; to 0, so add that :action FREE to the env-gen.

;; create a buffer to hold a scale
(def scale-buffer (buffer 16))

;; fill the buffer with a scale
(doseq [[i n] (map-indexed
               vector
               (scale :d2 :major (range 1 16)))]
  (buffer-set! scale-buffer i n))

;; define the synth
(defsynth saw-synth-6
  "a detuned and stereo-separated saw synth with a low-pass-filter and
   low-pass-filter LFO."
  [lpf-lfo-freq        {:default 4.1  :min 0.0 :max 10.0  :step 0.01}
   lpf-min-freq        {:default 400  :min 100 :max 9900  :step 100}
   lpf-max-freq        {:default 4000 :min 100 :max 10000 :step 100}
   lpf-res             {:default 0.1  :min 0.0 :max 1.0   :step 0.05}
   separation-delay-ms {:default 5.0  :min 0    :max 30.0  :step 0.1}
   lfo-level           {:default 1.4  :min 0.0 :max 5.0   :step 0.05}
   lfo-freq            {:default 1.8  :min 0.0 :max 10.0  :step 0.1}
   pitch-index         {:default 0    :min 0   :max 15    :step 1}
   adsr-attack-time    {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-decay-time     {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-sustain-level  {:default 0.5 :min 0.0  :max 1.0   :step 0.01}
   adsr-release-time   {:default 0.1 :min 0.0  :max 1.0   :step 0.01}
   adsr-peak-level     {:default 0.9 :min 0.0  :max 1.0   :step 0.01}
   adsr-curve          {:default -4  :min -5   :max 5     :step 1}
   gate                {:default 1.0 :min 0.0  :max 1.0   :step 1}]
  (let [pitch-midi (index:kr (:id scale-buffer) pitch-index)
        pitch-freq (midicps pitch-midi)
        lfo-out (* lfo-level (sin-osc lfo-freq))
        saws-out (mix (saw [pitch-freq (+ pitch-freq lfo-out)]))
        separation-delay (/ separation-delay-ms 1000.0)
        saws-out-2ch [saws-out (delay-c saws-out 1.0 separation-delay)]
        lpf-freq (lin-lin (sin-osc lpf-lfo-freq) -1 1 lpf-min-freq lpf-max-freq)
        lpf-out-2ch (moog-ff saws-out-2ch lpf-freq lpf-res)
        env-out (env-gen (adsr adsr-attack-time   adsr-decay-time
                               adsr-sustain-level adsr-release-time
                               adsr-peak-level    adsr-curve)
                         :gate gate)]
    (out 0 (* env-out lpf-out-2ch))))

;; Now, instantiate & control the synth interactively.  It is the most
;; fun to play with the stereo separation with headphones on.  Use the
;; gate control to turn the note on/off.  Slide the pitch-index around
;; to run through the scale of notes.
(live-synth-controller saw-synth-6)
