(ns config.test
  (:use clojure.contrib.test-is)
  (:require config))

(deftest test-basic []
         (config/set-all {:a 1 :b 2})
         (is (= 1 (config/value :a)))

         (config/value :foo "asdf")
         (is (= "asdf" (config/value :foo)))

         (config/defaults {:a 10 :b 20 :foo 30})
         (is (= 30 (config/value :foo))))

(defn delete-file [name]
  (.delete (java.io.File. name)))

(deftest test-persist 
  (try 
    (config/set-all {:a 1 :b 2})
    (config/save "test-config")
    (config/value :a 10)
    (config/value :b 20)
    (is (= 10 (config/value :a)))

    (config/restore "test-config")
    (is (= 2 (config/value :b)))
    (finally 
      (delete-file "test-config")))
