(ns overtone.gui.control
  (:use overtone.libs.event
        seesaw.core)
  (:require [seesaw.bind :as bind]))

(native!)

(defn control-slider
  [name init-val min-val max-val step val-atom]
  (let [spin-model (spinner-model init-val :from min-val :to max-val :by step)
        spinner (spinner :model spin-model)
        slider (slider :orientation :vertical :min min-val :max max-val :value init-val)
        label  (label name)
        pane   (vertical-panel :items [slider spinner label])]
    ;(listen slider :change (fn [e] (alert (str "changed slider value: " (.getValue slider)))))
   (bind/bind
      slider
     (bind/tee
       (bind/selection spinner)
       val-atom))
    (bind/bind
      (bind/selection spinner)
      (bind/tee
        slider
        val-atom))
    pane))

(defn control-surface
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

  )
