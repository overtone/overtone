(ns overtone.studio.pattern
  "Inspired by the SuperCollider pattern libary

  In our case patterns are just Clojure sequences, finite or infinite."
  (:require
   [overtone.algo.chance :as chance]))

(defn- pbind*
  "Internal helper for pbind, complicated by the fact that we want to cycle all
  seqs until we've fully consumed the longest seq, but we can't count because
  seqs could be infinite. So we track for which seqs we've reached the end at
  least once (done set)."
  [ks specs seqs done]
  (when-not (= (count done) (count ks))
    (let [vs (map (fn [v]
                    (if (sequential? v)
                      (first v)
                      v))
                  seqs)]
      (cons
       (zipmap ks vs)
       (lazy-seq
        (pbind*
         ks
         specs
         (map (fn [v spec]
                (if (sequential? v)
                  (let [n (next v)]
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
                                         (not (next s)))
                                k))
                            ks
                            seqs)))))))))

(defn pbind
  "Takes a map, with some of the map values seqs. Returns a sequence of maps, with
  each successive map value constructed by taking the next value of each
  sequence. Sequences wrap (cycle) until the longest sequence has been consumed.
  Non-sequential values are retained as-is.

  Similar to SuperCollider's `PBind`, part of the Pattern library. "
  [m]
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
    (pbind*
     ks
     specs
     seqs
     done)))

(defn pwhite
  ([min max]
   (repeatedly #(chance/rrand min max)))
  ([min max repeats]
   (repeatedly repeats #(chance/rrand min max))))
