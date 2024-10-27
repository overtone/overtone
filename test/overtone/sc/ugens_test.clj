(ns overtone.sc.ugens-test
  (:require [clojure.test :refer [is deftest]]
            [overtone.sc.machinery.ugen.sc-ugen :as sc-ugen]
            [overtone.sc.envelope :as env]
            [overtone.sc.ugens :as ug]))

(def base-env-gen-triangle [1.0 1.0 0.0 1.0 0.0 0 2 -99 -99 1 0.5 1 0 0 0.5 1 0])

(deftest append-sequence-kv-syntax-test
  (is (= base-env-gen-triangle
         (:args (ug/env-gen (env/triangle)))
         (:args (ug/env-gen :envelope (env/triangle)))
         (:args (ug/env-gen {:envelope (env/triangle)}))))
  (is (= (assoc base-env-gen-triangle 0 15.0)
         (:args (ug/env-gen (env/triangle) 15))
         (:args (ug/env-gen :envelope (env/triangle) :gate 15))
         (:args (ug/env-gen {:envelope (env/triangle) :gate 15}))))
  (is (= [base-env-gen-triangle
          (assoc base-env-gen-triangle 0 2.0)
          (assoc base-env-gen-triangle 0 3.0)]
         (map :args (ug/env-gen (env/triangle) [1 2 3]))
         (map :args (ug/env-gen (env/triangle) :gate [1 2 3]))
         (map :args (ug/env-gen :envelope (env/triangle) :gate [1 2 3]))
         (map :args (ug/env-gen :gate [1 2 3] :envelope (env/triangle)))
         (map :args (ug/env-gen {:envelope (env/triangle) :gate [1 2 3]})))))
