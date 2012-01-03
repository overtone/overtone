(ns overtone.gui.control
  (:use [overtone.gui dial]
        [seesaw core mig])
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
        slider (slider :id (keyword name) :class :synth-control-slider
                       :value scaled-init :min 0 :max SLIDER-MAX :orientation :horizontal)
        label  (label name)
        items  [[label "width 80:80:100"]
                [spinner "width 80:80:80"]
                [slider "wrap"]]]
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
    items))

(defn- control-dial
  [name init-val min-val max-val step val-atom]
  (let [spin-model (spinner-model (double init-val)
                                  :from (double min-val) :to (double max-val) :by (double step))
        spinner (spinner :model spin-model)
        dial (dial :id (keyword name) :class :synth-control-dial
                        :value init-val :min 0 :max SLIDER-MAX :orientation :horizontal)
        label  (label name)
        items  [[label "width 80:80:100"]
                [spinner "width 80:80:80"]
                [dial "wrap"]]]
    (bind/bind dial
               (comment bind/transform (fn [v]
                                 (let [val (+ min-val (* (- max-val min-val)
                                                         (/ v (double SLIDER-MAX))))]
                                   val)))
               val-atom)
    (bind/bind val-atom
               ;(bind/transform #(* (/ (- % min-val) (- max-val min-val)) SLIDER-MAX))
               dial)
    (bind/bind spinner val-atom)
    (bind/bind val-atom spinner)
    items))

(defn synth-controller
  "Create a GUI for the given synth or instrument.  Requires sufficient parameter metadata in the synthdef.

  (defsynth foo [freq {:default 440 :min 20 :max 10000 :step 1}]
    (out 0 (* [0.6 0.6] (env-gen (perc 0.01 0.2) :action FREE) (sin-osc freq))))

  (gui-for foo) ; pops up gui window that will modify active synths and default param values

  ; To reset the synth back to the original default paremeter values use
  (reset-synth-defaults foo)
  "
  [synth]
  (invoke-now
    (let [full-params (filter #(not (or (= :none (:type %))
                                        (nil? (:min %))
                                        (nil? (:max %))
                                        (nil? (:step %))))
                              (:params synth))]
      (if (zero? (count full-params))
        (alert (str "The " (:name synth) " synth or instrument does not have any parameters
with the required meta-data.  (e.g.  [freq {:default 80 :min 40 :max 880 :step 1}])"))
        (let [control-panes (mapcat
                              (fn [{:keys [name default min max step value]}]
                                (control-slider name default min max step value))
                              full-params)
              pane (mig-panel :constraints ["" "[right][center][center]" ""]
                              :items control-panes)
              frame (frame :title (:name synth)
                           :content pane
                           :on-close :dispose)]
          (-> frame pack! show!))))))

