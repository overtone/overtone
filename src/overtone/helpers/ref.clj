(ns
    ^{:doc "Reference helper fns"
      :author "Sam Aaron"}
  overtone.helpers.ref)

(defn swap-returning-prev!
  "Similar to swap! except returns vector containing the previous and new values

  (def a (atom 0))
  (swap-returning-prev! a inc) ;=> [0 1]"
  [atom f & args]
  (loop []
    (let [old-val  @atom
          new-val  (apply f (cons old-val args))
          success? (compare-and-set! atom old-val new-val)]
      (if success?
        [old-val new-val]
        (recur)))))

(defn reset-returning-prev!
  "Similar to reset! except returns vector containing the previous and new values

  (def a (atom 0))
  (reset-returning-prev! a 1) ;=> [0 1]"
  [atom val]
  (loop []
    (let [old-val @atom
          success? (compare-and-set! atom old-val val)]
      (if success?
        [old-val val]
        (recur)))))
