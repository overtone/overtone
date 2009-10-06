(ns bytes-test
  (:use 
     clojure.test
     (overtone bytes)))

(defn bytes-and-back [spec obj]
  (spec-read-bytes spec (spec-write-bytes spec obj)))

(defn no-sizes [obj]
  (apply hash-map 
         (apply concat
                (filter (fn [[k v]] (not (.startsWith (name k) "n-")))
                             obj))))

(defn same? [a b]
  (for [[k v] (no-sizes a) [kb vb] (no-sizes b)]
    (is (and (= k kb) (= v vb)))))

(defspec basic-type-spec
         :a :int8
         :b :int16
         :c :int32
         :d :float32
         :e :float64
         :f :string)

(deftest basic-type-test []
  (let [a {:a 1 :b 2 :c 3 :d 4 :e 5 :f "six"}
        b (bytes-and-back basic-type-spec a)
        c (spec basic-type-spec 1 2 3 4 5 "six")]
    (is (= a b))
    (is (= a c))
    (is (= b c))
    (is (thrown? Exception (spec-write-bytes basic-type-spec {:a 123})))))

(defspec array-spec
         :n-a :int8
         :a [:int16]
         :n-b :int32
         :b [:float32]
         :n-c :int64
         :c [:string])

(deftest array-test []
  (let [a {:a [1 2 3 4] 
           :b [3.23 4.3223 53.32 253.2 53.2 656.5] 
           :c ["foo" "bar" "baz"]}
        b (bytes-and-back array-spec a)]
    (is (same? a b))))

(defspec rhythm-spec 
         :name :string
         :length :int16
         :n-triggers :int32
         :triggers [:int8])

(defspec melody-spec
         :name :string
         :n-notes :int32
         :notes  [:int16])

(defspec song-spec
         :name   :string
         :bpm    :int8
         :rhythm rhythm-spec
         :melody melody-spec)

(deftest nested-spec-test []
  (let [r (spec rhythm-spec "test rhythm" 100 nil [1 2 3 4 5])
        m (spec melody-spec "test melody" 12 nil [2 3 4 54 23 43 98 23 98 54 87 23])
        s (spec song-spec "test song" 234 r m)
        s2 (bytes-and-back song-spec s)
        m2 (:melody s2)
        r2 (:rhythm s2)]
    (is (same? r r2))
    (is (same? m m2))
    (is (same? s s2))))

(defn run []
  (binding [*test-out* *out*]
    (run-tests 'bytes-test)))

