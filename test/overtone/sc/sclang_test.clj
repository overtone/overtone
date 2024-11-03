(ns overtone.sc.sclang-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :refer [deftest testing is]]
   [clojure.java.io :as io]
   [overtone.sc.synth :refer [synth?]]
   [overtone.test-helper :refer [ensure-server with-sync-reset]]
   [overtone.sc.sclang :as sclang])
  (:import [java.io File]))

(set! *warn-on-reflection* true)

(deftest transpile-test
  (testing "Simple"
    (is (= "SynthDef.help"
           (sclang/transpile [:. :SynthDef :help]))))

  (testing "SynthDef"
    (is (= '{:sc-clj
             [:SynthDef
              {:name overtone.sc.sclang-test/event,
               :args [[:freq] [:amp 0.5] :pan]}
              [:vars :env]
              [:=
               :env
               [:EnvGen.ar [:Env [0 1 1 0] [0.01 0.1 0.2]] {:doneAction 2}]]
              [:raw "0.postln" "\"eita\".postln"]
              [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp] :pan]]],
             :synthdef-name "overtone_sc_sclang-test_event",
             :file-path
             "resources/sc/synthdef/overtone_sc_sclang-test_event.scsyndef",
             :resource-path "sc/synthdef/overtone_sc_sclang-test_event.scsyndef",
             :metadata-file-path
             "resources/sc/synthdef/overtone_sc_sclang-test_event.edn",
             :metadata-resource-path
             "sc/synthdef/overtone_sc_sclang-test_event.edn",
             :synthdef-str
             "SynthDef(\"overtone_sc_sclang-test_event\", {
  arg freq, amp=(0.5), pan;
  var env;
  env = EnvGen.ar( Env( [0, 1, 1, 0], [0.01, 0.1, 0.2] ), doneAction: 2 );
  0.postln; \"eita\".postln;
  Out.ar( 0, Pan2.ar( Blip.ar( freq ) * env * amp, pan ) );
}).writeDefFile(\"resources/sc/synthdef\").add;",
             :synthdef-str-vec
             ["SynthDef(\"overtone_sc_sclang-test_event\", {"
              "  arg freq, amp=(0.5), pan;"
              "  var env;"
              "  env = EnvGen.ar( Env( [0, 1, 1, 0], [0.01, 0.1, 0.2] ), doneAction: 2 );"
              "  0.postln; \"eita\".postln;"
              "  Out.ar( 0, Pan2.ar( Blip.ar( freq ) * env * amp, pan ) );"
              "}).writeDefFile(\"resources/sc/synthdef\").add;"]}
           (sclang/transpile
            (sclang/SynthDef
             {:name `event
              :args [[:freq] [:amp 0.5] :pan]}
             [:vars :env]
             [:= :env [:EnvGen.ar
                       [:Env [0 1 1 0] [0.01 0.1 0.2]]
                       {:doneAction 2}]]
             [:raw "0.postln" "\"eita\".postln"]
             [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                         :pan]]))))))

(def this-ns (ns-name *ns*))

(deftest defsynth-test
  (with-sync-reset
    (fn []
      (let [g (gensym 'my-synth)
            edn-file (io/file (format "resources/sc/synthdef/overtone_sc_sclang-test_%s.edn"
                                      (sclang/munge-synthdef-name (name g))))
            scsyndef-file (io/file (format "resources/sc/synthdef/overtone_sc_sclang-test_%s.scsyndef"
                                           (sclang/munge-synthdef-name (name g))))]
        (try (let [f (binding [*ns* (the-ns this-ns)
                               sclang/*-check-proc-max-count* 100]
                       (eval `(sclang/defsynth ~g
                                "Some synth."
                                [~'freq 440, ~'amp 0.5, ~'pan 0.0]
                                [:vars :env]
                                [:= :env [:EnvGen.ar [:Env [0 1 1 0] [0.01 0.1 0.2]] {:doneAction 2}]]
                                [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp]
                                            :pan]])))
                   edn (-> (slurp edn-file)
                           edn/read-string)]
               (is (= {:sc-clj [:SynthDef {:args [[:freq 440] [:amp 0.5] [:pan 0.0]],
                                           :name (symbol "overtone.sc.sclang-test" (name g))}
                                [:vars :env]
                                [:= :env [:EnvGen.ar [:Env [0 1 1 0] [0.01 0.1 0.2]] {:doneAction 2}]]
                                [:Out.ar 0 [:Pan2.ar [:* [:Blip.ar :freq] :env :amp] :pan]]]}
                      edn))
               (is (.exists scsyndef-file))
               (is (synth? @f)
                   (pr-str (class @f)))
               (ensure-server
                 (fn []
                   (is (f))
                   ;;FIXME https://github.com/overtone/overtone/issues/569
                   ;(is (f 230 0.2 0.5))
                   ;;TODO https://github.com/overtone/overtone/issues/570
                   ;(is (f {:freq 230 :amp 0.3 :pan 0.5}))
                   (is (f :freq 230 :amp 0.3 :pan 0.5)))))
             (finally
               (ns-unmap this-ns g)
               (run! (fn [^File f] (when (.exists f) (.delete f)))
                     [edn-file scsyndef-file])))))))
