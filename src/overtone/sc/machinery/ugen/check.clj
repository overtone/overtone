(ns
  ^{:doc "UGen argument validation functions."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.machinery.ugen.check
  (:use [overtone.helpers.math :only [power-of-two?]]
        [overtone.sc.machinery.ugen defaults sc-ugen]
        [overtone.helpers.lib :only [overtone-ugen-name]]))

(defn rate-name= [obj rate]
  (= (:rate-name obj) rate))

(defn rate= [ug rate-int]
  (= (:rate ug) rate-int))

(defn name-of [obj]
  (if (:name obj)
    (overtone-ugen-name (:name obj))
    (with-out-str (pr obj))))

(defn name-of? [obj name]
  (= (name-of obj) name))

(defn- input-stream?
  [ug]
  (or (= :kr (:rate-name ug))
      (= :ar (:rate-name ug))))

(defn- buffer?
  "Determines whether the specified object is a buffer. Copying fn
  here to avoid cyclic dependencies."
  [buf]
  (isa? (type buf) :overtone.sc.buffer/buffer))

(defn- local-buffer?
  [buf]
  (and (sc-ugen? buf)
       (= "LocalBuf" (:name buf))))

(defn- index?
  [ug]
  (and (sc-ugen? ug)
       (= "Index" (:name ug))))

(defn- buffer-like?
  [buf]
  (or
   (buffer? buf)
   (local-buffer? buf)
   (index? buf)
   (number? buf)
   (control-proxy? buf)
   (output-proxy? buf)))

(defn ar? [obj] (= (:rate-name obj) :ar))
(defn kr? [obj] (= (:rate-name obj) :kr))
(defn ir? [obj] (or (number? obj) (= (:rate-name obj) :ir)))
(defn dr? [obj] (= (:rate-name obj) :dr))

(defmacro defcheck [name params default-message & exprs]
  (let [message (gensym "message")
        params-with-message (conj params message)]
    `(defn ~name
       (~params
        (fn ~'[rate num-outs inputs ugen spec]
          (when-not (do ~@exprs) (str ~default-message))))
       (~params-with-message
        (fn ~'[rate num-outs inputs ugen spec]
          (when-not (do ~@exprs) (str ~message " -- " ~default-message)))))))

(defcheck same-rate-as-first-input []
  (str "Rate mismatch: "
       (name-of (first inputs))
       " is at rate "
       (:rate-name (first inputs))
       " yet the containing ugen is at "
       (REVERSE-RATES rate))
  (= (:rate (first inputs)) rate))

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
  (str "All but the first " n " inputs must be audio rate")
  (every? ar? (drop n inputs)))

(defcheck all-but-first-input-ar []
  "All but the first input must be audio rate. Got " (vec inputs)
  (every? ar? (drop 1 inputs)))

(defcheck nth-input-ar [index]
  (str "The input at index " index " should be audio rate.")
  (ar? (nth inputs index)))

(defcheck num-outs-greater-than [n]
  (str "Must have " (+ n 1) " or more output channels")
  true)

(defcheck nth-input-number? [n]
  (str "Input with index " n " must be a number, control proxy or an ir ugen")
  (let [val (nth inputs n)]
    (or
     (control-proxy? val)
     (number? val)
     (= :ir (:rate-name val)))))

(defcheck nth-input-buffer? [n]
  (str "Input with index " n " must be a buffer. i.e. a buffer, local-buf or a number. Got:"  (nth inputs n))
  (let [val (nth inputs n)]
    (buffer-like? val)))

(defcheck nth-input-buffer-pow2? [n]
  (str "Input with index " n " must be a buffer with size which is a power of 2 an id or a control-proxy.")
  (let [buf (nth inputs n)]
    (or (or (and (buffer? buf)
                 (power-of-two? (:size buf)))
            (and (local-buffer? buf)
                 (power-of-two? (first (:args buf)))))
        (number? buf)
        (control-proxy? buf))))

(defcheck nth-input-power-of-2-or-zero? [n]
  (str "Input with index " n " must be a number which is either 0 or a power of 2.")
  (let [val (nth inputs n)]
    (or (zero? val)
        (power-of-two? val))))

(defcheck nth-input-power-of-2? [n]
  (str "Input with index " n " must be a number which is a power of 2.")
  (let [val (nth inputs n)]
    (power-of-two? val)))

(defcheck nth-input-stream? [n]
  (str "Input with index " n " must be an input stream i.e. a ugen at :kr or :ar")
  (let [val (nth inputs n)]
    (input-stream? val)))

(defcheck arg-is-sequential? [k]
  (str "Argument with key " k " must be a sequence.")
  (let [merged-args (:arg-map ugen)]
    (sequential? (get merged-args k))))

(defcheck arg-is-demand-ugen-or-list-of-demand-ugens? [k]
  (str "Argument with key " k " must either be a demand rate ugen or a list of ugens all at demand rate")
  (let [merged-args  (:arg-map ugen)
        demand-ugens (get merged-args k)]
    (if (sequential? demand-ugens)
      (every? #(= :dr (:rate-name %)) demand-ugens)
      (= :dr (:rate-name demand-ugens)))))

(defcheck arg-is-demand-ugen? [k]
  (str "Argument with key " k " must be a demand rate ugen")
  (let [merged-args (:arg-map ugen)
        demand-ugen (get merged-args k)]
    (= :dr (:rate-name demand-ugen))))

(defn- mk-check-all
  "Create a check-all fn which will check all the specified check-fns to see if
  they don't return errors (errors are represented as string return vals). If
  they do, concatanate the errors and return them."
  [& check-fns]
  (let [all-checks (apply juxt check-fns)]
    (fn [rate num-outs inputs ugen spec]
      (let [errors (all-checks rate num-outs inputs ugen spec)]
        (when-not (every? nil? errors)
          (apply str errors))))))

(defn- mk-when-rate
  "Create a rate check fn with the specified rate"
  [rate-name]
  (fn [& check-fns]
    (let [check-all-fn (apply mk-check-all check-fns)]
      (fn [rate num-outs inputs ugen spec]
        (when (= rate (RATES rate-name))
          (check-all-fn rate num-outs inputs ugen spec))))))

(defn when-ar
  "Takes a list of check fns and ensures they all pass if the ugen is :ar rate"
  [& check-fns]
  (apply (mk-when-rate :ar) check-fns))

(defn when-kr
  "Takes a list of check fns and ensures they all pass if the ugen is :kr rate"
  [& check-fns]
  (apply (mk-when-rate :kr) check-fns))
