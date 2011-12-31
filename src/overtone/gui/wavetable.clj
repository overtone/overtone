(ns overtone.gui.wavetable
  (:use [overtone.sc server buffer]
        [overtone.music time]
        [seesaw core graphics color])
  (:require [seesaw.bind :as bind]))


(def WAVE-Y-PADDING 10)

(defn- paint-wave-table
  "Paint the dial widget group"
  [buf c g]
  (try
    (let [w (width c)
          h (height c)
          line-style (style :foreground "#FFFFFF" :stroke 1.0 :cap :round :dash [10 10])
          data (buffer-data @buf)
          x-array (int-array w)
          y-array (int-array w)
          step (/ (:size @buf) w)
          y-scale h] ;(- h (* 2 WAVE-Y-PADDING))]

      ; draw the midpoint axis
      (draw g (line 0 (/ h 2) w (/ h 2)) line-style)
      ;(draw g (line (/ w 2) 0 (/ w 2) h) line-style)

      (dotimes [i w]
        (aset ^ints x-array i i)
        (aset ^ints y-array i (int (* y-scale 0.5
                                (+ 1.0 (* -1 (aget ^floats data (unchecked-multiply i step))))))))

      (.setColor g (color 0 140 236))
      (.setStroke g (stroke :width 2.0))
      (.drawPolyline g x-array y-array w))
    (catch Exception ex
      (println (str "Error in paint-wave-table: " ex))
      (.printStackTrace ex))))

(defn wave-panel
  [buf]
  (border-panel :hgap 5 :vgap 5 :border 5
                :center (canvas :id :wave-canvas
                                :background "#000000"
                                :paint (partial paint-wave-table buf))))


(defn interpolate
  [a b steps]
  (let [shift (/ (- b a) (float (dec steps)))]
    (concat (take (dec steps) (iterate #(+ shift %) a)) [b])))

(defn- editor-drag-handler
  [buf last-drag event]
  (try
    (let [buf @buf
          source (.getComponent event)
          w (width source)
          h (height source)
          cx (.getX event)
          cy (.getY event)]
      (when (and (>= cx 0) (< cx w) (>= cy 0) (< cy h))
        (let [buf-size (:size buf)
              step (/ buf-size (float w))
              [last-x last-y] @last-drag

              x1 (max 0 (min last-x cx))
              x2 cx
              x3 (min w (max last-x cx))

              x1-idx (Math/round (* x1 step))
              x2-idx (Math/round (* x2 step))
              x3-idx (Math/round (* x3 step))

              cy-val (* -1 (- (* 2 (* cy (/ 1.0 h))) 1))

              buf-data (buffer-data buf)
              [start-idx y-vals] (if (< x1 x2)
                                   [x1-idx (interpolate (nth buf-data x1-idx) cy-val
                                                        (inc (- x2-idx x1-idx)))]
                                   [x2-idx [cy-val]])
              y-vals (if (> x3-idx x2-idx)
                       (concat y-vals (interpolate cy-val (nth buf-data x3-idx)
                                                   (inc (- x3-idx x2-idx))))
                       y-vals)]
          (reset! last-drag [cx cy])
          (buffer-write! buf start-idx y-vals)
          (.repaint source))))
        (catch Exception ex
          (println (str "Error in drag-handler:" ex))
          (.printStackTrace ex))))

(defn- editor-press-handler
  [last-drag event]
  (reset! last-drag [(.getX event) (.getY event)]))

(defn- add-behaviors
  [buf f]
  (let [canvas (select f [:#wave-canvas])
        last-drag (atom nil)]
    (listen canvas
            :mouse-dragged (partial editor-drag-handler buf last-drag)
            :mouse-pressed (partial editor-press-handler last-drag))))

(defn wave-table-editor
  [buf]
  (invoke-now
    (try
      (let [buf-atom (atom buf)
            wave-pane (wave-panel buf-atom)
            f (frame :title "Wave Table"
                     :content (wave-panel buf-atom)
                     :width 1024 :height 760
                     :minimum-size [640 :by 480])]
        (add-behaviors buf-atom f)
        (-> f pack! show!)
        (with-meta
          {:frame f :buf buf-atom}
          {:type ::wave-table-editor}))
      (catch Exception e
        (.printStackTrace e)))))

(defn set-wave-table!
  [editor buf]
  (reset! (:buf editor) buf))

(defn fill-buffer
  [b f]
  (let [size (:size b)]
    (buffer-write! b (map #(f (/ (* % 2 Math/PI) size)) (range 0 size)))
    b))

(comment
  (use 'overtone.live)
  (use :reload 'overtone.gui.wavetable)
  (def b (buffer 1024))
  (fill-buffer b #(Math/cos %))
  (wave-table-editor b)

  (defsynth table-player
    [buf 0 freq 440]
    (out 0 (* [0.8 0.8] (osc buf freq))))

  )
