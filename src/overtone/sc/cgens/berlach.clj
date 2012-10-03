(ns overtone.sc.cgens.berlach
  (:use [overtone.sc defcgen ugens]
        [overtone.helpers seq]))

(defcgen soft-clip-amp
  "Berlach Soft Clip Amp"
  [in {:doc "Input signal"}
   pregain {:default 1 :doc ""}]
  ""
  (:ar (softclip (* in pregain))))
