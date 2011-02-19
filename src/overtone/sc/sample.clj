(ns
  ^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.sc.sample
  (:use [overtone.sc core synth ugen buffer allocator]
        [overtone event util]))

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
        sample (with-meta {:id id
                           :size n-frames
                           :path path
                           :ready? ready}
                          {:type ::sample})]
    (on-done "/b_allocRead" #(reset! ready true))
    (snd "/b_allocRead" id path start n-frames)
    (dosync (alter loaded-samples* assoc [path args] sample))
    sample))

(defn load-sample
  "Load a wav file into a memory buffer.  Returns the buffer.

    ; load a sample a
    (load-sample \"/home/rosejn/studio/samples/kit/boom.wav\")

  "
  [path & args]
  (dosync (alter loaded-samples* assoc [path args] nil))
  (if (connected?)
    (apply load-sample* path args)))

(defn- load-all-samples []
  (doseq [[[path args] buf] @loaded-samples*]
    (apply load-sample* path args)))

(on-sync-event :connected :sample-loader load-all-samples)

(defn sample?
  [s]
  (isa? (type s) ::sample))

;; Samples are just audio files loaded into a buffer, so buffer
;; functions work on samples too.
(derive ::sample :overtone.sc.core/buffer)

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
