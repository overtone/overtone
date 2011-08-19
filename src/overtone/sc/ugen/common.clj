(ns
  ^{:doc "Code that is common to many ugens.  Includes validation and argument manipulation functions."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.ugen.common)

(defn as-ar [a])

;; checks

(defn rate-of [obj]
  (:rate obj))

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
  "the first input must be audio rate"
  (ar? (first inputs)))

(defcheck all-inputs-ar []
  "all inputs must be audio rate"
  (every? ar? inputs))

(defcheck first-n-inputs-ar [n]
  (str "the first " n " inputs must be audio rate")
  (every? ar? (take n inputs)))

(defcheck after-n-inputs-rest-ar [n]
  (str "all but the first " n " inputs must be audio rate")
  (every? ar? (drop n inputs)))

(defcheck all-but-first-input-ar []
          ""
          true)

(defcheck nth-input-ar [index]
          ""
          true)

(defcheck named-input-ar [name]
          ""
          true)

(defcheck same-rate-as-first-input []
  "the rate must match the rate of the first input"
  (= rate (:rate (first inputs))))

(defcheck num-outs-greater-than [n]
  (str "must have " (+ n 1) " or more output channels")
          true)

(defn check-all [& check-fns]
  (let [all-checks (juxt check-fns)]
    (fn [rate num-outs inputs spec]
      (let [errors (all-checks rate num-outs inputs spec)]
        (when-not (every? nil? errors)
                (apply str errors))))))

(defn when-ar [& check-fns]
  (fn [rate num-outs inputs spec]
    (if (= rate :ar)
      (apply check-all check-fns))))
