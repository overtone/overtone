(ns
    ^{:doc "Records and fns for representing SCUgens. These are to be distinguised with ugens which are Overtone functions which compile down into SCUGens. Trees of SCUGens can then, in turn, be compiled down into a binary synth format for shipping to SCServer."
      :author "Sam Aaron"}
    overtone.sc.machinery.ugen.sc-ugen
  (:use [overtone.sc.machinery.ugen defaults]
        [overtone.helpers lib]))

(defrecord SCUGen [id name rate rate-name special args n-outputs spec])
(derive SCUGen ::sc-ugen)

(defn sc-ugen? [obj] (isa? (type obj) ::sc-ugen))

(defn sc-ugen
  "Create a new SCUGen instance. Throws an error if any of the args are nil."
  [id name rate rate-name special args n-outputs spec]
  (if (or (nil? id)
          (nil? name)
          (nil? rate)
          (nil? rate-name)
          (nil? special)
          (nil? args)
          (nil? n-outputs)
          (nil? spec))
    (throw (IllegalArgumentException. (str "Attempted to create an SCUGen with nil args. Got " [id name rate rate-name special args n-outputs spec])))
    (SCUGen. id name rate rate-name special args n-outputs spec)))

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
  (.write w (str "#<sc-ugen: " (overtone-ugen-name (:name ug)) (:rate-name ug) " [" (count-ugen-args ug) "]>")))

(defrecord ControlProxy [name value rate rate-name])
(derive ControlProxy ::control-proxy)
(derive ::control-proxy ::sc-ugen)

(defn control-proxy
  "Create a new control proxy with the specified name, value and rate. Rate
  defaults to :kr. Specifically handles :tr which is really a TrigControl
  ugen at :kr. Throws an error if any of the args are nil."
  ([name value] (control-proxy name value :kr))
  ([name value rate-name]
     (let [rate (if (= :tr)
                  (:kr RATES)
                  (rate-name RATES))]
       (if (or (nil? name)
               (nil? value)
               (nil? rate)
               (nil? rate-name))
         (throw (IllegalArgumentException. (str "Attempted to create a ControlProxy with nil args. Got " [name value rate rate-name])))
         (ControlProxy. name value rate rate-name)))))

(defrecord OutputProxy [name ugen rate rate-name index])
(derive OutputProxy ::output-proxy)
(derive ::output-proxy ::sc-ugen)

(defn output-proxy
  "Create a new output proxy. Throws an error if any of the args are nil."
  [ugen index]
  (let [rate (:rate ugen)
        rate-name (REVERSE-RATES (:rate ugen))]
    (if (or (nil? ugen)
            (nil? rate)
            (nil? rate-name)
            (nil? index))
      (throw (IllegalArgumentException. (str "Attempted to create an OutputProxy with nil args. Got " [ugen rate rate-name index])))
      (OutputProxy. "OutputProxy" ugen rate rate-name index))))

(defn control-proxy?
  [obj]
  (isa? (type obj) ::control-proxy))

(defn output-proxy?
  [obj]
  (isa? (type obj) ::output-proxy))
