(ns
    ^{:doc "Audio effects library"
      :author "Jeff Rose"}
  overtone.studio.fx
  (:use [overtone.libs event]
        [overtone.sc synth gens]))

(defonce __FX-SYNTHS__
  (do
    (defsynth fx-noise-gate
      [in-bus 20 out-bus 10 threshold 0.4
       slope-below 1 slope-above 0.1
       clamp-time 0.01 relax-time 0.1]
      (let [source (in in-bus)]
        (out out-bus
             (compander source source threshold
                        slope-below slope-above
                        clamp-time relax-time))))

    (defsynth fx-compressor
      [in-bus 20 out-bus 10 threshold 0.2
       slope-below 1 slope-above 0.5
       clamp-time 0.01 relax-time 0.01]
      (let [source (in in-bus)]
        (out out-bus
             (compander source source threshold
                        slope-below slope-above
                        clamp-time relax-time))))

    (defsynth fx-limiter
      [in-bus 20 out-bus 10 threshold 0.2
       slope-below 1 slope-above 0.1
       clamp-time 0.01 relax-time 0.01]
      (let [source (in in-bus)]
        (out out-bus
             (compander source source threshold
                        slope-below slope-above
                        clamp-time relax-time))))

    (defsynth fx-sustainer
      [in-bus 20 out-bus 10 threshold 0.2
       slope-below 1 slope-above 0.5
       clamp-time 0.01 relax-time 0.01]
      (let [source (in in-bus)]
        (out out-bus
             (compander source source threshold
                        slope-below slope-above
                        clamp-time relax-time))))

    (defsynth fx-reverb
      [in-bus 20 out-bus 10
       wet-dry 0.5 room-size 0.5 dampening 0.5]
      (out out-bus
           (* 1.4 (free-verb (in in-bus) wet-dry room-size dampening))))

    (defsynth fx-echo
      [in-bus 20 out-bus 10
       max-delay 1.0 delay-time 0.4 decay-time 2.0]
      (let [source (in in-bus)
            echo (comb-n source max-delay delay-time decay-time)]
        (out out-bus (pan2 (+ echo source) 0))))

    (defsynth fx-chorus
      [in-bus 20 out-bus 10
       rate 0.002 depth 0.01]
      (let [src (in in-bus)
            dub-depth (* 2 depth)
            rates [rate (+ rate 0.001)]
            osc (+ dub-depth (* dub-depth (sin-osc:kr rates)))
            dly-a (delay-l src 0.3 osc)
            sig (apply + src dly-a)]
        (out out-bus (* 0.3 sig))))

    (defsynth fx-distortion
      [in-bus 20 out-bus 10
       boost 4 level 0.01]
      (let [src (in in-bus)]
        (out out-bus (distort (* boost (clip2 src level))))))

    (defsynth fx-rlpf
      [in-bus 20 out-bus 10
       cutoff 40000 res 0.6]
      (let [src (in in-bus)]
        (out out-bus (rlpf src cutoff res))))

    (defsynth fx-rhpf
      [in-bus 20 out-bus 10
       cutoff 2 res 0.6]
      (let [src (in in-bus)]
        (out out-bus (rhpf src cutoff res))))
    ))
