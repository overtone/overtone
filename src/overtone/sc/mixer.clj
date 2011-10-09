(ns
    ^{:doc "Overtone mixers for left, right and mono channels"
      :author "Sam Aaron"}
  overtone.sc.mixer
  (:use [overtone.libs deps event]
        [overtone.sc synth gens server info]))

(defonce bus-mixers* (ref {:in [] :out []}))

(on-event "/server-audio-clipping" (fn [msg]
                                     (println "TOO LOUD!! (audio clipped) Bus:" (int (nth (:args msg) 2))))
          ::server-audio-clipping-warner)

(defonce __BUS-MIXERS__
  (do
    (defsynth out-bus-mixer [in-bus 20 out-bus 0
                             volume 1 master-volume 1]
      (let [source  (internal:in in-bus)
            source  (* volume master-volume source)
            limited (compander source source 0.7
                               1 0.1
                               0.05 0.05)
            clipped (clip2 limited 1)]
        (send-reply (trig1 (> (a2k source) 2) 0.25) "/server-audio-clipping" out-bus)
        (internal:out out-bus limited)))

    (defsynth in-bus-mixer [in-bus 10 out-bus 0
                            gain 1 master-gain 1]
      (let [source  (internal:in in-bus)
            source  (* gain master-gain source)]
        (internal:out out-bus source)))))


(defn start-mixers
  []
  (ensure-connected!)
  (let [in-cnt          (server-num-input-buses)
        out-cnt         (server-num-output-buses)
        safe-out-offset (+ in-cnt out-cnt)
        safe-in-offset  (+ safe-out-offset out-cnt)
        out-mixers      (doall
                         (map
                          (fn [out-bus]
                            (println "in: " (+ safe-out-offset out-bus) ", out: " out-bus)
                            (out-bus-mixer :pos :head
                                           :target (main-mixer-group)
                                           :in-bus (+ safe-out-offset out-bus)
                                           :out-bus out-bus))
                          (range out-cnt)))
        in-mixers       (doall
                         (map
                          (fn [in-bus]
                            (println "in: " (+ out-cnt in-bus) ", out: " (+ safe-in-offset in-bus))
                            (in-bus-mixer :pos :head
                                          :target (main-input-group)
                                          :in-bus (+ out-cnt in-bus)
                                          :out-bus (+ safe-in-offset in-bus)))
                          (range in-cnt)))]

    (dosync
     (ref-set bus-mixers* {:in in-mixers :out out-mixers}))))

(on-deps [:core-groups-created :synthdefs-loaded] ::start-bus-mixers start-mixers)
(on-sync-event :shutdown ::reset-bus-mixers #(dosync
                                              (ref-set bus-mixers* {:in [] :out []})))
