(ns devices.axiom
  (:use overtone.live))

; NOTES:
;   The midi system can get a bit smarter.  If each device profile also includes the device string that
;   will be available from midi device itself, then we can start doing some auto-setup of handlers etc...
;
; * midi faders (left to right) :chan 0, :cmd 176, :data1 [71 - x] :data2 (slider value)
; * keys :chan 0, :cmd 144, :data1 [36 - 96] :data2 (velocity)

(defn axiom []
  (midi-in "axiom"))

;(defsynth trancy-waves [note 40 amp 0.8 gate 1
;                        a 0.01 s 0.4 r 0.3]
;  (* amp (env-gen (asr a s r) gate 1 0 1 :free)
;     (rlpf
;        (+ (saw (midicps note))
;        (saw (midicps (+ 9 note)))
;        (sin-osc (midicps (- 12 note))))
;        (midicps note) 0.6)))
;
;(def note-synths* (ref {}))
;
;(defn midi-player [event ts]
;  (if (zero? (:vel event))
;    (do
;      (ctl (get @note-synths* (:note event)) :gate 0)
;      (dosync (alter note-synths* dissoc (:note event))))
;    (dosync (alter note-synths* assoc (:note event)
;                   (trancy-waves (:note event) (/ (:vel event) 128.0))))))
;
;(midi-handle-events axiom #'midi-player)

;(def midi-log* (ref []))
;
;(defn msg-logger [msg tstamp]
;  (dosync (alter midi-log* conj msg)))
;
;(def ctl-map* (ref {}))
;
;(defn ctl-handler [msg tstamp]
;  (if (contains? @ctl-map* (:data1 msg))
;    (dosync (ref-set (get @ctl-map* (:data1 msg)) (:data2 msg)))))
;
;(defn note-handler [msg tstamp]
;  (if (and (= 0 (:chan msg)) (= 144 (:cmd msg)))
;    (let [freq (midi-hz (:data1 msg))
;          amp (* 0.5 (/ (:data2 msg) 128))
;          wah (* 30 (/ @diiiing-wah* 128))
;          depth (* 60 (/ @diiiing-depth* 128))]
;      (dosync (alter midi-log* conj [freq amp]))
;      (hit (now) :diiiing :freq freq :amp amp :wah wah :depth depth))))
;
;(defn jam-handler [msg tstamp]
;  (msg-logger msg tstamp)
;  (note-handler msg tstamp)
;  (ctl-handler msg tstamp))
;
;; Registers a handler function with the midi system for this input device.
;(midi-handle-events axiom jam-handler)
