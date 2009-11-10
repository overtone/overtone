(ns synthdef-test
  (:import (java.io FileInputStream FileOutputStream 
              DataInputStream DataOutputStream
              BufferedInputStream BufferedOutputStream 
              ByteArrayOutputStream ByteArrayInputStream))
  (:use (overtone sc synthdef envelope utils)
     test-utils
     clojure.test
     clj-backtrace.repl))

(defn sawzall-raw 
  "This data was read by this library, but written by jcollider."
  []
  {:name "sawzall" 
   :n-constants (short 1)
   :constants [(float 0.0)]
   :n-params  (short 1)
   :params    [(float 50.0)]
   :n-pnames  (short 1)
   :pnames    [{:index (short 0), :name "note"}]
   :n-ugens   (short 4)
   :ugens     [{:outputs [{:rate (byte 1)}], :inputs [], :special (short 0), :n-outputs (short 1), :n-inputs (short 0), :rate (short 1), :name "Control"}

               {:outputs [{:rate (byte 1)}], :inputs [{:index (short 0), :src (short 0)}], :special (short 17), 
                :n-outputs (short 1), :n-inputs (short 1), :rate (byte 1), :name "UnaryOpUGen"}

               {:outputs [{:rate (byte 2)}], :inputs [{:index (short 0), :src (short 1)}], :special (short 0), 
                :n-outputs (short 1), :n-inputs (short 1), :rate (byte 2), :name "Saw"}

               {:outputs [], :inputs [{:index (short 0), :src (short -1)} {:index (short 0), :src (short 2)}], 
                :special (short 0), :n-outputs (short 0), :n-inputs (short 2), :rate (byte 2), :name "Out"}]
   :n-variants 0
   :variants []})

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
    (is (= a (dissoc b :version :id)))))

(defsynth mini-sin {}
  (out.ar 0 (sin-osc.ar 440 0)))

;SynthDef("round-kick", {|amp= 0.5, decay= 0.6, freq= 65|
;        var env, snd;
;        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;        snd= SinOsc.ar(freq, pi*0.5, amp);
;        Out.ar(0, Pan2.ar(snd*env, 0));
;}).store;
;(defsynth test-kick {:amp 0.5 :decay 0.6 :freq 65}
;  (out.ar 0 (pan2.ar (sin-osc.ar :freq (* Math/PI 0.5) :amp)
;                     (env-gen.ar (perc 0 :decay) :done-free))))
;
(defn mini-bytes []
  (bytes-and-back synthdef-spec (synthdef-file mini-sin)))

(deftest native-synth-test
  (let [bytes (synthdef-bytes mini-sin)
        sdef  (synthdef-read-bytes bytes)
        synth (first (:synths sdef))
        ugens (:ugens synth)
        sin (first ugens)
        out (second ugens)]
    (is (= 1 (:version sdef)))
    (is (= 1 (:n-synths sdef)))
    (is (= "mini-sin" (:name synth)))
    (is (= 2 (:n-constants synth)))
    (is (= (sort [0.0 440.0]) (sort (:constants synth))))
    (is (= 0 (:n-params synth)))
    (is (= 2 (:n-ugens synth)))
    (is (= 2 (count (:ugens synth))))
    (is (= "SinOsc" (:name sin)))
    (is (= "Out" (:name out)))
    (is (= 2 (:rate sin)))
    (is (= 2 (:rate out)))
    (is (= 2 (:n-inputs sin)))
    (is (= 2 (:n-inputs out)))
    (is (= 1 (:n-outputs sin)))
    (is (= 0 (:n-outputs out)))
    (is (= {:src -1 :index 0} (first (:inputs sin))))
    (is (= {:src -1 :index 1} (second (:inputs sin))))
    (is (= {:src -1, :index 1} (first (:inputs out))))
    (is (= {:src 0, :index 0} (second (:inputs out))))
    ))

(def TOM-DEF "test/data/tom.scsyndef")
(def KICK-DEF "test/data/round-kick.scsyndef")

(defn rw-file-test [path]
  (let [a (synthdef-read-file path)
        b (bytes-and-back synthdef-spec a)]
    (is (= a b))))

(deftest read-write-test []
  (rw-file-test TOM-DEF)
  (rw-file-test KICK-DEF))

(defn synthdef-tests []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
