;    Initially from Christophe Grand's code.  Need to figure out where to put
;    this kind of thing in the source tree, and how licensing works.  For now
;    it's in the overtone namespace, but it should probably be under Grand's.

;   Copyright (c) Christophe Grand, 2009. All rights reserved.
 
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this 
;   distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
 
(ns overtone.insertion-point
 "Provides an insertion-point based on zippers."
  (:refer-clojure :exclude [next])
  (:require [clojure.zip :as z]))
 
;;; core functions
;; constructor  
(defn insertion-point
 "Returns an insertion point around the given loc, or nil.
  bias can take one of these 4 values:
  :right or :left when loc is not at the root,
  :append or :prepend when loc is at a branch."
 [loc bias]
  (condp = bias
    :right (when-let [r (or (z/right loc) (z/up loc))] [loc r])
    :left  (when-let [l (or (z/left loc) (z/up loc))] [l loc])
    :append [(or (when-let [d (z/down loc)] (z/rightmost d)) loc) loc]
    :prepend [loc (or (z/down loc) loc)]))
 
;; accessors
(defn right-loc
 "Returns the loc to the right of the insertion-point or nil." 
 [[l r]]
  (when-not (or (= (z/up l) r) (= l r)) r))
  
(defn left-loc
 "Returns the loc to the left of the insertion-point or nil." 
 [[l r]]
  (when-not (or (= l (z/up r)) (= l r)) l))
 
(defn up-loc 
 "Returns the parent loc of the insertion-point." 
 [[_ r :as ip]]
  (if-let [r (right-loc ip)] (z/up r) r)) 
 
 
;;; other functions
(defn right
 "Returns the insertion-point to the right of the right loc, or nil."
 [ip]
  (when-let [r (right-loc ip)]
    (insertion-point r :right)))
 
(defn left 
 "Returns the insertion-point to the left of the left loc, or nil."
 [ip]
  (when-let [l (left-loc ip)]
    (insertion-point l :left)))
 
(defn down-right 
 "Returns the insertion-point that prepends to the right loc, or nil."
 [ip]
  (when-let [r (right-loc ip)]
    (insertion-point r :prepend))) 
 
(defn down-left 
 "Returns the insertion-point that appends to the left loc, or nil."
 [ip]
  (when-let [l (left-loc ip)]
    (insertion-point l :append)))
 
(defn up-right 
 "Returns the insertion-point to the right of the parent loc, or nil."
 [ip]
  (-> ip up-loc (insertion-point :right)))  
 
(defn up-left 
 "Returns the insertion-point to the left of the parent loc, or nil."
 [ip]
  (-> ip up-loc (insertion-point :left)))  
 
(defn next
 "Returns the next insertion-point to the right (depth-first), or nil."
 [ip]
  (if-let [r (right-loc ip)]
    (insertion-point r (if (z/branch? r) :prepend :right))
    (up-right ip)))
 
(defn prev 
 "Returns the previous insertion-point to the left (depth-first), or nil."
 [ip]
  (if-let [l (left-loc ip)]
    (insertion-point l (if (z/branch? l) :append :left))
    (up-left ip)))
 
(defn insert-left 
 "Inserts to the left of the insertion-point, without moving."
 [ip item]
  (if-let [r (right-loc ip)]
    (-> r (z/insert-left item) (insertion-point :left))
    (-> ip up-loc (z/append-child item) (insertion-point :append))))
 
(defn insert-right 
 "Inserts to the right of the insertion-point, without moving."
 [ip item]
  (if-let [l (left-loc ip)]
    (-> l (z/insert-right item) (insertion-point :right))
    (-> ip up-loc (z/insert-child item) (insertion-point :prepend))))
 
(defn- remove-loc [l]
  (let [lefts (z/lefts l)
        rights (z/rights l)
        u (z/up l)
        n (z/make-node u (z/node u) (concat lefts rights))]
    (loop [ip (-> u (z/replace n) (insertion-point :prepend)) s (seq lefts)] 
      (if s 
        (recur (right ip) (clojure.core/next s)) 
        ip))))
 
(defn remove-left 
 "Removes the item to the left of the insertion-point, if any."
 [ip]
  (if-let [l (left-loc ip)] (remove-loc l) ip))
 
(defn remove-right 
 "Removes the item to the right of the insertion-point, if any."
 [ip]
  (if-let [r (right-loc ip)] (remove-loc r) ip))
 
(comment
  (defn show-ip [ip] (-> ip (insert-left '*) first z/root))
  (def e (-> [] z/vector-zip (insertion-point :append)))
  (-> e show-ip) ; [*]
  (-> e (insert-left 1) show-ip) ; [1 *]
  (-> e (insert-left 1) (insert-right 2) show-ip) ; [1 * 2]
  (-> e (insert-left 1) (insert-right 2) left show-ip) ; [* 1 2]
  (-> e (insert-left [1 2]) show-ip) ; [[1 2] *]
  (-> e (insert-left [1 2]) left show-ip) ; [* [1 2]]
  (-> e (insert-left [1 2]) left right show-ip) ; [[1 2] *]
  (-> e (insert-left [1 2]) left next show-ip) ; [[* 1 2]]
  (-> e (insert-left [1 2]) left next next show-ip) ; [[1 * 2]]
  (-> e (insert-left [1 2]) left next next next show-ip) ; [[1 2 *]]
  (-> e (insert-left [1 2]) left next next next next show-ip) ; [[1 2] *]
  (-> e (insert-left [1 2]) left next next next next prev show-ip) ; [[1 2 *]]
)
