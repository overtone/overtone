(ns synthdef-test
  (:import (java.io FileInputStream FileOutputStream 
              DataInputStream DataOutputStream
              BufferedInputStream BufferedOutputStream 
              ByteArrayOutputStream ByteArrayInputStream))
  (:use (overtone sc synthdef)
     bytes-test
     clojure.test
     clojure.contrib.logging))

(swap! *allow-direct-logging* not)

(defn sawzall-raw 
  "This data was read by this library, but written by jcollider."
  []
  {:name "sawzall" 
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

(def FOO "/home/rosejn/projects/overtone/foo.scd")
(def FURL (java.net.URL. (str "file:" FOO)))
(def FOO2 "/home/rosejn/projects/overtone/foo2.scd")
(def FURL2 (java.net.URL. (str "file:" FOO2)))

;(defn jc []
;  (synthdef-write-file (foo) FOO2)
;  (de.sciss.jcollider.SynthDef/readDefFile FURL2))

(defn jc-load-file [path]
  (de.sciss.jcollider.SynthDef/readDefFile (java.net.URL. (str "file:" path))))

(defn dia-file [path]
  (de.sciss.jcollider.gui.SynthDefDiagram. 
    (first (jc-load-file path))))

(defn jc-load [sdef]
 (de.sciss.jcollider.SynthDef/readDefFile 
             (-> (synthdef-bytes sdef) (ByteArrayInputStream.) 
               (BufferedInputStream.) (DataInputStream.))))

(defn dia [sdef]
  (de.sciss.jcollider.gui.SynthDefDiagram. 
    (first (jc-load sdef))))

(deftest self-consistent-syndef
  (let [a (synthdef-file (sawzall-raw))
        b (bytes-and-back synthdef-spec a)]
    (is (same? a b))))

(defsynth mini-sin
  (out.ar 0 (sin-osc.ar 440)))

(defn mini-bytes []
  (bytes-and-back synthdef-spec (synthdef-file mini-sin)))

(deftest native-synth-test
  (let [bytes (synthdef-bytes mini-sin)
        sdef  (synthdef-read-bytes bytes)
        synth (first (:synths sdef))
        [out sin] (:ugens sdef)] 
    (is (= 1 (:version sdef)))
    (is (= 1 (:n-synths sdef)))
    (is (= "mini-sin" (:name synth)))
    (is (= 2 (:n-constants synth)))
;    (is (= (set [0.0 440.0]) (set (:constants synth))))
    (is (= 0 (:n-params synth)))
    (is (= 2 (:n-ugens synth)))
    (is (= "SinOsc" (:name sin)))
    (is (= "Out" (:name out)))
    ))

(defn go []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
