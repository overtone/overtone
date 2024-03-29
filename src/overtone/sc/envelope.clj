(ns overtone.sc.envelope
  "An envelope defines a waveform that will be used to control another component
  of a synthesizer over time. It is typical to use envelopes to control the
  amplitude of a source waveform. For example, an envelope will dictate that a
  sound should start quick and loud, but then drop shortly and tail off gently.
  Another common usage is to see envelopes controlling filter cutoff values over
  time.

  These are the typical envelope functions found in SuperCollider, and they
  output a series of numbers that is understood by the SC synth engine."
  {:author "Jeff Rose, Sam Aaron"}
  (:use
   [overtone.helpers lib]
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
   :cubed       7})

(defn- curve->shape-id
    "Map curve to envelope shape. If curve is a keyword, look it up in
     ENV-SHAPES, otherwise assume it to be the generic cuve shape id of
     5"
    [curve]
    (get ENV-SHAPES curve 5))

(defn- curve->curve-id
    "Map curve to curve id. If curve is a keyword, assume the curve id
     is a generic shape (0) otherwise, preserve curve id"
    [curve]
    (if (get ENV-SHAPES curve)
      0
      curve))

(defn- curves->shape-ids
  "Create a repeating shapes list corresponding to a specific shape type.
  Looks shape s up in ENV-SHAPES if it's a keyword. If not, it assumes the
  val represents the bespoke curve vals for a generic curve shape (shape
  type 5). the bespoke curve vals aren't used here, but are picked up in
  curve-value.
  Mirrors *shapeNumber in supercollider/SCClassLibrary/Common/Audio/Env.sc"
  [curves]
  (cycle (map curve->shape-id curves)))

(defn- curves->curve-ids
  "Create the curves id list for the specified curves. For all standard shapes this
  list of vals isn't used. It's only required when the shape is a generic
  'curve' shape when the curve vals represent the curvature value for each
  segment. A single float is repeated for all segments whilst a list of floats
  can be used to represent the curvature value for each segment individually.
  Mirrors curveValue in supercollider/SCClassLibrary/Common/Audio/Env.sc"
  [curves]
  (cycle (map curve->curve-id curves)))


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

   Optionally a curve or list of curves may be specified. A single
   curve (as a keyword or float) will be repeated for all segments. A
   list of keywords or floats will be cycled through for all segments.

   Options are:

   * :step              - flat segments
   * :linear            - linear segments, the default
   * :exponential       - natural exponential growth and decay. In this
                          case, the levels must all be nonzero and the have
                          the same sign.
   * :sine              - sinusoidal S shaped segments.
   * :welch             - sinusoidal segments shaped like the sides of a
                          Welch window.
   * :squared           - Squared segments
   * :cubed             - Cubed segments
   * a float            - a curvature value to be repeated for all segments.
                          Positive numbers curve the segment up whilst
                          negative numbers curve the segment down.
   * a list of keywords - individual values for each segment. To be cycled
     and or floats        through for all segments.


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
  ([levels durations curves]
   (envelope levels durations curves -99))
  ([levels durations curves release-node]
   (envelope levels durations curves release-node -99))
  ([levels durations curves release-node loop-node]
   (let [curves    (if (sequential? curves)
                     curves
                     [curves])
         shape-ids (curves->shape-ids curves)
         curve-ids (curves->curve-ids curves)]
     (apply vector
            (concat [(first levels) (count durations) release-node loop-node]
                    (interleave (rest levels) durations shape-ids curve-ids))))))

