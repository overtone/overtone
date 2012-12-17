(ns
    ^{:doc "An oscilloscope style waveform viewer"
      :author "Jeff Rose & Sam Aaron"}
  overtone.gui.scope
  (:import [java.awt Graphics Dimension Color BasicStroke BorderLayout RenderingHints]
           [java.awt.event WindowListener ComponentListener]
           [java.awt.geom Rectangle2D$Float Path2D$Float]
           [javax.swing JFrame JPanel JSlider]
           [java.util.concurrent TimeoutException])
  (:use [clojure.stacktrace]
        [overtone.helpers lib]
        [overtone.libs event deps]
        [overtone.sc defaults server synth ugens buffer node foundation-groups]
        [overtone.sc.cgens buf-io]
        [overtone.studio core util])
  (:require [clojure.set :as set]
            [overtone.config.log :as log]
            [overtone.at-at :as at-at]))

(defonce scope-group*     (ref 0))
(defonce scopes*          (ref {}))
(defonce scope-pool       (at-at/mk-pool))
(defonce scopes-running?* (ref false))

(defonce SCOPE-BUF-SIZE 2048) ; size must be a power of 2 for FFT
(defonce FPS            10)
(defonce WIDTH          600)
(defonce HEIGHT         400)
(defonce X-PADDING      5)
(defonce Y-PADDING      10)

(on-deps :studio-setup-completed ::create-scope-group
  #(dosync
     (ref-set scope-group*
              (group "Scope" :tail (foundation-monitor-group)))
     (satisfy-deps :scope-group-created)))

(defn- ensure-internal-server!
  "Throws an exception if the server isn't internal - scope relies on
  fast access to shared buffers with the server which is currently only
  available with the internal server. Also ensures server is connected."
  []
  (when (server-disconnected?)
    (throw (Exception. "Cannot use scopes until a server has been booted or connected")))
  (when (external-server?)
    (throw (Exception. (str "Sorry, it's only possible to use scopes with an internal server. Your server connection info is as follows: " (connection-info))))))

(defn- update-scope-data
  "Updates the scope by reading the current status of the buffer and repainting.
  Currently only updates bus scope as there's a bug in scsynth-jna which
  crashes the server after too many calls to buffer-data for a large
  buffer. As buffers tend to be large, updating the scope frequently
  will cause the crash to happen sooner. Need to remove this limitation
  when scsynth-jna is fixed."
  [s]
  (let [{:keys [buf size width height panel y-arrays x-array panel]} s
        frames    (if @(:update? s) (buffer-data buf) @(:frames s))
        step      (/ (buffer-size buf) width)
        y-scale   (- height (* 2 Y-PADDING))
        [y-a y-b] @y-arrays]

    (when-not (empty? frames)
      (dotimes [x width]
        (aset ^ints y-b x
              (int (* y-scale
                      (aget ^floats frames (unchecked-multiply x step))))))
      (reset! y-arrays [y-b y-a])
      (.repaint panel))

    (when (and (not (:bus-synth s))
               @(:update? s))
      (reset! (:frames s) frames)
      (reset! (:update? s) false))))

(defn- update-scopes []
  (dorun (map update-scope-data (vals @scopes*))))

