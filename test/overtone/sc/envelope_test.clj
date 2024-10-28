(ns overtone.sc.envelope-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.sc.envelope :as sut]))

(deftest defunk-line-numbers-test
  (is (every? (comp pos-int? (meta #'sut/triangle)) [:line :column])))
