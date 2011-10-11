(ns
  ^{:doc "Code that is common to many ugens."
     :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.machinery.ugen.common
  (:use [overtone.util lib]
        [overtone.sc.machinery.ugen special-ops]
        [overtone.sc buffer bus]))

(defn buffer->id
  "Returns a function that converts any buffer arguments to their :id property
  value."
  [ugen]
  (update-in ugen [:args]
             (fn [args]
               (map #(if (buffer? %) (:id %) %) args))))

(defn bus->id
  "Returns a function that converts any bus arguments to their :id property
  value."
  [ugen]
  (update-in ugen [:args]
             (fn [args]
               (map #(if (bus? %) (:id %) %) args))))


(defn real-ugen-name
  [ugen]
  (overtone-ugen-name
    (case (:name ugen)
      "UnaryOpUGen"
      (get REVERSE-UNARY-OPS (:special ugen))

      "BinaryOpUGen"
      (get REVERSE-BINARY-OPS (:special ugen))

      (:name ugen))))
