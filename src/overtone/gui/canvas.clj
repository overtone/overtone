(ns overtone.gui.canvas
  (:use [penumbra opengl compute])
  (:require [penumbra.app :as app]))

(comment load and display image
(defn init [state]
  (enable :texture-2d)
  (assoc state
    :texture (load-texture-from-file "/home/rosejn/media/images/carbon orbitals.jpg")))

; Display an image in a window
(defn display [_ state]
  (println (:texture state))
  (blit (:texture state)))

(defn start []
  (app/start {:init init :display display} {}))
  )

(comment generated texture and render to texture

(defn textured-quad []
  (push-matrix
    (translate -0.5 -0.5 0.5)
    (normal 0 0 -1)
    (draw-quads
      (texture 1 1) (vertex 1 1 0)
      (texture 0 1) (vertex 0 1 0)
      (texture 0 0) (vertex 0 0 0)
      (texture 1 0) (vertex 1 0 0))))

(defn textured-cube []
  (dotimes [_ 4]
    (rotate 90 0 1 0)
    (textured-quad))
  (rotate 90 1 0 0)
  (textured-quad)
  (rotate 180 1 0 0)
  (textured-quad))

(defn xor [a b] (or (and a (not b)) (and (not a) b)))

(defn init-textures []
  (let [view (create-byte-texture 256 256)
        checkers (create-byte-texture 128 128)]
    (draw-to-subsampled-texture!
      checkers
      (fn [[x y] _]
        (if (xor (even? (bit-shift-right x 4)) (even? (bit-shift-right y 4)))
          [1 0 0 1]
          [0 0 0 1])))
    [checkers view]))

;;;;;;;;;;;;;;;;;

(defn init [state]
  (app/title! "Render to Texture")
  (tex-env :texture-env :texture-env-mode :modulate)
  (enable :texture-2d)
  (enable :depth-test)
  (enable :light0)
  (enable :lighting)
  (line-width 3)
  (let [[checkers view] (init-textures)]
    (assoc state
      :checkers checkers
      :view view)))

(defn reshape [_ state]
  (load-identity)
  (scale 1 1 -1)
  (translate 0 0 2))

(defn mouse-drag [[dx dy] [x y] button state]
  (let [[w h] (app/dimensions)]
    (if (< x (int (/ w 2)))
      (let [[lx ly] (:left state)]
        (assoc state :left [(- lx dy) (- ly dx)]))
      (let [[rx ry] (:right state)]
        (assoc state :right [(- rx dy) (- ry dx)])))))

(defn display [[delta time] state]
  (let [[lx ly] (:left state)
        [rx ry] (:right state)
        checkers (:checkers state)
        view (:view state)
        [w h] (app/dimensions)]

    (light 0
       :position [-1 -1 1 0])
    (material :front-and-back
       :ambient-and-diffuse [0.8 0.1 0.1 1])

    ;;render the checkered cube to a texture
    (render-to-texture view
     (clear 0.5 0.5 0.5)
     (with-projection (frustum-view 50. 1. 0.1 10.)
       (push-matrix
        (rotate lx 1 0 0) (rotate ly 0 1 0)
        (with-texture checkers
          (textured-cube)))))

    (clear 0 0 0)

    (with-projection (frustum-view 90. (double (/ w 2.0 h)) 0.1 10.)
      ;;render the checkered cube to the window
      (with-texture checkers
        (with-viewport [0 0 (/ w 2.0) h]
          (push-matrix
           (rotate lx 1 0 0) (rotate ly 0 1 0)
           (textured-cube))))
      ;;render a cube with the checkered cube texture
      (with-texture view
        (with-viewport [(/ w 2.0) 0 (/ w 2.0) h]
          (push-matrix
           (rotate rx 1 0 0) (rotate ry 0 1 0)
           (textured-cube)))))
    
    ;;draw a dividing line
    (with-disabled [:texture-2d :lighting]
      (with-projection (ortho-view 0 1 0 1 0 1)
        (push-matrix
         (load-identity)
         (draw-lines (vertex 0.5 0) (vertex 0.5 1)))))))

(defn start []
  (app/start
   {:display display, :mouse-drag mouse-drag, :reshape reshape, :init init}
   {:left [0 0], :right [0 0], :checkers nil, :view nil}))
)


(comment tripped out convolution on random rectangles
(defn draw-rect [x y w h]
  (with-disabled :texture-rectangle
    (draw-quads
     (vertex x y)
     (vertex (+ x w) y)
     (vertex (+ x w) (+ y h))
     (vertex x (+ y h)))))

(defn reset-image [tex]
  (render-to-texture tex
    (clear)                 
    (with-projection (ortho-view 0 2 0 2 -1 1)
      (dotimes [_ 100]
        (apply color (take 3 (repeatedly rand)))
        (apply draw-rect (take 4 (repeatedly rand))))))
  (app/repaint!)
  tex)

(defn init [state]

  (app/title! "Convolution")

  (defmap detect-edges
    (let [a (float4 0.0)
          b (float4 0.0)]
      (convolve %2
        (+= a (* %2 %1)))
      (convolve %3
        (+= b (* %3 %1)))
      (sqrt (+ (* a a) (* b b)))))

  (def filter-1
    (wrap (map float
               [-1 0 1
                -2 0 2
                -1 0 1])))

  (def filter-2
    (wrap (map float
               [1 2 1
                0 0 0
                -1 -2 -1])))

  (enable :texture-rectangle)
  (ortho-view 0 2 2 0 -1 1)
  (assoc state
    :tex (reset-image (create-byte-texture :texture-rectangle 512 512))))

(defn key-press [key state]
  (let [tex (:tex state)]
    (cond
     (= key " ")
     (assoc state
       :tex (reset-image tex))
     (= key :return)
     (assoc state
       :tex (detect-edges tex [filter-1] [filter-2]))
     :else
     state)))

(defn display [_ state]
  (blit (:tex state)))

(defn start []
  (app/start
   {:display display, :key-press key-press, :init init}
   {}))
)

(defn init [state]
    (app/vsync! true)
    state)
 
(defn reshape [[x y width height] state]
    (frustum-view 60.0 (/ (double width) height) 1.0 100.0)
    (load-identity)
    state)
 
(defn display [[delta time] state]
    (translate 0 -0.93 -3)
    (rotate (rem (* 90 time) 360) 0 1 0)
    (draw-triangles
         (color 1 0 0) (vertex 1 0)
         (color 0 1 0) (vertex -1 0)
         (color 0 0 1) (vertex 0 1.86))
    (app/repaint!))
 
(app/start
    {:display display, :reshape reshape, :init init}
    {})
