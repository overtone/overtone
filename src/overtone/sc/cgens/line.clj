(ns overtone.sc.cgens.line
  (:use [overtone.sc defcgen ugens]))

(defcgen varlag
  "Variable shaped lag"
  [in     {:default 0 :doc "Input to lag"}
   time   {:default 0.1 :doc "Lag time in seconds"}
   curvature {:default 0 :doc "Control curvature if shape input is 5 (default). 0 means linear, positive and negative numbers curve the segment up and down."}
   shape  {:default 5 :doc "Shape of curve. 0: step, 1: linear, 2: exponential, 3: sine, 4: welch, 5: custom (use curvature param), 6: squared, 7: cubed, 8: hold"}

   ]
  "Similar to Lag but with other curve shapes than exponential. A change on the input will take the specified time to reach the new value. Useful for smoothing out control signals."
  (:kr
   (let [gate (+ (+ (impulse:kr 0 0) (> (abs (hpz1 in)) 0))
                 (> (abs (hpz1 time)) 0) )]
     (env-gen [in 1 -99 -99 in time shape curvature] gate))))


(defcgen lin-lin
  "Map values from one linear range to another"
  [in {:default 0.0 :doc "Input to convert"}
   srclo {:default 0.0 :doc "Lower limit of input range"}
   srchi {:default 1.0 :doc "Upper limit of input range"}
   dstlo {:default 1.0 :doc "Lower limit of output range"}
   dsthi {:default 2.0 :doc "Upper limit of output range"}
   ]
  ""
  (:ar (let [scale (/ (- dsthi dstlo) (- srchi srclo))
             offset (- dstlo (* scale srclo))]
         (mul-add in scale offset)))

  (:kr (let [scale (/ (- dsthi dstlo) (- srchi srclo))
             offset (- dstlo (* scale srclo))]
         (mul-add in scale offset))))

(defcgen range-lin
  "Map ugens with default range linearly to another"
  [in    {:default 0.0 :doc "Input to convert (should have range -1 to 1)"}
   dstlo {:default 1.0 :doc "Lower limit of output range"}
   dsthi {:default 2.0 :doc "Upper limit of output range"}]
  "Linearly maps input signal with expected range of -1 to 1 to the
   specified range from dstlo to dsthi."
  (:ar (lin-lin:ar in -1 1 dstlo dsthi))
  (:kr (lin-lin:kr in -1 1 dstlo dsthi)))
