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
        [overtone.studio core util])
  (:require [clojure.set :as set]
            [overtone.config.log :as log]
            [overtone.at-at :as at-at]))

(defonce SCOPE-BUF-SIZE 4096)
(defonce FPS 10)
(defonce scopes* (ref {}))
(defonce scope-pool (at-at/mk-pool))
(defonce scopes-running?* (ref false))
(defonce WIDTH 600)
(defonce HEIGHT 400)
(defonce X-PADDING 5)
(defonce Y-PADDING 10)
(defonce scope-group* (ref 0))

(on-deps :studio-setup-completed
         ::create-scope-group #(dosync
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
    (throw (Exception. (str "Sorry, it's  only possible to use scopes with an internal server. Your server connection info is as follows: " (connection-info))))))

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

(defn pscope
  "Creates a 'perminent' scope, i.e. one where the window is always kept
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (require 'examples.basic)

  (defonce test-frame (JFrame. "scope"))
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
      (bus->buf 20 (:id b))
      (bus->bus 20 0)))

  (defn- spectrogram [in-bus]
    (let [fft-buf (buffer 2048)
          buf (buffer 2048)]
      (Thread/sleep 100)
      (freq-scope-zero in-bus fft-buf buf)))

  (defn test-scope []
    (if (not (server-connected?))
      (do
        (boot-server)
        (on :examples-ready go-go-scope))
      (go-go-scope))
    (.show test-frame))
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Spectragraph stuff to be worked on
(comment
                                        ; Note: The fft ugen writes into a buffer:
                                        ; dc, nyquist, real, imaginary, real, imaginary....
  (comment defn- update-scope []
           (let [{:keys [buf width height panel]} @scope*
                 frames  (buffer-data buf)
                 n-reals (/ (- (:size buf) 2) 2)
                 step    (int (/ n-reals width))
                 y-scale (/ (- height (* 2 Y-PADDING)) 2)
                 y-shift (+ (/ height 2) Y-PADDING)]
             (dotimes [x width]
               (aset ^ints y-array x
                     (int (+ y-shift
                             (* y-scale
                                (aget ^floats frames
                                      (+ 2 (* 2 (unchecked-multiply x step))))))))))
           (.repaint (:panel @scope*)))

  (defsynth freq-scope-zero [in-bus 0 fft-buf 0 scope-buf 1
                             rate 4 phase 1 db-factor 0.02]
    (let [n-samples (* 0.5 (- (buf-samples:kr fft-buf) 2))
          signal (in in-bus)
          freqs  (fft fft-buf signal 0.75 :hann)
                                        ;        chain  (pv-mag-smear fft-buf 1)
          phasor (+ (+ n-samples 2)
                    (* n-samples
                       (lf-saw (/ rate (buf-dur:kr fft-buf)) phase)))
          phasor (round phasor 2)]
      (scope-out (* db-factor (ampdb (* 0.00285 (buf-rd 1 fft-buf phasor 1 1))))
                 scope-buf)))


  (defsynth freqs [in-bus 10 fft-buf 0]
    (let [n-samples (* 0.5 (- (buf-samples:kr fft-buf) 2))
          signal    (in in-bus 1)]
      (fft fft-buf signal 0.75 :hann)))



  (defsynth scoper-outer [buf 0]
    (scope-out (sin-osc 200) buf))
  (scope-out)

  (defn freq-scope-buf [buf]
    )

  )
