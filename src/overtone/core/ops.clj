(def generics #{"+" "-" "*" "/"})

(defmethod ga/+ [::ugen ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        0
        (list a b)))

(defmethod ga/+ [::ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        0
        (list a b)))

(defmethod ga/+ [root-type ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        0
        (list a b)))

(defmethod ga/- [::ugen ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        1
        (list a b)))

(defmethod ga/- [::ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        1
        (list a b)))

(defmethod ga/- [root-type ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        1
        (list a b)))

(defmethod ga/* [::ugen ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        2
        (list a b)))

(defmethod ga/* [::ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        2
        (list a b)))

(defmethod ga/* [root-type ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        2
        (list a b)))

(def div-meth (var-get (resolve (symbol "clojure.contrib.generic.arithmetic" "/"))))

(defmethod div-meth [::ugen ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (max (op-rate a) (op-rate b))
        4
        (list a b)))

(defmethod div-meth [::ugen root-type]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate a)
        4
        (list a b)))

(defmethod div-meth [root-type ::ugen]
  [a b]
  (ugen (get UGEN-SPEC-MAP "binaryopugen")
        (op-rate b)
        4
        (list a b)))
