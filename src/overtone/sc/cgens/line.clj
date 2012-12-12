(ns overtone.sc.cgens.line
  (:use [overtone.sc defcgen ugens]))

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
