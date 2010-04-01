(ns examples.basic
  (:use overtone.live))

(refer-ugens)

(defn wah-wah [freq depth]
  (* depth (sin-osc:kr freq)))

(on :connected 
    #(do
       (defsynth ticker [freq 2]
         (* (sin-osc 440) (env-gen (perc 0.1 0.2) (sin-osc:kr freq))))

       (defsynth sizzle [bus 0 amp 0.4 depth 10 freq 220 lfo 8] 
         (out bus (* amp (saw (+ freq (wah-wah lfo depth))))))

       (defsynth bus->buf [bus 20 buf 0]
         (record-buf (in bus) buf))

       (defsynth bus->bus [in-bus 20 out-bus 0]
         (out out-bus (in in-bus)))
       (event :examples-ready)))

