(ns overtone.gui.control
  (:use [overtone.gui dial spinner-label]
        [overtone.sc.node :only [ctl]]
        [seesaw core mig]
        [seesaw.border :only [line-border]])
  (:require [seesaw.bind :as bind]))


(defn scale-val [[from-min from-max] [to-min to-max] v]
  (let [from-min (double from-min)
        from-max (double from-max)
        to-min   (double to-min)
        to-max   (double to-max)
        v        (double v)]
    (+ to-min
     (* (/ (- v from-min)
           (- from-max from-min))
        (- to-max to-min)))))

(defn- control-slider
  [name min-val max-val step val-atom]
  (let [init-val   @val-atom
        ;_ (println (str name) ": " init-val min-val max-val step)
        spin-model  (spinner-model (double init-val)
                                   :from (double min-val)
                                   :to (double max-val)
                                   :by (double step))
        spinner     (spinner-label
                      :class :synth-control-spinner
                      :halign :center
                      :border (line-border :thickness 1 :color :darkgrey)
                      :model spin-model)
        slider-max  (int (/ (- max-val min-val) step))
        scaled-init (scale-val [min-val max-val] [0 slider-max] init-val)
        slider      (slider :class :synth-control-slider
                            :value scaled-init :min 0 :max slider-max
                            :orientation :horizontal)
        label  (label name)
        items  [[label "width 80:80:100"]
                [spinner "width 80:80:80"]
                [slider "wrap"]]]

    ; return 2 element vector of items and cleanup function. hmmm.
    [items
     (juxt
       (bind/bind
         slider
         (bind/transform (partial scale-val [0 slider-max] [min-val max-val]))
         val-atom
         (bind/transform (partial scale-val [min-val max-val] [0 slider-max]))
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

(defn synth-controller-panel
  [synth]
  (let [full-params (filter #(not (or (= :none (:type %))
                                        (nil? (:min %))
                                        (nil? (:max %))
                                        (nil? (:step %))))
                              (:params synth))]
      (if (zero? (count full-params))
        (do
          (alert (str "The '" (:name synth) "'"
                    " synth or instrument does not have any parameters"
                    " with the required meta-data. "
                    " (e.g.  [freq {:default 80 :min 40 :max 880 :step 1}])"))
          {:synth synth
           :panel (label :text "Missing parameters" :border (:name synth))
           :cleanup (fn [])})
        (let [control-panes (map
                              (fn [{:keys [name default min max step value]}]
                                (control-slider name min max step value))
                              full-params)
              cleanup       (apply juxt (map second control-panes))
              panel (mig-panel :constraints ["" "[right][center][center]" ""]
                               :items (mapcat first control-panes)
                               :border (:name synth))]
          {:synth synth
           :panel panel
           :cleanup cleanup}))))

(defn synth-controller
  "Create a GUI for the given synths or instruments.  Each synth must have sufficient
   parameter metadata in the synthdef.

    (defsynth foo [freq {:default 440 :min 20 :max 10000 :step 1}]
      (out 0 (* [0.6 0.6] (env-gen (perc 0.01 0.2) :action FREE) (sin-osc freq))))

    ; pops up gui window that will modify active synths and default param values
    (synth-controller foo)

    ; To reset the synth back to the original default paremeter values use
    (reset-synth-defaults foo)
  "
  [& synths]
  (invoke-now
    (let [panels  (map synth-controller-panel synths)
          cleanup (apply juxt (map :cleanup panels))
          frame (frame :title "Synth Controllers"
                       :content (scrollable (vertical-panel
                                  :border 5
                                  :items (map :panel panels)))
                       :on-close :dispose)]
      ; When the window is closed, unhook everything from the synth
      ; so the ui can be garbage collected, etc.
      (listen frame :window-closed (fn [_] (cleanup)))
      (-> frame pack! show!))))

(defn live-synth-controller
  "Create a synth instance and attach a synth-controller to it.
  Allows you to control the-synth interactively.

  (defsynth foo [freq1 {:default 332 :min 200 :max 800 :step 1}
                 freq2 {:default 333 :min 200 :max 800 :step 1}]
     (out 0 (pan2 (mix [(lf-tri freq1) (lf-tri freq2)]))))

  (live-synth-controller foo)"
  [the-synth]
  (let [the-instance (the-synth)
        the-watchers (map 
                      (fn [param] 
                        (add-watch
                         (:value param)
                         (keyword (:name param)) 
                         (fn [_ _ _ val] (ctl the-instance (keyword (:name param)) val))))
                      (:params the-synth))
        the-controller (synth-controller the-synth)]
    (vector the-instance the-watchers the-controller)))

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
