(ns overtone.jna-path)

;; set jna.library.path to poitn to java.library.path so any libraries
;; required by JNA can be pulled in as jar dependencies from clojars
(defonce __SET_JNA_PATH__
  (System/setProperty "jna.library.path" (System/getProperty "java.library.path")))
