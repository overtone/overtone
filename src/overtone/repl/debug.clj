(ns overtone.repl.debug
  (:use [clojure.pprint]
        [overtone.sc.machinery.ugen.defaults]
        [overtone.sc.machinery.ugen.special-ops]
        [overtone.sc.machinery.ugen.specs]
        [overtone.sc.machinery.synthdef]
        [overtone.helpers seq lib]))

(defn- unify-input
  [input ugens constants]
  (if (= -1 (:src input))
    (nth constants (:index input))
    (let [idx (:src input)]
      {:name (:name (nth ugens idx))
       :id idx})))

(defn- unify-ugen-name
  [ugen]
  (let [name (:name ugen)]
    (cond
     (= "BinaryOpUGen" name) (REVERSE-BINARY-OPS (:special ugen))
     (= "UnaryOpUGen" name) (REVERSE-UNARY-OPS (:special ugen))
     :else (overtone-ugen-name name))))

(defn- unify-ugen
  [ugen ugens sdef]
  {:name (unify-ugen-name ugen)
   :rate (REVERSE-RATES (:rate ugen))
   :special (:special ugen)
   :inputs (map #(unify-input % ugens (:constants sdef)) (:inputs ugen))
   :n-outputs (count (:outputs ugen))
   :outputs (:outputs ugen)
   :id (:id ugen)})

(defn- fix-input-refs
  [inputs ugens]
  (map (fn [input]
         (if (number? input)
           input
           {:name (overtone-ugen-name (:name input))
            :id (index-of ugens (find-first #(= (:id %) (:id input)) ugens))}))
       inputs))

(defn- unify-ugens
  [orig-ugens sdef]
  (let [ugens (map (fn [ug idx] (assoc ug :id idx)) orig-ugens (range))
        ugens (map #(unify-ugen % ugens sdef) ugens)
        ugens (map (fn [ug o-ug idx]
                     (let [inputs      (fix-input-refs (:inputs ug) ugens )
                           input-specs (or (:args (get-ugen-spec (:name ug)))
                                           [{:name "a"}
                                            {:name "b"}]) ]
                       (assoc ug
                         :id idx
                         :inputs (loop [res {}
                                        inputs inputs
                                        input-specs input-specs]
                                   (if (empty? input-specs)
                                     res
                                     (if (ugen-sequence-mode? (:mode (first input-specs)))
                                       (let [arg-seq (drop (dec (count input-specs)) inputs)
                                             inputs  (drop-last (count arg-seq) inputs)]
                                         (recur (assoc res (keyword (:name (first input-specs))) (vec arg-seq))
                                                inputs
                                                (rest input-specs)))

                                       (recur (assoc res (keyword (:name (first input-specs))) (first inputs))
                                              (rest inputs)
                                              (rest input-specs))))))))
                   ugens
                   orig-ugens
                   (range))]
    (sort #(compare (:name %1) (:name %2)) ugens)))

(defn- unify-synthdef-sdef
  [sdef]
  {:name    (:name sdef)
   :params  (:params sdef)
   :pnames  (:pnames sdef)
   :n-ugens (count (:ugens sdef))
   :ugens   (unify-ugens (:ugens sdef) sdef)})

(defmulti unify-synthdef
  "Munge synthdef into a readable 'unified' format - ensures that two
  similar synthdefs will be similarly ordered. Does not preserve ugen
  order. Useful for comparing two ugen synthdefs (i.e. an Overtone and
  SCLang synthdef) side-by-side.

  Accepts at least the following synthdef args types:
  * synths
  * synthdefs
  * imported-synthdefs
  * keyword (will look up the synthdef with the name matching keyword
    in the default SuperCollider synthdef directory
  * string (will look up the synthdef with a path matching string)
  * URL (will look up the synthdef with URL)"
  type)

(defmethod unify-synthdef :overtone.sc.machinery.synthdef/synthdef
  [sdef]
  (unify-synthdef-sdef sdef))

(defmethod unify-synthdef :overtone.sc.machinery.synthdef/imported-synthdef
  [sdef]
  (unify-synthdef-sdef sdef))

(defmethod unify-synthdef clojure.lang.Keyword
  [sdef-k]
  (unify-synthdef (synthdef-read sdef-k)))

(defmethod unify-synthdef java.lang.String
  [sdef-s]
  (unify-synthdef (synthdef-read sdef-s)))

(defmethod unify-synthdef java.net.URL
  [sdef-u]
  (unify-synthdef (synthdef-read sdef-u)))

(defmethod unify-synthdef overtone.sc.synth.Synth
  [sdef]
  (unify-synthdef (:sdef sdef)))

(defmethod unify-synthdef :default
  [sdef]
  (throw (IllegalArgumentException. (str "Unknown synthdef type to unify: " (type sdef)))))

(defn pp-unified-synthdef
  "Pretty print a unified version of an sc synth based on the name of
  the scsynth. Looks into the appropriate SC directory for synthdef."
  [synth-name]
  (pprint
   (unify-synthdef
    (overtone.sc.machinery.synthdef/synthdef-read (keyword synth-name)))))

(defn pp-sc-synth
  "Pretty print an unmodified version of an sc synth based on the name
  of the scsynth. Looks into the appropriate SC directory for
  synthdef."
  [synth-name]
  (pprint
   (overtone.sc.machinery.synthdef/synthdef-read (keyword synth-name))))

(defn pp-unified-synth
  "Pretty print a unified version of an Overtone synth."
  [synth]
  (pprint
   (unify-synthdef
    (:sdef synth))))

(defn pp-synth
  "Pretty print an unmodified version of an Overtone synth"
  [synth]
  (pprint
   (:sdef synth)))

(defmacro opp
  "Pretty-print x (or *1 if no argument is passed)"
  ([& args]
     (if (empty? args)
       `(pprint *1)
       `(pprint ~(first args)))))
