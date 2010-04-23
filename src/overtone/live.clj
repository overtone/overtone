(ns overtone.live
  (:require midi osc byte-spec
            clojure.stacktrace
            (overtone.core config time-utils log sc ugen synth synthdef envelope)
            (overtone.music rhythm pitch tuning)))

; TODO: make this work with namespace prefixes too...
;   (immigrate 'overtone.instruments)
(defn immigrate
 "Create a public var in this namespace for each public var in the
 namespaces named by ns-names. The created vars have the same name, value,
 and metadata as the original except that their :ns metadata value is this
 namespace."
 [& ns-names]
 (doseq [ns ns-names]
   (require ns)
   (doseq [[sym var] (ns-publics ns)]
     (let [sym (with-meta sym (assoc (meta var) :ns *ns*))]
       (if (.isBound var)
         (intern *ns* sym (var-get var))
         (intern *ns* sym))))))

(immigrate
  'osc
  'midi
  'overtone.core.time-utils
  'overtone.core.util
  'overtone.core.event
  'overtone.core.sc
  'overtone.core.ugen
  'overtone.core.envelope
  'overtone.core.synth
  'overtone.core.synthdef
  'overtone.music.rhythm
  'overtone.music.pitch
  'overtone.music.tuning
  'overtone.gui.curve
  'overtone.gui.scope
  'clojure.stacktrace
  )

;(refer-ugens *ns*)

