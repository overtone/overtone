(ns util-test
  (:use overtone.core.util
        clojure.test))

(deftest arg-mapper-test
  (let [defaults {'a 1 'b 2 'c 3 'd 4}
        names (keys defaults)]
    (is (= defaults (arg-mapper [] 
                                names
                                defaults)))

    (is (= (conj defaults {'a 10 'b 20})
           (arg-mapper [10 20] 
                       names
                       defaults)))

    (is (= (conj defaults '{a 10 b 20 c 30 d 40})
           (arg-mapper [10 20 :c 30 :d 40] 
                       names
                       defaults)))))

(defn util-tests []
  (binding [*test-out* *out*]
    (run-tests 'util-test)))
