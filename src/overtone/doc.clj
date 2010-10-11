(ns overtone.doc
  (:require overtone.sc.ugen))

; TODO: Write a couple functions that we can use to generate nice
; UGEN documentation text files (or maybe markdown, html...?) sorted by
; name, category, rate, etc.

(defn write-ugen-doc [path]
  (spit path (with-out-str
               (doseq [ugen overtone.core.ugen/UGEN-SPECS]
                 (println "Name: " (:name ugen))
                 (println (:doc ugen))))))
