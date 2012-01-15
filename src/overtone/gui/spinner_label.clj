(ns overtone.gui.spinner-label
  (:use [seesaw.core]
        [seesaw.behave :only [when-mouse-dragged]]
        [seesaw.meta :only [get-meta put-meta!]]
        [seesaw.options :only [apply-options option-map default-option]]
        [seesaw.keymap :only [map-key]]
        [seesaw.widget-options :only [WidgetOptionProvider]]
        [seesaw.value :only [Value value* value!*]]
        [seesaw.selection :only [Selection]])
  (:require [seesaw.bind :as bind]))

(defn spinner-label-proxy []
  (proxy [javax.swing.JLabel] []))

(def ^{:private true} SpinnerLabelClass (class (spinner-label-proxy)))

(defn- make-state [this]
  { :model (atom nil)
    :unbind (atom (fn []))
    :update (fn [v this] (.setText this (str v)))})

(defn- get-model [this] @(:model (get-meta this ::state)))
(defn- set-model [this m]
  (let [{:keys [model unbind update]} (get-meta this ::state)]
    (@unbind)
    (reset! model m)
    (reset! unbind (bind/bind m (bind/b-do* update this)))
    (update (.getValue m) this)))

(defn spinner-label
  "Same API as #'seesaw.core/spinner, but only a label is displayed and the
  current value is modified by dragging the mouse up and down over it."
  [& opts]
  (let [widget (spinner-label-proxy)
        state  (make-state widget)]
    (put-meta! widget ::state state)
    (set-model widget (spinner-model 0.0))
    (when-mouse-dragged
      widget
      :drag (fn [e [dx dy]]
              (let [m @(:model state)
                    next (.getNextValue m)
                    prev (.getPreviousValue m)]
                (cond
                  (and next (neg? dy)) (.setValue m next)
                  (and prev (pos? dy)) (.setValue m prev)))))
    (apply-options
      widget
      (concat
        []
        opts))
    widget))

(def spinner-label-options
  (merge
    default-options
    (option-map
      (default-option :model
        set-model
        get-model
        "A spinner-model"))))

(extend-type (do SpinnerLabelClass)
  WidgetOptionProvider
    (get-widget-option-map* [this] [spinner-label-options])
    (get-layout-option-map* [this] nil)

  Value
    (container?* [this] false)
    ; TODO implement value for spinner models in Seesaw
    (value* [this] (.getValue (config this :model)))
    (value!* [this v] (.setValue (config this :model) v))
  Selection
    (get-selection [this] [(value this)])
    (set-selection [this [v]] (value! this v))

  bind/ToBindable
    (to-bindable* [this] (config this :model)))

(comment
  (use 'overtone.gui.spinner-label 'seesaw.core 'seesaw.dev)
  (def sl (spinner-label :id :my-spin-label :model (spinner-model 10.0 :from 0.0 :to 100.0 :by 0.5)))
  (selection! sl 99.5)
  (bind/bind sl (bind/b-do [v] (println "new value " (value sl) ", " (selection sl))))
  (-> (frame :content sl) pack! show!))

