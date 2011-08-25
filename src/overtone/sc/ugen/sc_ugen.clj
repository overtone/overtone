(ns overtone.sc.ugen.sc-ugen
  (:use [overtone.sc.ugen defaults]))

(defrecord SCUGen [id name rate rate-name special args n-outputs])
(derive SCUGen ::sc-ugen)

(defn sc-ugen? [obj] (isa? (type obj) ::sc-ugen))

(defn sc-ugen
  "Create a new SCUGen instance"
  [id name rate rate-name special args n-outputs]
  (SCUGen. id name rate rate-name special args n-outputs))

(defn count-ugen-args
  "Count the number of ugens in the args of ug (and their args recursively)"
  [ug]
  (let [args (:args ug)]
    (reduce (fn [sum arg]
              (if (sc-ugen? arg)
                (+ sum 1 (count-ugen-args arg))
                sum))
            0
            args)))

(defmethod print-method SCUGen [ug w]
  (.write w (str "#<sc-ugen: " (:name ug) " with " (count-ugen-args ug) " internal sc-ugens>")))

(defrecord ControlProxy [name value rate rate-name])
(derive ControlProxy ::sc-ugen)

(defn control-proxy
  "Create a new control proxy with the specified name, value and rate. Rate
  defaults to :kr. Specifically handles :tr which is really a TrigControl
  ugen at :kr."
  ([name value] (control-proxy name value :kr))
  ([name value rate]
     (ControlProxy. name value (if (= :tr)
                                 (:kr RATES)
                                 (rate RATES)) rate)))

(defrecord OutputProxy [ugen rate rate-name index])
(derive OutputProxy ::sc-ugen)

(defn output-proxy [ugen index]
  (OutputProxy. ugen (:rate ugen) (REVERSE-RATES (:rate ugen)) index))


(defn control-proxy? [obj] (= ControlProxy (type obj)))
(defn output-proxy? [obj] (= OutputProxy (type obj)))
