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
        [overtone.helpers.file :only [glob canonical-path resolve-tilde-path mk-path file-extension]])
  (:require [overtone.sc.envelope :refer [asr]]
            [overtone.sc.info :refer [server-sample-rate]]
            [overtone.libs.counters :refer [next-id]]
            [overtone.osc :refer [osc-send]]))

(declare sample-player)

(defonce ^{:private true} __RECORDS__
  (do
    (defrecord Sample [id size n-channels rate status path args name]
      to-sc-id*
      (to-sc-id [this] (:id this)))

    (defrecord-ifn PlayableSample
      [id size n-channels rate status path args name]
      sample-player
      to-sc-id*
      (to-sc-id [this] (:id this)))))

(defn sample?
  "Returns true if s is a sample"
  [s]
  (isa? (type s) ::sample))

(defmethod print-method Sample [b w]
  (.write w (format "#<buffer[%s]: %s %fs %s %d>"
                    (name @(:status b))
                    (:name b)
                    (:duration b)
                    (cond
                     (= 1 (:n-channels b)) "mono"
                     (= 2 (:n-channels b)) "stereo"
                     :else (str (:n-channels b) " channels"))
                    (:id b))))

(defmethod print-method PlayableSample [b w]
  (.write w (format "#<buffer[%s]: %s %fs %s %d>"
                    (name @(:status b))
                    (:name b)
                    (:duration b)
                    (cond
                      (= 1 (:n-channels b)) "mono"
                      (= 2 (:n-channels b)) "stereo"
                      :else (str (:n-channels b) " channels"))
                    (:id b))))

;; Define a default wav player synth
(defonce __DEFINE-PLAYERS__
  (do

    (defsynth mono-partial-player
      "Plays a mono buffer from start pos to end pos (represented as
       values between 0 and 1). May be looped via the loop?
       argument. Release time is the release phase after the looping has
       finished to remove clipping."
      [buf 0 rate 1 start 0 end 1 loop? 0 amp 1 release 0 out-bus 0]
      (let [n-frames  (buf-frames buf)
            rate      (* rate (buf-rate-scale buf))
            start-pos (* start n-frames)
            end-pos   (* end n-frames)
            phase     (phasor:ar :start start-pos :end end-pos :rate rate)
            snd       (buf-rd 1 buf phase)
            e-gate    (+ loop?
                         (a2k (latch:ar (line 1 0 0.0001) (bpz2 phase))))
            env       (env-gen (asr 0 1 release) :gate e-gate :action FREE)]
        (out out-bus (* amp env snd))))

    (defsynth stereo-partial-player
      "Plays a stereo buffer from start pos to end pos (represented as
       values between 0 and 1). May be looped via the loop?
       argument. Release time is the release phase after the looping has
       finished to remove clipping."
      [buf 0 rate 1 start 0 end 1 loop? 0 amp 1 release 0 out-bus 0]
      (let [n-frames  (buf-frames buf)
            rate      (* rate (buf-rate-scale buf))
            start-pos (* start n-frames)
            end-pos   (* end n-frames)
            phase     (phasor:ar :start start-pos :end end-pos :rate rate)
            snd       (buf-rd 2 buf phase)
            e-gate    (+ loop?
                         (a2k (latch:ar (line 1 0 0.0001) (bpz2 phase))))
            env       (env-gen (asr 0 1 release) :gate e-gate :action FREE)]
        (out out-bus (* amp env snd))))

    (defsynth mono-stream-player
      "Plays a single channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 amp 1 pan 0 out-bus 0]
      (out out-bus (* amp
                      (pan2
                       (scaled-v-disk-in 1 buf rate loop?)
                       pan))))

    (defsynth stereo-stream-player
      "Plays a dual channel streaming buffer-cue. Must be freed manually when
      done."
      [buf 0 rate 1 loop? 0 amp 1 pan 0 out-bus 0]
      (let [s (scaled-v-disk-in 2 buf rate loop?)]
        (out out-bus (* amp (balance2 (first s) (second s) pan)))))))


(defonce loaded-samples* (atom {}))
(defonce cached-samples* (atom {}))


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


(defn- assert-audio-files [file-seq]
  (run! (fn [f]
          (let [ext (file-extension f)]
            (assert (or (= ext "aif") (= ext "aiff") (= ext "wav"))
                    (if (.isDirectory f)
                      (str "The file " (.getPath f) " is a directory. "
                           "If you wish to load all the files inside the directory, "
                           "you need to provide the path with a glob pattern (asterix).\n"
                           "Example \"~/samples/*\" or \"~/samples/*.wav\".")
                      (str "The file " (.getPath f) " was not a .wav or .aiff audiofile. "
                           "If you wish to choose only audiofile from a directory, you could "
                           "provide the glob pattern like in this:\n \"~/samples/*.wav\".")))))
        file-seq))

