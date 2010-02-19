(ns overtone.graph.base
  (:import (org.hypergraphdb HyperGraph HGEnvironment HGConfiguration
                             HGValueLink HGPersistentHandle HGHandleFactory)
     (org.hypergraphdb.query HGQueryCondition)
     (org.hypergraphdb HGQuery$hg)))

(defn graph 
  "Open a graph-db located at the given path.  Creates a new DB if none
  exists.  The DB optionally has transactions enabled."
  [path & transactional?]
  (let [tx? (or transactional? false)
        config (HGConfiguration.)]
    (.setTransactional config tx?)
    (HGEnvironment/get path config)))

(defn link 
  "Create a 'hyper-link' between a set of nodes."
  [label & nodes]
  (let [edge (HGValueLink. label (into-array nodes))]
    edge))

(defn handle-bytes 
  "Serialize an atom handle so it can be retrieved later."
  [g lnk]
  (.toByteArray (.getPersistentHandle g lnk)))

(defn bytes-handle 
  "Returns an atom handle object from a serialized atom handle byte array."
  [bytes]
  (HGHandleFactory/makeHandle bytes))

(defmacro hg [meth & args]
  (let [method (symbol (str "HGQuery$hg/" meth))]
  `(~method ~@args)))

(defn type-query
  "Simple query that iterates over a single atom type applying the given conditions."
  [g a-type val & a-cond]
  (seq (hg getAll g 
           (hg and (into-array HGQueryCondition 
                               [(hg type a-type) (hg eq val)])))))

;  (let [a-cond (or a-cond :eq)
;        cnd (get CMP-OPS a-cond)]
;    (map #(.get g %) 
;         (iterator-seq (.find g (TypedValueCondition. a-type val cnd))))))

