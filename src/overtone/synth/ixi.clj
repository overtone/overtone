(ns
    ^{:doc "A port of the synths found in The Thor's The Ixi Lang"
      :author "Sam Aaron"}
  overtone.synth.ixi
  (:use [overtone.core]))

(defsynth impulser
  [gate 1
   pan 0
   amp 1
   out-bus 0]
  (let [kill-env (env-gen:ar (adsr 0.0000001 1 0.2) gate :action FREE)
        imp      (impulse:ar 1)
        x        (* (pan2 (* imp (env-gen:ar (perc 0.0000001 0.2))) pan)
                    amp)]
    (out out-bus (leak-dc (limiter x)))))

(defsynth kick
  [mod-freq 2.6
   mod-index 5
   sustain 0.4
   beater-noise-level 0.025
   pan 0
   amp 0.3
   out-bus 0]
  (let [freq               80
        pitch-contour      (line:kr (* freq 2) freq 0.02)
        drum-osc           (pm-osc pitch-contour mod-freq (/ mod-index 1.3))
        drum-lpf           (lpf drum-osc 1000)
        drum-env           (* drum-lpf
                              (env-gen:ar (perc 0.005 sustain) 1 :action FREE))
        beater-source      (* (white-noise) beater-noise-level)
        beater-hpf         (hpf beater-source 500)
        lpf-cutoff-contour (line:kr 6000 500 0.03)
        beater-lpf         (lpf beater-hpf lpf-cutoff-contour)
        beater-env         (* beater-lpf
                              (env-gen:ar (perc 0.000001 1) :action FREE))
        kick-mix           (* (+ drum-env beater-env) 2 amp)]
    (out out-bus (pan2 kick-mix pan))))

(defsynth kick2
  [sustain 0.26
   pan 0
   amp 0.3
   out-bus 0]
  (let [env0  (env-gen:ar
               (envelope [0.5 1 0.5 0]
                         [0.005 0.06 sustain]
                         [-4 -2 -4])
               :action FREE)
        env1  (env-gen:ar
               (envelope [110 59 29]
                         [0.005 0.29]
                         [-4 -5]))
        env1m (midicps env1)
        son   (+ (lf-pulse:ar env1m 0 0.5 )
                 -0.5
                 (white-noise))
        son   (* (lpf son (* env1m 1.5)) env0)
        son   (+ son (* (sin-osc env1m 0.5)
                        env0))
        son   (* son 1.2)
        son   (clip2 son 1)]
    (out out-bus (pan2 (* son amp) pan))))

(defsynth kick3
  [high 150
   low 33
   phase 1.5
   dur 0.35
   sustain 0.4
   pan 0
   amp 0.3
   out-bus 0]
  (let [signal (* amp (sin-osc (x-line:kr high low dur) (* Math/PI phase)))
        signal (* signal
                  (env-gen:ar (perc 0.0001 sustain)
                              :action FREE))]
    (out out-bus (pan2 signal pan))))

(defsynth snare
  [drum-mode-level 1
   snare-level 50
   snare-tightness 1200
   sustain 0.04
   pan 0
   amp 0.3
   out-bus 0]
  (let [freq 305
        drum-mode-env   (env-gen:ar (perc 0.005 sustain) :action FREE)
        drum-mode-sin-1 (* (sin-osc (* freq 0.53)) drum-mode-env 0.5)
        drum-mode-sin-2 (* (sin-osc freq) drum-mode-env 0.5)
        drum-mode-pmosc (* 5 drum-mode-env (pm-osc (saw (* freq 0.85)) 184 (/ 0.5 1.3)))
        drum-mode-mix   (* drum-mode-level (+ drum-mode-sin-1 drum-mode-sin-2 drum-mode-pmosc))
        snare-noise     (* amp 0.8 (lf-noise0 9000))
        snare-env       (env-gen:ar (perc 0.0001 sustain) :action FREE)
        snare-brf-1     (* 0.5 (brf snare-noise 8000 0.1))
        snare-brf-2     (* 0.5 (brf snare-brf-1 5000 0.1))
        snare-brf-3     (* 0.5 (brf snare-brf-2 3600 0.1))
        snare-brf-4     (* snare-env (brf snare-brf-3 2000 0.1))
        snare-reson     (* snare-level (resonz snare-brf-4 snare-tightness))
        snare-drum-mix  (* amp (+ drum-mode-mix snare-reson))]
    (out out-bus (pan2 snare-drum-mix pan))))
