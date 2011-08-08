(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use
   [overtone event util deps]
   [overtone.sc core synth ugen buffer allocator]))

; Define a default wav player synth
(defsynth mono-player
  "Plays a single channel audio buffer."
  [buf 0 rate 1.0 start-pos 0.0 loop? 0]
  (out 0 (pan2
           (play-buf 1 buf rate
                     1 start-pos loop?
                     :free))))

(defsynth stereo-player [buf 0 rate 1.0 start-pos 0.0 loop? 0]
  (out 0
       (play-buf 2 buf rate
                 1 start-pos loop?
                 :free)))

(defonce loaded-samples* (ref {}))

(defn- load-sample*
  [path & args]
  (let [id (alloc-id :audio-buffer)
        arg-map (apply hash-map args)
        start (get arg-map :start 0)
        n-frames (get arg-map :n-frames 0)
        ready (atom :loading)
        info (atom {})
        sample (with-meta {:id id
                           :path path
                           :info info
                           :ready? ready}
                          {:type ::sample})]
    (on-done "/b_allocRead" #(do
                               (reset! ready true)
                               (reset! info (buffer-info id))))
    (snd "/b_allocRead" id path start n-frames)
    (dosync (alter loaded-samples* assoc [path args] sample))
    sample))

(defn load-sample
  "Load a wav file into a memory buffer.  Returns the buffer.

    ; load a sample a
    (load-sample \"/home/rosejn/studio/samples/kit/boom.wav\")

  Takes optional params :start and :size. Allocates buffer to number of channels
  of file and number of samples requested (:size), or fewer if sound file is
  smaller than requested. Reads sound file data from the given starting frame
  in the file (:start). If the number of frames argument is less than or equal
  to zero, the entire file is read.

  "
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

(defn sample-ready?
  "Check whether a sample has completed allocating and/or loading data."
  [sample]

  @(:ready? sample))

(defn sload-sample
  "Loads a sample synchronously. Blocks the current thread until the server
   has booted and the sample has been sucessfully loaded. See load-sample"
  [path & args]
  (wait-until-connected)
  (let [sample (apply load-sample path args)]
    (while (not (sample-ready? sample))
      (Thread/sleep 50))
    sample))

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
(def *sample-root* "/home/duke/samples/")

(defn sample-seq
  ([] (seq (.list (java.io.File. *sample-root*))))
  ([pred] (filter pred (sample-seq))))

(defn sample-load [name]
  (sample (str *sample-root* name)))
