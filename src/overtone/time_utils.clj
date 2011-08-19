(ns
  ^{:doc "Functions to help manage and structure computation in time."
     :author "Jeff Rose"}
  overtone.time-utils
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue))
  (:use (overtone event util)))

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
  "Creates a new pool of threads to schedule new events for. Pool size defaults to the cpu count + 2"
  ([] (make-pool (+ 2 (cpu-count))))
  ([num-threads]
     (ScheduledThreadPoolExecutor. num-threads)))

(def player-pool* (agent (make-pool)))

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

(defn- stop-and-reset-pool*
  [pool now?]
  (let [num-threads (.getCorePoolSize pool)
        new-pool (make-pool num-threads)]
    (if now?
      (.shutdownNow pool)
      (.shutdown pool))

    new-pool))

(defn stop-and-reset-pool!
  "Shuts down a given pool (passed in as an agent) either immediately or not depending on whether the optional now param is
   used. The pool is then reset to a fresh new pool preserving the original size."
  ([pool-ag] (stop-and-reset-pool! pool-ag false))
  ([pool-ag now?]
     (send-off pool-ag stop-and-reset-pool* now?)))

(on-sync-event :reset #(stop-and-reset-pool! player-pool* true) ::player-reset)

(defn stop-player [player & [now]]
  (.cancel player (or now false)))


(defn now []
  (System/currentTimeMillis))

; Recursion in Time
;   By passing a function using #'foo syntax instead of just foo, when later
; called by the scheduler it will lookup based on the symbol rather than using
; the instance of the function defined earlier.
; (apply-at (+ dur (now)) #'my-melody arg1 arg2 [])

(def APPLY-AHEAD 300)

(defn apply-at
  "Calls (apply f args argseq) APPLY-AHEAD ms before ms-time."
  {:arglists '([ms-time f args* argseq])}
  [#^clojure.lang.IFn ms-time f & args]
  (let [delay-time (- ms-time APPLY-AHEAD (now))]
    (if (<= delay-time 0)
      (apply f (#'clojure.core/spread args))
      (schedule #(apply f (#'clojure.core/spread args)) delay-time))))
