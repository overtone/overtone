(ns overtone.console.viz
  (:use vijual
        [overtone.sc core]
        [overtone.studio core]))

(defn- check-inst-group
  "Replaces a string 'Group <N>' where N is the group number of an instrument
  with the instrument name, otherwise just returns the txt unchanged."
  [txt]
  (if-let [ins (first (filter #(= txt (str "Group " (:group %)))
                              (vals @instruments*)))]
    (:name ins)
    txt))

(defn- group-alias
  "Replace generic node-tree labels with nicer aliases."
  [txt]
  (if (string? txt)
    (cond
      (= "Group 0" txt) "Root"
      (= (str "Group " @inst-group*) txt) "Instruments"
      (= (str "Group " @synth-group*) txt) "Synthesizers"
      :else (check-inst-group txt))
    txt))

; Note: If we really want to render other node types this should be a multimethod.
(defn- vijual-node
  [node]
  (cond
   (contains? node :group)     (group-alias (str "Group " (node :group)))
   (contains? node :synth)     (str "ids: " (node :id))
   (contains? node :synth-set) (apply str "ids: " (interpose ", " (node :ids)))
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

(defn vijual-tree
  [tree]
  (let [node     (vijual-node (dissoc tree :children))
        children (tree :children)]
    (if (pos? (count children))
      (apply conj [node]
             (for [i (vijual-synths children)]
               (vijual-tree i)))
      [node])))

(defn print-node-tree
  "Pretty print the tree of live synthesizer instances.  Takes the same args as (node-tree)."
  [& args]
  (draw-tree [(vijual-tree (apply node-tree args))]))

(defmethod clojure.core/print-method :overtone.core.sc/node-tree [tree writer]
  (print-method (draw-tree [(vijual-tree tree)]) writer))
