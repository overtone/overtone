(ns
    ^{:doc "Functions for returning information regarding the connected SC server"}
  overtone.sc.info
  (:use [overtone.libs event]
        [overtone.sc synth gens node server]
        [overtone.util lib]))

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
  []
  (when (disconnected?)
    (throw (Exception. "Please connect to a server before attempting to ask for server-info.o")))
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
