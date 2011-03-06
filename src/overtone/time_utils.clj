(ns
  ^{:doc "Functions to help manage and structure computation in time."
     :author "Jeff Rose"}
  overtone.time-utils
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue))
  (:use (overtone event)))

; Time

; NOTES:
; System "time of day" clock => ms since the epoch (00:00 of Jan. 1, 1970)
; Accurate to the OS clock interrupt interval, which is typically about 10ms.
;(System/currentTimeMillis)

; Uses the highest resolution timer available, returning a free-running count
; in nanoseconds, although resolution is typically on the order of microseconds.
; Only valid when compared with other nanoTime values, so used for event timing.
;(System/nanoTime)
;
; Apparently there is also this un-documented class, which uses the CPU counter
; directly, and it's available on all Sun JVM supported platforms.
;
; (def perf (sun.misc.Perf/getPerf))
;
; resolution in ticks per second
; (.highResFrequency perf) => 1000000 (mhz) on my laptop
;
; current number of ticks since start of this JVM
; (.highResCounter perf)
;
; The ScheduledThreadPoolExecutor is supposed to use the best available clock,
; so that's where we'll start.  Eventually we should try with the real-time JVM if
; it seems that timing isn't accurate enough.

(defn make-pool
  "creates a new pool of threads to schedule new events for. Defaults to 10 threads."
  ([] (make-pool 10))
  ([num-threads]
     (ScheduledThreadPoolExecutor. num-threads)))

(def player-pool* (atom (make-pool)))

(defn now []
  (System/currentTimeMillis))

(defn schedule
  "Schedules fun to be executed after ms-delay milliseconds. Pool defaults to the player-pool."
  ([fun ms-delay] (schedule fun ms-delay @player-pool*))
  ([fun ms-delay pool]
     (.schedule pool fun (long ms-delay) TimeUnit/MILLISECONDS)))

(defn periodic
  "Calls fun every ms-period, and takes an optional initial-delay for the first call in ms. Pool defaults to the player-pool."
  ([fun ms-period] (periodic fun ms-period 0))
  ([fun ms-period initial-delay] (periodic fun ms-period initial-delay @player-pool*))
  ([fun ms-period initial-delay pool]
     (let [initial-delay (long initial-delay)
           ms-period     (long ms-period)]
       (.scheduleAtFixedRate pool fun initial-delay ms-period TimeUnit/MILLISECONDS))))

(defn stop-and-reset-pool!
  "Shuts down a given pool (passed in as an atom) either immediately or not depending on whether the optional now param is used.
   The pool is then reset to a fresh new pool preserving the original size."
  [pool-ref & [now]]
  (let [pool @pool-ref
        num-threads (.getCorePoolSize pool)]
    (reset! pool-ref (make-pool num-threads))
    (if now
      (.shutdownNow pool)
      (.shutdown pool))))
  (on-sync-event :reset ::player-reset #(stop-and-reset-pool! player-pool* true ))


(defn stop-player [player & [now]]
  (.cancel player (or now false)))

; Recursion in Time
;   By passing a function using #'foo syntax instead of just foo, when later
; called by the scheduler it will lookup based on the symbol rather than using
; the instance of the function defined earlier.
; (apply-at (+ dur (now)) #'my-melody arg1 arg2 [])

(def *APPLY-AHEAD* 150)

(defn apply-at
  "Calls (apply f args argseq) as soon after ms-time (timestamp in milliseconds) as possible."
  {:arglists '([ms-time f args* argseq])}
  [ms-time #^clojure.lang.IFn f & args]
  (let [delay-time (- ms-time *APPLY-AHEAD* (now))]
    (if (<= delay-time 0)
      (apply f (#'clojure.core/spread args))
      (schedule #(apply f (#'clojure.core/spread args)) delay-time))))

