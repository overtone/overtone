(ns overtone.sc.examples.vosim
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens]
        [overtone.sc.cgens line mix]))

(defexamples vosim
  (:mouse1
   "Use mouse X location to control the frequency of the vosim."
   "The mouse X location controls the frequency of this simple vosim
    example.  From the .schelp file."

   rate :ar
   []
   "
   (vosim (impulse 100) (mouse-x 440 880 1) 3 0.99)"
   contributor "Roger Allen")
  
  (:mouse2
   "Use the mouse X & Y location to modify some random vosim noises."
   "The mouse X location controls the frequency and the mouse Y
    location controls the decay factor.  A series of random tones is
    played and augmented by these mouse controls.  Example is from the
    .schelp file."

   rate :ar
   []
   "
   (let [p (t-rand:ar 0.0 1.0 (impulse:ar 6))
         t (impulse:ar (* 9 (+ 1 (> p 0.95))))
         f (t-rand:ar [40.0 120.0 220.0] [440.0 990.0 880.0] t)
         n (t-rand:ar 4.0 [8.0 16.0 32.0] t)
         d (t-rand:ar [0.2 0.4 0.6] [0.6 0.8 1.0] t)
         a (t-rand:ar 0.0 [0.2 0.6 1.0] t)
         l (t-rand:ar -1.0 1.0 t)
         x (mouse-x:kr 0.25 2.0)
         y (mouse-y:kr 0.25 1.5)
         z 9.0
         x_ (* x (lin-lin (lf-noise2:kr z) -1.0 1.0 0.25 2.0))
         y_ (* y (lin-lin (lf-noise2:kr z) -1.0 1.0 0.25 2.0))]
      (out:ar 0 (pan2:ar (mix:ar (* (vosim:ar t (* f x_) n (* d y_)) a)) l 1)))"
   contributor "Roger Allen"))
