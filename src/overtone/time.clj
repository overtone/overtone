(ns overtone.time
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue)))

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
; The ScheduledThreadPoolExecutor is supposed to use the best available clock, 
; so that's where we'll start.  Eventually we should try with the real-time JVM if
; it seems that timing isn't accurate enough.

(def NUM-PLAYER-THREADS 10)
(def *player-pool* (ScheduledThreadPoolExecutor. NUM-PLAYER-THREADS))

(defn schedule 
  "Schedules fun to be executed after ms-delay milliseconds."
  [fun ms-delay]
  (.schedule *player-pool* fun (long ms-delay) TimeUnit/MILLISECONDS))

(defn periodic 
  "Calls fun every ms-period, and takes an optional initial-delay for the first call."

  [fun ms-period & [initial-delay]]
  (let [initial-delay (if initial-delay 
                        (long initial-delay)
                        (long 0))]
    (.scheduleAtFixedRate *player-pool* fun initial-delay (long ms-period) TimeUnit/MILLISECONDS)))

(defn stop-players [& [now]]
  (if now
    (.shutdownNow *player-pool*)
    (.shutdown *player-pool*))
  (def *player-pool* (ScheduledThreadPoolExecutor. NUM-PLAYER-THREADS)))

(defn stop-player [player & [now]]
  (.cancel player (or now false)))

; Timer based "recursion in time" 
;  The idea is to create something along the lines of impromptu, except do it
; using a macro and an exception.
;  * save the arguments inside the exception object
;  * throw the exception to pop off the call-stack to some base level
;  * in the exception handler lookup which function threw the exception and store it in the exception too
;  * add an event to the task queue for the specified time in the future
;  * when event fires call the function with the arguments saved in the exception 

; By passing a function using #'foo syntax instead of just foo, when later called by the scheduler
; it will lookup based on the symbol rather than using the instance of the function defined earlier.
; (callback (+ dur (now)) #'my-melody arg1 arg2)

(defn callback [ms func & args]
  (schedule #(apply func args) ms))

; Rhythm

; A rhythm system should let us refer to time in terms of rhythmic units like beat, bar, measure,
; and it should convert these units to real time units (ms) based on the current BPM and signature settings.

(def *bpm (ref 120))           ; beats per minute
(def *signature (ref [4 4]))     ; time signature

(defn bpm
  ([] @*bpm)
  ([new-bpm] (dosync (ref-set *bpm new-bpm))))

(defn signature 
  ([] @*signature)
  ([per-bar note-val] (dosync (ref-set *signature [per-bar note-val]))))

(defn beat 
  "Convert b beats to milliseconds at the current bpm."
  ([] (beat 1))
  ([b] (* (/ 60000 @*bpm) b)))

(defn bar 
  "Convert b bars to milliseconds at the current bpm."
  ([] (bar 1))
  ([b] (* (bar 1) (first @*signature) b)))

