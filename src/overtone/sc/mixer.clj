(ns
    ^{:doc "Overtone mixers for left, right and mono channels"
      :author "Sam Aaron"}
  overtone.sc.mixer
  (:use [overtone.libs deps event]
        [overtone.sc synth gens server info node]
        [overtone.sc.machinery defaults]))

(defonce master-vol*  (ref MASTER-VOL))
(defonce master-gain* (ref MASTER-GAIN))
(defonce bus-mixers*  (ref {:in [] :out []}))

(add-watch master-vol*
           ::update-vol-on-server
           (fn [k r old new-vol]
             (println "updating volume to " new-vol (main-mixer-group))
             (ctl (main-mixer-group) :master-volume new-vol)))

(add-watch master-gain*
           ::update-gain-on-server
           (fn [k r old new-gain]
             (ctl (main-input-group) :master-gain new-gain)))

(on-event "/server-audio-clipping-rogue-input"
          (fn [msg]
            (println "TOO LOUD!! (clipped) Bus:"
                     (int (nth (:args msg) 2))
                     "- fix source synth"))
          ::server-audio-clipping-warner-input)

(on-event "/server-audio-clipping-rogue-vol"
          (fn [msg]
            (println "TOO LOUD!! (clipped) Bus:"
                     (int (nth (:args msg) 2))
                     "- lower master vol") )
          ::server-audio-clipping-warner-vol)

(defonce __BUS-MIXERS__
  (do
    (defsynth out-bus-mixer [in-bus 20 out-bus 0
                             volume 0.5 master-volume @master-vol*]
      (let [source        (internal:in in-bus)
            source        (clip2 source 2)
            not-safe?     (> (a2k source) 1)
            limited       (compander source source 0.7
                                     1 0.1
                                     0.05 0.05)
            std-clipped   (clip2 limited 1)
            safe-clipped  (clip2 limited 0.1)
            safe-snd      (select not-safe? [std-clipped safe-clipped])
            amplified-snd (* volume master-volume safe-snd)
            final-snd     (clip2 amplified-snd 1)]
        (send-reply (trig1 (> (a2k amplified-snd) 1) 0.25)
                    "/server-audio-clipping-rogue-vol"
                    out-bus)
        (send-reply (trig1 not-safe? 0.25)
                    "/server-audio-clipping-rogue-input"
                    out-bus)
        (internal:out out-bus final-snd)))

    (defsynth in-bus-mixer [in-bus 10 out-bus 0
                            gain 1 master-gain @master-gain*]
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
                            (out-bus-mixer :pos :head
                                           :target (main-mixer-group)
                                           :in-bus (+ safe-out-offset out-bus)
                                           :out-bus out-bus))
                          (range out-cnt)))
        in-mixers       (doall
                         (map
                          (fn [in-bus]
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

(defn volume
  "Set the volume on the master mixer. When called with no params, retrieves the
   current value"
  ([] @master-vol*)
  ([vol] (dosync (ref-set master-vol* vol))))

(defn input-gain
  "Set the input gain on the master mixer. When called with no params, retrieves
  the current value"
  ([] @master-gain*)
  ([gain] (dosync (ref-set master-gain* gain))))
