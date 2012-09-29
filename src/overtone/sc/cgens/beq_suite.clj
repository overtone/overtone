(ns overtone.sc.cgens.beq-suite
  (:use [overtone.sc defcgen ugens]))

(defcgen b-low-pass4
  "24db/oct rolloff - 4th order resonant Low Pass Filter"
  [in {:doc "Input signal"}
   freq {:default 1200 :doc "cutoff frequency"}
   rq {:default 1 :doc "the reciprocal of Q. bandwidth / cutoff-freq."}]

  ""

  (:ar (let [rq     (sqrt rq)
             sr     (sample-rate)
             w0     (* Math/PI 2 freq (sample-dur))
             cos_w0 (cos w0)
             i      (- 1 cos_w0)
             alpha  (* (sin w0) 0.5 rq)
             b0rz   (reciprocal (+ 1 alpha))
             a0     (* i 0.5 b0rz)
             a1     (* i b0rz)
             b1     (* cos_w0 2 b0rz)
             b2     (* (- 1 alpha) (neg b0rz))]
         (sos:ar (sos:ar in a0 a1 a0 b1 b2) a0 a1 a0 b1 b2))))

(defcgen b-hi-pass4
  "24db/oct rolloff - 4th order resonant Hi Pass Filter"
  [in {:doc "Input signal"}
   freq {:default 1200 :doc "cutoff frequency"}
   rq {:default 1 :doc "the reciprocal of Q. bandwidth / cutoff-freq."}]

  ""

  (:ar (let [rq     (sqrt rq)
             sr     (sample-rate)
             w0     (* Math/PI 2 freq (sample-dur))
             cos_w0 (cos w0)
             i      (+ 1 cos_w0)
             alpha  (* (sin w0) 0.5 rq)
             b0rz   (reciprocal (+ 1 alpha))
             a0     (* i 0.5 b0rz)
             a1     (* (neg i) b0rz)
             b1     (* cos_w0 2 b0rz)
             b2     (* (- 1 alpha) (neg b0rz))]
         (sos:ar (sos:ar in a0 a1 a0 b1 b2) a0 a1 a0 b1 b2))))
