(ns overtone
  (:use (overtone ugen))
  (:require (overtone time sc synth envelope synthdef midi 
                      rhythm pitch tuning studio log)))

; Thanks to James Reeves for this, taken from Compojure.

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
(defmacro overtone []
  `(do
     (immigrate
       'overtone.util
       'overtone.time
       'overtone.osc
       'overtone.sc
       'overtone.ugen
       'overtone.envelope
       'overtone.synth
       'overtone.synthdef
       'overtone.midi
       'overtone.rhythm
       'overtone.pitch
       'overtone.tuning
       'overtone.studio
       'overtone.inst
       )

     (refer-ugens *ns*)))

