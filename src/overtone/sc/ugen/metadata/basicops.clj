(ns overtone.sc.ugen.metadata.basicops
  (:use [overtone.sc.ugen common constants]))

;;see binaryopugen.clj and unaryopugen.clj for detailed docspecs for these two ugens

(def specs
  [
   {:name "UnaryOpUGen",
    :args [{:name "a" :doc "Single input"}],
    :rates #{:auto}
    :default-rate :auto
    :categories [["Unary Operations"]]
    :doc "Multi-function ugen representing many operations (e.g. neg, abs, floor, sqrt, midicps, etc...)"}

   {:name "BinaryOpUGen",
    :args [{:name "a" :doc "First input"}
           {:name "b" :doc "Second input"}],
    :rates #{:dr :ir :ar :kr}
    :default-rate :auto

    :doc "Multi-function ugen representing many operations (e.g. +, *, <, min, max, etc...)"}
   ])