(defn- paint-scope [^Graphics g id]
  (if-let [scope (get @scopes* id)]
    (let [{:keys [background width height color x-array y-arrays slider]} scope
          s-val (.getValue slider)
          y-zoom (if (> s-val 49)
                   (+ 1 (* 0.1 (- s-val 50)))
                   (+ (* 0.02 s-val) 0.01))
          y-shift (+ (/ height 2.0) Y-PADDING)
          [y-a y-b] @y-arrays]
      (doto g
        (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
        (.setColor ^Color background)
        (.fillRect 0 0 width height)
        (.setColor ^Color (Color. 100 100 100))
        (.drawRect 0 0 width height)
        (.setColor ^Color color)
        (.translate 0 y-shift)
        (.scale 1 (* -1 y-zoom))
        (.drawPolyline ^ints x-array ^ints y-a width)))))

(defn- scope-panel [id width height]
  (let [panel (proxy [JPanel] [true]
                (paint [g] (paint-scope g id)))
        _ (.setPreferredSize panel (Dimension. width height))]
    panel))

(defn- scope-frame
  "Display scope window. If you specify keep-on-top to be true, the
  window will stay on top of the other windows in your environment."
  ([panel slider title keep-on-top width height]
     (let [f (JFrame. title)
           cp (.getContentPane f)
           side (JPanel. (BorderLayout.))]
       (.add side slider BorderLayout/CENTER)
       (doto cp
         (.add side BorderLayout/WEST)
         (.add panel BorderLayout/CENTER))
       (doto f
         (.setPreferredSize (Dimension. width height))
         (.pack)
         (.show)
         (.setAlwaysOnTop keep-on-top)))))

(defn scopes-start
  "Schedule the scope to be updated every (/ 1000 FPS) ms (unless the
  scopes are already running in which case it does nothing."
  []
  (ensure-internal-server!)
  (dosync
   (when-not @scopes-running?*
     (at-at/every (/ 1000 FPS) update-scopes scope-pool :desc "Scope refresh fn")
     (ref-set scopes-running?* true))))

(defn- reset-data-arrays
  [scope]
  (let [width     (scope :width)
        x-array   (scope :x-array)
        height    (scope :height)
        [y-a y-b] @(scope :y-arrays)]

    (dotimes [i width]
      (aset x-array i i))

    (dotimes [i width]
      (aset y-a i (/ height 2))
      (aset y-b i (/ height 2)))))

(defn- empty-scope-data
  []
  (dorun (map reset-data-arrays (vals @scopes*))))

(defn scopes-stop
  "Stop all scopes from running."
  []
  (ensure-internal-server!)
  (at-at/stop-and-reset-pool! scope-pool)
  (empty-scope-data)
  (dosync (ref-set scopes-running?* false)))

(defn- start-bus-synth
  [bus buf]
  (bus->buf :target @scope-group* bus buf))

(defn- scope-bus
  "Set a bus to view in the scope."
  [s]
  (let [buf (buffer SCOPE-BUF-SIZE)
        bus-synth (start-bus-synth (:num s) buf)]
    (assoc s
      :size SCOPE-BUF-SIZE
      :bus-synth bus-synth
      :buf buf)))


(defsynth bus-freqs->buf
  [in-bus 0 scope-buf 1 fft-buf-size 2048 rate 1 db-factor 0.02]
  (let [phase     (- 1 (* rate (reciprocal fft-buf-size)))
        fft-buf   (local-buf fft-buf-size 1)
        n-samples (* 0.5 (- (buf-samples:ir fft-buf) 2))
        signal    (in in-bus 1)
        freqs     (fft fft-buf signal 0.75 HANN)
        smoothed  (pv-mag-smear fft-buf 1)
        indexer   (+ n-samples 2
                     (* (lf-saw (/ rate (buf-dur:ir fft-buf)) phase)
                        n-samples))
        indexer   (round indexer 2)
        src       (buf-rd 1 fft-buf indexer 1 1)
        freq-vals (+ 1 (* db-factor (ampdb (* src 0.00285))))]
    (record-buf freq-vals scope-buf)))


(defn- start-bus-freq-synth
  [bus buf]
  (bus-freqs->buf :target @scope-group* bus buf))

(defn- scope-bus-freq
  [s]
  (let [buf (buffer SCOPE-BUF-SIZE)
        bus-synth (start-bus-freq-synth (:num s) buf)]
    (assoc s
      :size SCOPE-BUF-SIZE
      :bus-synth bus-synth
      :buf buf)))

(defn- scope-buf
  "Set a buffer to view in the scope."
  [s]
  (let [info (buffer-info (:num s))]
    (assoc s
      :size (:size info)
      :buf  info)))

(defn scope-close
  "Close a given scope. Copes with the case where the server has crashed
  by handling timeout errors when killing the scope's bus-synth."
  [s]
  (log/info (str "Closing scope: \n" s))
  (let [{:keys [id bus-synth buf]} s]
    (when (and bus-synth
               (server-connected?))
      (try
        (kill bus-synth)
        (catch Exception e)))
    (dosync (alter scopes* dissoc id))))

(defn- mk-scope
  [num kind keep-on-top width height]
  (let [id    (uuid)
        name  (str kind ": " num)
        panel (scope-panel id width height)
        slider (JSlider. JSlider/VERTICAL 0 99 50)
        frame (scope-frame panel slider name keep-on-top width height)
        x-array (int-array width)
        y-a     (int-array width)
        y-b     (int-array width)
        scope {:id id
               :name name
               :size 0
               :num num
               :panel panel
               :slider slider
               :kind kind
               :color (Color. 0 130 226)
               :background (Color. 50 50 50)
               :frame frame
               :width width
               :height height
               :x-array x-array
               :y-arrays (atom [y-a y-b])
               :update? (atom true)
               :frames (atom [])}

        _ (reset-data-arrays scope)]
    (.addWindowListener frame
      (reify WindowListener
        (windowActivated [this e])
        (windowClosing [this e]
                       (scope-close (get @scopes* id)))
        (windowDeactivated [this e])
        (windowDeiconified [this e])
        (windowIconified [this e])
        (windowOpened [this e])
        (windowClosed [this e])))
    (comment .addComponentListener frame
      (reify ComponentListener
        (componentHidden [this e])
        (componentMoved  [this e])
        (componentResized [this e]
          (let [w (.getWidth frame)
                h (.getHeight frame)
                xs (int-array w)
                ya (int-array w)
                yb (int-array w)]
            (dosync
              (let [s (get (ensure scopes*) id)
                    s (assoc s
                             :width w
                             :height h
                             :x-array xs
                             :y-arrays (atom [ya yb]))]
                (alter scopes* assoc id s)))))
        (componentShown [this e])))

    (case kind
          :bus (scope-bus scope)
          :bus-freq (scope-bus-freq scope)
          :buf (scope-buf scope))))

(defn scope
  "Create a scope for either a bus or a buffer. Defaults to scoping bus 0.
   Example use:
   (scope :bus 1)
   (scope :buf 10)"
  ([&{:keys [bus buf keep-on-top]
      :or {bus 0
           buf -1
           keep-on-top false}}]
     (ensure-internal-server!)
     (let [buf (if (buffer? buf) (:id buf) buf)
           kind (if (= -1 buf) :bus :buf)
           num  (if (= -1 buf) bus buf)
           s (mk-scope num kind keep-on-top WIDTH HEIGHT)]
       (dosync (alter scopes* assoc (:id s) s))
       (scopes-start))))

(defn spectrogram
  "Create frequency scope for a bus.  Defaults to bus 0.
   Example use:
   (spectrogram :bus 1)"
  ([&{:keys [bus keep-on-top]
      :or {bus 0
           keep-on-top false}}]
     (ensure-internal-server!)
     (let [s (mk-scope bus :bus-freq keep-on-top WIDTH HEIGHT)]
       (dosync (alter scopes* assoc (:id s) s))
       (scopes-start))))

(defn pscope
  "Creates a 'permanent' scope, where the window is always kept
  on top of other OS windows. See scope."
  ([& args]
     (ensure-internal-server!)
     (apply scope (concat args [:keep-on-top true]))))


(defn- reset-scopes
  "Restart scopes if they have already been running"
  []
  (ensure-internal-server!)
  (dosync
   (ref-set scopes*
            (reduce (fn [new-scopes [k v]]
                      (let [new-scope (if (= :bus (:kind v))
                                        (scope-bus v)
                                        v)]
                        (assoc new-scopes k new-scope)))
                    {}
                    @scopes*))
   (scopes-start)))


(on-deps #{:synthdefs-loaded :scope-group-created} ::reset-scopes #(when (internal-server?)
                                                                     (reset-scopes)))
