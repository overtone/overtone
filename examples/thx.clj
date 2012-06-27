(ns examples.thx
  (:use [overtone.live]))

;;Original THX sound simulation by Geirmund Simonsen:
;;{
;;        var randomFreq, ampEnv, riseCurve, doAdder, doArray;
;;        doAdder = 0;
;;        doArray = [77, 74, 72, 70, 65, 62, 60, 58, 53, 50, 46, 34].midicps;
;;        randomFreq = EnvGen.kr(Env([1,1,0.007], [8, 6], [0, -4]), 1);
;;        ampEnv = EnvGen.kr(Env([0.07,0.07,0.21], [8, 6], [0, 1]), 1);
;;        doArray.do({ |item| doAdder = Pan2.ar(Saw.ar((LFNoise2.kr(1.3, 100, 230)*randomFreq) +
;;        	EnvGen.kr(Env([0,0,item],[8, 6], [0, -3]), 1)), LFNoise2.kr(1.3)) + doAdder; });
;;        Out.ar(0, doAdder*ampEnv);
;;}.play

(definst thx [gate 1]
  (let [target-pitches (map midi->hz [77 74 72 70 65 62 60 58 53 50 46 34 26 22 14 10])
        r-freq         (env-gen:kr (envelope [1 1 0.007 10] [8 4 2] [0 -4 1] 2) gate)
        amp-env        (env-gen:kr (envelope [0 0.07 0.21 0] [8 4 2] [0 1 1] 2) gate :action FREE)
        mk-noise       (fn [ug-osc]
                         (mix (map #(pan2 (ug-osc (+ (* r-freq (+ 230 (* 100 (lf-noise2:kr 1.3))))
                                                     (env-gen:kr (envelope [0 0 %] [8 6] [0 -3]))))
                                          (lf-noise2:kr 5))
                                   target-pitches)))
        saws           (mk-noise saw)
        sins           (mk-noise sin-osc)
        snd            (+ (* saws amp-env) (* sins amp-env))]
    (* 0.5 (g-verb snd 9 0.7 0))))

;;play the instrument:
; (thx)
;;kill it off when you're ready
; (ctl thx :gate 0)
