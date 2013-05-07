(ns overtone.repl.debug
  (:use [clojure.pprint]
        [overtone.sc.machinery.ugen.defaults]
        [overtone.sc.machinery.ugen.special-ops]
        [overtone.sc.machinery.ugen.specs]
        [overtone.sc.machinery.synthdef]
        [overtone.helpers seq lib]))

(defn- control-ugen-name?
  [ug-n]
  (or (= "trig-control" ug-n)
      (= "audio-control" ug-n)
      (= "control" ug-n)))

(defn- control-ugen?
  [ug]
  (control-ugen-name? (:name ug)))

(defn- mk-control-id
  [ug-id output-idx]
  (str "control_" ug-id "_" output-idx))

(defn- unify-input
  [input ugens constants]
  (if (= -1 (:src input))
    (nth constants (:index input))
    (let [idx (:src input)
          n (:name (nth ugens idx))
          id (if (control-ugen-name? (overtone-ugen-name n))
               (mk-control-id idx (:index input))
               idx)]
      {:name n
       :id id})))

(defn- unify-ugen-name
  [ugen]
  (let [name (:name ugen)]
    (cond
     (= "BinaryOpUGen" name) (REVERSE-BINARY-OPS (:special ugen))
     (= "UnaryOpUGen" name) (REVERSE-UNARY-OPS (:special ugen))
     :else (overtone-ugen-name name))))

(defn- unify-ugen
  [ugen ugens sdef]
  {:name      (unify-ugen-name ugen)
   :rate      (REVERSE-RATES (:rate ugen))
   :special   (:special ugen)
   :inputs    (map #(unify-input % ugens (:constants sdef)) (:inputs ugen))
   :n-outputs (count (:outputs ugen))
   :outputs   (:outputs ugen)
   :id        (:id ugen)})

(defn- fix-input-refs
  "Resolve input references to inline their name if another ugen or the
   value if a float."
  [inputs ugens]
  (map (fn [input]
         (if (number? input)
           input
           (let [n (overtone-ugen-name (:name input))]
             {:name n
              :id (index-of ugens (find-first #(= (:id %) (:id input)) ugens))})))
       inputs))

(defn- unify-ugen-inputs
  [ugens]
  (map (fn [ug idx]
         (let [inputs      (fix-input-refs (:inputs ug) ugens)
               input-specs (or (:args (get-ugen-spec (:name ug)))
                               [{:name "a"}
                                {:name "b"}])]
           (assoc ug
             :id idx
             :inputs (loop [res         {}
                            inputs      inputs
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
       (range)))

(defn- find-control-offset
  "Calculate the output offset of the specified control ugen in the list
   of ugens. The first control ugen has an offset of 0, the second has
   an offest equalling the number of outputs of the first control ugen
   and the third equalling the summation of the number of outputs of the
   first and second control ugens, etc."
  [c-name sdef]
  (let [ctl-ugs (filter (fn [ug]
                          (control-ugen-name? (overtone-ugen-name (:name ug))))
                        (:ugens sdef))]
    (loop [res 0
           ugs ctl-ugs]
      (when (empty? ugs)
        (throw (Exception. (str "Couldn't find ugen with name " c-name
                                " when calculating control offset val.") )))
      (let [ug (first ugs)]
        (if (= c-name (overtone-ugen-name (:name ug)) )
          res
          (recur (+ res (:n-outputs ug)) (rest ugs)))))))

(defn- expand-control-ug
  [ug c-idx sdef]
  (reduce (fn [res idx]
            (conj res (assoc ug
                        :inputs {}
                        :orig-id c-idx
                        :control-param (nth (:unified-params sdef) (+ (find-control-offset (:name ug) sdef)))
                        :default 1
                        :id (mk-control-id (:id ug) idx)
                        :outputs (nth (:outputs ug) idx))))
          []
          (range (:n-outputs ug))))


(defn expand-control-ugs
  "expands control ugens to a number of psuedo ugens which are easier to
 understand"
  [control-ugs sdef]
  (mapcat (fn [ug idx]  (expand-control-ug ug idx sdef)) control-ugs (range)))

(defn- unify-control-ugens
  [ugens sdef]
  (let [control-ugs          (filter control-ugen? ugens)
        std-ugs              (remove control-ugen? ugens)
        expanded-control-ugs (expand-control-ugs control-ugs sdef)]
    (concat expanded-control-ugs std-ugs)
))

(defn- unify-ugens
  [orig-ugens sdef]
  (let [ugens (map (fn [ug idx] (assoc ug :id idx)) orig-ugens (range))
        ugens (map #(unify-ugen % ugens sdef) ugens)
        ugens (unify-control-ugens ugens sdef)
        ugens (unify-ugen-inputs ugens)
        ]

    (sort #(compare (:name %1) (:name %2)) ugens)))

(defn- unify-params
  [sdef]
  (map (fn [pname]
         (-> pname
             (assoc :default (nth (:params sdef) (:index pname)))
             (dissoc :index)))
       (:pnames sdef)))

(defn- unify-synthdef-sdef
  [sdef]
  (with-meta
    (let [unified-params (unify-params sdef) ]
      {:name    (:name sdef)
       :params  unified-params
       :n-ugens (count (:ugens sdef))
       :ugens   (unify-ugens (:ugens sdef) (assoc sdef :unified-params unified-params))})
    {:type ::unified-synthdef}))

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

(defmethod unify-synthdef overtone.studio.inst.Inst
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
