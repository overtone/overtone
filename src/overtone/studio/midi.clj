(ns overtone.studio.midi
  (:use [overtone.sc node]
        [overtone midi]
        [overtone.at-at :only (mk-pool every)]
        [overtone.libs.event])
  (:require [overtone.config.log :as log]))

(defonce MIDI-POOL (mk-pool))
(defonce midi-devices* (atom {}))

(def EXCLUDED-DEVICES #{"Real Time Sequencer" "Java Sound Synthesizer"})

(defn midi-event
  "Trigger a global midi event."
  [dev msg ts]
  (let [command (or (:cmd msg)
                    (:command msg))]
    (event [:midi command] msg)
    (event [:midi-device (dev :vendor) (dev :name) (dev :description) command] msg)))

(defn- detect-midi-devices
  "Designed to run periodically and update the midi-devices* atom with
  the latest list of midi sources and add event handlers to new devices."
  []
  (try
    (let [old-devs     (set (keys @midi-devices*))
          devs         (midi-sources)
          devs         (filter #(not (old-devs (:device %))) devs)
          receivers    (doall (filter
                               (fn [dev]
                                 (try
                                   (midi-handle-events (midi-in dev) #(midi-event dev %1 %2))
                                   true
                                   (catch Exception e
                                     (log/warn "Can't listen to midi device: " dev "\n" e)
                                     false)))
                               devs))
          device-names (map :name devs)
          n-devs       (count device-names)
          dev-map      (apply hash-map (interleave (map :device devs) receivers))]
      (swap! midi-devices* merge dev-map)
      (when (pos? n-devs)
         (log/info "Connected " n-devs " midi devices: " device-names)))
    (catch Exception ex
      (println "Got exception in detect-midi-devices!" ex))))

; The rate at which we poll for new midi devices
(def MIDI-POLL-RATE 2000)

(defonce __DEVICE-POLLER__
  (every MIDI-POLL-RATE #'detect-midi-devices MIDI-POOL :desc "Check for new midi devices"))

(defn midi-poly-player
  "Sets up the event handlers and manages synth instances to easily play
  a polyphonic instrument with a midi controller.  The play-fn should
  take the note and velocity as the only two arguments, and the synth
  should have a gate parameter that can be set to zero when a :note-off
  event is received.

    (definst ding
      [note 60 velocity 100 gate 1]
      (let [freq (midicps note)
            amp  (/ velocity 127.0)
            snd  (sin-osc freq)
            env  (env-gen (adsr 0.001 0.1 0.6 0.3) gate :action FREE)]
        (* amp env snd)))

    (def dinger (midi-poly-player ding))"
  [play-fn]
  (let [notes*  (atom {})
        on-key  (keyword (gensym 'note-on))
        off-key (keyword (gensym 'note-off))]
    (on-event [:midi :note-on] (fn [{note :note velocity :velocity}]
                                 (swap! notes* assoc note (play-fn note velocity)))
              on-key)
    (on-event [:midi :note-off] (fn [{note :note velocity :velocity}]
                                  (let [n (get @notes* note)]
                                    (node-control n :gate 0))
                                  (swap! notes* dissoc note))
              off-key)
    {:notes* notes*
     :on-key on-key
     :off-key off-key}))
