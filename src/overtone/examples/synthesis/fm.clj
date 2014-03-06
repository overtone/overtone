(ns overtone.examples.synthesis.fm
  (:use overtone.live))

(defsynth fm [carrier 440 divisor 2.0 depth 1.0 out-bus 0]
  (let [modulator (/ carrier divisor)
        mod-env   (env-gen (lin 1 0 6))
        amp-env   (env-gen (lin 1 1 5) :action FREE)]
    (out out-bus (pan2 (* 0.5 amp-env
                          (sin-osc (+ carrier
                                      (* mod-env  (* carrier depth) (sin-osc modulator)))))))))

; Some of these are more or less interesting
(fm)
(fm 220)
(fm 220 3)
(fm 220 10)
(fm 440)
(fm 440 2 4)
(fm 440 2 8)
(fm 440 4 4)
(fm 220 4 8)
(fm 880 4 4)
(fm 110 4 4)
(fm 220 2 4)
(fm 220 2 8)
(fm 440 8 8)
(fm 440 8 2)
(fm 440 (/ 4 3) 2)
(fm 440 (/ 5 3) 2)
(fm 440 (/ 7 3) 2)
(fm 440 (/ 4 3) 4)
(fm 440 (/ 5 3) 4)
(fm 440 (/ 7 3) 4)
(fm 220 (/ 7 5) 2)
(fm 220 (/ 7 5) 4)
(fm 110 4 2)
(fm 110 4 4)
(fm 110 4 8)
