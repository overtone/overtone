(ns overtone.gui.control
  (:use overtone.libs.event
        seesaw.core)
  (:require [seesaw.bind :as bind]
            [clojure.set :as set]))

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
                                   val)))
               val-atom)
    (bind/bind val-atom
               (bind/transform #(* (/ (- % min-val) (- max-val min-val)) SLIDER-MAX))
               slider)
    (bind/bind spinner val-atom)
    (bind/bind val-atom spinner)
    pane))

(defn synth-controller
  "Create a GUI for the given synth or instrument.  Requires sufficient parameter metadata in the synthdef.

  (defsynth foo [freq {:default 440 :min 20 :max 10000 :step 1}]
    (out 0 (* [0.6 0.6] (env-gen (perc 0.01 0.2) :action FREE) (sin-osc freq))))

  (gui-for foo) ; pops up gui window that will modify active synths and default param values

  ; To reset the synth back to the original default paremeter values use
  (reset-synth-defaults foo)
  "
  [synth]
  (assert (every? (fn [{:keys [name default min max step value]}]
                    (and name default min max step value))
                  (:params synth))
          "Not all synth params have the required metadata: :default, :min, :max, :step
Example:
          (defsynth foo [freq {:default 440 :min 10 :max 10000 :step 1}] ...)
")
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

