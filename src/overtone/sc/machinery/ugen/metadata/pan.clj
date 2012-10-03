(ns overtone.sc.machinery.ugen.metadata.pan
  (:use [overtone.sc.machinery.ugen common check]))

;; Panner : MultiOutUGen {
;;  checkNInputs { arg n;
;;    if (rate == 'audio') {
;;      n.do {| i |
;;        if (inputs.at(i).rate != 'audio') {
;;          //"failed".postln;
;;          ^("input " ++ i ++ " is not audio rate: " + inputs.at(i) + inputs.at(0).rate);
;;        };
;;      };
;;     };
;;      ^this.checkValidInputs
;;    }
;;    checkInputs { ^this.checkNInputs(1) }
;; }

;; XFade : UGen {
;;  checkNInputs { arg n;
;;    if (rate == 'audio') {
;;      n.do {| i |
;;        if (inputs.at(i).rate != 'audio') {
;;          ^("input " ++ i ++ " is not audio rate: " + inputs.at(i) + inputs.at(0).rate);
;;        };
;;      };
;;     };
;;      ^nil
;;    }
;; }

(def specs
  (map
   #(assoc % :check (when-ar (first-input-ar)))
   [

    {:name "Pan2"
     :args [{:name "in"
             :doc "input signal"}

            {:name "pos"
             :default 0.0
             :doc "pan position, -1 is left, +1 is right"}

            {:name "level"
             :default 1.0
             :doc "a control rate level input"}]

     :num-outs 2
     :check (nth-input-stream? 0)
     :doc "Two channel (stereo) equal power panner."}


    {:name "LinPan2"
     :extends "Pan2"
     :doc "Two channel (stereo) linear panner. This one sounds more like
           the Rhodes tremolo than Pan2." }


    {:name "Pan4",
     :args [{:name "in",
             :doc "input signal"}

            {:name "xpos",
             :default 0.0,
             :doc "x pan position from -1 to +1 (left to right)"}

            {:name "ypos",
             :default 0.0,
             :doc "y pan position from -1 to +1 (back to front)"}

            {:name "level",
             :default 1.0,
             :doc "a control rate level input."}]

     :num-outs 4
     :check (nth-input-stream? 0)
     :doc "Four channel equal power panner. Outputs are in order
           LeftFront, RightFront, LeftBack, RightBack." }


    {:name "Balance2",
     :args [{:name "left",
             :doc "channel 1 of input stereo signal"}

            {:name "right",
             :doc "channel 2 of input stereo signal"}

            {:name "pos",
             :default 0.0,
             :doc "pan position, -1 is left, +1 is right"}

            {:name "level",
             :default 1.0,
             :doc "a control rate level input."}]

     :num-outs 2
     :doc "Equal power panning balances two channels; by panning, you
           are favouring one or other channel in the mix, and the other
           loses power. The middle pan position (pos=0.0) corresponds to
           the original stereo mix; full left (pos of -1) is essentially
           just left channel playing, full right (pos of 1) just the
           right. The output of Balance2 remains a stereo signal." }


    {:name "Rotate2",
     :args [{:name "x",
             :doc "input signal"}

            {:name "y",
             :doc "input signal"}

            {:name "pos",
             :default 0.0,
             :doc "angle to rotate around the circle from -1 to +1. -1
                   is 180 degrees, -0.5 is left, 0 is forward, +0.5 is
                   right, +1 is behind." }]
     :num-outs 2
     :check [(nth-input-stream? 0)
             (nth-input-stream? 1)]
     :doc "Rotate2 can be used for rotating an ambisonic B-format sound
           field around an axis.

           Rotate2 does an equal power rotation so it also works well on
           stereo sounds.  It takes two audio inputs (x, y) and an angle
           control (pos).  It outputs two channels (x, y).  It computes
           this:

           xout = cos(angle) * xin + sin(angle) * yin;

           yout = cos(angle) * yin - sin(angle) * xin;

           where angle = pos * pi, so that -1 becomes -pi and +1 becomes
           +pi.  This allows you to use an LFSaw to do continuous
           rotation around a circle." }


    {:name "PanB",
     :args [{:name "in",
             :doc "input signal"}

            {:name "azimuth",
             :default 0.0,
             :doc "in radians, -pi to +pi"}

            {:name "elevation",
             :default 0.0,
             :doc "in radians, -0.5pi to +0.5pi"}

            {:name "gain",
             :default 1.0,
             :doc "a control rate level input"}]

     :num-outs 4
     :check (nth-input-stream? 0)
     :doc "Ambisonic B format panner. Output channels are in order
           W,X,Y,Z." }


    {:name "PanB2",
     :args [{:name "in",
             :doc "input signal"}

            {:name "azimuth",
             :default 0.0,
             :doc "position around the circle from -1 to +1. -1 is
                   behind, -0.5 is left, 0 is forward, +0.5 is right, +1
                   is behind." }

            {:name "gain",
             :default 1.0,
             :doc "amplitude control"}]

     :num-outs 3
     :check (nth-input-stream? 0)
     :doc "Encode a mono signal to two dimensional ambisonic B-format."}


    {:name "BiPanB2",
     :args [{:name "in-a",
             :doc "input signal A"}

            {:name "in-b",
             :doc "input signal B"}

            {:name "azimuth",
             :doc "position around the circle from -1 to +1. -1 is
                   behind, -0.5 is left, 0 is forward, +0.5 is right, +1
                   is behind." }

            {:name "gain",
             :default 1.0,
             :doc "amplitude control"}]

     :num-outs 3
     :check [(when-ar (first-n-inputs-ar 2))
             (nth-input-stream? 0)
             (nth-input-stream? 1)]
     :doc "Encode a two channel signal to two dimensional ambisonic
           B-format.  This puts two channels at opposite poles of a 2D
           ambisonic field.  This is one way to map a stereo sound onto
           a soundfield.  It is equivalent to:

           PanB2(inA, azimuth, gain) + PanB2(inB, azimuth + 1, gain)"}


    {:name "DecodeB2",
     :args [{:name "numChannels"
             :mode :num-outs,
             :doc "number of output speakers. Typically 4 to 8."}

            {:name "w",
             :doc "B-format signal"}

            {:name "x",
             :doc "B-format signal"}

            {:name "y",
             :doc "B-format signal"}

            {:name "orientation",
             :default 0.5,
             :doc "Should be zero if the front is a vertex of the
                   polygon. The first speaker will be directly in
                   front. Should be 0.5 if the front bisects a side of
                   the polygon. Then the first speaker will be the one
                   left of center. Default is 0.5." }]
     :doc "2D Ambisonic B-format decoder. Decode a two dimensional
           ambisonic B-format signal to a set of speakers in a regular
           polygon.  The outputs will be in clockwise order. The
           position of the first speaker is either center or left of
           center."
     :check (when-ar (first-n-inputs-ar 3))}


    {:name "PanAz",
     :args [{:name "num-channels"
             :mode :num-outs,
             :doc "number of output channels"}

            {:name "in",
             :doc "input signal"}

            {:name "pos",
             :default 0.0,
             :doc "pan position. Channels are evenly spaced over a
                   cyclic period of 2.0 with 0.0 equal to the position
                   directly in front, 2.0/numChans a clockwise shift
                   1/numChans of the way around the ring, 4.0/numChans
                   equal to a shift of 2/numChans, etc. Thus all
                   channels will be cyclically panned through if a
                   sawtooth wave from -1 to +1 is used to modulate the
                   pos. N.B. Front may or may not correspond to a
                   speaker depending on the setting of the orientation
                   arg, see below." }

            {:name "level",
             :default 1.0,
             :doc "a control rate level input."}

            {:name "width",
             :default 2.0,
             :doc "The width of the panning envelope. Nominally this is
                   2.0 which pans between pairs of adjacent
                   speakers. Width values greater than two will spread
                   the pan over greater numbers of speakers. Width
                   values less than one will leave silent gaps between
                   speakers." }

            {:name "orientation",
             :default 0.5,
             :doc "Should be zero if the front is a vertex of the
                   polygon. The first speaker will be directly in
                   front. Should be 0.5 if the front bisects a side of
                   the polygon. Then the first speaker will be the one
                   left of center. Default is 0.5." }]
     :check (nth-input-stream? 1)
     :doc "Multichannel equal power panner."}

    {:name "XFade2",
     :args [{:name "inA"
             :doc "input signal A"}

            {:name "inB"
             :doc "input signal B"}

            {:name "pan",
             :default 0.0
             :doc "Pan between the two input signals with -1 being inA
                   only and 1 being inB only with values between being a
                   mix of the two." }

            {:name "level",
             :default 1.0
             :doc "Output level - 0 being silent and 1 being original
                   volume"}]

     :check [(when-ar (first-n-inputs-ar 2))
             (nth-input-stream? 0)
             (nth-input-stream? 1)]
     :rates #{:ar :kr}
     :doc "Equal power two channel cross fade"}

    {:name "LinXFade2",
     :args [{:name "inA",
             :doc "input signal A"}

            {:name "inB",
             :doc "input signal B"}

            {:name "pan",
             :default 0.0,
             :doc "cross fade position from -1 to +1"}

            {:name "level",
             :default 1.0,
             :doc "a control rate level input"}]

     :rates #{:ar :kr}
     :doc "Two channel linear crossfader."
     :check [(when-ar (first-n-inputs-ar 2))
             (nth-input-stream? 0)
             (nth-input-stream? 1)]}]))
