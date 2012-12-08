(ns overtone.gui.pianoroll
  (:use [seesaw core graphics make-widget color]
        [overtone.sc server]
        [overtone.gui spinner-label]
        [overtone.music time]
        [seesaw.border :only [line-border]])
  (:require [seesaw.bind :as bind]
            [overtone.config.log :as log])
  (:import [javax.swing DefaultBoundedRangeModel]))

(def ^{:private true} NOTE-TYPES
  [:white :black :white :black :white :white :black :white :black :white :black :white])

(def ^{:private true} NOTE-PADDING 2)

(def MEASURE-STYLE    (style :stroke 2.0 :foreground (color 100 100 100 200)))
(def BEAT-STYLE       (style :stroke 1.25 :foreground (color 100 100 100 175)))
(def TICK-STYLE       (style :stroke 1.0 :foreground (color 100 100 100 100)))
(def WHITE-NOTE-STYLE (style :stroke 0.25
                             :foreground (color 100 100 100 100)
                             :background (color 255 255 255)))
(def BLACK-NOTE-STYLE (style :stroke 0.25
                             :foreground (color 100 100 100 100)
                             :background (color 180 180 180)))
(def NOTE-STYLE       (style :stroke 1.0
                             :background (color 0 255 0 150)
                             :foreground (color 0 150 0)))
(defn- paint-piano-roll
  "Paint the dial widget group"
  [state* c g]
  (try
    (let [state @state*
          {:keys [num-octaves num-measures beats-per-measure steps-per-beat]} state
          w                        (width c)
          h                        (height c)
          num-notes                (* num-octaves 12)
          measure-width            (/ w num-measures)
          beat-width               (/ measure-width beats-per-measure)
          note-width               (/ beat-width steps-per-beat)
          note-height              (/ h num-notes)
          num-ticks                (* num-measures beats-per-measure steps-per-beat)]

      ; horizontal row per key
      (dotimes [note-y num-notes]
        (let [nty       (* note-y note-height)
              note-type (nth NOTE-TYPES (mod note-y 12))]
          (draw g (rect 0 nty w note-height)
                (case note-type
                  :black BLACK-NOTE-STYLE
                  :white WHITE-NOTE-STYLE))))

      ; vertical bars
      (dotimes [measure num-measures]
        (let [x (* measure measure-width)]
          (draw g (rect x 0 measure-width h)
                MEASURE-STYLE)))

      ; vertical beat and step lines
      (dotimes [beat num-ticks]
        (let [x (* beat note-width)]
          (draw g (line x 0 x h)
                (if (= 0 (mod beat beats-per-measure))
                  BEAT-STYLE
                  TICK-STYLE))))

      ; notes
      (doseq [[x y] (keys (:notes @state*))]
        (draw g (rounded-rect (+ NOTE-PADDING (* x note-width))
                              (+ NOTE-PADDING (* y note-height))
                              (- note-width (* 2 NOTE-PADDING))
                              (- note-height (* 2 NOTE-PADDING)))
              NOTE-STYLE)))
    (catch Exception e
      (log/warn (str "Exception in paint-piano-roll: " e
                     (with-out-str (.printStackTrace e)))))))

(defn- mouse-event->note-coords
  [{:keys [num-octaves num-measures beats-per-measure steps-per-beat]} e]
  (let [c (.getSource e)
        measure-width (/ (width c) num-measures)
        beat-width (/ measure-width beats-per-measure)
        note-width (/ beat-width steps-per-beat)
        note-height (/ (height c) (* 12 num-octaves))
        x (int (/ (.getX e) note-width))
        y (int (/ (.getY e) note-height))]
    [x y]))

(defn- toggle-note
  [state coords]
  (if ((:notes state) coords)
    (assoc state :notes (dissoc (:notes state) coords))
    (assoc-in state [:notes coords] true)))

