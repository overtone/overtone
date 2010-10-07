(ns overtone.console.viz
  (:use vijual
        [overtone.core sc]))

; Note: If we really want to render other node types this should be a multimethod.
(defn- vijual-node
  [node]
  (cond
   (contains? node :group)     (str "Group " (node :group))
   (contains? node :synth)     (str "Synth " (node :id) " " (node :synth))
   (contains? node :synth-set) (str (count (node :ids)) " Synths " (node :synth-set))
   (true) (throw (Exception. "Please implement a vijual node renderer for this node type"))))

(defn- vijual-synths
  [node-list]
  (let [synth-matcher    #(% :synth)
        synths           (filter synth-matcher node-list)
        non-synths       (filter (complement synth-matcher) node-list)
        compact          #(remove nil? %)
        compacted-synths (reduce (fn [sum node] (update-in sum [(node :synth) :ids]
                                                           #(-> (conj [(node :id)] %) flatten compact)))
                                 {} synths)
        synth-sets       (into [] (map (fn [[k v]] (if (< 1 (count (v :ids)))
                                                     {:synth-set k :ids (v :ids)}
                                                     {:synth k :id (first (v :ids))}))
                                       compacted-synths))]
       (into non-synths synth-sets)))

(defn- prepare-tree-for-vijual
  [tree]
  (let [node     (vijual-node (dissoc tree :children))
        children (tree :children)]
    (if (pos? (count children))
      (apply conj [node]
             (for [i (vijual-synths children)]
               (prepare-tree-for-vijual i)))
      [node])))

(defn print-node-tree
  "Pretty print the tree of live synthesizer instances.  Takes the same args as (node-tree)."
  [& args]
  (draw-tree [(prepare-tree-for-vijual (apply node-tree args))]))

(defmethod clojure.core/print-method :overtone.core.sc/node-tree [tree writer]
  (print-method (draw-tree [(prepare-tree-for-vijual tree)]) writer))
