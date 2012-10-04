(ns overtone.sc.machinery.ugen.metadata.unaryopugen
  (:use [overtone.helpers lib]))

(def unnormalized-unaryopugen-docspecs
  {"neg" {:summary "Signal inversion"
          :doc "Invert input signal a by changing its sign.

                i.e. 1 -> -1 and -0.5 -> 0.5"}

   "not-pos?"    {:summary "Check for non positive signal"
                  :doc "Determines whether the signal is negative or
                        not. If it is less than or equal to 0 then
                        outputs 1, otherwise outputs 0

                        i.e. 1 -> 0,  -0.5 -> 1, 0 -> 1"}

   "abs"         {:summary "Absolute value"
                  :doc "Outputs the value of input a without regard to
                        its sign.

                        i.e 1 -> 1 and -0.5 0.5"}

   "ceil"        {:summary "Next higher integer"
                  :doc "Rounds input a up the next highest integer.

                        i.e. 5.1 -> 6 and 7.0 -> 7"}

   "floor"       {:summary "Previous lower integer"
                  :doc "Rounds input a down to the previous lower
                       integer.

                       i.e. 5.9 -> 5 and 7.0 -> 7"}

   "frac"        {:summary "Fractional part of signal"
                  :doc "Outputs the fractional part between input a and
                        the next higher integer. This means that
                        negative input vals may result in a different
                        output than positive input vals. Output is
                        always positive.

                        i.e. 3.5 -> 0.5, 34.2 -> 0.2, -5.2 -> 0.8"}

   "sign"        {:summary "Sign of signal"
                  :doc "Outputs -1 when a < 0, +1 when a > 0, 0 when a
                        is 0." }

   "squared"     {:summary "Squared value"
                  :doc "Outputs the square of input a.

                        i.e. 5 -> 25 and -3 -> 9"}

   "cubed"       {:summary "Cubed value"
                  :doc "Outputs the cube of input a.

                        i.e. 2 -> 8 and -3 -> -27"}

   "sqrt"        {:summary "Square root"
                  :doc "Outputs the square root of input a. The
                        definition of square root is extended for
                        signals so that when a is < 0 (sqrt a)
                        returns (- (sqrt -a)).

                        i.e. 9 -> 3 and -25 -> -5"}

   "exp"         {:summary "Exponential function"
                  :doc "Outputs e to the power of a, where e is the
                        mathematical constant (approximately
                        2.718281828) and a is the input signal.

                        i.e. 1 -> 2.71828 and 10 -> 22026.5"}

   "reciprocal"  {:summary "Multiplicative inverse"
                  :doc "Outputs the multiplicative inverse of a. This is
                        typically denoted by 1/a or a^-1.

                        i.e. 10 -> 0.1 and -1 -> -1"}

   "midicps"     {:args [{:name "midi-note" :doc "MIDI note to convert"}]
                  :summary "Convert MIDI note to cycles per second"
                  :doc "Outputs the corresponding Hz or cycles per
                        second for input a representing a MIDI note. Inverse
                        of cpsmidi.

                        i.e 69 -> 440 and 80 -> 830.609"}

   "cpsmidi"     {:args [{:name "cps" :doc "Cycles per second"}]
                  :summary "convert cycles per second to MIDI note"
                  :doc "Outputs the corresponding MIDI note for input a
                        representing cycles per second or Hz. Doesn't
                        always return a discrete integer. Inverse of
                        midicps.

                        i.e. 440 -> 69 and 500 -> 71.2131"}

   "midiratio"   {:args [{:name "num-semitones" :doc "Interval in MIDI notes"}]
                  :summary "Convert an interval in MIDI notes into a frequency ratio"
                  :doc "Outputs a frequency ratio that corresponds to
                        the interval in MIDI notes represented by
                        num-semitones. In other words, outputs the
                        amount to multiply a frequency by in order to
                        alter it by num-semitones. Inverse of ratiomidi.

                        i.e. 12 -> 2, -2 -> 0.890899"}

   "ratiomidi"   {:args [{:name "freq-ratio" :doc "Cycles per second"}]
                  :summary "Convert a frequency ratio to an interval in MIDI notes"
                  :doc "Outputs the number of semitones difference
                        created when multiplying a given frequency by
                        freq-ratio. Inverse of midiratio.

                        i.e. 0.890899 -> -1.99999 and 2 -> 12 "}

   "dbamp"       {:args [{:name "decibels" :doc "Volume in decibels"}]
                  :summary "convert decibels to linear amplitude"
                  :doc "Convert decibels to the linear amplitude. For
                        example, to raise the amplitude by 20db, you
                        should multiply the signal by 10. Inverse of
                        ampdb.

                        i.e. 20 -> 10 and 50 -> 316.228"}

   "ampdb"       {:args [{:name "amplitude" :doc "Linear amplitude"}]
                  :summary "Convert linear amplitude to decibels"
                  :doc "Convert a linear amplitude to number of
                        decibels. For example, a 20db raise in amplitude
                        is equivalent to multiplying a signal by
                        10. Inverse of dbamp.

                        i.e. 10 -> 20 and 316.228 -> 50"}

   "octcps"      {:args [{:name "dec-octaves" :doc "Decimal octaves"}]
                  :summary "Convert decimal octaves to cycles per second"
                  :doc "Outputs the number of cycles per second
                        represented by dec-octaves.

                        i.e. 1 -> 32.7031 -4 -> 1.02197"}

   "cpsoct"      {:args [{:name "cps" :doc "Cycles per second"}]
                  :summary "Convert cycles per second to decimal octaves"
                  :doc "Outputs the number of decimal octaves
                        represented by cps

                        i.e. 32.7031 -> 0.99999 and 500 -> 4.93442"}

   "log"         {:summary "Natural logarithm"
                  :doc "Outputs the natural log of input a

                        i.e. 2.71828 -> 0.99999 and 10 -> 2.30259"}

   "log2"        {:summary "Base 2 logarithm"
                  :doc "Outputs the base 2 log of input a

                        i.e. 8 -> 3 and 256 -> 8"}

   "log10"       {:summary "Base 10 logarithm"
                  :doc "Outputs the base 10 log of input a

                        i.e. 10 -> 1 and 200 -> 2.30103"}

   "sin"         {:summary "Sine function"
                  :doc "Outputs the sine of input a

                        i.e. 1.5707986 -> 1 and 3.14159265 -> ~0"}

   "cos"         {:summary "Cosine function"
                  :doc "Outputs the cosine of input a

                        i.e. 0 -> 1 and 3.14159265 -> -1"}

   "tan"         {:summary "Tangent function"
                  :doc "Outputs the tangent of input a

                        i.e. 0 -> 0 and 1.570 -> 1255.85"}

   "asin"        {:summary "Arcsine function"
                  :doc "Outputs the arcsine of input a

                        i.e. 1 -> 1.5708 and 3.14159265 -> nan"}

   "acos"        {:summary "Arccosine function"
                  :doc "Outputs the arccosine of input a

                        i.e. 1 -> 0 and 3.14159265 -> nan"}

   "atan"        {:summary "Arctangent function"
                  :doc "Outputs the arctangent of input a

                        i.e. 0 -> 0 and 1 -> 0.785398"}

   "sinh"        {:summary "Hyperbolic sine function"
                  :doc "Outputs the hyperbolic sine of input a

                        i.e. 0 -> 0 and 1 -> 1.1752"}

   "cosh"        {:summary "Hyperbolic cosine function"
                  :doc "Outputs the hyperbolic cosine of input a

                        i.e. 0 -> 1 and 1 -> 1.54308"}

   "tanh"        {:summary "Hyperbolic tangent function"
                  :doc "Outputs the hyperbolic tangent of input a.

                        Acts similar to clip2 with a value of 1 in that
                        it ensures that the output never exceeds +1 or
                        -1. However, it differs from clip2 in that it
                        massages the whole of the input rather than the
                        values which exceed the limits such that the
                        angles near the clipping ranges are rounder.

                        i.e. 0 -> 0 and 1 -> 0.761594"}

   "distort"     {:summary "Nonlinear distortion"
                  :doc "Maps input a to a value between 0 and 1 if a is
                        positive and between 0 and -1 if a is
                        negative. Slope of mapping is initially steep,
                        then the gradient reduces as it tends towards
                        1. Negative part is a mirror image. Always
                        returns a value between -1 and 1. See softclip
                        if you require a linear region.

                        i.e. 0 -> 0, 1 -> 0.5, 10 -> 0.909091,
                        1000 -> 0.999001 and -1 -> 0.5"}

   "softclip"    {:summary "Nonlinear distortion with linear region"
                  :doc "Maps input a to a value between 0 and 1 if a is
                        positive and between 0 and -1 if a is
                        negative. Gradient of mapping is linear between
                        -0.5 and 0.5 and then gradually reduces as it
                        tends towards 1. Negative part is a mirror
                        image. Always returns a value between -1 and 1.

                        i.e. 0 -> 0, 0.2 -> 0.2, 0.5 -> 0.5,
                        0.75 -> 0.666667, 10 -> 0.975 and 1000 -> 0.99975"}

   "rect-window" {:summary "Rectangular window function"
                  :doc "Maps input a onto a rectangular window which
                        spans between 0 and 1 with height 1. If the
                        input signal is between 0 and 1 inclusive, the
                        output will be 1, otherwise the output will be
                        0. Always returns either 0 or 1.

                        i.e. -0.1 -> 0, 0 -> 1, 0.5 -> 1,
                        0.99 -> 1, 1 -> 1, 1.01 -> 0 and 2000 -> 0"}

   "han-window"  {:summary "Hanning window function"
                  :doc "Maps input a onto a hanning window which spans
                        between 0 and 1 with a peak at 0.5. Any input
                        between 0 and 1 is mapped to the corresponding
                        val in hanning window (looks like a sharp tooth
                        - gradient quickly increases and then decreases
                        to the top of the tooth). All other input vals
                        map to 0. Always returns a value between 0 and
                        1.

                        i.e. 0.1 -> 0.0954915, 0.3 -> 0.654509, 0.5 -> 1,
                        0.7 -> 0.654509 and 0.9 -> 0.0954915"}

   "wel-window"  {:summary "Welch window function"
                  :doc "Maps input a onto a welch window which spans
                       between 0 and 1 with a peak at 0.5. Any input
                       between 0 and 1 is mapped to the corresponding
                       val in welch window (looks like a rhino horn -
                       gradient starts off high and gradually degreases
                       to the top of the horn). All other input vals map
                       to 0. Always returns a value between 0 and 1.

                       i.e. 0.1 -> 0.309017, 0.3 -> 0.809017, 0.5 -> 1,
                       0.7 -> 0.809017 and 0.9 -> 0.309017"}

   "tri-window"  {:summary "Triangle window function"
                  :doc "Maps input a onto a triangle window which spans
                        between 0 and 1 with a peak at 0.5. Any input
                        between 0 and 1 is mapped to the corresponding
                        val in the triangle window. All other input vals
                        map to 0. Always returns a value between 0 and
                        1." }

   })

(def unaryopugen-docspecs
  (into {} (map
            (fn [[k v]] [(normalize-ugen-name k) v])
            unnormalized-unaryopugen-docspecs)))
