(ns overtone.examples.tweets
  (:use [overtone.live]))


;;http://twitter.com/redFrik/status/328819081610399744
;;play{a=SinOsc ;a.ar(a.ar(1/[8,9])*4+[400,202],CombC.ar(InFeedback.ar([1,0]).lagud(a.ar(1/9)+1/88,a.ar(1/8)+1/99),1,0.08,9))}//

(defsynth redfrik-3288
  [freq-mul 1]
  (let [freq     (* freq-mul (+ [400 202] (* 4 (sin-osc (/ 1 [8 9])))))
        feedback (lag-ud (in-feedback [1 0])
                         (* (+ (sin-osc 1/9)
                               1)
                            1/88)
                         (* (+ (sin-osc 1/8)
                               1)
                            1/99))
        phase (comb-c feedback 1 0.08 9)
        ]
    (out 0 (sin-osc freq phase))))

;;(def r (redfrik-3288))
;;(ctl r :freq-mul 1)
;;(kill r)

;;https://twitter.com/redFrik/status/329680702205468674
;;{|k|play{a=SinOsc;Mix({|i|LeakDC.ar(Pan2.ar(a.ar(1/9+i,0,j=a.ar(i+1/99)),a.ar(i+1+k*(j.ceil*39+39),a.ar(k+2),j)))}!9)/3}}!2//

(defsynth redfrik-3296
  []
  (let [
        part (fn [i]
               (let [j (sin-osc (+ i 1/99))]
                 (leak-dc (pan2 (* j (sin-osc (+ i 1/9)))
                                (* j (sin-osc (* (+ 39 (* 39 (ceil j))) (+ i 1 0))
                                              (sin-osc (+ 2 0))))))))]
    (out 0 (/ (mix (map part (range 9))) 3))))


(do
  (redfrik-3296 )
  (redfrik-3296 ))

;;(show-graphviz-synth redfrik-3296)