(defmacro defunk-env [fn-name docstring args & body]
  `(do
     (defunk ~(symbol (str "env-" fn-name)) ~docstring ~args ~@body)
     (defunk ~fn-name ~docstring ~args ~@body)))

(defunk-env triangle
  "Create a triangle envelope description array suitable for use with the
  env-gen ugen"
  [dur 1 level 1]
  (with-overloaded-ugens
    (let [dur (* dur 0.5)]
      (envelope [0 level 0] [dur dur]))))

(defunk-env sine
  "Create a sine envelope description suitable for use with the env-gen ugen"
  [dur 1 level 1]
  (with-overloaded-ugens
    (let [dur (* dur 0.5)]
      (envelope [0 level 0] [dur dur] :sine))))

(defunk-env perc
  "Create a percussive envelope description suitable for use with the env-gen
  ugen"
  [attack 0.01 release 1 level 1 curve -4]
  (with-overloaded-ugens
    (envelope [0 level 0] [attack release] curve)))

(defunk-env lin
  "Create a trapezoidal envelope description suitable for use with the env-gen
  ugen"
  [attack 0.01 sustain 1 release 1 level 1 curve :linear]
  (with-overloaded-ugens
    (envelope [0 level level 0] [attack sustain release] curve)))

(def lin-env lin) ;; support legacy code

(defunk-env cutoff
  "Create a cutoff envelope description suitable for use with the env-gen ugen"
  [release 0.1 level 1 curve :linear]
  (with-overloaded-ugens
    (envelope [level 0] [release] curve 0)))

(defunk-env dadsr
  "Create a delayed attack decay sustain release envelope suitable for use with
  the env-gen ugen"
  [delay-t 0.1
               attack 0.01 decay 0.3 sustain 0.5 release 1
               level 1 curve -4 bias 0]
  (with-overloaded-ugens
    (envelope
      (map #(+ %1 bias) [0 0 level (* level sustain) 0])
      [delay-t attack decay release] curve)))

(defunk-env adsr
  "Create an attack decay sustain release envelope
  suitable for use as the envelope parameter of the
  env-gen ugen.

  attack  - the time it takes to go from 0 to the
            specified amplitude level (this defaults to
            1)

  decay   - the time it takes to go from the specified
            amplitude level to sustain * level (also
            defaulting to 1)

  sustain - the fraction of the level to use as the
            sustain amplitude

  release - the time it takes to go from the sustain
            amplitude to 0

  level   - the level of the amplitude after the attack,
            and the value to multiply the sustain
            fraction with to determine the sustain
            amplitude

  curve   - the envelope curve

  bias    - a value to add with every value of the envelope

  This envelope has multiple phases: attack, decay,
  sustain and release. Once the attack phase has started,
  after the specified attack time, the envelope value is
  the specified level + bias. Next the decay phase kicks
  in. After the decay time, the amplitude is at the
  sustain level + bias. The amplitude then stays at this
  level indefinitely. This is, until the gate of the
  outer env-gen is released. Once this gate is released,
  the envelope enters the release phase, and after
  release time, the amplitude is 0 + bias."
  [attack 0.01 decay 0.3 sustain 1 release 1
   level 1 curve -4 bias 0]
  (with-overloaded-ugens
    (envelope
     (map #(+ %1 bias) [0 level (* level sustain) 0])
     [attack decay release] curve 2)))

(defunk-env adsr-ng
  "Create an non-gated attack decay sustain release envelope
  suitable for use as the envelope parameter of the
  env-gen ugen.

  attack       - the time it takes to go from 0 to the
                 attack level (this defaults to 1)

  attack-level - level of the amplitude at the attack,
                 immediately before decay stage starts


  decay        - the time it takes to go from the specified
                 amplitude level to sustain * level (also
                 defaulting to 1)

  sustain      - sustain duration


  release      - the time it takes to go from the sustain
                 amplitude to 0

  level        - the level of the amplitude after the attack,
                 and the value to multiply the sustain
                 fraction with to determine the sustain
                 amplitude

  curve        - the envelope curve

  bias         - a value to add with every value of the
                 envelope

  This envelope has multiple phases: attack, decay, sustain and
  release. Once the attack phase has started, after the specified attack
  time, the envelope value is the attack-level + bias. Next the decay
  phase kicks in. After the decay time, the amplitude is at level +
  bias. The amplitude then stays at this level for sustain seconds, and
  then enters the release phase.  After release time, the amplitude
  is 0 + bias."
  [attack 0.01 decay 0.3 sustain 1 release 1
   attack-level 1 level 1 curve :linear bias 0]
  (with-overloaded-ugens
    (envelope
     (map #(+ % bias) [0 attack-level level level 0])
     [attack decay sustain release]
     curve)))

(defunk-env asr
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
  (if (< (Math/abs (double curvature)) 0.0001)
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
