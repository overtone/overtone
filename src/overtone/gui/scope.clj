(ns
  ^{:doc "An oscilloscope style waveform viewer"
     :author "Jeff Rose"}
  overtone.gui.scope
  (:import
    (java.awt Graphics Dimension Color BasicStroke)
    (java.awt.geom Rectangle2D$Float Path2D$Float)
     (javax.swing JFrame JPanel)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup SGTransform SGComponent
                                 SGAbstractShape$Mode)
     (com.sun.scenario.scenegraph.event SGMouseAdapter))
  (:use
     [overtone.core event sc synth ugen util time-utils]
    clojure.stacktrace)
  (:require [overtone.core.log :as log]
            [clojure.set :as set]))

(defonce scope* (ref {:buf false
                      :buf-size 0
                      :bus 0
                      :fps 10
                      :status :off
                      :runner nil
                      :panel nil
                      :color (Color. 0 130 226)
                      :background (Color. 50 50 50)
                      :width 600
                      :height 400}))

; Some utility synths for signal routing and scoping
(defsynth bus->buf [bus 20 buf 0]
  (record-buf (in bus) buf))

(defsynth bus->bus [in-bus 20 out-bus 0]
  (out out-bus (in in-bus)))

(defsynth scoper-outer [buf 0]
  (scope-out (sin-osc 200) buf))

;		// linear
;	SynthDef("freqScope0", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
;		var signal, chain, result, phasor, numSamples, mul, add;
;		mul = 0.00285;
;		numSamples = (BufSamples.kr(fftbufnum) - 2) * 0.5; // 1023 (bufsize=2048)
;		signal = In.ar(in);
;		chain = FFT(fftbufnum, signal, hop: 0.75, wintype:1);
;		chain = PV_MagSmear(chain, 1);
;		// -1023 to 1023, 0 to 2046, 2 to 2048 (skip first 2 elements DC and Nyquist)
;		phasor = LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, numSamples, numSamples + 2);
;		phasor = phasor.round(2); // the evens are magnitude
;		ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
;	}).send(server);

(defsynth freq-scope-zero [in-bus 0 fft-buf 0 scope-buf 1
                           rate 4 phase 1 db-factor 0.02]
  (let [n-samples (* 0.5 (- (buf-samples fft-buf) 2))
        signal (in in-bus)
        freqs (fft fft-buf signal 0.75 :hann)
        chain  (pv-mag-smear fft-buf 1)
        phasor (+ (+ n-samples 2)
                  (* n-samples
                     (lf-saw (/ rate (buf-dur fft-buf)) phase)))
        phasor (round phasor 2)]
    (scope-out (* db-factor (ampdb (* 0.00285 (buf-rd 1 fft-buf phasor 1 1))))
               scope-buf)))

;	// logarithmic
;	SynthDef("freqScope1", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
;		var signal, chain, result, phasor, halfSamples, mul, add;
;		mul = 0.00285;
;		halfSamples = BufSamples.kr(fftbufnum) * 0.5;
;		signal = In.ar(in);
;		chain = FFT(fftbufnum, signal, hop: 0.75, wintype:1);
;		chain = PV_MagSmear(chain, 1);
;		phasor = halfSamples.pow(LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5)) * 2; // 2 to bufsize
;		phasor = phasor.round(2); // the evens are magnitude
;		ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
;	}).send(server);
;
;(def scope-buf (buffer 2048))

;(defsynth mellow [out-bus 20 freq 440 len 20]
;  (out out-bus
;  (* (x-line:kr 0.8 0.01 len :free) 0.1
;    (+ (sin-osc (/ freq 2))
;       (rlpf (saw (+ freq (* (sin-osc 6) 6))) 440 0.2)))))
;

(defonce x-array (int-array (:width @scope*)))
(defonce _x-init (dotimes [i (:width @scope*)]
                   (aset x-array i i)))

(defonce y-array (int-array (:width @scope*)))
(defonce _y-init (dotimes [i (:width @scope*)]
                   (aset y-array i (/ (:height @scope*) 2))))

(def X-PADDING 5)
(def Y-PADDING 10)

(defn- update-scope []
  (let [{:keys [buf buf-size width height panel]} @scope*
        frames (buffer-data (:buf @scope*))
        step (int (/ buf-size width))
        y-scale (/ (- height (* 2 Y-PADDING)) 2)
        y-shift (+ (/ height 2) Y-PADDING)]
    (dotimes [x width]
      (aset ^ints y-array x
            (int (+ y-shift
                    (* y-scale
                       (aget ^floats frames (unchecked-multiply x step))))))))
  (.repaint (:panel @scope*)))

