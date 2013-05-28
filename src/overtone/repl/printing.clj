(ns overtone.repl.printing
  (:use [clojure.pprint]))

(defmulti oprint type)

(defmethod oprint :default
  [& args]
  (apply pprint args))

(defmethod oprint :overtone.sc.machinery.synthdef/synthdef
  [& args]
  (let [obj (first args)]
    ))


(oprint 1)
