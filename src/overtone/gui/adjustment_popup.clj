(ns overtone.gui.adjustment-popup
  (:use [seesaw core color font])
  (:require [seesaw.bind :as bind])
  (:import javax.swing.JWindow))

(def ^{:private true} OFFSET 12)
(def ^{:private true} BORDER 8)

(def ^{:private true} label-font (font :name "Arial" :size 14 :style :bold))
(def ^{:private true} value-font (font :name "Arial" :size 14 :style :bold))

(defn adjustment-popup-for
  "Create a temporary popup window showing a changing value for a bindable widget.

  Example:
    (adjustment-popup-for widget \"Volume:\")
  "
  [widget popup-label]
  (invoke-now
    (let [popup (JWindow.)
          txt-label (label :id :popup-label :text popup-label :font label-font)
          val-label (label :id :popup-value :text (value widget) :font value-font
                           :foreground (color 0 140 236))
          body (border-panel :border BORDER
                 :north txt-label
                 :center (flow-panel :align :center :items [val-label]))]
      (doto popup
        (.setBackground (color :black))
        (.add body)
        (.pack))
      (bind/bind widget val-label)
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
      (bind/bind widget (bind/b-do [_] (repaint! widget)))
      popup)))


(comment

(require '[seesaw.core :as saw])
(use 'overtone.gui.adjustment-popup)
(use 'overtone.gui.dial)
(def s (saw/slider :value 50 :min 0 :max 100 :orientation :vertical))
(def s-adj (adjustment-popup-for s "Value:"))

(def d (dial :value 50 :min 0 :max 100))
(def d-adj (adjustment-popup-for d "Pan:"))
(def f (saw/frame :title "testing" :minimum-size [200 :by 400]
              :content (saw/vertical-panel :items [s d])))
(saw/show! f)

)
