(ns
  ^{:doc "Functions to help manage and structure computation in time."
     :author "Jeff Rose and Sam Aaron"}
  overtone.music.time
  (:use [overtone.libs event]
        [overtone.helpers lib])
  (:require [overtone.at-at :as at-at]))

;;Scheduled thread pool (created by at-at) which is to be used by default for
;;all scheduled musical functions (players).
(defonce player-pool (at-at/mk-pool))

(defn now
  "Returns the current time in ms"
  []
  (System/currentTimeMillis))

(defn after-delay
  "Schedules fun to be executed after ms-delay milliseconds. Pool
  defaults to the player-pool."
  ([ms-delay fun] (after-delay ms-delay fun "Overtone delayed fn"))
  ([ms-delay fun description]
     (at-at/at (+ (now) ms-delay) fun player-pool :desc description)))

(defn periodic
  "Calls fun every ms-period, and takes an optional initial-delay for
  the first call in ms."
  ([ms-period fun] (periodic ms-period fun 0))
  ([ms-period fun initial-delay] (periodic ms-period fun initial-delay "Overtone periodic fn"))
  ([ms-period fun initial-delay description]
     (at-at/every ms-period
                  fun
                  player-pool
                  :initial-delay initial-delay
                  :desc description)))

(defn interspaced
  "Calls fun repeatedly with an interspacing of ms-period, i.e. the next
   call of fun will happen ms-period milliseconds after the completion
   of the previous call. Also takes an optional initial-delay for the
   first call in ms."
  ([ms-period fun] (interspaced ms-period fun 0))
  ([ms-period fun initial-delay] (interspaced ms-period fun initial-delay "Overtone interspaced fn"))
  ([ms-period fun initial-delay description]
     (at-at/interspaced ms-period
                        fun
                        player-pool
                        :initial-delay initial-delay
                        :desc description)))

;;Ensure all scheduled player fns are stopped when Overtone is reset
;;(typically triggered by a call to stop)
(on-sync-event :reset
               (fn [event-info] (at-at/stop-and-reset-pool! player-pool
                                                           :strategy :kill))
               ::player-reset)

(defn stop-player
  "Stop scheduled fn gracefully if it hasn't already executed."
  [sched-fn]
  (if (number? sched-fn)
    (at-at/stop sched-fn player-pool)
    (at-at/stop sched-fn)))

(defn kill-player
  "Kills scheduled fn immediately if it hasn't already executed. You
  are also able to specify player by job id - see print-schedule."
  [sched-fn]
  (if (number? sched-fn)
    (at-at/kill sched-fn player-pool)
    (at-at/kill sched-fn)))

(def ^{:dynamic true :private true} *apply-ahead*
  "Amount of time apply-by is scheduled to execute *before* it was
  scheduled by the user. This is to give room for any computation/gc
  cycles and to allow the executing fn to schedule actions on scsynth
  ahead of time using the at macro."
  300)

(defn apply-by
  "Ahead-of-schedule function appliction. Works identically to
   apply, except that it takes an additional initial argument:
   ms-time. If ms-time is in the future, function application is delayed
   until *apply-ahead* ms before that time, if ms-time is in the past
   function application is immediate.

   Can be used to implement the 'temporal recursion' pattern. This is
   where a function has a call to apply-by at its tail:

   (defn foo
     [t freq]
     (at t (my-synth freq))
     (let [next-t (+ t 200)
           next-f (+ freq 100]
       (apply-by next-t #'foo [next-t next-f])))

   (foo (now) 100)

   The fn foo is written in a recursive style, yet the recursion is
   scheduled for application 200ms in the future. By passing a function
   using #'foo syntax instead of the symbole foo, when later called by
   the scheduler it will lookup based on the symbol rather than using
   the instance of the function defined earlier. This allows us to
   redefine foo whilst the temporal recursion is continuing to execute.

   Note that by using apply-by, we can schedule events to happen at
   exactly time t within the body of the fn, as the scheduled recursion
   of the fn itself happens ahead of t. apply-by is therefore typically
   used in conjunction with the at macro for scheduling SuperCollider
   server events.

   To stop an executing temporal recursion pattern, either redefine the
   function to not call itself, or use (stop)."
  {:arglists '([ms-time f args* argseq])}
  [#^clojure.lang.IFn ms-time f & args]
  (let [delay-time (- ms-time *apply-ahead* (now))]
    (if (<= delay-time 0)
      (after-delay 0 #(apply f (#'clojure.core/spread args)))
      (after-delay delay-time #(apply f (#'clojure.core/spread args))))))

(defn apply-at
  "Scheduled function appliction. Works identically to apply, except
   that it takes an additional initial argument: ms-time. If ms-time is
   in the future, function application is delayed until that time, if
   ms-time is in the past function application is immediate.

   Can be used to implement the 'temporal recursion' pattern. This is
   where a function has a call to apply-at at its tail:

   (defn foo
     [t val]
     (println val)
     (let [next-t (+ t 200)]
       (apply-at next-t #'foo [next-t (inc val)])))

   (foo (now) 0) ;=> 0, 1, 2, 3...

   The fn foo is written in a recursive style, yet the recursion is
   scheduled for application 200ms in the future. By passing a function
   using #'foo syntax instead of the symbole foo, when later called by
   the scheduler it will lookup based on the symbol rather than using
   the instance of the function defined earlier. This allows us to
   redefine foo whilst the temporal recursion is continuing to execute.

   To stop an executing temporal recursion pattern, either redefine the
   function to not call itself, or use (stop)."
  {:arglists '([ms-time f args* argseq])}
  [#^clojure.lang.IFn ms-time f & args]
  (let [delay-time (- ms-time (now))]
    (if (<= delay-time 0)
      (after-delay 0 #(apply f (#'clojure.core/spread args)))
      (after-delay delay-time #(apply f (#'clojure.core/spread args))))))

(defn show-schedule
  "Print the schedule of currently running audio players."
  []
  (at-at/show-schedule player-pool))
