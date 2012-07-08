(ns sc-three
  (:use [overtone.live]))

;; Helpers

;; This is the default synth. Is it accessible from Overtone?
(comment
                SynthDef(\default, { arg out=0, freq=440, amp=0.1, pan=0, gate=1;
                                var z;
                                z = LPF.ar(
                                                Mix.new(VarSaw.ar(freq + [0, Rand(-0.4,0.0), Rand(0.0,0.4)], 0, 0.3, 0.3)),
                                                XLine.kr(Rand(4000,5000), Rand(2500,3200), 1)
                                        ) * Linen.kr(gate, 0.01, 0.7, 0.3, 2);
                                OffsetOut.ar(out, Pan2.ar(z, pan, amp));
                }, [\ir])
)

(defsynth s [out 0 freq 440 amp 0.1 pan 0 gate 1]
  (let [z (* (lpf (mix (var-saw [freq (+ freq (ranged-rand -0.4 0.0)) (+ freq (ranged-rand 0.0 0.4))] 0 0.3 0.3))
                  (x-line:kr (ranged-rand 4000 5000) (ranged-rand 2500 3200) 1))
             (linen:kr gate 0.01 0.7 0.3 2))]
    (offset-out out (pan2 z pan amp))))

(defn release
  ([x releaseTime] (ctl x :gate (- releaseTime)))
  ([x] (release x 0.0)))

(defn wait-release
  ([x releaseTime] (if @x (release @x releaseTime) (recur x releaseTime)))
  ([x] (wait-release x 0.0)))

(defn generator [& thunks]
  (let [r (agent thunks)]
    #(if (seq @r)
       (do
         (send-off r (fn [ts] (when (seq ts) ((first ts))) (rest ts)))
         'ok)
       'done)))

;; Page 83
;; "foo" repeats every second
;; SystemClock.sched(0, {"foo".postln; 1.0});
(periodic 1000 #(println "foo"))
;; "bar" repeats at a random delay
;; SystemClock.sched(0, {"bar".postln; 1.0.rand});
(defn bar []
  (println "bar")
  (after-delay (rand-int 1000) #'bar))
(bar)
;; clear all scheduled events
;; SystemClock.clear;
(stop)

;; Page 85
(comment
  // Fermata
  (
   r = Routine ({
                 x = Synth(\default, [freq: 76.midicps]);
                 1.wait;

                 x.release(0.1);
                 y = Synth(\default, [freq: 73.midicps]);
                 "Waiting...".postln;
                 nil.yield; // fermata

                 y.release(0.1);
                 z = Synth(\default, [freq: 69.midicps]);
                 2.wait;
                 z.release();
                 });
   // do this then wait for the fermata
   r.play;
   // feel the sweet tonic...
   r.play;
  )
)

(def go
  (let [y (atom nil)]
   (generator
    (fn []
      (let [x (s :freq (midi->hz 76))]
        (after-delay
         1000
         #(do
            (release x 0.1)
            (reset! y (s :freq (midi->hz 73)))))))
    (fn []
      (wait-release y 0.1)
      (let [z (s :freq (midi->hz 69))]
        (after-delay
         2000
         #(release z)))))))
(go)
(go)


;; Page 88
///////////////////////////////////////////////////////////////
// Figure 3.3 Nesting tasks inside routines
(comment
(
r = Routine({
        c = TempoClock.new; // make a TempoClock
        // start a 'wobbly' loop
        t = Task({
                loop({
                        x.release(0.1);
                        x = Synth(\default, [freq: 61.midicps, amp: 0.2]);
                        0.2.wait;
                        x.release(0.1);
                        x = Synth(\default, [freq: 67.midicps, amp: 0.2]);
                        rrand(0.075, 0.25).wait; // random wait from 0.1 to 0.25 seconds
                });
        }, c); // use the TempoClock to play this Task
        t.start;
        nil.yield;

        // now add some notes
        y = Synth(\default, [freq: 73.midicps, amp: 0.3]);
        nil.yield;
        y.release(0.1);
        y = Synth(\default, [freq: 79.midicps, amp: 0.3]);
        c.tempo = 2; // double time
        nil.yield;
        t.stop; y.release(1); x.release(0.1); // stop the Task and Synths
});
)

r.next; // start loop
r.next; // first note
r.next; // second note; loop goes 'double time'
r.next; // stop loop and fade
)

(do
  (def cont (atom true))
  (def x (atom nil))
  (def y (atom nil))
  (def t (atom 1))

  (defn go1 []
    (when @x (release @x 0.1))
    (when @cont
      (reset! x (s :freq (midi->hz 61) :amp 0.2))
      (after-delay
       (/ 200 @t)
       #(do
          (release @x 0.1)
          (reset! x (s :freq (midi->hz 67) :amp 0.2))
          (after-delay
           (/ (ranged-rand 75 250) @t)
           #'go1)))))

  (defn go2 []
    (reset! y (s :freq (midi->hz 73) :amp 0.3)))

  (defn go3 []
    (release @y 0.1)
    (reset! y (s :freq (midi->hz 79) :amp 0.3))
    (reset! t 2))

  (defn go4 []
    (release @y 0.1)
    (reset! cont false))

  (def go (generator go1 go2 go3 go4))
)
(go)
(go)
(go)
(go)

;; Page 89
///////////////////////////////////////////////////////////////
// Figure 3.4 Using patterns within a task
(comment
(// random notes from lydian b7 scale
p = Pxrand([64, 66, 68, 70, 71, 73, 74, 76], inf).asStream;
// ordered sequence of durations
q = Pseq([1, 2, 0.5], inf).asStream;
t = Task({
        loop({
                x.release(2);
                x = Synth(\default, [freq: p.value.midicps]);
                q.value.wait;
        });
});
t.start;
)
t.stop; x.release(2);
)

(defn no-twice-in-a-row [s]
  (filter identity (map (fn [a b] (and (not= a b) a)) s (rest s))))

(do
  (def p (no-twice-in-a-row (chosen-from [64 66 68 70 71 73 74 76])))
  (def q (cycle [1000 2000 500]))

  (def cont (atom true))
  (def x (atom nil))

  (defn task [p q]
    (when @x (release @x 2))
    (when @cont
      (reset! x (s :freq (midi->hz (first p))))
      (apply-at
       (+ (now) (first q))
       #'task (rest p) (rest q) [])))

  (defn start-task []
    (task p q))

  (defn stop-task []
    (reset! cont false))
)
(start-task)
(stop-task)

;; Page 90
///////////////////////////////////////////////////////////////
// Figure 3.5 Thanks to polymorphism we can substitute objects that understand the same message
(comment
(
p = 64; // a constant note
q = Pseq([1, 2, 0.5], inf).asStream; // ordered sequence of durations
t = Task({
        loop({
                x.release(2);
                x = Synth(\default, [freq: p.value.midicps]);
                q.value.wait;
        });
});
t.start;
)
// now change p
p = Pseq([64, 66, 68], inf).asStream; // to a Pattern: do re mi
p = { rrand(64, 76) }; // to a Function: random notes from a chromatic octave
t.stop; x.release(2);
)
