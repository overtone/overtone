(ns
    ^{:doc    "A mock of `overtone.music.time` for :link clock"
      :author "Hlöðver Sigurðsson"}
    overtone.music.time-ableton-link
  (:use [overtone.libs event]
        [overtone.helpers lib])
  (:require [overtone.ableton-link :as link]
            [overtone.sc.protocols :as protocols]))


;;Scheduled thread pool (created by at-at) which is to be used by default for
;;all scheduled musical functions (players).
;; (defonce player-pool (at-at/mk-pool))

(defn now
  "Returns the current time in ms.
   Used internally in Overtone. If using
   overtone.link, refer to `get-beat`"
  []
  (System/currentTimeMillis))



(defn get-beat
  "Return the current link beat,
   analogous to `now`."
  []
  (link/get-beat))

(defn after-delay
  "Schedules fun to be executed after beat-delay in beats."
  [beat-delay fun]
  (link/after-delay beat-delay fun))

(defn periodic
  "Calls fun every ms-period, and takes an optional initial-delay for
  the first call in ms."
  ([beat-period fun]
   (periodic beat-period fun 0))
  ([beat-period fun initial-delay]
   (link/every beat-period :initial-delay 0)))

(defn interspaced
  "Not implemented, just throws an error"
  (throw (Error. (str "interspaced is not yet implemented with ableton-link, "
                      "use overtone.music.time/interspaced for at-at implementation."))))

(on-sync-event :reset
               (fn [event-info]
                 (link/enable-link false)
                 (link/enable-link true))
               ::player-reset)

(defn stop-player
  "Stop scheduled fn gracefully if it hasn't already executed."
  [& scheduled-events]
  (apply link/stop scheduled-events))

(defn kill-player
  "Clears all scheduled link events aggressively."
  []
  (link/stop-all))

(def ^{:dynamic true :private true} *apply-ahead*
  "Amount of beats apply-by is scheduled to execute *before* it was
  scheduled by the user. This is to give room for any computation/gc
  cycles and to allow the executing fn to schedule actions on scsynth
  ahead of time using the at macro."
  0.5)

(defn apply-by
  "Ahead-of-schedule function appliction. Works identically to
   apply, except that it takes an additional initial argument:
   ms-time. If ms-time is in the future, function application is delayed
   until *apply-ahead* ms before that time, if ms-time is in the past
   function application is immediate.

   If you wish to apply at a specific time rather than slightly before
   it, see apply-at.

   Can be used to implement the 'temporal recursion' pattern. This is
   where a function has a call to apply-by at its tail:

   (defn foo
     [t freq]
     (at t (my-synth freq))
     (let [next-t (+ t 200)
           next-f (+ freq 100)]
       (apply-by next-t #'foo [next-t next-f])))

   (foo (get-beat) 100)

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
  {:arglists '([beats-delay f args* argseq])}
  [#^clojure.lang.IFn beats-delay f & args]
  (let [delay-time (- beats-delay *apply-ahead* (get-beat))]
    (if (<= delay-time 0)
      (link/after 0 #(apply f (#'clojure.core/spread args)))
      (link/after delay-time #(apply f (#'clojure.core/spread args))))))

(defn apply-at
  "Scheduled function application. Works identically to apply, except
   that it takes an additional initial argument: ms-time. If ms-time is
   in the future, function application is delayed until that time, if
   ms-time is in the past function application is immediate.

   If you wish to apply slightly before specific time rather than
   exactly at it, see apply-by.

   Can be used to implement the 'temporal recursion' pattern. This is
   where a function has a call to apply-at at its tail:

   (defn foo
     [t val]
     (println val)
     (let [next-t (+ t 200)]
       (apply-at next-t #'foo [next-t (inc val)])))

   (foo (get-beat) 0) ;=> 0, 1, 2, 3...

   The fn foo is written in a recursive style, yet the recursion is
   scheduled for application 200ms in the future. By passing a function
   using #'foo syntax instead of the symbole foo, when later called by
   the scheduler it will lookup based on the symbol rather than using
   the instance of the function defined earlier. This allows us to
   redefine foo whilst the temporal recursion is continuing to execute.

   To stop an executing temporal recursion pattern, either redefine the
   function to not call itself, or use (stop)."
  {:arglists '([beats-delay f args* argseq])}
  [#^clojure.lang.IFn beats-delay f & args]
  (let [delay-time (- beats-delay (get-beat))]
    (if (<= delay-time 0)
      (link/after 0 #(apply f (#'clojure.core/spread args)))
      (link/after after delay-time #(apply f (#'clojure.core/spread args))))))

(defn show-schedule
  "Print the schedule of currently running audio players."
  []
  ;;(at-at/show-schedule player-pool)
  )

#_(extend-protocol protocols/IKillable
    overtone.at_at.RecurringJob
    (kill* [job] (stop-player job))

    overtone.at_at.ScheduledJob
    (kill* [job] (stop-player job)))