(defn- paint-scope [g]
  (let [{:keys [background width height color]} @scope*]
    (.setColor ^Graphics g ^Color background)
    (.fillRect ^Graphics g 0 0 width height)
    (.setColor ^Graphics g ^Color (Color. 100 100 100))
    (.drawRect ^Graphics g 0 0 width height)

    (.setColor ^Graphics g ^Color color)
    (.drawPolyline ^Graphics g ^ints x-array ^ints y-array width)))

(defn- clean-scope []
  (dosync
    (if (:tmp-buf @scope*)
      (buffer-free (:buf @scope*)))
    (if-let [s (:bus-synth @scope*)]
      (kill s))
    (alter scope* assoc :buf nil :buf-size 0 :tmp-buf false
           :bus nil :bus-synth nil))
  (dotimes [i (:width @scope*)]
    (aset y-array i (/ (:height @scope*) 2)))
  (.repaint (:panel @scope*)))

(defn scope-buf
  "Set a buffer to view in the scope."
  [buf]
  (clean-scope)
  (dosync (alter scope* assoc
                 :buf buf
                 :buf-size (count (buffer-data buf))))
  (update-scope))

(defn- wait-for-buffer [b]
  (loop [i 0]
    (cond
      (= 20 i) nil
      (not (ready? b)) (do
                                (java.lang.Thread/sleep 50)
                                (recur (inc i))))))

(def scope-bus-buf* (ref nil))

(defn scope-bus
  "Set a bus to view in the scope."
  [bus]
  (clean-scope)
  (let [buf (or @scope-bus-buf* (buffer 2048))
        _ (println "buf: " buf)
        _ (wait-for-buffer buf)
        bus-synth (bus->buf :target 0 :position :tail bus buf)]
    (println "bus-synth: " bus-synth)
    (if (not @scope-bus-buf*)
      (dosync (ref-set scope-bus-buf* buf)))
    (println "scope-bus-buf: " @scope-bus-buf*)
    (dosync
      (alter scope* assoc
                   :buf buf
                   :buf-size 2048
                   :bus bus
                   :tmp-buf true
                   :bus-synth bus-synth))
    (apply-at update-scope (+ (now) 1000))
    (update-scope)))

(defn freq-scope-buf [buf]
  )

(defn scope-panel []
  (let [p (proxy [JPanel] [true]
            (paint [g] (paint-scope g)))]
    (dosync (alter scope* assoc :panel p))
    (doto p
      ;(.setIgnoreRepaint true)
      (.setPreferredSize (Dimension. 600 400)))
    p))

(dotimes [i (:width @scope*)] (aset x-array i i))

(defn scope-frame []
  (let [f (JFrame. "scope")]
    (doto f
      (.setPreferredSize (Dimension. 600 400))
      (.add (scope-panel))
      (.pack)
      (.show))))

(defn scope-on []
  (dosync (alter scope* assoc
                 :status :on
                 :runner (periodic update-scope (/ 1000 (:fps @scope*))))))

(defn scope-off []
  (.cancel (:runner @scope*) true)
  (dosync (alter scope* assoc
                 :status :off
                 :runner nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
(require 'examples.basic)

(defonce test-frame (JFrame. "scope"))
(defonce test-panel (JSGPanel.))
(defonce _test-scope (do
             (.setPreferredSize test-panel (Dimension. 600 400))
             (.add (.getContentPane test-frame) test-panel)
             (.setScene test-panel (scope))
             (.pack test-frame)
             (.show test-frame)))

(defn- go-go-scope []
  (let [b (buffer 2048)]
    (Thread/sleep 100)
    (scope-buf b)
    (scope-on)
    (examples.basic/sizzle :bus 20)
    (examples.basic/bus->buf 20 (:id b))
    (examples.basic/bus->bus 20 0)))

(defn- spectrogram [in-bus]
  (let [fft-buf (buffer 2048)
        buf (buffer 2048)]
    (Thread/sleep 100)
    (freq-scope-zero in-bus fft-buf buf)))

(defn test-scope []
  (if (not (connected?))
    (do
      (boot)
      (on :examples-ready go-go-scope))
    (go-go-scope))
  (.show test-frame))
)
