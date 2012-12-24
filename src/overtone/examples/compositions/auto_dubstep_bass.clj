(ns overtone.examples.compositions.auto-dubstep-bass
  (:use [overtone.live]))

;;Dan Stowell's dubstep bass

;;//s.boot
;;{
;; var trig, note, son, sweep;
;;
;; trig = CoinGate.kr(0.5, Impulse.kr(2));
;;
;; note = Demand.kr(trig, 0, Dseq((22,24..44).midicps.scramble, inf));
;;
;; sweep = LFSaw.ar(Demand.kr(trig, 0, Drand([1, 2, 2, 3, 4, 5, 6, 8, 16], inf))).exprange(40, 5000);
;;
;; son = LFSaw.ar(note * [0.99, 1, 1.01]).sum;
;; son = LPF.ar(son, sweep);
;; son = Normalizer.ar(son);
;; son = son + BPF.ar(son, 2000, 2);
;;
;; //////// special flavours:
;; // hi manster
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, 1000) * 4]);
;; // sweep manster
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, sweep) * 4]);
;; // decimate
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son.round(0.1)]);
;;
;; son = (son * 5).tanh;
;; son = son + GVerb.ar(son, 10, 0.1, 0.7, mul: 0.3);
;; son.dup
;;}.play

(demo 30
      (let [trig (coin-gate 0.5 (impulse:kr 2))
            note (demand trig 0 (dseq (shuffle (map midi->hz (conj (range 24 45) 22))) INF))
            sweep (lin-exp (lf-saw (demand trig 0 (drand [1 2 2 3 4 5 6 8 16] INF))) -1 1 40 5000)

            son (mix (lf-saw (* note [0.99 1 1.01])))
            son (lpf son sweep)
            son (normalizer son)
            son (+ son (bpf son 2000 2))

            ;;special flavours
            ;;hi manster
            son (select (< (t-rand:kr :trig trig) 0.05) [son (* 4 (hpf son 1000))])

            ;;sweep manster
            son (select (< (t-rand:kr :trig trig) 0.05) [son (* 4 (hpf son sweep))])

            ;;decimate
            son (select (< (t-rand:kr :trig trig) 0.05) [son (round son 0.1)])

            son (tanh (* son 5))
            son (+ son (* 0.3 (g-verb son 10 0.1 0.7)))
            son (* 0.3 son)]

        [son son]))

;;(stop)
