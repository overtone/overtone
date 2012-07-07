(ns sc-three
  (:use [overtone.live]))

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

(defn release [x releaseTime]
  (ctl x :gate (- releaseTime)))

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

;; Is there a better way?
(do
  (def cont (atom 0))
  (defn go []
    (swap! cont inc))
  (loop []
    (case @cont
      0 (recur)
      1 (do
          (s :freq (midi->hz 76))
          (Thread/sleep 1000)
          (stop)
          (s :freq (midi->hz 73))
          (go)
          (recur))
      2 (recur)
      3 (do
          (stop)
          (s :freq (midi->hz 69))
          (Thread/sleep 2000)
          (stop)))))

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
   (loop []
     (when (not (nil? @x)) (release @x 0.1))
     (when @cont
       (reset! x (s :freq (midi->hz 61) :amp 0.2))
       (Thread/sleep (/ 200 @t))
       (release @x 0.1)
       (reset! x (s :freq (midi->hz 67) :amp 0.2))
       (Thread/sleep (/ (ranged-rand 75 250) @t))
       (recur))))

  (defn go2 []
    (reset! y (s :freq (midi->hz 73) :amp 0.3)))

  (defn go3 []
    (when (not (nil? @y)) (release @y 0.1))
    (reset! y (s :freq (midi->hz 79) :amp 0.3))
    (reset! t 2))

  (defn go4 []
    (when (not (nil? @y)) (release @y 0.1))
    (reset! cont false))
)

(go1)
(go2)
(go3)
(go4)
