(ns overtone.studio.pattern
  "Inspired by the SuperCollider pattern libary

  In our case patterns are just Clojure sequences, finite or infinite."
  (:require
   [overtone.algo.chance :as chance]))

(defn pfirst
  "Like first, but recursively descends into into sequences."
  [s]
  (let [v (first s)]
    (if (sequential? v)
      (recur v)
      v)))

(defn pnext
  "Like next, but lazily flattens nested sequences."
  [s]
  (let [[x & xs] s]
    (if (sequential? x)
      (recur
       (if (seq x)
         (concat x xs)
         xs))
      xs)))

(defn- pbind*
  "Internal helper for pbind, complicated by the fact that we want to cycle all
  seqs until we've fully consumed the longest seq, but we can't count because
  seqs could be infinite. So we track for which seqs we've reached the end at
  least once (done set)."
  [ks specs seqs done]
  (when-not (= (count done) (count ks))
    (let [vs (map #(if (sequential? %) (pfirst %) %) seqs)]
      (cons
       (zipmap ks vs)
       (lazy-seq
        (pbind*
         ks
         specs
         (map (fn [v spec]
                (if (sequential? v)
                  (let [n (pnext v)]
                    (if (nil? n)
                      spec
                      n))
                  v))
              seqs
              specs)
         (into done
               (remove nil?
                       (map (fn [k s]
                              (when (and (sequential? s)
                                         (not (pnext s)))
                                k))
                            ks
                            seqs)))))))))

(defn pbind
  "Takes a map, with some of the map values seqs. Returns a sequence of maps, with
  each successive map value constructed by taking the next value of each
  sequence. Sequences wrap (cycle) until the longest sequence has been consumed.
  Non-sequential values are retained as-is.

  Similar to SuperCollider's `PBind`, part of the Pattern library. "
  ([m repeat]
   (cond
     (= 0 repeat)
     nil
     (= Float/POSITIVE_INFINITY repeat)
     (concat (pbind m) (pbind m repeat))
     :else
     (concat (pbind m) (pbind m (dec repeat)))))
  ([m]
   (let [ks (keys m)
         specs (map m ks)
         seqs (map (fn [v]
                     (if (sequential? v)
                       (seq v)
                       v))
                   specs)
         done (set (remove nil?
                           (map (fn [k s]
                                  (when (not (sequential? s))
                                    k))
                                ks
                                seqs)))]
     (if  (= (count done) (count ks))
       [m]
       (pbind*
        ks
        specs
        seqs
        done)))))

(defn pwhite
  ([min max]
   (repeatedly #(chance/rrand min max)))
  ([min max repeats]
   (repeatedly repeats #(chance/rrand min max))))
