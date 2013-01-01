(ns test-helper
  "Helpful functions and macro's for writing Overtone tests."
  (:use overtone.core
        clojure.test)
  (:import [java.util.concurrent TimeoutException]))


;; ns helpers

;; borrowed from clojure.test-helper
(defmacro eval-in-temp-ns
  "Evaluates the given forms in a temporary ns and restores the
  current ns when done. Ensures that the temporary ns is removed."
  [& forms]
  `(let [temp-ns# (gensym)]
     (try
       (binding [*ns* *ns*]
         (in-ns temp-ns#)
         (clojure.core/use 'clojure.core)
         (eval '(do ~@forms)))
       (finally
         (remove-ns temp-ns#)))))


;; Async Helpers
;;
;; Useful bits of code for automated testing of asynchronous
;; code.

(defn invoke-timeout
  "Invokes the function 'f' with the given timeout. Attempts to
  interrupt the current thread if the timeout is exceeded. Returns the
  result of invoking 'f' or throws a TimeoutException."
  [f timeout-ms]
  (let [thr (Thread/currentThread)
        fut (future (Thread/sleep timeout-ms)
                    (.interrupt thr))]
    (try (f)
         (catch InterruptedException e
           (throw (TimeoutException. "Execution timed out!")))
         (finally
           (future-cancel fut)))))

(defmacro timeout
  "Invokes a body of expressions with the given timeout. Attempts to
  interrupt the current thread if the timeout is exceeded. Returns the
  result of the last expression or throws a TimeoutException."
  [ms & body]
  `(invoke-timeout (^{:once true} fn [] ~@body) ~ms))

(defn wait-while
  "Blocks the current thread while `pred` returns true.

  Optional arguments [default]:
    `timeout-ms`   time in ms to wait before TimeoutException [nil]
    `interval-ms`  time in ms to wait before re-invoking `pred` [1]"
  ([pred]
     (wait-while pred nil 1))
  ([pred timeout-ms]
     (wait-while pred timeout-ms 1))
  ([pred timeout-ms interval-ms]
     (let [thunk #(loop []
                    (when (pred)
                      (Thread/sleep interval-ms)
                      (recur)))]
       (if timeout-ms
         (invoke-timeout thunk timeout-ms)
         (thunk)))))

(defn wait-until
  "Blocks the current thread until `pred` returns true.
  See `wait-while` for supported options."
  ([pred]
     (wait-until pred nil 1))
  ([pred timeout-ms]
     (wait-until pred timeout-ms 1))
  ([pred timeout-ms interval-ms]
     (wait-while (complement pred) timeout-ms interval-ms)))


;; Test Fixtures

(def with-server-sync #'overtone.sc.machinery.server.comms/with-server-sync)

(defn with-internal-server
  "Fixture. Ensures that a fresh scsynth server is booted before
  running your tests. Kill's any pre-existing server and cleans up
  when done."
  [f]
  (kill-server)
  (boot-internal-server)
  (wait-until-mixer-booted)
  (f)
  (kill-server))

(defn with-sync-reset
  "Fixture. Ensures that the server gets reset after each test.
  Synchronously stops active nodes in the default foundation-group,
  clears the osc message queue, and kills all scheduled jobs in the
  player-pool if any."
  [f] (do (f) (sync-event :reset)))


;; Some quick tests to verify test-helpers...

(deftest eval-in-temp-ns-test
  (testing "without errors..."
    (let [ns *ns*
          ns-count (count (all-ns))]
      (eval-in-temp-ns (+ 1 2 3))
      (is (= ns-count (count (all-ns))) "removes temp-ns when done")
      (is (= ns *ns*) "restores original ns")))

  (testing "with errors..."
    (let [ns *ns*
          ns-count (count (all-ns))]
      (try (eval-in-temp-ns (/ 1 0))
           (catch java.lang.ArithmeticException e))
      (is (= ns-count (count (all-ns))) "removes temp-ns when done")
      (is (= ns *ns*) "restores original ns"))))

(deftest invoke-timeout-test
  (testing "invoke-timeout..."
    (let [n (Thread/activeCount)]
      (invoke-timeout #(Thread/sleep 100) 1000)
      (is (= n (Thread/activeCount)) "cleans up threads")
      (is (thrown? TimeoutException (invoke-timeout #(Thread/sleep 1000) 100))
          "throws TimeoutException"))))

(deftest wait-while-test
  (testing "wait-while..."

    (let [done? (atom nil)]
      (future (Thread/sleep 1000)
              (reset! done? :done))
      (wait-while #(not @done?))
      (is (= :done @done?)
          "delays the current thread"))

    (let [done? (atom nil)]
      (future (Thread/sleep 1000)
              (reset! done? :done))
      (is (thrown? TimeoutException (wait-while #(not @done?) 100))
          "throws TimeoutException"))))


(comment
  (run-tests)
  )