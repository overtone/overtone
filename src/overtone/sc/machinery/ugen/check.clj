(ns
  ^{:doc "UGen argument validation functions."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.machinery.ugen.check
  (:use [overtone.sc.machinery.ugen defaults]))

(defn rate-of [obj]
  (:rate-name obj))

(defn rate-of? [obj rate]
  (= (rate-of obj) rate))

(defn name-of [obj]
  (:name obj))

(defn name-of? [obj name]
  (= (name-of obj) name))

(defn ar? [obj] (= (rate-of obj) :ar))
(defn kr? [obj] (= (rate-of obj) :kr))
(defn ir? [obj] (= (rate-of obj) :ir))
(defn dr? [obj] (= (rate-of obj) :dr))

(defmacro defcheck [name params default-message expr]
  (let [message (gensym "message")
        params-with-message (conj params message)]
    `(defn ~name
       (~params
        (fn ~'[rate num-outs inputs spec]
          (when-not ~expr ~default-message)))
       (~params-with-message
        (fn ~'[rate num-outs inputs spec]
          (when-not ~expr ~message))))))

(defcheck same-rate-as-first-input []
  (str (name-of (first inputs)) "must be same rate as called ugen, i.e. " rate)
  (= (rate-of (first inputs)) rate))

(defcheck first-input-ar []
  (str "The first input must be audio rate. Got " (:rate-name (first inputs)))
  (ar? (first inputs)))

(defcheck all-inputs-ar []
  (str "All inputs must be audio rate. Got " (vec (map :rate-name inputs)))
  (every? ar? inputs))

(defcheck first-n-inputs-ar [n]
  (str "The first " n " inputs must be audio rate. Got " (vec (map :rate-name (take n inputs))))
  (every? ar? (take n inputs)))

(defcheck after-n-inputs-rest-ar [n]
  (str "all but the first " n " inputs must be audio rate")
  (every? ar? (drop n inputs)))

(defcheck all-but-first-input-ar []
  "All but the first input must be audio rate"
  (every? ar? (drop 1 inputs)))

(defcheck nth-input-ar [index]
  (str "The input at index " index " should be audio rate" )
  (ar? (nth inputs index)))

(defcheck num-outs-greater-than [n]
  (str "must have " (+ n 1) " or more output channels")
          true)

(defn- mk-check-all
  "Create a check-all fn which will check all the specified check-fns to see if
  they don't return errors (errors are represented as string return vals). If
  they do, concatanate the errors and return them."
  [& check-fns]
  (let [all-checks (apply juxt check-fns)]
    (fn [rate num-outs inputs spec]
      (let [errors (all-checks rate num-outs inputs spec)]
        (when-not (every? nil? errors)
          (apply str errors))))))

(defn- mk-when-rate
  "Create a rate check fn with the specified rate"
  [rate-name]
  (fn [& check-fns]
    (let [check-all-fn (apply mk-check-all check-fns)]
      (fn [rate num-outs inputs spec]
        (when (= rate (RATES rate-name))
          (check-all-fn rate num-outs inputs spec))))))

(defn when-ar
  "Takes a list of check fns and ensures they all pass if the ugen is :ar rate"
  [& check-fns]
  (apply (mk-when-rate :ar) check-fns))

(defn when-kr
  "Takes a list of check fns and ensures they all pass if the ugen is :kr rate"
  [& check-fns]
  (apply (mk-when-rate :kr) check-fns))
