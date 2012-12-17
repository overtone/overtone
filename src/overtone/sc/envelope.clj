(ns
  ^{:doc "An envelope defines a waveform that will be used to control another
          component of a synthesizer over time.  It is typical to use envelopes
          to control the amplitude of a source waveform.  For example, an
          envelope will dictate that a sound should start quick and loud, but
          then drop shortly and tail off gently.  Another common usage is to
          see envelopes controlling filter cutoff values over time.

          These are the typical envelope functions found in SuperCollider, and
          they output a series of numbers that is understood by the SC synth
          engine."
     :author "Jeff Rose, Sam Aaron"}
  overtone.sc.envelope
  (:use [overtone.helpers lib]
        [overtone.sc ugens]))

(def ENV-SHAPES
  {:step        0
   :lin         1
   :linear      1
   :exp         2
   :exponential 2
   :sin         3
   :sine        3
   :wel         4
   :welch       4
   :sqr         6
   :squared     6
   :cub         7
   :cubed       7
   })


(defn- shape->id
  "Create a repeating shapes list corresponding to a specific shape type.
  Looks shape s up in ENV-SHAPES if it's a keyword. If not, it assumes the
  val represents the bespoke curve vals for a generic curve shape (shape
  type 5). the bespoke curve vals aren't used here, but are picked up in
  curve-value.
  Mirrors *shapeNumber in supercollider/SCClassLibrary/Common/Audio/Env.sc"
  [s]
  (if (keyword? s)
    (repeat (s ENV-SHAPES))
    (repeat 5)))

(defn- curve-value
  "Create the curves list for this curve type. For all standard shapes this
  list of vals isn't used. It's only required when the shape is a generic
  'curve' shape when the curve vals represent the curvature value for each
  segment. A single float is repeated for all segments whilst a list of floats
  can be used to represent the curvature value for each segment individually.
  Mirrors curveValue in supercollider/SCClassLibrary/Common/Audio/Env.sc"
  ;;
  [c]
  (cond
   (sequential? c)  c
   (number? c) (repeat c)
   :else (repeat 0)))

;; Envelope specs describe a series of segments of a line, which can be used to automate
;; control values in synths.
;;
;;  [ <initialLevel>,
;;    <numberOfSegments>,
;;    <releaseNode>,
;;    <loopNode>,
;;    <segment1TargetLevel>, <segment1Duration>, <segment1Shape>, <segment1Curve>,
;;    ...
;;    <segment-N...> ]

