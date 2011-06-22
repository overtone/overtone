(ns example.demand
  (:use overtone.core))

(demo 2
      (let [trig (impulse:kr 8)
            freqs (dseq [440 880 220] Float/POSITIVE_INFINITY)
            note-gen (demand:kr trig 0 freqs)
            src (sin-osc note-gen)]
        (pan2 (* 0.1 src))))
