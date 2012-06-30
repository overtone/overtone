(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use [clojure.java.io :only [file]]
        [overtone.helpers lib]
        [overtone.libs event deps]
        [overtone.sc server synth ugens buffer]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc.cgens buf-io io]
        [overtone.helpers.file :only [glob canonical-path resolve-tilde-path mk-path]]))

; Define a default wav player synth
(defonce __DEFINE-PLAYERS__
  (do
    (defsynth mono-player
      "Plays a single channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]
      (out 0 (* vol
                (pan2
                 (scaled-play-buf 1 buf rate
                                  1 start-pos loop?
                                  FREE)))))

    (defsynth stereo-player
      "Plays a dual channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]
      (out 0 (* vol
                (scaled-play-buf 2 buf rate
                                 1 start-pos loop?
                                 FREE))))

    (defsynth mono-stream-player
      "Plays a single channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 vol 1]
      (out 0 (* vol
                (pan2
                 (scaled-v-disk-in 1 buf rate loop?)))))

    (defsynth stereo-stream-player
      "Plays a dual channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 vol 1]
      (out 0 (* vol
                (scaled-v-disk-in 2 buf rate loop?))))))

(defonce loaded-samples* (ref {}))

(defrecord Sample [id size n-channels rate allocated-on-server path args name])

(defn- load-sample*
  [path & args]
  (let [path (canonical-path path)
        f    (file path)]
    (when-not (.exists f)
      (throw (Exception. (str "Unable to load sample - file does not exist: " path))))
    (let [arg-map  (apply hash-map args)
          f-name   (or (:name args) (.getName f))
          start    (get arg-map :start 0)
          n-frames (get arg-map :size 0)
          buf      (buffer-alloc-read path start n-frames)]
      (let [sample (map->Sample
                    (assoc buf
                      :path path
                      :args args
                      :name f-name))]
        (dosync (alter loaded-samples* assoc [path args] sample))
        sample))))

(defn load-sample
  "Synchronously load a .wav or .aiff file into a memory buffer. Returns the
  buffer.

    ; e.g.
    (load-sample \"~/studio/samples/kit/boom.wav\")

  Takes optional params :start and :size. Allocates buffer to number of channels
  of file and number of samples requested (:size), or fewer if sound file is
  smaller than requested. Reads sound file data from the given starting frame
  in the file (:start). If the number of frames argument is less than or equal
  to zero, the entire file is read."
  [path & args]
  (let [path (canonical-path path)]
    (if-let [sample (get @loaded-samples* [path args])]
      sample
      (do
        (dosync (alter loaded-samples* assoc [path args] nil))
        (if (server-connected?)
          (apply load-sample* path args))))))

(defn load-samples
  "Takes a directoy path or glob path (see #'overtone.helpers.file/glob) and
  loads up all matching samples and returns a seq of maps representing
  information for each loaded sample (see load-sample). Samples should be in
  .aiff or .wav format."
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
  "Returns true if s is a sample"
  [s]
  (isa? (type s) ::sample))

(defn- free-loaded-sample
  [[[path args] buf]]
  (if (server-connected?)
    (do (buffer-free buf)
        (dosync (alter loaded-samples*
                       dissoc
                       [path args])))))

(defn free-all-loaded-samples
  "Free all buffers associated with a loaded sample and the memory they
  consume. Also remove each sample from @loaded-samples once freed"
  []
  (doseq [loaded-sample @loaded-samples*]
    (free-loaded-sample loaded-sample)))

(defn free-sample
  "Free the buffer associated with smpl and the memory it consumes. Uses the
  cached version from @loaded-samples* in case the server has crashed or been
  rebooted. Also remove the sample from @loaded-samples."
  [smpl]
  (assert sample? smpl)
  (let [path (:path smpl)
        args (:args smpl)
        buf  (get @loaded-samples* [path args])]
    (free-loaded-sample [[path args] buf])
    :sample-freed))

(defn sample-player
  "Play the specified sample with either a mono or stereo player
  depending on the number of channels in the sample. Accepts same args
  as both players, namely:
  [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1]"
  [smpl & pargs] {:pre [(sample? smpl)]}
  (let [{:keys [path args]}     smpl
        {:keys [id n-channels]} (get @loaded-samples* [path args])]
    (cond
      (= n-channels 1) (apply mono-player id pargs)
      (= n-channels 2) (apply stereo-player id pargs))))

(defrecord-ifn PlayableSample
  [id size n-channels rate allocated-on-server path args name]
  sample-player)

(defn sample
  "Loads a .wav or .aiff file into a memory buffer. Returns a function
   capable of playing that sample. Memoizes result and returns same
   sample on subsequent calls.

   ; e.g.
   (sample \"~/music/samples/flibble.wav\")

  "
  [path & args]
  (let [smpl (apply load-sample path args)]
    (map->PlayableSample smpl)))

;; Samples are just audio files loaded into a buffer, so buffer
;; functions work on samples too.
(derive Sample         ::sample)
(derive PlayableSample ::playable-sample)

(derive ::sample :overtone.sc.buffer/file-buffer)
(derive ::playable-sample ::sample)

(defmethod buffer-id ::sample [sample] (:id sample))

(defmacro defsample [s-name path & args]
  `(def ~s-name (sample ~path ~@args)))