(on-sync-event :shutdown (fn [event-info]
                           (when (internal-server?)
                             (scopes-stop)
                             (dorun
                              (map (fn [s] scope-close s) @scopes*))))
               ::stop-scopes)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Spectragraph stuff to be worked on
;; Note: The fft ugen writes into a buffer:
;; dc, nyquist, real, imaginary, real, imaginary....
;  (comment defn- update-scope []
;           (let [{:keys [buf width height panel]} @scope*
;                 frames  (buffer-data buf)
;                 n-reals (/ (- (:size buf) 2) 2)
;                 step    (int (/ n-reals width))
;                 y-scale (/ (- height (* 2 Y-PADDING)) 2)
;                 y-shift (+ (/ height 2) Y-PADDING)]
;             (dotimes [x width]
;               (aset ^ints y-array x
;                     (int (+ y-shift
;                             (* y-scale
;                                (aget ^floats frames
;                                      (+ 2 (* 2 (unchecked-multiply x step))))))))))
;           (.repaint (:panel @scope*)))
;
;  (defsynth freq-scope-zero [in-bus 0 fft-buf 0 scope-buf 1
;                             rate 4 phase 1 db-factor 0.02]
;    (let [n-samples (* 0.5 (- (buf-samples:kr fft-buf) 2))
;          signal (in in-bus)
;          freqs  (fft fft-buf signal 0.75 :hann)
;                                        ;        chain  (pv-mag-smear fft-buf 1)
;          phasor (+ (+ n-samples 2)
;                    (* n-samples
;                       (lf-saw (/ rate (buf-dur:kr fft-buf)) phase)))
;          phasor (round phasor 2)]
;      (scope-out (* db-factor (ampdb (* 0.00285 (buf-rd 1 fft-buf phasor 1 1))))
;                 scope-buf)))
;
;        SynthDef("freqScope0_shm", { arg in=0, fftBufSize = 2048, scopebufnum=1, rate=4, dbFactor = 0.02;
;            var phase = 1 - (rate * fftBufSize.reciprocal);
;            var signal, chain, result, phasor, numSamples, mul, add;
;            var fftbufnum = LocalBuf(fftBufSize, 1);
;            mul = 0.00285;
;            numSamples = (BufSamples.ir(fftbufnum) - 2) * 0.5; // 1023 (bufsize=2048)
;            signal = In.ar(in);
;            chain = FFT(fftbufnum, signal, hop: 0.75, wintype:1);
;            chain = PV_MagSmear(chain, 1);
;            // -1023 to 1023, 0 to 2046, 2 to 2048 (skip first 2 elements DC and Nyquist)
;            phasor = LFSaw.ar(rate/BufDur.ir(fftbufnum), phase, numSamples, numSamples + 2);
;            phasor = phasor.round(2); // the evens are magnitude
;            ScopeOut2.ar( ((BufRd.ar(1, fftbufnum, phasor, 1, 1) * mul).ampdb * dbFactor) + 1, scopebufnum, fftBufSize/rate);
;        }
;
