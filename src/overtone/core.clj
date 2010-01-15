(ns overtone.core
  (:require (overtone.core sc ugen synth synthdef envelope log)
     (overtone.lib config midi time osc bytes)
     (overtone.music rhythm pitch tuning)
     (overtone studio)))

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

;(def *on-start-callbacks (ref {}))
;(def *on-reset-callbacks (ref {}))
;
;(defn on-start [callback]
;  (let [id (next-id :start-cb)]
;    (dosync (ref-set *on-start-callbacks 
;                     (assoc @*on-start-callbacks id callback)))
;    id))
;
;(defn remove-on-start-cb [id]
;  (dosync (alter *on-start-callbacks dissoc id)))
;

(immigrate
  'overtone.lib.osc
  'overtone.core.util
  'overtone.core.time
  'overtone.core.sc
  'overtone.core.ugen
  'overtone.core.envelope
  'overtone.core.synth
  'overtone.core.synthdef
  'overtone.lib.midi
  'overtone.lib.rhythm
  'overtone.lib.pitch
  'overtone.lib.tuning
  ;'overtone.studio
  ;'overtone.inst
  )

;(refer-ugens *ns*)

