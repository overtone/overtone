(ns
  ^{:doc "Code that is common to many ugens."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.machinery.ugen.common
  (:use [overtone.util lib]
        [overtone.sc.machinery.ugen special-ops]))


(defn real-ugen-name
  [ugen]
  (overtone-ugen-name
    (case (:name ugen)
      "UnaryOpUGen"
      (get REVERSE-UNARY-OPS (:special ugen))

      "BinaryOpUGen"
      (get REVERSE-BINARY-OPS (:special ugen))

      (:name ugen))))
