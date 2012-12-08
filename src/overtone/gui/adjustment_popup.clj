(ns overtone.gui.adjustment-popup
  (:use [seesaw core color font]
        [overtone.config log])
  (:require [seesaw.bind :as bind]
            [overtone.config.log :as log])
  (:import javax.swing.JWindow))

(def ^{:private true} OFFSET 12)
(def ^{:private true} BORDER 8)

(def ^{:private true} label-font (font :name "Arial" :size 16 :style :bold))
(def ^{:private true} value-font (font :name "Arial" :size 16 :style :bold))

(def ^{:private true} OVERTONE-BLUE (color 0 140 236))

(defn adjustment-popup
  "Create a temporary popup window showing a changing value for a bindable widget.

  Example:
  (adjustment-popup widget \"Volume:\")
  "
  [& {:keys [widget label value bindable]
      :or {label "Value:" value "0.0"}}]
  (invoke-now
    (with-error-log "adjustment-popup"
      (let [popup (JWindow.)
            txt-label (seesaw.core/label :id :popup-label :text label :font label-font)
            val-label (seesaw.core/label :id :popup-value :text value
                                         :font value-font
                                         :foreground OVERTONE-BLUE)
            body (border-panel :border BORDER
                               :north txt-label
                               :center (flow-panel :align :center :items [val-label]))
            bindable (or bindable widget)]
        (doto popup
          (.add body)
          (.pack))
        (bind/bind bindable val-label)
        (listen widget
                :mouse-pressed (fn [ev]
                                 (let [{:keys [x y]} (bean (.getLocationOnScreen widget))
                                       w (.getWidth widget)
                                       h (.getHeight widget)
                                       popup-y (- (+ y (/ h 2)) (/ (.getHeight popup) 2))
                                       popup-x (+ x w OFFSET)]
                                   (move! popup :to [popup-x popup-y])
                                   (show! popup)))
                :mouse-released (fn [_]
                                  (hide! popup)))
                      popup))))
