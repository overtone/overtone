(ns overtone.helpers.sc-lang
  "Convert a subset of sclang to Overtone

  Convert strings of SuperCollider's built-in language to Overtone forms. Fairly
  minimal, does not support assignements, code blocks, etc. But can handle basic
  combinations of UGens. Useful when porting synths, could even be used to embed
  snippet of sc-lang directly inside Overtone synth definitions.

  (macroexpand '(sc-lang \"Out.ar(0, SinOsc.ar(440))\"))
  ;; => (do (out:ar 0 (sin-osc:ar 440)))
  "
  (:use [overtone.helpers lib])
  (:require [clojure.zip :as z]))

(defn- prepend-child
  [loc item]
  (z/replace loc (z/make-node loc (z/node loc) (concat [item] (z/children loc)))))

(defn- zip-map [f zipper]
  (first (drop-while (complement z/end?) (iterate (comp z/next f) zipper))))

(defn- tokenize [code]
  (z/root (reduce #(cond
                  (#{\space \,} %2) (-> %1
                                      (z/insert-right "")
                                      z/right)
                  (= \( %2) (-> %1
                              (z/insert-right '())
                              z/right
                              (z/append-child "")
                              z/down)
                  (= \) %2) (z/up %1)
                  true (z/edit %1 str %2))
                  (z/down (z/seq-zip (list ""))) code)))

(defn- despace [tokens]
  (z/root (zip-map #(if (= "" (z/node %)) (z/remove %) %) (z/seq-zip tokens))))

(defn- infix [tokens]
  (loop [zipper (z/down (z/seq-zip tokens))]
    (if (some-> zipper
          z/right
          z/branch?)
      (recur (let [f (z/node zipper)]
               (-> zipper
                 z/remove
                 z/next
                 (prepend-child f))))
      (if (z/end? zipper)
        (z/root zipper)
        (recur (z/next zipper))))))

(defn- infix-math [tokens]
  (z/root (zip-map #(if (#{"+" "-" "*" "/"} (z/node %))
                      (let [op (z/node %)
                            right (z/node (z/right %))
                            left (z/node (z/left %))]
                        (-> %
                          z/right
                          z/remove
                          z/left
                          z/remove
                          z/next
                          (z/replace (list op left right))
                          z/down
                          z/rightmost))
                      %) (z/seq-zip tokens))))

(defn- ban-kwmath [tokens]
  (z/root (zip-map
            #(if-let [op ({"add:" "+",  "mul:" "*"} (z/node %))]
               (let  [right (z/node (z/right %))
                      left (z/lefts %)]
                 (-> %
                   z/up
                   (z/replace (list op left right))))
               %)
          (z/seq-zip tokens))))

(defn- fnames [tokens]
  (let [code (z/seq-zip tokens)
        overtone-ugen-name (comp
                             #(.replaceAll % "\\.(ar|kr)" ":$1")
                             overtone-ugen-name)]
   (z/root (zip-map #(if (string? (z/node %))
                       (z/edit % overtone-ugen-name)
                       %)
                    code))))

;(defn assignment [tokens])

(defn- real-types [tokens]
  (z/root
    (zip-map #(if (z/branch? %)
                %
                (z/replace % (read-string (z/node %))))
             (z/seq-zip tokens))))

(defmacro sc-lang [code]
  (-> code
      tokenize
      despace
      infix
      infix-math
      ban-kwmath
      fnames
      real-types
      (conj 'do)))
