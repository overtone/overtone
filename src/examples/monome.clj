(ns examples.monome
  (:use overtone.live)
  (:require [monome-serial.monome :as mono]))

(refer-ugens)

;(boot)

(defsynth plop [freq 440 len 0.4] 
  (* 0.4 (env-gen (perc 0.02 len) 1 1 0 1 :free) 
     (sin-osc [(+ (* 3 (sin-osc 20)) freq) (/ freq 2)])))

(def m (mono/open "/dev/ttyUSB0"))

(defn plopper [cmd x y] 
  (when (= cmd :down) 
    (plop (+ 100 (* 10 (* x x))) (* y 0.2))))

;(mono/add-handler m plopper)

(def stick         (load-sample "/home/rosejn/studio/samples/jazz-drumkit/STICK.aif"))
(def clap          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/CLAP1.aif"))
(def conga-1       (load-sample "/home/rosejn/studio/samples/jazz-drumkit/CONGA1.aif"))
(def conga-2       (load-sample "/home/rosejn/studio/samples/jazz-drumkit/CONGA2.aif"))
(def cymbal-1      (load-sample "/home/rosejn/studio/samples/jazz-drumkit/CYMBAL03.aif"))
(def cymbal-2      (load-sample "/home/rosejn/studio/samples/jazz-drumkit/CYMBAL08.aif"))
(def cy1           (load-sample "/home/rosejn/studio/samples/jazz-drumkit/Cy1Brushkit.aif"))
(def cy2           (load-sample "/home/rosejn/studio/samples/jazz-drumkit/Cy2Brushkit.aif"))
(def bass          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/bdBrushkit.aif"))
(def snare         (load-sample "/home/rosejn/studio/samples/jazz-drumkit/SD6Brushkit.aif"))
(def tamb          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/TAMB.aif"))
(def hi-hat        (load-sample "/home/rosejn/studio/samples/jazz-drumkit/HIHAT01.aif"))
(def hhos          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/HHosBrushkit.aif"))
(def hho2          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/HHo2Brushkit.aif"))
(def tthi          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/TThiBrushkit.aif"))
(def tthi2         (load-sample "/home/rosejn/studio/samples/jazz-drumkit/TThi2Brushkit.aif"))
(def ttlo          (load-sample "/home/rosejn/studio/samples/jazz-drumkit/TTloBrushkit.aif"))

(def kit [stick clap conga-1 conga-2 cymbal-1 cymbal-2 cy1 cy2 bass snare tamb hi-hat hhos hho2 tthi tthi2 ttlo])     

(defn drummer [cmd x y]
  (when (= cmd :down)
    (hit (nth kit x))))

(def loops
 [[1 0 0 0 1 0 0 0]
  [1 0 1 0 1 0 1 0]
  [1 1 0 0 1 1 0 0]
  [1 0 1 0 1 1 0 0]
  [1 0 0 1 1 0 0 0]
  [1 0 0 0 1 1 0 0]
  [1 1 0 0 1 0 0 0]
  [1 1 1 1 1 1 1 1]])

(def current-loops* (ref {}))

(def tick (beat-ms 1/4 480))

(defn make-metro [bpm]
  (let [start (now)
        tick (beat-ms 1/4 bpm)]
    (fn 
      ([] (long (/ (- (now) start) tick)))
      ([beat] (+ (* beat tick) start)))))

(defn metro-bpm [metro bpm])

(def clock (make-metro 60))

(defn loop-player [inst index beat pattern]
  (when (get @current-loops* index)
    (let [next-beat (clock (inc beat))]
      (if (= 1 (first pattern))
        (at (clock beat) (hit inst)))
      (call-at (- next-beat 100) #'loop-player inst index (inc beat) (next pattern)))))

(defn looper [cmd x y]
  (condp = cmd
    :down (do 
            (dosync (alter current-loops* assoc y true)) 
            (loop-player (nth kit x) y (inc (clock)) (cycle (nth loops y))))
    :up   (dosync (alter current-loops* assoc y false))))

(defn foo [beat] 
  (hit-at (clock beat) :plop :freq 440)
  (call-at (- (clock (inc beat)) 100) #'foo (inc beat)))

(defn bar [beat] 
  (hit-at (clock beat) :plop :freq 220)
  (call-at (- (clock (+ beat 2)) 100) #'bar (+ 2 beat)))

(mono/add-handler m looper)
