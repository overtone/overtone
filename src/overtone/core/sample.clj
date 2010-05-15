(ns 
  #^{:doc "Making it easy to load and play audio samples (wav or aif files)."
     :author "Jeff Rose"}
  overtone.core.sample
  (:use (overtone.core synth ugen sc event util)))

(refer-ugens)

; Define a default wav player synth
(on :connected #(
                 (defsynth mono-player [buf 0 rate 1.0 start-pos 0.0 loop? 0]
                   (overtone.ugens/play-buf 1 buf rate
                                            1 start-pos 0.0
                                            (if loop? 1 0) :free))
                 (defsynth stereo-player [buf 0 rate 1.0 start-pos 0.0 loop? 0]
                   (overtone.ugens/play-buf 2 buf rate
                                            1 start-pos 0.0
                                            (if loop? 1 0) :free))))
(defn load-sample
  "Load a wav file into a memory buffer.  Returns the buffer.

    ; load a sample a
    (load-sample \"/home/rosejn/studio/samples/kit/boom.wav\")

  "
  [path & args]
  {:pre [(connected?)]}
  (let [id (alloc-id :audio-buffer)
        args (apply hash-map args)
        start (get args :start 0)
        n-frames (get args :n-frames 0)
        ready (atom :loading)
        sample (with-meta {:id id
                           :size n-frames
                           :path path
                           :ready? ready}
                          {:type ::sample})]
    (on-done "/b_allocRead" #(reset! ready true))
    (snd "/b_allocRead" id path start n-frames)
    sample))

(defn sample?
  [s]
  (isa? (type s) ::sample))

;; Samples are just audio files loaded into a buffer, so buffer
;; functions work on samples too.
(derive ::sample :overtone.core.sc/buffer)

(defn sample
  "Loads a wave file into a memory buffer. Returns a function capable
   of playing that sample.

   ; e.g.
   (sample \"/Users/sam/music/samples/flibble.wav\")

  "
  [path]
  {:pre [(connected?)]}
  (let [s          (load-sample path)
        player     (fn [& args] (apply mono-player s args))]
    (callable-map (merge {:player player} s)
                  player)))


(defmethod buffer-id ::sample [sample] (:id (:buf sample)))
