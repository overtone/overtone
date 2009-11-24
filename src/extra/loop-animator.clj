(ns loop-animator
  (:use rosado.processing)
  (:import (javax.swing JFrame))
  (:import (processing.core PApplet)))

(def cur-frame (ref 0))
(def frames (ref []))
(def last-time (ref 0))
(def mouse-x (ref 0))
(def mouse-y (ref 0))
(def pmouse-x (ref 0))
(def pmouse-y (ref 0))
(def mouse-pressed (ref false))

(defn next-frame []
  (let [frame (get-pixel)
        next-frame (inc @cur-frame)
        next-frame (if (>= next-frame (count @frames)) 0 next-frame)]
      (dosync 
        (alter frames assoc @cur-frame frame)
        (ref-set cur-frame next-frame))
    (image (get @frames next-frame) 0 0)))

(defn draw-frame
  [dst]
  (let [cur-time (millis)]
    (if (> cur-time (+ @last-time 30))
      (do
        (next-frame)
        (dosync (ref-set last-time cur-time))))
    (if @mouse-pressed
      (line @pmouse-x @pmouse-y @mouse-x @mouse-y))
    (dosync 
      (ref-set pmouse-x @mouse-x)
      (ref-set pmouse-y @mouse-y))))

(def hypno-applet
	 (proxy [PApplet] []
	   (setup []
       (binding [*applet* this]
         (size 640 200)
         (stroke-weight 12)
         (smooth)
         (background-int 204)
         (dosync (ref-set frames 
                          (vec (for [i (range 24)] (get-pixel)))))))

      (draw [] 
        (binding [*applet* this]
          (draw-frame this)))

      (mousePressed [event]
                    (dosync 
                      (ref-set mouse-pressed true)
                      (ref-set mouse-x (.getX event))
                      (ref-set mouse-y (.getY event))))
  
      (mouseReleased [event]
                    (dosync (ref-set mouse-pressed false)))
  
      (mouseDragged [event]
                    (dosync 
                      (ref-set mouse-x (.getX event))
                      (ref-set mouse-y (.getY event))))))
  
(.init hypno-applet)

(def swing-frame (JFrame. "Hypnotize"))

(doto swing-frame
	(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
	(.setSize 200 200)
	(.add hypno-applet)
	(.pack)
	(.show))