(defn- piano-roll-panel
  "Returns a panel containing a piano roll sequencer gui."
  [state*]
  (try
    (let [state @state*
          {:keys [num-octaves num-measures beats-per-measure steps-per-beat]} state
          piano-roll (canvas :id :piano-roll
                             :paint (partial paint-piano-roll state*))
          panel (border-panel :center piano-roll)]

      (listen piano-roll
              :mouse-pressed
              (fn [e]
                (swap! state* toggle-note (mouse-event->note-coords state e))))
      panel)
    (catch Exception e
      (log/warn (str "Exception in piano-roll: " e)))))

;              :mouse-dragged
;              (fn [e]
;                (let [x-cell (int (/ (.getX e) note-width))
;                      y-cell (int (/ (.getY e) note-height))
;                      measure (int (/ piano-roll-width MEASURE-WIDTH))
;                      current-x-cell (int (/ (.getX e) note-width))
;                      cell-delta (- current-x-cell @cell-x-start)
;                      diff(+ 1 cell-delta)]
;
;                  (update-note x-cell y-cell active-notes-atom active-note diff)))
;
;              :mouse-released
;              (fn [e]
;                ;(swap! notes-atom )
;                ))

(defrecord PianoRoll [frame state])

(defn- toggle-playing [state]
  (update-in state [:playing?] not))

(defn- piano-player
  [state-atom beat]
  (let [state @state-atom]
    (when (:playing? state)
      (let [inst (:inst state)
            metro (:metronome state)
            notes (:notes state)
            offset (:offset state)
            index (mod beat (:steps state))
            next-beat (inc beat)]

        (swap! state-atom assoc-in [:step] index)

        (doseq [[x y] (keys notes)]
          (when (= x index)
            (at (metro beat) (inst (+ offset y)))))

        (apply-at (metro next-beat) #'piano-player
                  [state-atom next-beat])))))

(defn piano-roll
  [metro inst & {:keys [octaves measures beats-per-measure steps-per-beat offset]
                 :or {measures          4
                      beats-per-measure 4
                      steps-per-beat    4
                      octaves           4
                      offset            24}
                 :as options}]
  (invoke-now
    (try
      (let [state* (atom {:playing? false
                          :metronome metro
                          :inst inst
                          :num-octaves octaves
                          :num-measures measures
                          :beats-per-measure beats-per-measure
                          :steps-per-beat steps-per-beat
                          :offset offset
                          :notes {}
                          :steps (* measures beats-per-measure steps-per-beat)})
            play-btn     (button :text "Play")
            bpm-spinner  (spinner-label :class :sequencer-bpm-spinner
                                        :halign :center
                                        :border (line-border :thickness 1 :color :darkgrey)
                                        :maximum-size [60 :by 100]
                                        :model (spinner-model (:bpm metro) :from 1 :to 10000 :by 1))
            control-pane (toolbar :floatable? false
                                  :items [play-btn    [:fill-h 5]
                                          bpm-spinner [:fill-h 5] "bpm"])
            piano-roll (piano-roll-panel state*)
            panel      (border-panel :id :piano-roll
                                     :north control-pane
                                     :center piano-roll)
            f          (-> (frame :title "Piano Roll"
                                  :on-close :dispose
                                  :minimum-size [600 :by 500]
                                  :content panel)
                         pack!
                         show!)]
        (bind/bind bpm-spinner (bind/b-do [v] (metro :bpm v)))
        (bind/bind state* (bind/b-do [v] (repaint! piano-roll)))
        (listen play-btn :action
                (fn [e]
                  (let [playing? (:playing? (swap! state* toggle-playing))]
                    (config! play-btn :text (if playing? "stop" "play"))
                    (if playing?
                      (piano-player state* (metro))))))
        (with-meta
          (map->PianoRoll {:frame f :state state*})
          {:type :sequencer}))
      (catch Exception e
        (log/warn (str "Exception in piano roll: " e
                          (with-out-str (.printStackTrace e))))))))
