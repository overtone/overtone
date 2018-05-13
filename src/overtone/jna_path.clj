(ns overtone.jna-path
  (:require [overtone.helpers.file :refer [ensure-native]]
            [overtone.helpers.system :refer [get-os]]))

;; set jna.library.path to point to native libraries
;; dependant on OS. No path merge to prevent clj-native
;; from pulling out third party lib files from path
(defonce __SET_JNA_PATH__
  (do (ensure-native)
      (case (get-os)
        :linux   (System/setProperty "jna.library.path" "native/linux/x86_64")
        :mac     (System/setProperty "jna.library.path" "native/macosx/x86_64")
        :windows (System/setProperty "jna.library.path" "native/windows/x86_64"))))
