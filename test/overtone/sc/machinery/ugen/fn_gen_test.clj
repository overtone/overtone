(ns overtone.sc.machinery.ugen.fn-gen-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.sc.machinery.ugen.fn-gen :as sut]))

(def example-spec {:args [{:name "envelope", :mode :append-sequence, :expands? false}
                          {:name "gate", :default 1.0, :expands? true}
                          {:name "level-scale", :default 1.0, :expands? true}
                          {:name "level-bias", :default 0.0, :expands? true}
                          {:name "time-scale", :default 1.0, :expands? true}
                          {:name "action", :default 0, :expands? true}]})

(deftest multichannel-expand-test
  (is (= '([[1 2 3]]) (sut/multichannel-expand example-spec [[1 2 3]])))
  (is (= '([[1 2 3] 4]) (sut/multichannel-expand example-spec [[1 2 3] 4])))
  (is (= '([[1 2 3] 4]
           [[1 2 3] 5])
         (sut/multichannel-expand example-spec [[1 2 3] [4 5]])))
  (is (= '([[1 2 3] :gate 4]) (sut/multichannel-expand example-spec [[1 2 3] :gate 4])))
  (is (= '([[1 2 3] :gate 4]
           [[1 2 3] :gate 5])
         (sut/multichannel-expand example-spec [[1 2 3] :gate [4 5]])))
  (is (= '([:envelope [1 2 3]]) (sut/multichannel-expand example-spec [:envelope [1 2 3]])))
  (is (= '([:envelope [1 2 3] :gate 4]) (sut/multichannel-expand example-spec [:envelope [1 2 3] :gate 4])))
  (is (= '([:envelope [1 2 3] :gate 4]
           [:envelope [1 2 3] :gate 5])
         (sut/multichannel-expand example-spec [:envelope [1 2 3] :gate [4 5]]))))
