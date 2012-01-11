(ns overtone.gui.pianoroll
  (:use [seesaw core graphics make-widget color])
  (:require [seesaw.bind :as bind])
  (:import [javax.swing DefaultBoundedRangeModel]))

(def note-types [:white :black :white :black :white :white :black :white :black :white :black :white])
(def MEASURE_WIDTH 150)
(def NOTE_HEIGHT 10)
(def NUM_MEASURES 4)
(def NUM_OCTAVES 4)
(def STEPS_PER_BEAT 4)

(def ACTIVE_NOTE_PADDING 2)

(def NUM_BEATS 4)
(def NUM_BARS 4)


(defn note-matrix [notes n-octaves n-measures n-bars n-beats n-steps-per-beat]
    (let [n-keys (* n-octaves 12)
          n-total-bars (* n-bars n-measures)
          n-steps (* n-measures (* n-bars (* n-steps-per-beat n-beats)))]
      (reset! notes (vec (repeat n-steps (vec (repeat n-keys 0.0)))))))

(defn paint-active-notes [notes active-notes g note-width note-height]
  (let []
     (doseq [[k notes] @active-notes] 
       (doseq [[n note-data] notes]
          (let [x (double (+ ACTIVE_NOTE_PADDING (* k note-width))) 
                y (double (+ ACTIVE_NOTE_PADDING (* n note-height)))
                w (- (* (note-data :duration) note-width) (* 2 ACTIVE_NOTE_PADDING))
                h (- note-height (* 2 ACTIVE_NOTE_PADDING))]
            (draw g
              (rounded-rect x y w h 3 3) 
                (style :stroke 1.0 
                       :background (color 0 255 0 200)
                       :foreground (color 0 150 0))))
                       )
                       )))

(defn remove-active-note [matrix x-cell y-cell]

)

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
        measure-border           (style :stroke 2.0 
                                        :foreground (color 100 100 100 200))
        note-border              (style :stroke 1.0 
                                        :foreground (color 100 100 100 100))
        beat-note-border         (style :stroke 1.25
                                        :foreground (color 100 100 100 175))
        white-note-track-border  (style :stroke 0.25 
                                        :foreground (color 100 100 100 100) 
                                        :background (color 255 255 255))
        black-note-track-border  (style :stroke 0.25 
                                        :foreground (color 100 100 100 100) 
                                        :background (color 180 180 180))
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
                    (rect ntx nty measure-width note-height) 
                      black-note-track-border)
                (= note-type :white)
                  (draw g
                    (rect ntx nty measure-width note-height) 
                      white-note-track-border))))
          (dotimes [note-x resolution]
            (let [nx (+ (* i measure-width) (* note-x note-width))]
              (if 
                (= 0 (mod note-x num-beats))
                  (draw g
                    (line nx 0 nx total-height) beat-note-border)
                  (draw g
                  (line nx 0 nx total-height) note-border)
                )

                ))
           (draw g
            (rect (* i measure-width) 0 measure-width (- total-height 1)) 
              measure-border))

          ;; paint the active notes last
          (paint-active-notes notes active-notes g note-width note-height)))

(defn contains-in? 
  [note-map x y]
  (let [result (not (nil? (get (get note-map x) y)))]
    result))

(defn contains-in? [note-map x y note-width]
  (let [r (atom nil)]
    (doseq [[note-x notes] note-map] 
      (if (<= note-x x)
        (do
          (doseq [[ny data] notes]
            (let [note-y ny
                  x-max (+ note-x (data :duration))]
                ;;just look at notes in this y range
                (if 
                  (and (<= (- x 1) x-max)
                       (>= (- x 1) note-x)
                       (= y note-y)) ;; bounds checking
                       (do
                          ; (println "contains-in? is in" r x x-max x note-x y note-y)
                          (swap! r (fn [_] {note-x {note-y data}}))))
                        )))))
    ; (println "contains in? return-note" r)
    @r))


(defn update-active-notes [active-notes-atom active-note x y x-cell y-cell note-width note-height]
  (let [x (int (/ x note-width))
        y (int (/ y note-height))
        length (count @active-notes-atom)
        return-note (atom (contains-in? @active-notes-atom x y note-width))
        new-note-data {:velocity 1.0 :duration 1}]

      (if (not (nil? @return-note))
        (do
            (doseq [[k note] @return-note] 
              (doseq [[n note-data] note] 
               ; (swap! return-note (fn [_] {k {n note-data}} ))
                (swap! x-cell (fn [_] k))
                (swap! y-cell (fn [_] n)))))
        (do 
          (swap! x-cell (fn [_] x))
          (swap! y-cell (fn [_] y))
          (swap! active-notes-atom (fn [notes] (assoc-in notes [x y] new-note-data)))
          (swap! return-note (fn [note] (assoc-in note [x y] new-note-data)))))
            @return-note))


(defn update-note [x y active-notes-atom active-note cell-diff]
  (let [v 1.0
        cell-x-start (first (keys @active-note))
        cell-y-start (first (keys (@active-note (first (keys @active-note)))))
        offset (- x cell-x-start)
        offset-diff (+ offset cell-diff)]  ;; <<-- NEED TO FIX THIS SO THAT IT GRABS THE CURRENT VELOCITY NOT JUST SETS IT TO 0
    (swap! active-notes-atom (fn [notes] 
      (update-in notes [cell-x-start cell-y-start] (fn [_]{:velocity v :duration cell-diff})))) ;;adding one to the cell diff makes the update happen directly under the mouse. if you dont it is always one cell behind when dragging
      (swap! active-note (fn [_] {cell-x-start {cell-y-start {:velocity v :duration cell-diff}}}))))

(def active-note (atom nil))

(defn piano-roll-widget
  [num-measures num-bars num-beats num-steps-per-beat num-octaves]
  (let [notes-atom (atom (note-matrix (atom nil) num-octaves num-measures num-bars num-beats num-steps-per-beat))
        active-notes-atom (atom {})
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
        cell-x-start (atom nil) ;; hold the starting cell location of the current note
        cell-y-start (atom nil)
        start-x (atom nil)      ;; hold the starting cell of the current gesture
        start-y (atom nil)
        active-note (atom {})
                ]


    (listen piano-roll
      :mouse-pressed
        (fn [e] 
          (let [x (.getX e) 
                y (.getY e)]
            (swap! start-x (fn [_] (int (/ x note-width))))
            (swap! start-y (fn [_] (int (/ y note-height))))
            (swap! active-note (fn [n] (update-active-notes active-notes-atom active-note x y cell-x-start cell-y-start note-width note-height)))
            (println "mouse pressed" start-x start-y cell-x-start cell-y-start)
            (.repaint (.getSource e))))

      :mouse-dragged
        (fn [e]
          (let [x-cell (int (/ (.getX e) note-width))
                y-cell (int (/ (.getY e) note-height))
                measure (int (/ piano-roll-width MEASURE_WIDTH))
                current-x-cell (int (/ (.getX e) note-width))
                cell-delta (- current-x-cell @cell-x-start)
                diff(if (> cell-delta 0) cell-delta 0)]
                    (println "mouse dragged" x-cell y-cell start-x start-y cell-x-start cell-y-start cell-delta diff)

                    (update-note x-cell y-cell active-notes-atom active-note diff)

                    (.repaint (.getSource e))))

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
                        :minimum-size [601 :by 502]
                        :content panel)
               pack!
               show!)]
        f))))



; (def m {0 {0 {:duration 8 :velocity 1.0}}, {3 {:duration 8 :velocity 1.0}}, {5 {:duration 8 :velocity 1.0}}})