
(ns overtone.core.ugens
  (:require [overtone.core.ugens.common :only check-valid-inputs]))


(synth [foo:ar ])

(defn specs-from [namespaces]
  (let [full-nss (map #(symbol (str "overtone.core.ugens." %) namespaces))]
    (map (fn [ns]
           (require [ns :only 'specs])
           ()))))

(def specs (specs-from '[basicops osc line filter beq-suite io
                         buf-io trig noise chaos misc]))

(defn- normalize-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

(defn clj-ugen-name [sc-name rate]
  (let [name (normalize-name sc-name)]
    (symbol (if (rate #{:ar :kr})
              (str name rate)
              name))))

(defn add-to-synth-context! [ugen-proxy]
  (println "added: " ugen-proxy))

(defn property [spec key]
  (or (spec key)
      ({:rates #{:ar :kr}
        :fixed-outs 1
        :out-type :fixed
        :signal-range :bipolar} key)))

(declare muladd)
(defn define-ugen [{sc-name :name
                    rate :rate
                    argspec :args
                    has-muladd :muladd :as ugen-spec}]
  (let [args (if has-muladd
               (concat argspec [{:name "mul" :default 1.0}
                                {:name "add" :default 0.0}])
               argspec)
        fn-name (clj-ugen-name sc-name rate)
        arity (count args)
        paramvec (vec (map #(symbol (:name %)) args))
        default-args (map :default (filter :default args))
        base-body (if has-muladd
                    `(~paramvec
                      (let [args# (map vector '~paramvec ~paramvec)
                            args# (split-at (- ~arity 2) args#)
                            [base-args# [[_# mul#] [_# add#]]] args#
                            ugen-proxy# {:sc-name ~sc-name
                                         :fn-name '~fn-name
                                         :rate ~rate
                                         :args base-args#}]
                              (add-to-synth-context! ugen-proxy#)
                              (muladd ugen-proxy# mul# add#)))
                    `(~paramvec
                      (let [args# (map vector '~paramvec ~paramvec)
                            ugen-proxy# {:sc-name ~sc-name
                                         :fn-name '~fn-name
                                         :rate ~rate
                                         :args args#}]
                        (add-to-synth-context! ugen-proxy#)
                        ugen-proxy#)))
        default-arg-bodies ()
        body (cons base-body default-arg-bodies)
        ugen-func (eval `(defn ~fn-name ~@body))
        expansion-spec (expansion-spec-for)]
    ugen-func))

; (let [p true x (gensym) y (gensym)] `(let [~x 7 ~y 8] ~(if p x y)))

(defn define-ugens [specs]
  (doseq [sub-spec (apply concat
                          (map (fn [spec]
                                 (map #(assoc spec :rate %)
                                      (spec :rates)))
                               specs))]
    (define-ugen sub-spec)))

;; TODO where/when should this be defined/declared?

(defn as-ar [obj]
  (unless (ar? obj)
          (k2a obj)))

(define-ugens (take 20 all-ugen-specs))

(defmacro named-synth [name params & exprs]
  (binding [*synth-context* []]
    (let [letvec ] ; [foo <control-proxy default: x> bar <control-p ...]
      (eval `(let ~letvec ~@exprs)))
    (let [])))

(defmacro synth [params & exprs]
  (let [name (gensym "overtone_")]
    `(named-synth ~name ~params ~@exprs)))

(defmacro defsynth [name params & exprs]
  `(def ~name (named-synth ~name ~params ~@exprs)))

;; (defn parallel-seqs [& seqs]
;;  "takes n seqs and returns a seq of vectors of length n, lazily
;;   (take 4 (parallel-seqs (repeat 5) (cycle [1 2 3]))) ->
;;     ([5 1] [5 2] [5 3] [5 1])"
;;  (apply map vector seqs))

;; (defmacro defugen [name rate args nouts out-type]
;;   (let [body '([x] x)]
;;     `(defn ~name ~body)))

;; (defmacro foo [name val]
;;   `(def ~name ~val))

;; (defn define-ugens-from-spec [{:keys [base-name args rates nouts out-type]}]
;;   (let [rates (map rates-map rates)
;;         names (map #(ugen-name base-name %) rates)]
;;     (doseq [[name rate] (parallel-seqs names rates)] 
;;  )))

;; (defugen 'foo 1 2 3 4)
;; (foo 77 9)


;(doseq (map define-ugens-from-spec true-ugen-specs))

;(println (take 10 (map ugen-spec ugen-specs)))
;(println  (reduce conj #{} (map keys ugen-specs)))
;(println  (reduce conj #{} (map :fixed-outs ugen-specs)))
;(println  (reduce conj #{} (map :out-type ugen-specs)))
;(println  (reduce conj #{} (map :rates ugen-specs)))
