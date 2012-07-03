(ns overtone.repl.debug
  (:use [clojure.pprint]
        [overtone.sc.machinery.ugen.defaults]
        [overtone.helpers seq]))

(defn- unify-input
  [input ugens constants]
  (if (= -1 (:src input))
    (nth constants (:index input))
    (let [idx (:src input)]
      {:name (:name (nth ugens idx))
       :id idx})))

(defn- unify-ugen
  [ugen ugens sdef]
  {:name (:name ugen)
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
           {:name (:name input)
            :id (index-of ugens (find-first #(= (:id %) (:id input)) ugens))}))
       inputs))

(defn- unify-ugens
  [orig-ugens sdef]
  (let [ugens (map (fn [ug idx] (assoc ug :id idx)) orig-ugens (range))
        ugens (map #(unify-ugen % ugens sdef) ugens)
        ugens (map (fn [ug o-ug idx]
                     (assoc ug
                       :id idx
                       :inputs (into {}
                                     (map (fn [clean-input input]
                                            [(keyword (:name input))
                                             clean-input])
                                          (fix-input-refs (:inputs ug) ugens)
                                          (-> o-ug :spec :args)))))
                   ugens
                   orig-ugens
                   (range))]
    (sort #(compare (:name %1) (:name %2)) ugens)))

(defn unify-synthdef
  "Munge synthdef into a readable 'unified' format - ensures that two
  similar synthdefs will be similarly ordered. Does not preserve ugen
  order. Useful for comparing two ugen synthdefs (i.e. an Overtone and
  SCLang synthdef) side-by-side."
  [sdef]
  {:name    (:name sdef)
   :params  (:params sdef)
   :pnames  (:pnames sdef)
   :n-ugens (count (:ugens sdef))
   :ugens   (unify-ugens (:ugens sdef) sdef)})

(defn pp-unified-sc-synth
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
