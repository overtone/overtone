(ns synthdef-test
  (:use (overtone synthdef)
     clojure.test
     clj-backtrace.repl))

(defn jsaw-full []
  {:name "jsaw" 
   :n-constants 1
   :constants [0.0]
   :n-params  1
   :params    [50.0]
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

(defsynth mini-sin
  (out.ar 0 (sin-osc.ar 440)))

(defn jsaw []
  (synthdef-file (jsaw-full)))

(def FOO "/home/rosejn/projects/overtone/foo.scd")
(def FURL (java.net.URL. (str "file:" FOO)))
(def FOO2 "/home/rosejn/projects/overtone/foo2.scd")
(def FURL2 (java.net.URL. (str "file:" FOO2)))

;(defn jc []
;  (synthdef-write-file (foo) FOO2)
;  (de.sciss.jcollider.SynthDef/readDefFile FURL2))
;
;(defn show [url]
;  (de.sciss.jcollider.gui.SynthDefDiagram. 
;    (first (de.sciss.jcollider.SynthDef/readDefFile url))))
(defn bytes-and-back [sdef]
  (synthdef-read-bytes (synthdef-write-bytes (synthdef-file sdef))))

(defn jc-load [path]
  (de.sciss.jcollider.SynthDef/readDefFile (java.net.URL. (str "file:" path))))

(deftest read-write-bundle
  (let [a (jsaw)
        b (bytes-and-back a)]
    (is (= (:n-sdefs b) 1))))

(defn go []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
