(ns
    ^{:doc "Utility functions for synth construction"
      :author "Sam Aaron"}
  overtone.helpers.synth)

(def valid-synth-node-pos #{:head :tail :before :after :replace})

(defn- validate-target-pos!
  [target pos]
  (when-not (valid-synth-node-pos pos)
    (throw (IllegalArgumentException. (str "Invalid synth node target. Was expecting one of " valid-synth-node-pos ", found: " pos)))))

(defn- extract-target-pos-args*
  [args default-target default-pos]
  (let [initial-arg (first args)]
    (when (some #{initial-arg}
                [:target :tgt :pos :position])
      (throw (IllegalArgumentException. (str "Specifying :target and :position at the start of the standard synth args is now deprecated. Use new vec form i.e. [:after my-g] or [:tail foo-g]"))))
    (when (and (vector? initial-arg)
               (not= 2 (count initial-arg)))
      (throw (IllegalArgumentException. (str "Target and position vector must contain only two arguments (i.e. [:after my-g]). Found: " initial-arg))))
    (if (vector? initial-arg)
      [(second initial-arg) (first initial-arg) (rest args)]
      [default-target default-pos args])))

(defn extract-target-pos-args
  [args default-target default-pos]
  (let [[tgt pos args :as extracted] (extract-target-pos-args* args default-target default-pos)]
    (validate-target-pos! tgt pos)
    extracted))
