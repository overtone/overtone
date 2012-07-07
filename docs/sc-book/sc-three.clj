(ns sc-three
  (:use [overtone.live]))

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

;; Is there a \default synthesizer in Overtone?
(defsynth s [freq 440]
  (let [src (* 0.3 (sin-osc freq))]
    (out 0 src)))

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
