(ns overtone.helpers.atom-test
  (:use clojure.test)
  (:require [overtone.helpers.atom :refer [atom-view]])
  (:import
   (java.util.concurrent Semaphore)))

(deftest atom-view-swap!
  (let [a (atom {:val 0})
        threads 10
        reps 100
        s (Semaphore. 0)
        thread (fn []
                 (let [av (atom-view a :val)]
                   (.start
                    (Thread.
                     (fn []
                       (try
                         (doseq [_ (range reps)]
                           ;; Mix view and atom updates
                           (swap! av inc)
                           (swap! a update :val inc))
                         (finally
                           (.release s))))))
                   av))
        views (doall (repeatedly threads thread))]
    (.acquire s threads)
    (is (apply = (* 2 threads reps) (:val @a) (map deref views)))))

(deftest atom-view-check-and-set!
  (let [a (atom {:val -1})
        threads 10
        reps 100
        count (atom 0)
        s (Semaphore. 0)
        thread (fn []
                 (let [av (atom-view a :val)]
                   (.start
                    (Thread.
                     (fn []
                       (try
                         (doseq [i (range reps)]
                           (when (compare-and-set! av (dec i) i)
                             (swap! count inc)))
                         (finally
                           (.release s))))))
                   av))
        views (doall (repeatedly threads thread))]
    (.acquire s threads)
    (is (apply = (dec reps) (:val @a) (map deref views)))))
