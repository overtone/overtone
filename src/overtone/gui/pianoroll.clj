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

(defn note-matrix [notes n-octaves n-measures n-bars n-beats n-steps-per-beat]
    (let [n-keys (* n-octaves 12)
          n-total-bars (* n-bars n-measures)
          n-steps (* n-measures (* n-bars (* n-steps-per-beat n-beats)))]
      (reset! notes (vec (repeat n-steps (vec (repeat n-keys 0.0)))))))

(defn update-note
  [matrix beat note velocity]
    (update-in matrix [beat note] (fn [_] velocity)))

(defn paint-active-notes [notes active-notes g note-width note-height]
  (let [active @active-notes]
      (dotimes [i (count active)]
        (let [x (double (* (nth (nth active i) 0) note-width)) 
              y (double (* (nth (nth active i) 1) note-height))]
          (draw g
           (rounded-rect x y note-width note-height 6 6) (style 
                                                            :stroke 1.0 
                                                            :background (color 0 255 0 200)
                                                            :foreground (color 0 150 0)))))))

(defn paint-piano-roll
  "Paint the dial widget group"
  [num-measures num-bars num-beats num-steps-per-beat num-octaves notes active-notes c g]
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
            (rect (* i measure-width) 0 measure-width (- total-height 1)) measure-border))

          ;; paint the active notes last
          (paint-active-notes notes active-notes g note-width note-height)))

(defn update-active-notes [notes-atom active-notes-atom creating-note x-cell y-cell]
  (let [x x-cell
        y y-cell]
    (if 
      (= 1 (nth (nth @notes-atom x-cell) y-cell))
          (do
            ; (println "adding new note @" x-cell y-cell)
            ; (println @active-notes-atom)
            (swap! creating-note (fn [b] true))
            (swap! active-notes-atom (fn [notes] (conj notes [x-cell y-cell])))
            ; (println @active-notes-atom)
            )
          (do 
            ; (println "removing a note @" x-cell y-cell)
            ; (println @active-notes-atom)
            (swap! active-notes-atom (fn [notes] (remove #{[x-cell y-cell]} @active-notes-atom)))
            ; (println @active-notes-atom)
            ))))

(defn piano-roll-widget
  [num-measures num-bars num-beats num-steps-per-beat num-octaves]
  (let [notes-atom (atom (note-matrix (atom nil) num-octaves num-measures num-bars num-beats num-steps-per-beat))
        active-notes-atom (atom [])
        piano-roll (canvas  :id :piano-roll
                            :paint (partial paint-piano-roll 4 4 4 4 4 notes-atom active-notes-atom))
        panel (border-panel :center piano-roll)
        measure-width MEASURE_WIDTH
        piano-roll-width (* num-measures measure-width)
        bar-width (/ measure-width 4)
        beat-width (/ measure-width 4)
        note-width (/ beat-width 4)
        note-height NOTE_HEIGHT
        creating-note (atom false)
        current-count (atom 0)
        ]

    (listen piano-roll
      :mouse-pressed
        (fn [e] 
          (let [x (.getX e)
                y (.getY e)
                x-cell (int (/ x note-width))
                y-cell (int (/ y note-height))
                measure (int (/ piano-roll-width MEASURE_WIDTH))]
                (swap! notes-atom (fn [old-matrix] (update-note old-matrix x-cell y-cell (if (zero? (nth (nth old-matrix x-cell) y-cell)) 1 0))))

            (update-active-notes notes-atom active-notes-atom creating-note x-cell y-cell)
            (.repaint (.getSource e))
            ))
      :mouse-dragged
        (fn [e]
          (if creating-note 0 1))

      :mouse-released
        (fn [e]  
          (swap! creating-note (fn [b] false))
          (.repaint (.getSource e))))
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
                        :width 400 :height 400
                        :content panel)
               pack!
               show!)]

        (listen piano-roll
          :mouse-pressed
            (fn [e] (alert "pressed it"))
          :mouse-dragged
            (fn [e]))
        f))))
