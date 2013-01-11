(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use [clojure.java.io :only [file]]
        [overtone.helpers lib synth]
        [overtone.libs event deps]
        [overtone.sc server synth ugens buffer foundation-groups node]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc.cgens buf-io io]
        [overtone.studio core]
        [overtone.helpers.file :only [glob canonical-path resolve-tilde-path mk-path]]))

; Define a default wav player synth
(defonce __DEFINE-PLAYERS__
  (do
    (defsynth mono-player
      "Plays a single channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1 out-bus 0 ]
      (out out-bus (* vol
                      (pan2
                       (scaled-play-buf 1 buf rate
                                        1 start-pos loop?
                                        FREE)))))

    (defsynth stereo-player
      "Plays a dual channel audio buffer."
      [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol 1 out-bus 0]
      (out out-bus (* vol
                      (scaled-play-buf 2 buf rate
                                       1 start-pos loop?
                                       FREE))))

    (defsynth mono-stream-player
      "Plays a single channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 vol 1 out-bus 0]
      (out out-bus (* vol
                      (pan2
                       (scaled-v-disk-in 1 buf rate loop?)))))

    (defsynth stereo-stream-player
      "Plays a dual channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 vol 1 out-bus 0]
      (out out-bus (* vol
                      (scaled-v-disk-in 2 buf rate loop?))))))

(defonce loaded-samples* (atom {}))
(defonce cached-samples* (atom {}))

(defrecord Sample [id size n-channels rate allocated-on-server path args name]
  to-sc-id*
  (to-sc-id [this] (:id this)))

(defn- load-sample*
  [path arg-map]
  (let [path (canonical-path path)
        f    (file path)]
    (when-not (.exists f)
      (throw (Exception. (str "Unable to load sample - file does not exist: " path))))
    (let [f-name   (or (:name arg-map) (.getName f))
          start    (get arg-map :start 0)
          n-frames (get arg-map :size 0)
          buf      (buffer-alloc-read path start n-frames)
          sample   (map->Sample
                    (assoc buf
                      :path path
                      :args arg-map
                      :name f-name))]
      (swap! cached-samples* assoc [path arg-map] sample)
      (swap! loaded-samples* assoc (:id buf) sample)
      sample)))

(defn load-sample
  "Synchronously load a .wav or .aiff file into a memory buffer. Returns
   the buffer.

    ; e.g.
    (load-sample \"~/studio/samples/kit/boom.wav\")

  Takes optional params :start and :size. Allocates buffer to number of
  channels of file and number of samples requested (:size), or fewer if
  sound file is smaller than requested. Reads sound file data from the
  given starting frame in the file (:start). If the number of frames
  argument is less than or equal to zero, the entire file is read.

  If optional param :force is set to true, any previously create cache
  of the sample will be removed and the sample will be forcibly
  reloaded."
  [path & args]
  (ensure-connected!)
  (let [args   (apply hash-map args)
        force? (:force args)
        args   (select-keys args [:start :size])
        path   (canonical-path path)]
    (if-let [sample (and (not force?)
                         (get @cached-samples* [path args]))]
      sample
      (load-sample* path args))))

(defn load-samples
  "Takes a directory path or glob path (see #'overtone.helpers.file/glob)
   and loads up all matching samples and returns a seq of maps
   representing information for each loaded sample (see
   load-sample). Samples should be in .aiff or .wav format."
  [& path-glob]
  (let [path  (apply mk-path path-glob)
        path  (resolve-tilde-path path)
        files (glob path)]
    (doseq [file files]
      (let [path (.getAbsolutePath file)]
        (load-sample path))
      files)))

(defn- reload-all-samples []
  (reset! cached-samples* {})
  (reset! loaded-samples* {})
  (doseq [smpl (vals @loaded-samples*)]
    (apply load-sample* (:path smpl) (:args smpl))))

(on-deps :server-ready ::load-all-samples reload-all-samples)

(defn sample?
  "Returns true if s is a sample"
  [s]
  (isa? (type s) ::sample))

(defn- free-loaded-sample
  [smpl]
  (let [path (:path smpl)
        args (:args smpl)]
    (if (server-connected?)
      (do (buffer-free smpl)
          (swap! cached-samples* dissoc [path args])
          (swap! loaded-samples* dissoc (:id smpl))))))

(defn free-all-loaded-samples
  "Free all buffers associated with a loaded sample and the memory they
  consume. Also remove each sample from @loaded-samples once freed"
  []
  (doseq [loaded-sample (vals @loaded-samples*)]
    (free-loaded-sample loaded-sample)))

(defn free-sample
  "Free the buffer associated with smpl and the memory it consumes. Uses
   the cached version from @loaded-samples* in case the server has
   crashed or been rebooted. Also remove the sample from
   @loaded-samples."
  [smpl]
  (assert sample? smpl)
  (free-loaded-sample smpl)
  :sample-freed)

(defn sample-player
  "Play the specified sample with either a mono or stereo player
   depending on the number of channels in the sample. Accepts same args
   as both players, namely: [buf 0 rate 1.0 start-pos 0.0 loop? 0 vol
   1]"
  [smpl & pargs] {:pre [(sample? smpl)]}
  (let [{:keys [path args]}     smpl
        {:keys [id n-channels]} (get @cached-samples* [path args])
        [target pos pargs]      (extract-target-pos-args pargs
                                                               (foundation-default-group)
                                                               :tail)]
    (cond
      (= n-channels 1) (apply mono-player :tgt target :pos pos id pargs)
      (= n-channels 2) (apply stereo-player :tgt target :pos pos id pargs))))

(defrecord-ifn PlayableSample
  [id size n-channels rate allocated-on-server path args name]
  sample-player
  to-sc-id*
  (to-sc-id [this] (:id this)))

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
