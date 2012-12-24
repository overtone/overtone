(ns overtone.examples.compositions.auto-dubstep
  (:use [overtone.live]))

;; Dan Stowells' Dubstep Synth:
;; SClang version:
;;
;;s.waitForBoot{Ndef(\a).play;Ndef(\a,
;;{
;;var trig, freq, notes, wob, sweep, kickenv, kick, snare, swr, syn, bpm, x;
;;x = MouseX.kr(1, 4);
;;
;;
;;// START HERE:
;;
;;bpm = 120;
;;
;;notes = [40, 41, 28, 28, 28, 28, 27, 25, 35, 78];
;;
;;trig = Impulse.kr(bpm/120);
;;freq = Demand.kr(trig, 0, Dxrand(notes, inf)).lag(0.25).midicps;
;;swr = Demand.kr(trig, 0, Dseq([1, 6, 6, 2, 1, 2, 4, 8, 3, 3], inf));
;;sweep = LFTri.ar(swr).exprange(40, 3000);
;;
;;
;;// Here we make the wobble bass:
;;wob = Saw.ar(freq * [0.99, 1.01]).sum;
;;wob = LPF.ar(wob, sweep);
;;wob = Normalizer.ar(wob) * 0.8;
;;wob = wob + BPF.ar(wob, 1500, 2);
;;wob = wob + GVerb.ar(wob, 9, 0.7, 0.7, mul: 0.2);
;;
;;
;;// Here we add some drums:
;;kickenv = Decay.ar(T2A.ar(Demand.kr(Impulse.kr(bpm / 30),0,Dseq([1,0,0,0,0,0,1,0, 1,0,0,1,0,0,0,0],inf))),0.7);
;;kick = SinOsc.ar(40+(kickenv*kickenv*kickenv*200),0,7*kickenv).clip2;
;;snare = 3*PinkNoise.ar(1!2)*Decay.ar(Impulse.ar(bpm / 240, 0.5),[0.4,2],[1,0.05]).sum;
;;snare = (snare + BPF.ar(4*snare,2000)).clip2;
;;
;;// This line actually outputs the sound:
;;(wob + kick + snare).clip2;
;;
;;})}
;;
;; Directly translated to Overtone:

(demo 60
      (let [bpm     120
            notes   [40 41 28 28 28 27 25 35 78]
            trig    (impulse:kr (/ bpm 120))
            freq    (midicps (lag (demand trig 0 (dxrand notes INF)) 0.25))
            swr     (demand trig 0 (dseq [1 6 6 2 1 2 4 8 3 3] INF))
            sweep   (lin-exp (lf-tri swr) -1 1 40 3000)
            wob     (mix (saw (* freq [0.99 1.01])))
            wob     (lpf wob sweep)
            wob     (* 0.8 (normalizer wob))
            wob     (+ wob (bpf wob 1500 2))
            wob     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

            kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
            kick    (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
            kick    (clip2 kick 1)

            snare   (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
            snare   (+ snare (bpf (* 4 snare) 2000))
            snare   (clip2 snare 1)]

   (clip2 (+ wob kick snare) 1)))
;;(stop)
