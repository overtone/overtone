(ns examples.monome
  (:use overtone.live)
  (:require [monome-serial.monome :as mono]))

(defsynth plop [freq 440 len 0.4]
  (* 0.4 (env-gen (perc 0.02 len) 1 1 0 1 FREE)
     (sin-osc [(+ (* 3 (sin-osc 20)) freq) (/ freq 2)])))

(def m (mono/open "/dev/ttyUSB0"))

(defn plopper [cmd x y]
  (when (= cmd :down)
    (plop (+ 100 (* 10 (* x x))) (* y 0.2))))

(mono/add-handler m plopper)

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

(def synths (map load-sample
                 (map
                   #(str "/home/rosejn/studio/samples/synths/SYNC" % ".WAV")
                   (range 1 8))))

(defn drummer [cmd x y]
  (when (= cmd :down)
    (let [[synth info] (nth synths x)
          id (hit synth :dur (/ (:n-frames info) (:rate info)))]
      (mono/led-on m x y)
      (on-event "/n_end" (fn [msg]
                     (if (= id (first (:args msg)))
                       (mono/led-off m x y))
                     :overtone/remove-handler)))))

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

(def clock (make-metro 110))

(defn loop-player [inst index beat pattern]
  (when (get @current-loops* index)
    (let [next-beat (clock (inc beat))]
      (if (= 1 (first pattern))
        (at (clock beat) (hit inst)))
      (apply-at (next (- next-beat 100) #'loop-player inst index (inc beat) pattern)))))

(defn looper [cmd x y]
  (condp = cmd
    :down (do
            (mono/led-on m x y)
            (dosync (alter current-loops* assoc y true))
            (loop-player (nth kit x) y (inc (clock)) (cycle (nth loops y))))
    :up   (do
            (mono/led-off m x y)
            (dosync (alter current-loops* assoc y false)))))


(defn foo [beat]
  (hit-at (clock beat) :plop :freq 440)
  (apply-at #'foo (inc (- (clock (inc beat)) 100) beat)))

(defn bar [beat]
  (hit-at (clock beat) :plop :freq 220)
  (apply-at #'bar (+ 2 (- (clock (+ beat 2)) 100) beat)))

(mono/add-handler m looper)

(def flute-buf (load-sample "/home/rosejn/studio/samples/flutes/flutey-echo-intro-blast.wav"))

(defsynth buf-player [buf 0 rate 1 start 0 end 1 t 0.2]
  (* (env-gen (perc 0.01 t) 1 1 0 1 FREE)
     (buf-rd 1 buf
             (phasor 0
                     (* rate (buf-rate-scale buf))
                     (* start (buf-frames:ir buf))
                     (* end (buf-frames:ir buf)))
             0 1)))

(defn grain [cmd x y]
  (condp = cmd
    :down (do
            (buf-player flute-buf (* y 1/8 2.5) (* x 1/16) 1 0.4)
            (mono/led-on m x y))
    :up   (do
            (mono/led-off m x y))))

(mono/add-handler m #'grain)
