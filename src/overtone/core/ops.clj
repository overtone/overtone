(derive :overtone.core.ugen/ugen root-type)

(def generics #{"+" "-" "*" "/"})

;; TODO: replace a bunch of this boilerplate with a macro...

(defmethod ga/+ [:overtone.core.ugen/ugen :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        0
        (list a b)))

(defmethod ga/+ [:overtone.core.ugen/ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        0
        (list a b)))

(defmethod ga/+ [root-type :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        0
        (list a b)))

(defmethod ga/- [:overtone.core.ugen/ugen :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        1
        (list a b)))

(defmethod ga/- [:overtone.core.ugen/ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        1
        (list a b)))

(defmethod ga/- [root-type :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        1
        (list a b)))

(defmethod ga/* [:overtone.core.ugen/ugen :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        2
        (list a b)))

(defmethod ga/* [:overtone.core.ugen/ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        2
        (list a b)))

(defmethod ga/* [root-type :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        2
        (list a b)))

(def div-meth (var-get (resolve (symbol "clojure.contrib.generic.arithmetic" "/"))))

(defmethod div-meth [:overtone.core.ugen/ugen :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        4
        (list a b)))

(defmethod div-meth [:overtone.core.ugen/ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        4
        (list a b)))

(defmethod div-meth [root-type :overtone.core.ugen/ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        4
        (list a b)))
