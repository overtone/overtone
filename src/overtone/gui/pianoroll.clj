(ns overtone.gui.pianoroll
  (:use [seesaw core graphics make-widget color])
  (:require [seesaw.bind :as bind])
  (:import [javax.swing DefaultBoundedRangeModel]))

(def note-types [:white :black :white :black :white :white :black :white :black :white :black :white])

(defn paint-piano-roll
  "Paint the dial widget group"
  [c g]
  (let [w   (width c)
        h   (height c)
        num-octaves 4
        num-measures 4
        measure-width 150
        bar-width (/ measure-width 4)
        beat-width (/ measure-width 4)
        note-width (/ beat-width 4)
        note-height 10
        measure-border  (style :stroke 2.0 :foreground (color 100 100 100 50))
        note-border  (style :stroke 1.0 :foreground (color 100 100 100 100))
        white-note-track-border  (style :stroke 0.25 :foreground (color 100 100 100 100) :background (color 255 255 255))
        black-note-track-border  (style :stroke 0.25 :foreground (color 100 100 100 100) :background (color 180 180 180))
        num-notes (* num-octaves 12)
        total-height (* num-notes note-height)
        resolution 16]

        (dotimes [i num-measures]
          (dotimes [note-y num-notes]
            (let [ntx (* i measure-width)
                  nty (* note-y note-height)
                  note-type (nth note-types (mod note-y 12))]
              (cond 
                (= note-type :black)
                  (draw g
                    (rect ntx nty measure-width note-height) black-note-track-border)
                (= note-type :white)
                  (draw g
                    (rect ntx nty measure-width note-height) white-note-track-border))))
          (dotimes [note-x resolution]
            (let [nx (+ (* i measure-width) (* note-x note-width))]
              (draw g
                (line nx 0 nx total-height) note-border)))
          (draw g
            (rect (* i measure-width) 0 measure-width (- total-height 1)) measure-border))))

(defn piano-roll-widget
  []
  (let [piano-roll (canvas :id :piano-roll
                           :paint paint-piano-roll)
        panel (border-panel :center piano-roll)] 
    panel))

(defn create-piano-roll 
  [& {:keys [noctaves 
             resolution] 
      :or {  noctaves 4 
             resolution 16}}]
  (let [panel (piano-roll-widget)]
          panel))

(defn piano-roll
  ([]
   (invoke-now
      (let [piano-roll (create-piano-roll)
           panel (border-panel :id :piano-roll :center piano-roll)
           f (-> (frame :title "Piano Roll"
                        :on-close :dispose
                        :content panel)
               pack!
               show!)]

        (listen piano-roll
          :mouse-pressed
            (fn [e] (alert "pressed it"))
          :mouse-dragged
            (fn [e]))
        f))))
