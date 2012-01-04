(ns overtone.gui.pianoroll
  (:use [seesaw core graphics make-widget color])
  (:require [seesaw.bind :as bind])
  (:import [javax.swing DefaultBoundedRangeModel]))

(def note-types [:white :black :white :black :white :white :black :white :black :white :black :white])
(def MEASURE_WIDTH 150)
(def NOTE_HEIGHT 10)
(def NUM_MEASURES 4)
(def NUM_OCTAVES 4)
(def RESOLUTION 16)

(def NUM_BEATS 4)
(def NUM_BARS 4)

(defn note-matrix
  [notes n-measures n-octaves n-bars n-beats n-steps-per-beat]
    (let [n-keys (* n-octaves 12)
          n-total-bars (* n-bars n-measures)
          n-steps (* n-measures (* n-bars (* n-steps-per-beat n-beats)))]
      (reset! notes (vec (repeat n-steps (vec (repeat n-keys 0.0)))))))

(defn update-note
  [matrix beat note velocity]
    (update-in matrix [beat note] (fn [_] velocity)))

(defn paint-piano-roll
  "Paint the dial widget group"
  [num-measures num-bars num-beats num-steps-per-beat num-octaves c g]
  (let [w   (width c)
        h   (height c)
        num-octaves num-octaves
        num-measures num-measures
        measure-width MEASURE_WIDTH
        bar-width (/ measure-width 4)
        beat-width (/ measure-width 4)
        note-width (/ beat-width 4)
        note-height NOTE_HEIGHT
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


(def gnotes (atom nil))

(defn piano-roll-widget
  [num-measures num-bars num-beats num-steps-per-beat num-octaves]
  (let [notes-atom (note-matrix (atom nil) num-measures num-bars num-beats num-steps-per-beat num-octaves)  
        piano-roll (canvas  :id :piano-roll
                            :paint (partial paint-piano-roll 4 4 4 4 4))
        panel (border-panel :center piano-roll)
        measure-width MEASURE_WIDTH
        piano-roll-width (* num-measures measure-width)
        bar-width (/ measure-width 4)
        beat-width (/ measure-width 4)
        note-width (/ beat-width 4)
        note-height NOTE_HEIGHT
        ; gnotes (atom nil)
        ]

    (reset! gnotes notes-atom)

    (listen piano-roll
      :mouse-pressed
        (fn [e] 
          (let [x (.getX e)
                y (.getY e)
                x-cell (int (/ x note-width))
                y-cell (int (/ y note-height))
                measure (int (/ piano-roll-width MEASURE_WIDTH))
                current-velocity (nth (nth notes-atom x-cell) y-cell)
                result-velocity (if (== current-velocity 0.0) 
                                  (do 1.0)
                                  (do 0.0))

                ]

            (println x-cell y-cell current-velocity result-velocity (nth (nth @notes-atom x-cell) y-cell))
            (update-note @notes-atom x-cell y-cell result-velocity)
            
            ))
      :mouse-dragged
        (fn [e])) 
    panel))

(defn create-piano-roll 
  [& {:keys [noctaves 
             resolution] 
      :or {  noctaves 4 
             resolution 16}}]
  (let [panel (piano-roll-widget  4 4 4 4 4)]
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