(defn load-samples
  "Takes one or more directory and/or file paths,
   suppoerts glob or glob paths (see #'overtone.helpers.file/glob)
   and loads up all matching samples and returns a seq of maps
   representing information for each loaded sample (see
   load-sample). Samples should be in .aiff or .wav format."
  [& glob-paths]
  (let [paths (reduce (fn [paths-vector path-glob]
                        (into paths-vector
                              (let [files (glob path-glob)]
                                (assert-audio-files files)
                                (mapv #(.getAbsolutePath %)
                                      (sort files)))))
                      [] glob-paths)]
    (doall (mapv (fn [path] (load-sample path)) paths))))

(defn load-samples-async
  "Takes one or more directory and/or file paths,
   supports glob paths (see #'overtone.helpers.file/glob).
   Loads up all matching samples and returns a seq of maps
   representing information for each loaded sample (see
   load-sample). Samples should be in .aiff or .wav format.

   Returns immedietly a vector of samples,
   some of which could still be loading.
   The buffer id for the sample is available immedietly,
   the rest of the metadata, like n-frames, n-channels and rate
   are available via atom when the sample is loaded.

   This function is thought of as a faster but unsafer
   alternative to `load-samples`, which is synchronous."
  [& glob-paths]
  (let [paths           (reduce (fn [paths-vector path-glob]
                                  (into paths-vector
                                        (let [files (glob path-glob)]
                                          (assert-audio-files files)
                                          (mapv #(.getAbsolutePath %)
                                                (sort files)))))
                                [] glob-paths)
        ;; always load the entire sample, hence same args always
        cache-args            {:start 0 :size -1}
        paths-and-cache (reduce (fn [out-vec path]
                                  (conj out-vec
                                        (do (prn (contains? @cached-samples* [path cache-args]))
                                          (or (get @cached-samples* [path cache-args])
                                              path))))
                                [] paths)]
    (reduce (fn [return-samples path-or-cache]
              (if (sample? path-or-cache)
                (conj return-samples path-or-cache)
                (let [id          (next-id :audio-buffer)
                      *size       (atom nil)
                      *n-channels (atom nil)
                      *rate       (atom nil)
                      *status     (atom :loading)
                      path        path-or-cache
                      name        (.getName (file path))
                      sample      (Sample. id *size *n-channels *rate *status path cache-args name)]
                  (future
                    (recv "/done"
                          (fn [{:keys [args] :as msg}]
                            (when (and (= (first args) "/b_allocRead")
                                       (= (second args) id))
                              (snd "/b_query" id)
                              (future
                                (let [info                     (deref (recv "/b_info" (fn [msg] (= id (first (:args msg))))))
                                      [_ size rate n-channels] (:args info)
                                      server-rate              (server-sample-rate)
                                      rate-scale               (when (> server-rate 0)
                                                                 (/ rate server-rate))
                                      duration                 (when (> rate 0)
                                                                 (/ size rate))]
                                  (when (every? zero? [size rate n-channels])
                                    (throw (Exception.
                                            (str "Unable to read file - perhaps path is not a valid audio file (only "
                                                 supported-file-types " supported) : " path))))
                                  (reset! *size size)
                                  (reset! *n-channels n-channels)
                                  (reset! *status :live)
                                  (swap! cached-samples* assoc [path cache-args] sample)
                                  (swap! loaded-samples* assoc id sample))))))
                    (with-server-sync
                      #(snd "/b_allocRead" id path 0 -1)
                      (str "whilst allocating buffers for file: " path)))
                  (conj return-samples sample))))
            [] paths-and-cache)))

(defn- reload-all-samples []
  (let [previously-loaded-samples (vals @loaded-samples* )]
    (reset! cached-samples* {})
    (reset! loaded-samples* {})
    (doseq [smpl previously-loaded-samples]
      (apply load-sample* (:path smpl) (:args smpl)))))

(on-deps :server-ready ::load-all-samples reload-all-samples)

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
   depending on the number of channels in the sample. Always creates a
   stereo signal.

   Accepts same args as both players, namely:

   {:buf 0 :rate 1.0 :start-pos 0.0 :loop? 0 :amp 1 :out-bus 0}

   If you wish to specify a group target vector i.e. [:head foo-g] this
   argument must go *after* the smpl argument:

   (sample-player my-samp [:head foo-g] :rate 0.5)"
  [smpl & pargs] {:pre [(sample? smpl)]}
  (let [{:keys [path args]}     smpl
        {:keys [id n-channels]} (get @cached-samples* [path args])
        [target pos pargs]      (extract-target-pos-args pargs
                                                         (foundation-default-group)
                                                         :tail)]
    (cond
      (= n-channels 1) (apply mono-partial-player [pos target] id pargs)
      (= n-channels 2) (apply stereo-partial-player [pos target] id pargs))))



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

(defmacro defsample
  "Define a s-name as a var in the current namespace referencing a
   sample with the specified path and args.

   Equivalent to:
   (def s-name (sample path args...))"
  [s-name path & args]
  `(def ~s-name (sample ~path ~@args)))
