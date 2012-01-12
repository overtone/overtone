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

    ; return 2 element vector of items and cleanup function. hmmm.
    [items
     (juxt 
       (bind/bind 
         slider
         (bind/transform (fn [v]
                           (let [val (+ min-val (* (- max-val min-val)
                                                   (/ v (double SLIDER-MAX))))]
                             val)))
         val-atom
         (bind/transform #(* (/ (- % min-val) (- max-val min-val)) SLIDER-MAX))
         ; if the value's "close enough" to the spinner, stop. Otherwise,
         ; we get infinite loops caused by rounding error.
         (bind/filter #(>= (Math/abs (- % (value slider))) 0.01))
         slider)

       (bind/bind 
         spinner  
         val-atom 
         ; if the value's "close enough" to the slider, stop. Otherwise,
         ; we get infinite loops caused by rounding error.
         (bind/filter #(>= (Math/abs (- % (value spinner))) 0.01))
         spinner))]))

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
        (alert (str "The '" (:name synth) "'"
                    " synth or instrument does not have any parameters"
                    " with the required meta-data. "
                    " (e.g.  [freq {:default 80 :min 40 :max 880 :step 1}])"))
        (let [control-panes (map
                              (fn [{:keys [name default min max step value]}]
                                (control-slider name default min max step value))
                              full-params)
              cleanup       (apply juxt (map second control-panes))
              pane (mig-panel :constraints ["" "[right][center][center]" ""]
                              :items (mapcat first control-panes))
              frame (frame :title (:name synth)
                           :content pane
                           :on-close :dispose)]
          ; When the window is closed, unhook everything from the synth
          ; so the ui can be garbage collected, etc.
          (listen frame :window-closed (fn [_] (cleanup)))
          (-> frame pack! show!))))))

(comment
  (use 'overtone.gui.control)
  (def synth {:name "Test"
              :type :test-synth
              :params [{:name "freq"
                       :default 440
                       :min 40
                       :max 880
                       :step 1
                       :value (atom 440) }
                       {:name "amp"
                       :default 5.0 
                       :min 0.0 
                       :max 10.0 
                       :step 0.1
                       :value (atom 2.5) }]})
  (synth-controller synth))
