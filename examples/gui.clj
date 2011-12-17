(ns examples.gui
  (:use overtone.core))

; Create a synth with metadata that can be used to generate a GUI controller
(defsynth foo [note {:default 60 :min 0 :max 120 :step 1}
               attack {:default 0.002 :min 0.0001 :max 3.0 :step 0.001}
               decay  {:default 0.3 :min 0.0001 :max 3.0 :step 0.001}]
  (out 0 (* [0.8 0.8] (env-gen (perc attack decay) :action FREE)
            (sin-osc (midicps note)))))

; Build the gui
(synth-controller foo)

(def m (metronome 85))

(defn foo-player [b]
  (at (m b) (foo))
  (apply-at (m (inc b)) #'foo-player [(inc b)]))

; Start the synth looping, and play with the GUI to hear it change live
;(foo-player (m))

; Works with instruments too, as long as they have all the necessary metadata.
(definst bar [note {:default 60 :min 0 :max 120 :step 1}
              attack {:default 0.002 :min 0.0001 :max 3.0 :step 0.001}
              decay  {:default 0.3 :min 0.0001 :max 3.0 :step 0.001}]
  (* [0.8 0.8] (env-gen (perc attack decay) :action FREE)
            (sin-osc (midicps note) [0 0.2])))

(synth-controller bar)

(defn bar-player [b]
  (at (m b) (bar))
  (apply-at (m (inc b)) #'bar-player [(inc b)]))

(bar-player (m))
