(ns
    ^{:doc "A representation of time for the server"
      :author "Sam Aaron"}
    overtone.sc.clock
    (:use [overtone.sc synth ugens node bus server info foundation-groups]
          [overtone.libs deps])
    (:require [overtone.sc.defaults :as defaults]))

(defonce server-clock-start-time (atom nil))
(defonce wall-clock-s (atom nil ))

;; should only be modified by a 'singleton' wall-clock synth
;; global shared state FTW!
(defonce server-clock-b (control-bus 2 "Server Clock Buses"))

;; Only one of these should ever be created...
(defonce __SERVER-CLOCK-SYNTH__
  (defsynth __internal-wall-clock__ [tick-bus-2c 0]
    (let [[b-tick s-tick] (in:kr tick-bus-2c 2)
          maxed?          (= defaults/SC-MAX-FLOAT-VAL s-tick)
          small-tick      (select:kr maxed?
                                     [(+ s-tick 1)
                                      0])
          big-tick        (pulse-count maxed?)]
      (replace-out:kr tick-bus-2c [big-tick small-tick]))))

(defn- server-clock-reset-tick-b
  []
  (control-bus-set-range! server-clock-b [0 0]))

(defn- server-clock-start
  []
  (ensure-connected!)
  (assert (foundation-timing-group) "Couldn't find timing group")
  (kill __internal-wall-clock__)
  (server-clock-reset-tick-b)
  (let [start-t (+ (System/currentTimeMillis) 500)]
    (reset! server-clock-start-time start-t)
    (at start-t (__internal-wall-clock__ [:head (foundation-timing-group)] server-clock-b))))

(defn server-clock-n-ticks
  "Returns the number of internal ticks since the server clock was
   started. The server clock is implemented internally with a synth and
   the duration of each tick is measured in terms of the server's
   control rate.

   See server-clock-uptime and server-clock-time for functions that
   return time in milliseconds."
  []
  (let [[b-t s-t] (control-bus-get-range server-clock-b 2)]
    (+ (* b-t defaults/SC-MAX-FLOAT-VAL)
       s-t)))

(defn server-clock-uptime
  "Returns the uptime of the server clock in milliseconds."
  []
  (* 1000 (server-clock-n-ticks) (server-control-dur)))

(defn server-clock-time
  "Returns the time of the server in number of milliseconds since the
   epoch. Similar to system time, although not kept in sync. See
   server-clock-drift for the difference between the system and server
   clocks."
  []
  (+ @server-clock-start-time (server-clock-uptime)))

(defn server-clock-drift
  "Returns the difference between the server's clock and the system
   clock in ms.

   The system clock is accessed with the function (now). The server
   clock is internal to a running instance of a SuperCollider server and
   is implemented with a specific synth.

   The server clock was started in absolute synchronisation with the
   system clock, however it isn't kept in sync and will drift apart over
   time. This function returns the amount of drift."
  []
  (- (System/currentTimeMillis) (server-clock-time)))

(on-deps [:foundation-groups-created :synthdefs-loaded] ::start-clock server-clock-start)
