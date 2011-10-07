(ns
    ^{:doc "Functions for returning information regarding the connected SC server"}
  overtone.sc.info
  (:use [overtone.libs event]
        [overtone.sc synth gens node server]
        [overtone.util lib]))

(defonce output-bus-count* (ref nil))
(defonce input-bus-count* (ref nil))
(defonce audio-bus-count* (ref nil))
(defonce buffer-count* (ref nil))

(defonce __SERVER-INFO__
  (defsynth snd-server-info
    []
    (send-reply (impulse 2) "/server-info" [(sample-rate)
                                            (sample-dur)
                                            (radians-per-sample)
                                            (control-rate)
                                            (control-dur)
                                            (subsample-offset)
                                            (num-output-buses)
                                            (num-input-buses)
                                            (num-audio-buses)
                                            (num-control-buses)
                                            (num-buffers)
                                            (num-running-synths)])))

(defn server-info
  "Fetches a bunch of useful server info. Has to trigger and poll a synth to
  fetch data. See #'server-num-output-buses, #'server-num-input-buses,
  #'server-num-audio-buses and #'sever-num-buffers for fast cached versions
  of the static values in this info map."
  []
  (when (disconnected?)
    (throw (Exception. "Please connect to a server before attempting to ask for server-info.")))
  (let [prom (promise)]
    (on-event "/server-info"
              (fn [msg]
                (let [args (:args msg)
                      [nid nrid sr sd rps cr cd sso nob nib nab ncb nb nrs] args]
                  (deliver prom
                           {:sample-rate sr
                            :sample-dur sd
                            :radians-per-sample rps
                            :control-rate cr
                            :control-dur cd
                            :subsample-offset sso
                            :num-output-buses nob
                            :num-input-buses nib
                            :num-audio-buses nab
                            :num-buffers nb
                            :num-running-synths nrs})
                  :done))
              ::num-control-buses)
    (let [synth-id (snd-server-info)
          res (deref! prom)]
      (kill synth-id)
      res)))

(defn server-num-output-buses
  "Returns the number of output buses accessible by the server. This number may
  change depending on host architecture but is static for a given running server
  for the duration of boot."
  []
  (if-let [cnt @output-bus-count*]
    cnt
    (let [info (server-info)]
      (dosync
       (ref-set output-bus-count*  (:num-output-buses info))))))

(defn server-num-input-buses
  "Returns the number of input buses accessible by the server. This number may
  change depending on host architecture but is static for a given running
  server for the duration of boot."
  []
  (if-let [cnt @input-bus-count*]
    cnt
    (let [info (server-info)]
      (dosync
       (ref-set input-bus-count*  (:num-input-buses info))))))

(defn server-num-audio-buses
  "Returns the number of audio buses accessible by the server. This number may
  change depending on host architecture but is static for a given running server
  for the duration of boot."
  []
  (if-let [cnt @audio-bus-count*]
    cnt
    (let [info (server-info)]
      (dosync
       (ref-set audio-bus-count*  (:num-audio-buses info))))))

(defn server-num-buffers
  "Returns the number of buffers accessible by the server. This number may
  change depending on host architecture but is static for a given running server
  for the duration of boot."
  []
  (if-let [cnt @buffer-count*]
    cnt
    (let [info (server-info)]
      (dosync
       (ref-set buffer-count*  (:num-buffers info))))))

(on-sync-event :shutdown #(dosync
                           (ref-set output-bus-count* nil)
                           (ref-set input-bus-count* nil)
                           (ref-set audio-bus-count* nil)
                           (ref-set buffer-count* nil))
               ::reset-cached-server-info)
