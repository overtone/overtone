(ns overtone.sc.machinery.ugen.metadata.extras.vbap
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "VBAP"
    :summary "Vector Based Amplitude Panner"
    :args [{:name "num-chans"
               :default 1
               :mode :num-outs
               :doc "The number of output channels. This must be a
                     fixed integer and not a signal or a control
                     proxy. The architecture of the synth design
                     cannot change after it is compiled." }
           {:name "in"
            :default :none
            :doc "The signal to be panned."
            :rates #{:ar}}

           {:name "bufnum"
            :default 10
            :doc "The index of the buffer containing data
                  calculated by the function vbap-speaker-array. Its
                  number of channels must correspond to numChans
                  above." }
           
           {:name "azimuth"
            :default 0
            :doc "+/- 180 degrees from the median plane (i.e. straight ahead)" }

           {:name "elevation"
            :default 0
            :doc "+/- 90 degrees from azimuth plane" }

           {:name "spread"
            :default 0
            :doc "A value from 0-100. When 0, if the signal is panned
                  exactly to a speaker location the signal is only on
                  that speaker. At values higher than 0, the signal
                  will always be on more than one speaker. This can
                  smooth the panning effect by making localisation
                  blur more constant." }]

    :rates #{:ar :kr}

    :check (nth-input-stream? 1)

    :doc "An implementation of Vector Base Amplitude Panning. This
          allows for equal power panning of a source over an array of
          speakers on arbitrary positions on a circle (2D) or
          sphere (3D) around the listener. Normally this would be a
          ring, a dome, or a partial ring or dome.

          VBAP was created by Ville Pulkki. For more information on
          VBAP see http://www.acoustics.hut.fi/research/cat/vbap/

          Examples:

          ;;; ------------------------------------------------------------------------------------------
          ;;; 2-D:

          (do
            ;;; define Loudspeaker Positions
            (def vbap-data (vbap-speaker-array [-45 0 45 90 135 180 -135 -90]))
            ;;; init buffer on server and store the loudspeaker data matrices in it
            (def b (buffer (count vbap-data)))
            (buffer-write! b vbap-data)
            ;;; define a simple synth with pink noise as source
            (defsynth vbaptest
              [buf 0 azi 0 ele 0 spread 0]
              (out 0 (vbap 8 (pink-noise) buf azi ele spread))))

          ;;; start the synth

          (def vbapsynth (vbaptest b 0 0 0))

          ;;; change the azimuth

          (map-indexed #(at (+ (now) (* 1000 %1)) (ctl vbapsynth :azi %2))
             '(-45 0 45 90 135 180 -135 -90 -45))

          ;;; change spread and repeat the above command:

          ;;; more than one speaker
          (ctl vbapsynth :spread 40)

          ;;; only one speaker
          (ctl vbapsynth :spread 0)

          ;;; stop the synth
          (kill vbapsynth)

          ;;; ------------------------------------------------------------------------------------------
          ;;; 3-D:

          (do
            ;;; define Loudspeaker Positions in a zig-zag around the Listener
            (def vbap-data (vbap-speaker-array [[-45 0] [0 45] [45 0] [90 45] [135 0] [180 45] [-135 0] [-90 45]]))
            ;;; init buffer on server and store the loudspeaker data matrices in it
            (def b (buffer (count vbap-data)))
            (buffer-write! b vbap-data)
            ;;; define a simple synth with pink noise as source
            (defsynth vbaptest
              [buf 0 azi 0 ele 0 spread 0]
              (out 0 (vbap 8 (pink-noise) buf azi ele spread))))
          
          ;;; start the synth
          
          (def vbapsynth (vbaptest b 0 0 0))

          ;;; traverse all speakers in a zig-zag motion
          
          (map #(at (+ (now) (* 1000 %1)) (ctl vbapsynth :azi %2 :ele %3))
               (range)
               '(-45 0 45 90 135 180 -135 -90 -45)
               '(0 45 0 45 0 45 0 45 0))
          
          ;;; change spread and repeat the above command:
          
          ;;; more than one speaker involved
          (ctl vbapsynth :spread 40)
          
          ;;; only one speaker at a time
          (ctl vbapsynth :spread 0)
          
          ;;; stop the synth
          
          (kill vbapsynth)

"}])