(defn envelope
  "Create an envelope curve description array suitable for the env-gen ugen.
   Requires a list of levels (the points that the envelope will pass
   through and a list of durations (the duration in time of the lines
   between each point).

   Optionally a curve may be specified. This may be one of:
   * :step              - flat segments
   * :linear            - linear segments, the default
   * :exponential       - natural exponential growth and decay. In this
                          case, the levels must all be nonzero and the have
                          the same sign.
   * :sine              - sinusoidal S shaped segments.
   * :welch             - sinusoidal segments shaped like the sides of a
                          Welch window.
   * a Float            - a curvature value to be repeated for all segments.
   * an Array of Floats - individual curvature values for each segment.
                          Positive numbers curve the segment up whilst
                          negative numbers curve the segment down.

   If a release-node is specified (an integer index) the envelope will sustain
   at the release node until released which occurs when the gate input of the
   env-gen is set to zero.

   If a loop-node is specified (an integer index) the output will loop
   through those nodes starting at the loop node to the node immediately
   preceeding the release node, before back to the loop node, and so
   on. Note that the envelope only transitions to the release node when
   released. The loop is escaped when a gate signal is sent, which
   results in the output transitioning to the release node."

  ;;See prAsArray in supercollider/SCClassLibrary/Common/Audio/Env.sc
  ([levels durations]
     (envelope levels durations :linear))
  ([levels durations curve]
     (envelope levels durations curve -99))
  ([levels durations curve release-node]
     (envelope levels durations curve release-node -99))
  ([levels durations curve release-node loop-node]
     (let [shapes (shape->id curve)
           curves (curve-value curve)]
       (apply vector
              (concat [(first levels) (count durations) release-node loop-node]
                      (interleave (rest levels) durations shapes curves))))))

(defunk triangle
  "Create a triangle envelope description array suitable for use with the
  env-gen ugen"
  [dur 1 level 1]
  (with-overloaded-ugens
    (let [dur (* dur 0.5)]
      (envelope [0 level 0] [dur dur]))))

(defunk sine
  "Create a sine envelope description suitable for use with the env-gen ugen"
  [dur 1 level 1]
  (with-overloaded-ugens
    (let [dur (* dur 0.5)]
      (envelope [0 level 0] [dur dur] :sine))))

(defunk perc
  "Create a percussive envelope description suitable for use with the env-gen
  ugen"
  [attack 0.01 release 1 level 1 curve -4]
  (with-overloaded-ugens
    (envelope [0 level 0] [attack release] curve)))

(defunk lin-env
  "Create a trapezoidal envelope description suitable for use with the env-gen
  ugen"
  [attack 0.01 sustain 1 release 1 level 1 curve :linear]
  (with-overloaded-ugens
    (envelope [0 level level 0] [attack sustain release] curve)))

(defunk cutoff
  "Create a cutoff envelope description suitable for use with the env-gen ugen"
  [release 0.1 level 1 curve :linear]
  (with-overloaded-ugens
    (envelope [level 0] [release] curve 0)))

(defunk dadsr
  "Create a delayed attack decay sustain release envelope suitable for use with
  the env-gen ugen"
  [delay-t 0.1
               attack 0.01 decay 0.3 sustain 0.5 release 1
               level 1 curve -4 bias 0]
  (with-overloaded-ugens
    (envelope
      (map #(+ %1 bias) [0 0 level (* level sustain) 0])
      [delay-t attack decay release] curve)))

(defunk adsr
  "Create an attack decay sustain release envelope suitable for use with the
  env-gen ugen"
  [attack 0.01 decay 0.3 sustain 1 release 1
              level 1 curve -4 bias 0]
  (with-overloaded-ugens
    (envelope
      (map #(+ %1 bias) [0 level (* level sustain) 0])
      [attack decay release] curve 2)))

(defunk asr
  "Create an attack sustain release envelope sutable for use with the env-gen
  ugen"
  [attack 0.01 sustain 1 release 1 curve -4]
  (with-overloaded-ugens
    (envelope [0 sustain 0] [attack release] curve 1)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Envelope Curve Shapes
;; Thanks to ScalaCollider for these shape formulas!
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn step-shape [pos y1 y2]
  (if (< pos 1) y1 y2))

(defn linear-shape [pos y1 y2]
  (+ y1
     (* pos (- y2 y1))))

(defn exponential-shape [pos y1 y2]
  (let [limit (max 0.0001 y1)]
    (* limit (Math/pow (/ y2 limit) pos))))

(defn sine-shape [pos y1 y2]
  (+ y1
     (* (- y2 y1)
        (+ (* -1 (Math/cos (* Math/PI pos)) 0.5) 0.5))))

(defn welch-shape [pos y1 y2]
  (let [pos (if (< y1 y2) pos (- 1.0 pos))]
    (+ y1
       (* (- y2 y1)
          (Math/sin (* Math/PI 0.5 pos))))))

(defn curve-shape [pos y1 y2 curvature]
  (if (< (Math/abs curvature) 0.0001)
    (+ (* pos (- y2 y1))
       y1)
    (let [denominator (- 1.0 (Math/exp curvature))
          numerator   (- 1.0 (Math/exp (* pos curvature)))]
      (+ y1
         (* (- y2 y1) (/ numerator denominator))))))

(defn squared-shape [pos y1 y2]
  (let [y1-s (Math/sqrt y1)
        y2-s (Math/sqrt y2)
        yp (+ y1-s (* pos (- y2-s y1-s)))]
    (* yp yp)))

(defn cubed-shape [pos y1 y2]
  (let [y1-c (Math/pow y1 0.3333333)
        y2-c (Math/pow y2 0.3333333)
        yp (+ y1-c (* pos (- y2-c y1-c)))]
    (* yp yp yp)))
