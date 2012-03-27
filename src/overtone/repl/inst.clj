(ns overtone.repl.inst
  (:use [overtone.studio.inst :only (inst?)]))

(defn ns-instruments
  "Returns a sequence of all the (public) instruments in the given
  namespace.

  For example:

    => (use 'overtone.inst.drum)
    nil
    => (ns-instruments 'overtone.inst.drum)
    (...)
    => (step-sequencer (metronome 200) 8 (ns-instruments 'overtone.inst.drum))

  Note that the returned sequence is a sequence of instrument
  *objects*, not vars, so they'll print sort of like this:

    #<Object$Associative$IFn$IMeta$IObj$17272e52 instrument:dub-kick>

  "
  [ns]
  (for [[n v] (ns-publics ns) :when (inst? @v)]
    @v))

