(ns overtone.repl.debug
  (:use
   clojure.pprint
   overtone.sc.machinery.ugen.defaults
   overtone.sc.machinery.ugen.special-ops
   overtone.sc.machinery.ugen.specs
   overtone.sc.machinery.synthdef
   overtone.helpers.seq
   overtone.helpers.lib
   overtone.studio.inst))

(set! *warn-on-reflection* true)

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
                         (let [in-spec (first input-specs )]
                           (cond
                            (or (ugen-sequence-mode? (:mode in-spec))
                                (= true (:array in-spec)))
                            (let [arg-seq (drop (dec (count input-specs)) inputs)
                                  inputs  (drop-last (count arg-seq) inputs)]
                              (recur (assoc res (keyword (:name in-spec)) (vec arg-seq))
                                     inputs
                                     (rest input-specs)))

                            (= :num-outs (:mode in-spec))
                            (recur (assoc res (keyword (:name in-spec)) (:n-outputs ug))
                                   inputs
                                   (rest input-specs))

                            :else
                            (recur (assoc res (keyword (:name in-spec)) (first inputs))
                                   (rest inputs)
                                   (rest input-specs)))))))))
       ugens
       (range)))

(defn- find-control-offset
  "Calculate the output offset of the specified control ugen in the list
   of ugens. The first control ugen has an offset of 0, the second has
   an offest equalling the number of outputs of the first control ugen
   and the third equalling the summation of the number of outputs of the
   first and second control ugens, etc."
  [c-name rate sdef]
  (let [ctl-ugs (filter (fn [ug]
                          (control-ugen-name? (overtone-ugen-name (:name ug))))
                        (:ugens sdef))]
    (loop [result 0
           ugs ctl-ugs]
      (when (empty? ugs)
        (throw (Exception. (str "Couldn't find ugen with name " c-name
                                " when calculating control offset val.") )))
      (let [ug (first ugs)]
        (if (and (= c-name (overtone-ugen-name (:name ug)) )
                 (= rate (REVERSE-RATES (:rate ug))))
          result
          (recur (+ result (long (:n-outputs ug))) (rest ugs)))))))

(defn- expand-control-ug
  [ug c-idx sdef]
  (reduce (fn [res idx]
            (let [cp (assoc (nth (:unified-params sdef) (+ (find-control-offset (:name ug) (:rate ug) sdef) idx)) :rate (:rate ug))]
              (conj res (assoc ug
                               :inputs {}
                               :orig-id c-idx
                               :control-param cp
                               :default (:default cp)
                               :id (mk-control-id (:id ug) idx)
                               :outputs (nth (:outputs ug) idx)))))
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

(defn- unify-sdef
  [sdef]
  (with-meta
    (let [unified-params (unify-params sdef) ]
      {:name    (:name sdef)
       :params  unified-params
       :n-ugens (count (:ugens sdef))
       :ugens   (unify-ugens (:ugens sdef) (assoc sdef :unified-params unified-params))})
    {:type ::unified-synthdef}))

(defmulti sdef
  "Extract the synthesis definition (sdef) from the argument using the
  appropriate means

  Accepts at least the following synthdef args types:
  * synths
  * synthdefs
  * imported-synthdefs
  * keyword (will look up the synthdef with the name matching keyword
    in the default SuperCollider synthdef directory
  * string (will look up the synthdef with a path matching string)
  * URL (will look up the synthdef with URL)"
  type)

(defmethod sdef :overtone.sc.machinery.synthdef/synthdef
  [s]
  s)

(defmethod sdef :overtone.sc.machinery.synthdef/imported-synthdef
  [s]
  s)

(defmethod sdef clojure.lang.Keyword
  [s-k]
  (sdef (synthdef-read s-k)))

(defmethod sdef java.lang.String
  [s-s]
  (sdef (synthdef-read s-s)))

(defmethod sdef java.net.URL
  [s-u]
  (sdef (synthdef-read s-u)))

(defmethod sdef overtone.sc.synth.Synth
  [s]
  (sdef (:sdef s)))

(defmethod sdef overtone.studio.inst.Inst
  [s]
  (sdef (:sdef s)))

(defmethod sdef :default
  [sdef]
  (throw (IllegalArgumentException. (str "Unable to convert to sdef: " (type sdef)))))

(defn unified-sdef
  [s]
  "Munge synthdef into a readable 'unified' format - ensures that two
  similar synthdefs will be similarly ordered. Does not preserve ugen
  order. Useful for comparing two ugen synthdefs (i.e. an Overtone and
  SCLang synthdef) side-by-side."
  (unify-sdef (sdef s)))

(defn pp-unified-sdef
  "Pretty print a unified version of an overtone or sc synth. See
   sdef for arg options"
  [sdef]
  (pprint (unified-sdef sdef)))

(defn pp-sdef
  "Pretty print an unmodified version of an sc synth based on the name
  of the scsynth. Looks into the appropriate SC directory for
  synthdef."
  [s]
  (pprint (sdef s)))

(defmacro opp
  "Pretty-print x (or *1 if no argument is passed)"
  ([& args]
     (if (empty? args)
       `(pprint *1)
       `(pprint ~(first args)))))
