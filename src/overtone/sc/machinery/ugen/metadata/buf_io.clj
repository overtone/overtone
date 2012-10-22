(ns overtone.sc.machinery.ugen.metadata.buf-io
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
     [
      {:name "PlayBuf",
       :args [{:name "num-channels"
               :mode :num-outs
               :doc "The number of channels that the buffer will
                     be. This must be a fixed integer. The architechture
                     of the SynthDef cannot change after it is
                     compiled. Warning: if you supply a bufnum of a
                     buffer that has a different numChannels then you
                     have specified to the play-buf, it will fail
                     silently." }

              {:name "bufnum"
               :default 0
               :doc "The index of the buffer to use." }

              {:name "rate"
               :default 1.0
               :doc "1.0 is the server's sample rate, 2.0 is one octave
                     up, 0.5 is one octave down -1.0 is backwards normal
                     rate ... etc. Interpolation is cubic. Note: if the
                     buffer's sample rate is different from the
                     server's, you will need to multiply the desired
                     playback rate by (file's rate / server's rate). The
                     UGen (buf-rate-scale bufnum) returns this factor."}

              {:name "trigger"
               :default 1.0
               :doc "A trigger causes a jump to the startPos. A trigger
                     occurs when a signal changes from <= 0 to > 0." }

              {:name "start-pos"
               :default 0.0
               :doc "Sample frame to start playback." }

              {:name "loop"
               :default 0.0
               :doc "1 means true, 0 means false. This is modulateable."}

              {:name "action"
               :default 0}]
       :doc "Plays back a sample resident in a buffer"}


      {:name "TGrains",
       :args [{:name "num-channels"
               :mode :num-outs
               :default 2
               :doc "number of output channels"}

              {:name "trigger"
               :default 0
               :doc "at each trigger, the following arguments are
                     sampled and used as the arguments of a new grain. A
                     trigger occurs when a signal changes from <= 0 to >
                     0. If the trigger is audio rate then the grains
                     will start with sample accuracy."}

              {:name "bufnum"
               :default 0
               :doc "the index of the buffer to use. It must be a one
                     channel (mono) buffer." }

              {:name "rate"
               :default 1
               :doc "1.0 is normal, 2.0 is one octave up, 0.5 is one
                     octave down -1.0 is backwards normal rate. Unlike
                     PlayBuf, the rate is multiplied by BufRate, so you
                     needn't do that yourself." }

              {:name "center-pos"
               :default 0
               :doc "the position in the buffer in seconds at which the
                     grain envelope will reach maximum amplitude."}

              {:name "dur"
               :default 0.1
               :doc "duration of the grain in seconds"}

              {:name "pan" :default 0.0
               :doc "a value from -1 to 1. Determines where to pan the
                     output in the same manner as PanAz."}

              {:name "amp"
               :default 0.1
               :doc "amplitude of the grain." }

              {:name "interp"
               :default 4
               :doc "1,2,or 4. Determines whether the grain uses (1) no
                     interpolation, (2) linear interpolation, or (4)
                     cubic interpolation." }],
       :rates #{:ar}
       :check (num-outs-greater-than 1)
       :doc "sample playback from a buffer with fine control for doing
             granular synthesis. Triggers generate grains from a single
             channel (mono) buffer. Each grain has a Hann
             envelope (sin^2(x) for x from 0 to pi) and is panned
             between two channels of multiple outputs." }



      {:name "BufRd",
       :summary "Read the contents of a buffer at a specified index"
       :args [{:name "num-channels"
               :default 1
               :mode :num-outs
               :doc "The number of channels of the supplied buffer. This
                     must be a fixed integer. The architecture of the
                     SynthDef cannot change after it is
                     compiled. (Warning: if you supply a bufnum of a
                     buffer that has a different numChannels than you
                     have specified to the BufRd, it will fail
                     silently)." }

              {:name "bufnum"
               :default 0
               :doc "The index of the buffer to use"}

              {:name "phase"
               :default 0.0
               :doc "Audio rate modulatable index into the
                     buffer. Warning: The phase argument only offers
                     precision for addressing 2**24 samples (about 6.3
                     minutes at 44100Hz)"}

              {:name "loop"
               :default 1.0
               :doc "1 means true, 0 means false.  This is modulatable."}

              {:name "interpolation"
               :default 2
               :doc "1 means no interpolation, 2 is linear, 4 is cubic
                     interpolation"}]
       ;; :check (when-ar (nth-input-ar 2))
       :doc "reads the contents of a buffer at a given index."}

      {:name "BufWr",
       :args [{:name "input-array",
               :mode :append-sequence
               :doc "input ugens (channelArray)"}

              {:name "bufnum"
               :default 0
               :doc "the index of the buffer to use"}

              {:name "phase"
               :default 0.0
               :doc "modulatable index into the buffer (has to be audio
                     rate)." }

              {:name "loop"
               :default 1.0
               :doc "1 means true, 0 means false.  This is modulatable"}]
;;       :check (when-ar (nth-input-ar 1))
       :doc "writes to a buffer at a given index. Note, buf-wr (in
             difference to buf-rd) does not do multichannel expansion,
             because input is an array." }

      {:name "RecordBuf",
       :args [{:name "input-array",
               :mode :append-sequence
               :doc "an Array of input channels"}

              {:name "bufnum",
               :default 0
               :doc "the index of the buffer to use"}

              {:name "offset"
               :default 0.0
               :doc "an offset into the buffer in frames,"}

              {:name "rec-level",
               :default 1.0
               :doc "value to multiply by input before mixing with
                     existing data." }

              {:name "pre-level",
               :default 0.0
               :doc "value to multiply to existing data in buffer before
                     mixing with input"}

              {:name "run"
               :default 1.0
               :doc "If zero, then recording stops, otherwise recording
                     proceeds." }

              {:name "loop"
               :default 1.0
               :doc "If zero then don't loop, otherwise do.  This is
                     modulate-able. "}

              {:name "trigger"
               :default 1.0
               :doc "a trigger causes a jump to the offset position in
                     the Buffer. A trigger occurs when a signal changes
                     from <= 0 to > 0." }

              {:name "action", :default 0
               :doc "an integer representing an action to be aexecuted
                     when the buffer is finished playing. This can be
                     used to free the enclosing synth. Action is only
                     evaluated if loop is 0"}]
       :doc "record a stream of values into a buffer. If recLevel is 1.0
             and preLevel is 0.0 then the new input overwrites the old
             data. If they are both 1.0 then the new data is added to
             the existing data. (Any other settings are also valid.)
             Note that the number of channels must be fixed for the
             SynthDef, it cannot vary depending on which buffer you
             use." }

      {:name "ScopeOut",
       :args [{:name "input-array", :mode :append-sequence}
              {:name "bufnum", :default 0.0}],
       :rates #{:ar}
       :num-outs 0}

      {:name "LocalBuf"
       :args [{:name "num-channels" :default 1}
              {:name "num-frames"}]
       :check (nth-input-number? 1)
       :internal-name true
       :rates #{:ir}}

      {:name "MaxLocalBufs"
       :args [{:name "num-local-bufs"}]
       :check [(nth-input-number? 0)]
       :rates #{:ir}}

      ;; SetBuf : UGen {
      ;;        *new { arg buf, values, offset = 0;
      ;;                ^this.multiNewList(['scalar', buf, offset, values.size] ++ values)
      ;;        }
      ;; }

      {:name "SetBuf"
       :args [{:name "buf"}
              {:name "values" :mode :not-expanded}
              {:name "offset" :default 0}]
       ;;:init (fn [rate [buf values offset] spec]
       ;;         (apply vector buf offset (count values) values))
       }

      ;; ClearBuf : UGen {
      ;;        *new { arg buf;
      ;;                ^this.multiNew('scalar', buf)
      ;;        }
      ;; }

      {:name "ClearBuf",
       :args [{:name "buf"}],
       :rates #{:ir}}])
