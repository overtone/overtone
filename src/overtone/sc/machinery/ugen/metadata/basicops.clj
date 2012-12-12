(ns overtone.sc.machinery.ugen.metadata.basicops
  (:use [overtone.sc.machinery.ugen common check]))

;;see binaryopugen.clj and unaryopugen.clj for detailed docspecs for binary and unary opugens.

(def specs
  [
   {:name "UnaryOpUGen",
    :args [{:name "a" :doc "input"}],
    :rates #{:dr :ir :ar :kr}
    :default-rate :auto
    :categories [["Unary Operations"]]
    :doc "Multi-function unary ugen representing many operations (e.g. neg, abs, floor, sqrt, midicps, etc...)"}

   {:name "BinaryOpUGen",
    :args [{:name "a" :doc "First input"}
           {:name "b" :doc "Second input"}],
    :rates #{:dr :ir :ar :kr}
    :default-rate :auto
    :doc "Multi-function binary ugen representing many operations (e.g. +, *, <, min, max, etc...)"}

   {:name "MulAdd",
    :args [{:name "in" :doc "Input to modify"}
           {:name "mul" :doc "Multiplier Value"}
           {:name "add" :doc "Addition Value"}],
    :rates #{:dr :ir :ar :kr}
    :default-rate :auto
    :check (same-rate-as-first-input)
    :doc "Multiply the input source by mul then add the add value. Equivalent to, but more efficient than,  (+ add (* mul in))"}
   ])
