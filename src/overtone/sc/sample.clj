(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use [clojure.java.io :only [file]]
        [overtone.util lib]
        [overtone.libs event deps]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc server synth ugens buffer]
        [overtone.helpers.file :only [glob]]))

; Define a default wav player synth
(defonce __DEFINE-PLAYERS__
  (do
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
                     FREE)))))

(defonce loaded-samples* (ref {}))

(defn- load-sample*
  [path & args]
  (let [f (file path)]
    (when-not (.exists f)
      (throw (Exception. (str "Unable to load sample - file does not exist: " path))))
    (let [f-name (.getName f)
          id       (alloc-id :audio-buffer)
          arg-map  (apply hash-map args)
          start    (get arg-map :start 0)
          n-frames (get arg-map :n-frames 0)]
      (with-server-sync  #(snd "/b_allocRead" id path start n-frames))
      (let [info   (buffer-info id)
            sample (with-meta {:allocated-on-server (atom true)
                               :id id
                               :path path
                               :name f-name
                               :size (:n-frames info)
                               :rate (:rate info)
                               :n-channels (:n-channels info)}
                     {:type ::sample})]
        (dosync (alter loaded-samples* assoc [path args] sample))
        sample))))

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

(defn load-samples
  "Takes a directoy path or glob path (see #'overtone.helpers.file/glob) and
  loads up all matching samples and returns a seq of maps representing
  information for each loaded sample (see load-sample)"
  [path-glob]
  (let [files (glob path-glob)]
    (doall
     (map (fn [file]
            (let [path (.getAbsolutePath file)]
              (load-sample path)))
          files))))

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
