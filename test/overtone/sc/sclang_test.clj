(ns overtone.sc.sclang-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [overtone.sc.sclang :as sclang]))

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
