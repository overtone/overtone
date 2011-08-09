(derive :overtone.sc.ugen/sc-ugen root-type)

(def generics
  { "+"  :arithmetic
    "-"  :arithmetic
    "*"  :arithmetic
    "/"  :arithmetic
    ">"  :comparison
    "<"  :comparison
    "="  :comparison
    "<=" :comparison
    ">=" :comparison
;;TODO addme when available in gc    "!=" :comparison
    })

(def generic-namespaces
  { :arithmetic "ga"
    :comparison "gc"})

(defmacro mk-binary-op-ugen [sym]
  (let [generic-kind (get generics (to-str sym))
        gen-nspace   (get generic-namespaces generic-kind)
        sym (to-str sym)]
    `(do
      (defmethod ~(symbol gen-nspace sym) [:overtone.sc.ugen/sc-ugen :overtone.sc.ugen/sc-ugen]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (max (op-rate a#) (op-rate b#))
               ~(get BINARY-OPS-FULL sym)
               (list a# b#)))

      (defmethod ~(symbol gen-nspace sym) [root-type :overtone.sc.ugen/sc-ugen]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (op-rate b#)
               ~(get BINARY-OPS-FULL sym)
               (list a# b#)))

      (defmethod ~(symbol gen-nspace sym) [:overtone.sc.ugen/sc-ugen root-type]
         [a# b#]
         (ugen (get UGEN-SPECS "binaryopugen")
               (op-rate a#)
               ~(get BINARY-OPS-FULL sym)
               (list a# b#))))))

(mk-binary-op-ugen :+)
(mk-binary-op-ugen :-)
(mk-binary-op-ugen :*)
(mk-binary-op-ugen :<)
(mk-binary-op-ugen :>)
(mk-binary-op-ugen :<=)
(mk-binary-op-ugen :>=)
(mk-binary-op-ugen :=)
;; TODO addme when available in gc (mk-binary-op-ugen :!=)

(def div-meth (var-get (resolve (symbol "clojure.contrib.generic.arithmetic" "/"))))

(defmethod div-meth [:overtone.sc.ugen/sc-ugen :overtone.sc.ugen/sc-ugen]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (max (op-rate a) (op-rate b))
        4
        (list a b)))

(defmethod div-meth [:overtone.sc.ugen/sc-ugen root-type]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (op-rate a)
        4
        (list a b)))

(defmethod div-meth [root-type :overtone.sc.ugen/sc-ugen]
  [a b]
  (ugen (get UGEN-SPECS "binaryopugen")
        (op-rate b)
        4
        (list a b)))


(defmethod ga/- :overtone.sc.ugen/sc-ugen
  [a]
  (ugen (get UGEN-SPECS "unaryopugen")
        (op-rate a)
        0
        (list a)))
