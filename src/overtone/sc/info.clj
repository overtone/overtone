(ns
    ^{:doc "Functions for returning information regarding the connected SC server"
      :author "Sam Aaron"}
  overtone.sc.info
  (:use [overtone.libs event counters]
        [overtone.sc synth ugens node server]
        [overtone.sc.machinery.allocator]
        [overtone.helpers lib]))

(defonce output-bus-count* (atom nil))
(defonce input-bus-count*  (atom nil))
(defonce audio-bus-count*  (atom nil))
(defonce buffer-count*     (atom nil))
(defonce sample-rate*      (atom nil))

(defonce __SERVER-INFO__
  (defsynth snd-server-info
    [response-id -1]
    (send-reply (impulse 2)
                "/server-info"
                [(sample-rate)
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
                 (num-running-synths)]
                response-id)))

(defn server-info
  "Fetches a bunch of useful server info. Has to trigger and poll a synth to
  fetch data. See #'server-num-output-buses, #'server-num-input-buses,
  #'server-num-audio-buses and #'sever-num-buffers for fast cached versions
  of the static values in this info map. Note, the number of running synths
  will also include the synth used to obtain this information."
  []
  (ensure-connected!)
  (let [prom        (promise)
        response-id (next-id :response-id)]
    (on-event
     "/server-info"
     (fn [msg]
       (let [args (:args msg)
             [nid nrid sr sd rps cr cd sso nob nib nab ncb nb nrs] args]
         (when (= (int nrid) (int response-id))
            (deliver prom
                    {:sample-rate (long sr)
                     :sample-dur sd
                     :radians-per-sample rps
                     :control-rate cr
                     :control-dur cd
                     :subsample-offset sso
                     :num-output-buses (long nob)
                     :num-input-buses (long nib)
                     :num-audio-buses (long nab)
                     :num-buffers (long nb)
                     :num-running-synths (long nrs)})
            :overtone/remove-handler)))
     (keyword (str "overtone.sc.info/get-server-info_" (gensym))))
    (let [synth-id (snd-server-info response-id)
          res      (deref! prom)]
      (kill synth-id)
      res)))

(defn server-sample-rate
  "Returns the sample rate of the server. This number is cached for a given
  running server for the duration of boot"
  []
  (if-let [rate @sample-rate*]
    rate
    (let [info (server-info)]
      (reset! sample-rate* (:sample-rate info)))))

(defn server-num-output-buses
  "Returns the number of output buses accessible by the server. This number may
  change depending on host architecture but is cached for a given running server
  for the duration of boot."
  []
  (if-let [cnt @output-bus-count*]
    cnt
    (let [info (server-info)]
      (reset! output-bus-count*  (:num-output-buses info)))))

(defn server-num-input-buses
  "Returns the number of input buses accessible by the server. This number may
  change depending on host architecture but is cached for a given running
  server for the duration of boot."
  []
  (if-let [cnt @input-bus-count*]
    cnt
    (let [info (server-info)]
      (reset! input-bus-count*  (:num-input-buses info)))))

(defn server-num-audio-buses
  "Returns the number of audio buses accessible by the server. This number may
  change depending on host architecture but is cached for a given running server
  for the duration of boot."
  []
  (if-let [cnt @audio-bus-count*]
    cnt
    (let [info (server-info)]
      (reset! audio-bus-count*  (:num-audio-buses info)))))

(defn server-num-buffers
  "Returns the number of buffers accessible by the server. This number may
  change depending on host architecture but is cached for a given running server
  for the duration of boot."
  []
  (if-let [cnt @buffer-count*]
    cnt
    (let [info (server-info)]
      (reset! buffer-count*  (:num-buffers info)))))

(on-sync-event :shutdown (fn [event-info]
                           (reset! output-bus-count* nil)
                           (reset! input-bus-count* nil)
                           (reset! audio-bus-count* nil)
                           (reset! buffer-count* nil))
               ::reset-cached-server-info)
