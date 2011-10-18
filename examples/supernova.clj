(ns examples.supernova
  (:use [overtone.live]))

;;Examples translated from Tim Blechmann's Masters Thesis: http://tim.klingt.org/publications/tim_blechmann_supernova.pdf

;;Listing 1.1
;;SynthDef(\sweep, { arg duration = 10, amp = 0.01, freq_base = 1000, freq_target = 1100;
;;var	freq	=	Line.kr(freq_base ,	freq_target ,	duration); var sine = SinOsc.ar(freq); var env = EnvGen.kr(Env.linen(0.1, duration - 0.5, 0.4, amp),
;;doneAction: 2);
;;         Out.ar(0, sine * env); }).add;

(defsynth sweep-sin [duration 10 amp 0.01 freq-base 1000 freq-target 1100]
  (let [freq (line:kr freq-base freq-target duration)
        sine (sin-osc freq)
        env (env-gen:kr (lin-env 0.1 (- duration 0.5) 0.4 amp) :action FREE)]
    (out 0 (* sine env))))


;;Listing 1.2
;;
;;  ~r = Routine({ Synth(\sweep, [\duration, 10,
;;\amp, -12.dbamp, \freq_base ,	1000, \freq_target ,	1100]);
;;1.0.wait; Synth(\sweep, [\duration, 9,
;;\amp, -9.dbamp, \freq_base ,	1000, \freq_target ,	200]);
;;1.5.wait; Synth(\sweep, [\duration, 7.5,
;;\amp, -18.dbamp, \freq_base ,	1000, \freq_target ,	10000]);
;;}); ~r.play // run the routine

(let [t (+ 25 (now))]
  (at t (sweep-sin :duration 10
                   :amp (db->amp -12)
                   :freq-base 1000
                   :freq-target 1100))

  (at (+ 1000 t) (sweep-sin :duration 9
                   :amp (db->amp -9)
                   :freq-base 1000
                   :freq-target 200))

  (at (+ 2500 t) (sweep-sin :duration 7.5
                   :amp (db->amp -18)
                   :freq-base 1000
                   :freq-target 10000)))
