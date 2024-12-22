(ns
 ^{:doc "Atom helper fns"}
 overtone.helpers.atom)

(defn atom-view
  "Create atom-like presentation of a single key in an atom."
  ([a k]
   (reify
     clojure.lang.IDeref
     (deref [_this] (get @a k))

     clojure.lang.IAtom
     (swap [_this ^clojure.lang.IFn f]
       (-> (swap! a update k f)
           (get k)))
     (swap [_this ^clojure.lang.IFn f ^java.lang.Object a1]
       (-> (swap! a update k f a1)
           (get k)))
     (swap [_this ^clojure.lang.IFn f ^java.lang.Object a1 ^java.lang.Object a2]
       (-> (swap! a update k f a1 a2)
           (get k)))
     (swap [_this ^clojure.lang.IFn f ^java.lang.Object a1 ^java.lang.Object a2 ^clojure.lang.ISeq args]
       (-> (swap! a #(apply update % k f a1 a2 args))
           (get k)))
     (^boolean compareAndSet
       [_this ^java.lang.Object oldval ^java.lang.Object newval]
       (loop []
         (let [old @a
               curval (get old k)]
           (cond
             (not= curval oldval)
             false
             (compare-and-set! a old (assoc old k newval))
             true
             :else
             (recur)))))
     (reset [_this ^java.lang.Object newval]
       (-> (swap! a assoc k newval)
           (get k)))))
  ([a k v]
   (doto (atom-view a k)
     (reset! v))))

(comment
  (do (def a (atom {:key 1}))
      (def b (atom-view a :key 2))
      [(reset! b 2)
       (swap! b + 1)
       (swap! b + 1 1)
       (swap! b + 1 1 1 1)
       @b
       @a])
  )
