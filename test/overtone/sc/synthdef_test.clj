(ns overtone.sc.synthdef-test
  (:require [clojure.test :as t :refer [deftest is]]
            [overtone.config.log :as log]
            [overtone.sc.synth :refer [pre-synth synthdef]]
            [overtone.sc.ugens :refer [out:ar sin-osc:ar]]
            [overtone.byte-spec :as bspec]
            [overtone.sc.machinery.synthdef :refer [synth-spec synthdef-bytes synthdef-read]]))

(defn bytes-and-back [spec obj]
  (->> obj
       (bspec/spec-write-bytes spec) 
       (bspec/spec-read-bytes spec)))

(defn sawzall-raw
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
   :n-variants (short 0)
   :variants []})

(deftest self-consistent-syndef
  (let [a (sawzall-raw)
        b (bytes-and-back synth-spec a)]
    (is (= a b))))

(def mini-sin-pre
  (pre-synth
   "mini-sin" {"freq" 440}
   (out:ar 0 (sin-osc:ar 0))))

(deftest pre-synth-test
  (is (= (str mini-sin-pre)
         "[\"mini-sin\" [] (#<sc-ugen: sin-osc:ar [0]> #<sc-ugen: out:ar [1]>) [0.0]]")))

(def mini-sin
  (apply synthdef mini-sin-pre))

#_;;FIXME
(deftest native-synth-test
  (let [bytes (synthdef-bytes mini-sin)
        synth (synthdef-read bytes)
        ugens (:ugens synth)
        [control sin out] ugens]
    (is (= 1 (:n-constants synth)))
    (is (= [0.0] (:constants synth)))
    (is (= 1 (:n-params synth)))
    (is (= 3 (:n-ugens synth)))
    (is (= 3 (count (:ugens synth))))
    (is (= "Control" (:name control)))
    (is (= "SinOsc" (:name sin)))
    (is (= "Out" (:name out)))
    (is (= 1 (:rate control)))
    (is (= 2 (:rate sin)))
    (is (= 2 (:rate out)))
    (is (= 2 (:n-inputs sin)))
    (is (= 2 (:n-inputs out)))
    (is (= 1 (:n-outputs sin)))
    (is (= 0 (:n-outputs out)))
    (is (= {:src 0 :index 0} (first (:inputs sin))))
    (is (= {:src -1 :index 0} (second (:inputs sin))))
    (is (= {:src -1, :index 0} (first (:inputs out))))
    (is (= {:src 1, :index 0} (second (:inputs out))))))

(def TOM-DEF "test/data/tom.scsyndef")
(def KICK-DEF "test/data/round-kick.scsyndef")

(defn rw-file-test [path]
  (let [a (synthdef-read path)
        b (bytes-and-back synth-spec a)]
    (is (= a b))))

(deftest read-write-test []
  (rw-file-test TOM-DEF)
  (rw-file-test KICK-DEF))
