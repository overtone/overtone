(ns overtone.sc.machinery.ugen.metadata.extras.rfwu
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "SwitchDelay"
    :summary "Feedback delay line implementing switch-and-ramp buffer jumping"
    :args [{:name "in"
            :doc "Signal to be filtered"}

           {:name "dry-level"
            :default 1
            :doc "Level of dry signal"}

           {:name "wet-level"
            :default 1
            :doc "Level of delayed signal"}

           {:name "delay-time"
            :default 1
            :doc "Number of seconds to delay signal"}

           {:name "delay-factor"
            :default 0.7
            :doc "Multiplier for feedback level, affects the length of
                  the feedback tail. Limited slightly below 1 to avoid
                  speaker damaging mistakes."}

           {:name "max-delay-time"
            :default 20
            :doc "The maximum duration of the delay in seconds."}]
    :rates #{:ar}
    :doc "A feedback delay line which allows moving the buffer read
          position using the switch-and-ramp technique as described by
          Miller S. Puckette in his Theory and Techniques of Electronic
          Music book:

          http://crca.ucsd.edu/~msp/techniques/latest/book.pdf (chapter
          4)

          Altering the buffer read position, in order to affect the
          perceived delay speed/timing, creates a discontinuity in the
          signal, typically causing unwanted audible artefacts. The
          switch-and-ramp technique seeks to neutralise these artefacts
          and allow switching with minimal clicks."}


   {:name "AverageOutput"
    :summary "Average output between triggers."
    :args [{:name "in"
            :doc "Input signal"}

           {:name "trig"
            :default 1
            :doc "if changes from <= 0 to > 0, resets average and count to zero."}]
    :rates #{:ar :kr}
    :doc "The mean average output since the last received trigger."}])
