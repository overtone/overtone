(ns overtone.console.viz
  (:use vijual
        [overtone.sc core node]
        [overtone.studio core]))

;;TODO: figure out if this is necessary
(defn- check-inst-group
  "Replaces a string 'Group <N>' where N is the group number of an instrument
  with the instrument name, otherwise returns nil"
  [id]
  (if-let [ins (first (filter #(= id (:group %))
                              (vals @instruments*)))]
    (:name ins)))

(defn- group-label
  "Returns a string label for groups."
  [id]
  (str
    (cond
      (= 0 id) "root: "
      (= @inst-group* id) "insts: "
      (= @synth-group* id) "synths: "
      (= @mixer-group* id) "mixer: "
      (= @record-group* id) "recording: "
      :else (str (check-inst-group id) " group: "))
    id))

; Note: If we really want to render other node types this should be a multimethod.
(defn- vijual-node
  [node]
  (cond
   (= :group (:type node))     (group-label (:id node))
   (= :synth (:type node))     (str "synth: " (:id node))
   (contains? node :synth-set) (apply str "synths: " (interpose ", " (:ids node)))
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
