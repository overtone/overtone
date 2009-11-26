(ns synthdef-test
  (:import (java.io FileInputStream FileOutputStream 
              DataInputStream DataOutputStream
              BufferedInputStream BufferedOutputStream 
              ByteArrayOutputStream ByteArrayInputStream))
  (:use (overtone sc synthdef synth envelope utils)
     test-utils
     clojure.test
     clj-backtrace.repl))

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

(defsynth saw-sin {:freq-a 443
                   :freq-b 440}
  (out.ar 0 (+ (* 0.3 (saw.ar :freq-a))
               (* 0.3 (sin-osc.ar :freq-b 0)))))

(comment 
(load-synth saw-sin)
(let [note (hit (now) saw-sin :freq-a 400 :freq-b 402)]
  (ctl (+ (now) 500) note :freq-a 300 :freq-b 303)
  (kill (+ (now) 1000) note))
  )

;SynthDef("round-kick", {|amp= 0.5, decay= 0.6, freq= 65|
;        var env, snd;
;        env= EnvGen.ar(Env.perc(0, decay), doneAction:2);
;        snd= SinOsc.ar(freq, pi*0.5, amp);
;        Out.ar(0, Pan2.ar(snd*env, 0));
;}).store;
;(defsynth test-kick {:amp 0.5 :decay 0.6 :freq 65}
;  (out.ar 0 (pan2.ar (* (sin-osc.ar :freq (* Math/PI 0.5)) 
;                        :amp)
;                     (env-gen.ar (perc 0 :decay) :done-free))))
;
(defsynth mini-sin {:freq 440}
  (out.ar 0 (sin-osc.ar :freq 0)))

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
    (is (= {:src 1, :index 0} (second (:inputs out))))
    ))

(def TOM-DEF "test/data/tom.scsyndef")
(def KICK-DEF "test/data/round-kick.scsyndef")

(defn rw-file-test [path]
  (let [a (synthdef-read path)
        b (bytes-and-back synth-spec a)]
    (is (= a b))))

(deftest read-write-test []
  (rw-file-test TOM-DEF)
  (rw-file-test KICK-DEF))

; Some other day...
;(deftest decompile-test []
;  (is (= (synthdef-decompile mini-sin) (:src-code (meta mini-sin)))))

(defsynth overtone-scope {:in 0 :buf 1}
  (record-buf.ar (in.ar :in) :buf))

;TODO: Use this synthdef for a regression test, it found a few bugs
; * first we need better OSC feedback from the server
(defsynth buzz {:pitch 40 :cutoff 300}
  (let [a (lpf.ar (saw.ar (midicps :pitch)) (+ (lf-noise-1.kr 10) :cutoff))
        b (sin-osc.ar (midicps (- :pitch 12)))]
  (out.ar 0 (pan2.ar (+ a b)))))

(comment deftest buf-synths-test []
  (let [s (synth "scope-synth" {:in 0 :buf 1} 
                 (record-buf.ar (in.ar :in) :buf))]
    s))

(defn synthdef-tests []
  (binding [*test-out* *out*]
    (run-tests 'synthdef-test)))
