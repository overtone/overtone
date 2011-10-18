(ns examples.midi
  (:use overtone.live))

(refer-ugens)
(def kb (midi-in "axiom"))

(defsynth pad [freq 440 vel 0.4 amt 0.3 gate 1.0]
  (let [vel        (+ 0.5 (* 0.5 vel))
        env        (env-gen (adsr 0.01 0.1 0.7 0.1) gate 1 0 1 FREE)
        f-env      (env-gen (perc 1 3))
        src        (saw [freq (* freq 1.01)])
        signal     (rlpf (* 0.3 src)
                         (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)
        k          (/ (* 2 amt) (- 1 amt))
        distort    (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
        gate       (pulse (* 2 (+ 1 (sin-osc:kr 0.05))))
        compressor (compander distort gate 0.01 1 0.5 0.01 0.01)
        dampener   (+ 1 (* 0.5 (sin-osc:kr 0.5)))
        reverb     (free-verb compressor 0.5 0.5 dampener)
        echo       (comb-n reverb 0.4 0.3 0.5)]
    (* vel env echo)))

(def midi-log* (ref []))
(def controls* (ref {}))
(def notes* (ref {}))

(defn midi-player [event ts]
  (dosync (alter midi-log* conj event))
  (pad (midi->hz (:note event)) 0.8 (/ (get @controls* 71) 127.0)))

(midi-handle-events kb #'midi-player)

(defn midi-listener [event ts]
  (println "listener: " event)
  (try
    (dosync (alter midi-log* conj event))
    (condp = (:cmd event)
      :control-change (dosync (alter controls* assoc (:data1 event) (:data2 event)))
      :note-on (let [note (:note event)
                     id   (get @notes* note)]
                 (if id (ctl id :gate 0))
                 (dosync (alter notes* assoc note
                                (pad (midi->hz note)
                                     (/ (:vel event) 128.0)
                                     (/ (get @controls* 71) 127.0)))))
      :note-off (ctl (get @notes* (:note event)) :gate 0)
      true)
    (catch java.lang.Exception e (println "midi-listener exception: \n" e))))

(defn control-watcher [ctl-num, _, old-val, new-val]
  (println ctl-num ":" new-val))

(midi-handle-events kb #'midi-listener)

(midi-handle-events kb (fn [event ts] (dosync (alter conj midi-log* event))))

(defn midi-handle-events
  "Specify a single handler that will receive all midi events from the input device."
  [input fun]
  (let [receiver (proxy [Receiver] []
                   (close [] nil)
                   (send [msg timestamp] (dosync (alter midi-log* conj msg))))]
    (.setReceiver (:transmitter input) receiver)))
