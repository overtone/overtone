(ns
  ^{:doc "Functions to help manage and structure computation in time."
     :author "Jeff Rose and Sam Aaron"}
  overtone.music.time
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue))
  (:use [overtone.libs event]
        [overtone.util lib])
  (:require [overtone.at-at :as at-at]))

(defonce player-pool (at-at/mk-pool))

(defn now
  "Returns the current time in ms"
  []
  (System/currentTimeMillis))

(defn after-delay
  "Schedules fun to be executed after ms-delay milliseconds. Pool defaults to the player-pool."
  [ms-delay fun] (at-at/at (+ (now) ms-delay) fun) player-pool)

(defn periodic
  "Calls fun every ms-period, and takes an optional initial-delay for the first call in ms. Pool defaults to the player-pool."
  ([ms-period fun] (periodic ms-period fun 0))
  ([ms-period fun initial-delay]
     (at-at/every ms-period fun initial-delay player-pool)))

(on-sync-event :reset #(at-at/stop-and-reset-pool! player-pool true) ::player-reset)

(defn stop-player
  "Stop scheduled fn if it hasn't already executed."
  ([sched-fn] (at-at/cancel sched-fn))
  ([sched-fn cancel-immediately?] (at-at/cancel sched-fn cancel-immediately?)))

; Recursion in Time
;   By passing a function using #'foo syntax instead of just foo, when later
; called by the scheduler it will lookup based on the symbol rather than using
; the instance of the function defined earlier.
; (apply-at (+ dur (now)) #'my-melody arg1 arg2 [])

(def ^{:dynamic true} *apply-ahead* 300)

(defn apply-at
  "Calls (apply f args argseq) *apply-ahead* ms before ms-time."
  {:arglists '([ms-time f args* argseq])}
  [#^clojure.lang.IFn ms-time f & args]
  (let [delay-time (- ms-time *apply-ahead* (now))]
    (if (<= delay-time 0)
      (.execute @player-pool* #(apply f (#'clojure.core/spread args)))
      (after-delay delay-time #(apply f (#'clojure.core/spread args))))))
