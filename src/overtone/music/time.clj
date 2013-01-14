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
  [sched-fn] (at-at/stop sched-fn player-pool))

(defn kill-player
  "Kills scheduled fn immediately if it hasn't already executed. You
  are also able to specify player by job id - see print-schedule."
  [sched-fn] (at-at/kill sched-fn player-pool))

(def ^{:dynamic true :private true} *apply-ahead*
  "Amount of time apply-at is scheduled to execute *before* it was
  scheduled by the user. This is to give room for any computation/gc
  cycles and to allow the executing fn to schedule actions on scsynth
  ahead of time using the at macro."
  300)

(defn apply-at
  "Recursion in Time. Calls (apply f args argseq) *apply-ahead* ms
  before ms-time.

  By passing a function using #'foo syntax instead of just foo, when
  later called by the scheduler it will lookup based on the symbol
  rather than using the instance of the function defined earlier.

  (apply-at (+ dur (now)) #'my-melody arg1 arg2 [])"
  {:arglists '([ms-time f args* argseq])}
  [#^clojure.lang.IFn ms-time f & args]
  (let [delay-time (- ms-time *apply-ahead* (now))]
    (if (<= delay-time 0)
      (after-delay 0 #(apply f (#'clojure.core/spread args)))
      (after-delay delay-time #(apply f (#'clojure.core/spread args))))))

(defn show-schedule
  "Print the schedule of currently running audio players."
  []
  (at-at/show-schedule player-pool))
