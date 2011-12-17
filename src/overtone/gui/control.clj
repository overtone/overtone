(ns overtone.gui.control
  (:use overtone.libs.event
        seesaw.core)
  (:require [seesaw.bind :as bind]))

(native!)

(def SLIDER-MAX 1000)

(defn- control-slider
  [name init-val min-val max-val step val-atom]
  (let [spin-model (spinner-model (double init-val)
                                  :from (double min-val) :to (double max-val) :by (double step))
        spinner (spinner :model spin-model)
        scaled-init (* (/ (- init-val min-val)
                          (double (- max-val min-val)))
                       SLIDER-MAX)
        slider (slider :value scaled-init :min 0 :max SLIDER-MAX :orientation :vertical)
        label  (label name)
        pane   (vertical-panel :items [slider spinner label])]
    (bind/bind slider
               (bind/transform (fn [v]
                                 (let [val (+ min-val (* (- max-val min-val)
                                                         (/ v (double SLIDER-MAX))))]
                                   (if (integer? step)
                                     (int val)
                                     val))))
               val-atom)
    (bind/bind val-atom (bind/tee spinner slider))
    (bind/bind spinner val-atom)
    pane))

(defn gui-for
  "Create a GUI for the given synth or instrument.  Requires sufficient parameter metadata in the synthdef.

  (defsynth foo [freq {:default 440 :min 20 :max 10000 :step 1}]
    (out 0 (* [0.6 0.6] (env-gen (perc 0.01 0.2) :action FREE) (sin-osc freq))))

  (gui-for foo) ; pops up gui window that will modify active synths and default param values

  ; To reset the synth back to the original default paremeter values use
  (reset-synth-defaults foo)
  "
  [synth]
  (invoke-now
    (let [control-panes (map
                          (fn [{:keys [name default min max step value]}]
                            (control-slider name default min max step value))
                          (:params synth))
          pane (horizontal-panel :items control-panes)
          frame (frame :title (:name synth)
                       :content pane
                       :on-close :dispose)]
      (-> frame pack! show!))))

(comment

(defsynth foo [note {:default 60 :min 0 :max 120 :step 1}
               attack {:default 0.002 :min 0.0001 :max 3.0 :step 0.001}
               decay  {:default 0.3 :min 0.0001 :max 3.0 :step 0.001}]
  (out 0 (* [0.5 0.5] (env-gen (perc attack decay) :action FREE)
            (sin-osc (midicps note)))))

(def m (metronome 85))

(defn player [b]
  (at (m b) (foo))
  (apply-at (m (inc b)) #'player [(inc b)]))

)

