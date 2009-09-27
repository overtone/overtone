(ns overtone.rhythm
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit
                                 PriorityBlockingQueue)
     (java.util Timer TimerTask))
  (:use (overtone utils)))

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

; Recursion in Time 
;   By passing a function using #'foo syntax instead of just foo, when later 
; called by the scheduler it will lookup based on the symbol rather than using 
; the instance of the function defined earlier.
; (callback (+ dur (now)) #'my-melody arg1 arg2)

(defn callback [ms-time func & args]
  (let [delay-time (- ms-time (now))]
    (if (< delay-time 0)
      (apply func args)
      (schedule #(apply func args) delay-time))))

; Rhythm

; A rhythm system should let us refer to time in terms of rhythmic units like beat, bar, measure,
; and it should convert these units to real time units (ms) based on the current BPM and signature settings.

(defn beat-ms
  "Convert 'b' beats to milliseconds at the given 'bpm'."
  [b bpm] (* (/ 60000 bpm) b))

;(defn bar-ms 
;  "Convert b bars to milliseconds at the current bpm."
;  ([] (bar 1))
;  ([b] (* (bar 1) (first @*signature) b)))

; A metronome is used to pull musical content (typically notes) from one or more generators.  
; tpb = ticks-per-beat
(defn metronome [bpm & [tpb]]
  (let [tpb (or tpb 1)
        start (now)
        tick (/ (beat-ms 1 bpm) tpb)]
    {:bpm   bpm
     :tpb   tpb
     :tick  tick
     :start start
     :timer (Timer.)}))

(defn timer-task [fun]
  (proxy [TimerTask] []
               (run [] (fun))))

(defn on-tick [metro fun]
  (.scheduleAtFixedRate (:timer metro) 
                        (timer-task fun) 
                        (long 0) 
                        (long (:tick metro))))

(defn stop [metro]
  (.cancel (:timer metro)))

;== Grooves
;
; A groove represents a pattern of velocities and timing modifications that is 
; applied to a sequence of notes to adjust the feel.
;
; * swing 
; * jazz groove, latin groove 
; * techno grooves (hard on beat one)
; * make something more driving, or more layed back...
