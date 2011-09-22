(ns example.synthesis
  (:use overtone.live))

(scope)

; FM synthesis
(definst fma [freq-a 440 amp-a 0.7 freq-b 150 amp-b 0.7]
  (let [osc-a (* amp-a (sin-osc freq-a))
        osc-b (sin-osc (+ freq-b osc-a))]))
(fma)
(stop)

(definst fm-demo [freq 330 amp 0.2 amp-b 0.2]
  (let [osc-a (* (sin-osc (mouse-x 20 3000))
                 0.2)
        osc-b (* amp (sin-osc (* (mouse-y 3000 0) osc-a)))]
    osc-a))

(fm-demo)
(stop)

; Subtractive synthesis

; Karplus-Strong synthesis

; Physical modeling synthesis


