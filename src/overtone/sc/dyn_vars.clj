(ns overtone.sc.dyn-vars)

(defonce ^{:dynamic true} *inactive-node-modification-error* :exception)
(defonce ^{:dynamic true} *inactive-buffer-modification-error* :exception)
(defonce ^{:dynamic true} *block-node-until-ready?* true)
