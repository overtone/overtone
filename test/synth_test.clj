(ns synth-test
  (:use test-utils
     (overtone synth)))

(defsynth saw-sin {:freq-a 443
                   :freq-b 440}
  (out.ar 0 (+ (* 0.3 (saw.ar :freq-a))
               (* 0.3 (sin-osc.ar :freq-b 0)))))

(comment 
(load-synth saw-sin)
(let [note (hit (now) saw-sin :freq-a 400 :freq-b 402)]
  (ctl (+ (now) 500) note :freq-a 300 :freq-b 303)
  (kill (+ (now) 1000) note))
  )

;SynthDef("round-kick", {|amp= 0.5, decay= 0.6, freq= 65|
;        var env, snd;
;        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;        snd= SinOsc.ar(freq, pi*0.5, amp);
;        Out.ar(0, Pan2.ar(snd*env, 0));
;}).store;
;(defsynth test-kick {:amp 0.5 :decay 0.6 :freq 65}
;  (out.ar 0 (pan2.ar (* (sin-osc.ar :freq (* Math/PI 0.5)) 
;                        :amp)
;                     (env-gen.ar (perc 0 :decay) :done-free))))
;

; Some other day...
;(deftest decompile-test []
;  (is (= (synthdef-decompile mini-sin) (:src-code (meta mini-sin)))))

(defsynth overtone-scope {:in 0 :buf 1}
  (record-buf.ar (in.ar :in) :buf))

;TODO: Use this synthdef for a regression test, it found a few bugs
; * first we need better OSC feedback from the server
(defsynth buzz {:pitch 40 :cutoff 300}
  (let [a (lpf.ar (saw.ar (midicps :pitch)) (+ (lf-noise-1.kr 10) :cutoff))
        b (sin-osc.ar (midicps (- :pitch 12)))]
  (out.ar 0 (pan2.ar (+ a b)))))

(comment deftest buf-synths-test []
  (let [s (synth "scope-synth" {:in 0 :buf 1} 
                 (record-buf.ar (in.ar :in) :buf))]
    s))

(defn synthdef-tests []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
