(ns overtone.examples.advanced.triggers
  "Demonstrates how it's possible to send data back from SuperCollider to
  Overtone."
  (:require [overtone.live :refer :all]))

;; simply returns an incrementing id, to distinguish different triggers
(def uid (trig-id))

;; define a synth which uses send-trig
(defsynth wawa [t-id 0]
  (let [sig (* (var-saw 0.5) (sin-osc))]
    ;; Audio output
    (out 0 sig)
    ;; Trigger output: 20 times per second, send back volume
    (send-trig (impulse 20)
               t-id
               (loudness (fft (local-buf 1024) sig)))))

(def blocks [\▏ \▎ \▍ \▌ \▋ \▊ \▉ \█])

;; register a handler fn to receive the triggers
;; This renders a very basic volume bar
(on-trigger
 uid
 (fn [val]
   (println (str "\u001B[F" ;; go to start of previous line
                 (apply str (repeat (long val) \█))
                 (get blocks (Math/floor (* 8 (- val (Math/floor val)))))
                 (apply str (repeat (- 100 (long val)) " ")))))
 ::volume-bar)

;; create a new instance of synth wawa with trigger id as a param
(wawa uid)

;;Trigger handler can be removed with:
(remove-event-handler ::volume-bar)

;; Stop the synth (and thus also the triggers)
(stop)
