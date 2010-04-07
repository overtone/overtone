(ns examples.experiments
  (:use overtone.live))

(refer-ugens)

(defsynth saw-sin [freq-a 443
                   freq-b 440]
  (out 0 (+ (* 0.3 (saw freq-a))
               (* 0.3 (sin-osc freq-b 0)))))

(defsynth whoah []
  (let [sound (resonz (saw (map #(+ % (* (sin-osc 100) 1000)) [440 443 437])) (x-line 10000 10 10) (line 1 0.05 10))]
  (* (lf-saw:kr (line:kr 13 17 3)) (line:kr 1 0 10) sound)))

(defn square [freq]
  (pulse freq 0.5))

(defn mix [& args]
  (reduce + args))

(defsynth vintage-bass [note 60 velocity 100 detune 7 rq 0.4]
  (let [saw1 (* 0.75 (saw (midicps note)))
        saw2 (* 0.32 (saw (+ detune (midicps note))))
        sqr  (* 0.32 (square (midicps (- note 12))))
        amp  (/ 128.0 velocity)
        mx   (* amp (mix saw1 saw2 sqr))
        env-amp (+ 0.25 (* 0.55 amp))
        env (* env-amp (env-gen (adsr) velocity 1 0 1 :free))
        filt (rlpf mx (* env (midicps note)) rq)]
    (out 0 filt)))


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
