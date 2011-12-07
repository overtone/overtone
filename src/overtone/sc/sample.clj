(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use [clojure.java.io :only [file]]
        [overtone.util lib]
        [overtone.libs event deps]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc server synth gens buffer]
        [overtone.helpers.file :only [glob resolve-tilde-path mk-path]]))

; Define a default wav player synth
(defonce __DEFINE-PLAYERS__
  (do
    (defsynth mono-player
      "Plays a single channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]
      (let [rate (* rate (buf-rate-scale:kr buf))]
        (out 0 (* vol (pan2
                       (play-buf 1 buf rate
                                 1 start-pos loop?
                                 FREE))))))

    (defsynth stereo-player
      "Plays a dual channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]
      (let [rate (* rate (buf-rate-scale:kr buf))]
        (out 0
             (* vol (play-buf 2 buf rate
                              1 start-pos loop?
                              FREE)))))))

(defonce loaded-samples* (ref {}))

(defn- load-sample*
  [path & args]
  (let [path (resolve-tilde-path path)
        f    (file path)
        path (.getCanonicalPath f)]
    (when-not (.exists f)
      (throw (Exception. (str "Unable to load sample - file does not exist: " path))))
    (let [f-name (.getName f)
          id       (alloc-id :audio-buffer)
          arg-map  (apply hash-map args)
          start    (get arg-map :start 0)
          n-frames (get arg-map :size 0)]
      (with-server-sync  #(snd "/b_allocRead" id path start n-frames))
      (let [info   (buffer-info id)
            _      (when (and (= 0 (:size info))
                              (= 0.0 (:rate info))
                              (= 0 (:n-channels info)))
                     (free-id :audio-buffer id)
                     (throw (Exception. (str "Unable to load sample - file does not appear to be a valid audio file: " path))))
            sample (with-meta {:allocated-on-server (atom true)
                               :id id
                               :path path
                               :args args
                               :name f-name
                               :size (:size info)
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
  (let [path (resolve-tilde-path path)]
    (dosync (alter loaded-samples* assoc [path args] nil))
    (if (server-connected?)
      (apply load-sample* path args))))

(defn load-samples
  "Takes a directoy path or glob path (see #'overtone.helpers.file/glob) and
  loads up all matching samples and returns a seq of maps representing
  information for each loaded sample (see load-sample)"
  [& path-glob]
  (let [path  (apply mk-path path-glob)
        path  (resolve-tilde-path path)
        files (glob path)]
    (doall
     (map (fn [file]
            (let [path (.getAbsolutePath file)]
              (load-sample path)))
          files))))

(defn- load-all-samples []
  (doseq [[[path args] buf] @loaded-samples*]
    (apply load-sample* path args)))

(on-deps :server-ready ::load-all-samples load-all-samples)

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
  (let [{:keys [path args] :as s} (apply load-sample path args)
        player (fn [& pargs]
                 (let [id (:id (get @loaded-samples* [path args]))]
                   (apply mono-player id pargs)))]
    (callable-map (merge {:player player} s)
                  player)))

(defmethod buffer-id ::sample [sample] (:id sample))

(defmacro defsample [s-name path & args]
  `(def ~s-name (sample ~path ~@args)))
