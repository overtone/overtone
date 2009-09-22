(ns overtone.voice)

;(defn voice [synth & defaults]
;  (let [new-voice {:type     :voice
;                   :synth    synth
;                   :controls {}
;                   :defaults (apply hash-map defaults)}]
;    (dosync (alter *voices conj new-voice))
;    new-voice))

(defn voice? [obj]
  (and (map? obj) 
       (= :voice (:type obj))))

(defn voice-type [& args]
  (:voice-type (first args)))

(defmulti play-note voice-type)
