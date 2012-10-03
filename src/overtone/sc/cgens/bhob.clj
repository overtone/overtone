(ns overtone.sc.cgens.bhob
  (:use [overtone.sc defcgen ugens]
        [overtone.helpers lib]))

(defcgen d-gauss
  ""
  [lo {:default 0 :doc ""}
   hi {:default 1 :doc ""}
   length {:default INF :doc ""}]
  ""
  (:dr (internal:d-gauss:dr length hi lo)))
