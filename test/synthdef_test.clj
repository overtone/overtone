(ns synthdef-test
  (:use (overtone synthdef)
     clojure.test
     clj-backtrace.repl)
  (:require [clojure.zip :as zip]))

(defn saw []
  {:name "jsaw" 
   :n-constants 1
   :constants [0]
   :n-params  0
   :params    nil
   :n-pnames  1 
   :pnames    [{:index 0, :name "note"}]
   :n-ugens   4
   :ugens     [{:outputs [{:rate 1}], :inputs [], :special 0, :n-outputs 1, :n-inputs 0, :rate 1, :name "Control"}
               {:outputs [{:rate 1}], :inputs [{:index 0, :src 0}], :special 17, 
                :n-outputs 1, :n-inputs 1, :rate 1, :name "UnaryOpUGen"}
               {:outputs [{:rate 2}], :inputs [{:index 0, :src 1}], :special 0, 
                :n-outputs 1, :n-inputs 1, :rate 2, :name "Saw"}
               {:outputs [], :inputs [{:index 0, :src -1} {:index 0, :src 2}], 
                :special 0, :n-outputs 0, :n-inputs 2, :rate 2, :name "Out"}]})

(defn foo []
  (bundle [(saw)]))

(deftest read-write-bundle
  (let [b (bundle [(saw)])
        f (synth-def-bytes b)
        f2 (synth-def-read-bytes f)
        bzip (zip/seq-zip b)
        fzip (zip/seq-zip f2)]
    (loop [bloc bzip
           floc fzip]
      (if (zip/end? bloc)
        (zip/root bloc)
        (do
          (println "b: " (zip/node bloc) " f: " (zip/node floc))
          (recur (zip/next bloc) (zip/next floc)))))))

;    (doseq [[k v] b]
;      (println "test:" (str k ": " v " -> " (get f2 k)))
;      (is (= v (get f2 k))))
;    (is (= b f2))))
(def FOO "/home/rosejn/projects/overtone/foo.scd")
(def FURL (java.net.URL. (str "file:" FOO)))
(def FOO2 "/home/rosejn/projects/overtone/foo2.scd")
(def FURL2 (java.net.URL. (str "file:" FOO2)))

(defn jc []
  (synthdef-write-file (foo) FOO2)
  (de.sciss.jcollider.SynthDef/readDefFile FURL2))

(defn go []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
