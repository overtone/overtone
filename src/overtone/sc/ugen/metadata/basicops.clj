(ns overtone.sc.ugen.metadata.basicops
  (:use [overtone.sc.ugen common constants]))

(def specs
  [
   {:name "UnaryOpUGen",
    :args [{:name "a"}],
    :rates #{:dr :ir :ar :kr}
    :doc "Multi-function ugen representing many operations (e.g. neg, abs, floor, sqrt, midicps, etc...)"}

   {:name "BinaryOpUGen",
    :args [{:name "a"}
           {:name "b"}],
    :rates #{:dr :ir :ar :kr}
    :doc "Multi-function ugen representing many operations (e.g. +, *, <, min, max, etc...)"}
   ])
