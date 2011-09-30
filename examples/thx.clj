(ns examples.thx
  (use [overtone.live]))

;;THX sound simulation by Geirmund Simonsen
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


(demo 25
      (let [target-pitches (map midi->hz [77 74 72 70 65 62 60 58 53 50 46 34])
            r-freq         (env-gen:kr (envelope [1 1 0.007] [8 6] [0 -4]) 1)
            amp-env        (env-gen:kr (envelope [0.07 0.07 0.21] [8 6] [0 1]) 1)
            saws           (apply + (map #(pan2 (saw (+ (* r-freq (+ 230 (* 100 (lf-noise2:kr 1.3))))
                                                           (env-gen:kr (envelope [0 0 %] [8 6] [0 -3]) 1)))
                                                   (lf-noise2:kr 1.3)) target-pitches))]
        (* saws amp-env)))
