(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use
   [overtone event util deps]
   [overtone.sc.ugen.constants]
   [overtone.sc core synth ugen buffer allocator])
  (:import [java.io File]
           [javax.swing JFrame JFileChooser]))

; Define a default wav player synth
(defsynth mono-player
  "Plays a single channel audio buffer."
  [buf 0 rate 1.0 start-pos 0.0 loop? 0]
  (out 0 (pan2
           (play-buf 1 buf rate
                     1 start-pos loop?
                     FREE))))

(defsynth stereo-player [buf 0 rate 1.0 start-pos 0.0 loop? 0]
  (out 0
       (play-buf 2 buf rate
                 1 start-pos loop?
                 FREE)))

(defonce loaded-samples* (ref {}))

(defn- load-sample*
  [path & args]
  (let [id       (alloc-id :audio-buffer)
        arg-map  (apply hash-map args)
        start    (get arg-map :start 0)
        n-frames (get arg-map :n-frames 0)]
    (with-server-sync  #(snd "/b_allocRead" id path start n-frames))
    (let [info   (buffer-info id)
          sample (with-meta {:allocated-on-server (atom true)
                             :id id
                             :path path
                             :size (:n-frames info)
                             :rate (:rate info)
                             :n-channels (:n-channels info)}
                   {:type ::sample})]
      (dosync (alter loaded-samples* assoc [path args] sample))
      sample)))

(defn load-sample
  "Synchronously load a wav file into a memory buffer.  Returns the buffer.

    ; load a sample a
    (load-sample \"/home/rosejn/studio/samples/kit/boom.wav\")

  Takes optional params :start and :size. Allocates buffer to number of channels
  of file and number of samples requested (:size), or fewer if sound file is
  smaller than requested. Reads sound file data from the given starting frame
  in the file (:start). If the number of frames argument is less than or equal
  to zero, the entire file is read."
  [path & args]
  (dosync (alter loaded-samples* assoc [path args] nil))
  (if (connected?)
    (apply load-sample* path args)))

(defn- load-all-samples []
  (doseq [[[path args] buf] @loaded-samples*]
    (apply load-sample* path args)))

(on-deps :connected ::load-all-samples load-all-samples)

(defn sample?
  [s]
  (isa? (type s) ::sample))

;; Samples are just audio files loaded into a buffer, so buffer
;; functions work on samples too.
(derive ::sample :overtone.sc.buffer/buffer)

(defn sample
  "Loads a wave file into a memory buffer. Returns a function capable
   of playing that sample.

   ; e.g.
   (sample \"/Users/sam/music/samples/flibble.wav\")

  "
  [path & args]
  (let [s          (load-sample path)
        player     (fn [& pargs]
                     (let [id (:id (get @loaded-samples* [path args]))]
                       (if (empty? pargs)
                         (mono-player id)
                         (apply mono-player id pargs))))]
    (callable-map (merge {:player player} s)
                  player)))

(defmethod buffer-id ::sample [sample] (:id sample))

(defmacro defsample [s-name path]
  `(def ~s-name (sample ~path)))

;;;; simple sample manager
(def *samples-root* (atom "."))

(defn samples-seq
  ([] (seq (.list (java.io.File. @*samples-root*))))
  ([regex] (filter #(re-find (re-pattern regex) %) (samples-seq))))

(defn samples-load [name]
  (sample (str @*samples-root* name)))

(defn samples-choose []
  (sample
   (.. (doto (JFileChooser. (File. @*samples-root*))
        (.showOpenDialog (JFrame.)))
      getSelectedFile
      getCanonicalPath)))
