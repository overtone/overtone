(ns
    ^{:doc "Overtone mixers for left, right and mono channels"
      :author "Sam Aaron"}
  overtone.sc.mixer
  (:use [overtone.libs deps event]
        [overtone.helpers file]
        [overtone.sc synth gens server info node buffer]
        [overtone.sc.machinery defaults]))

(defonce master-vol*  (ref MASTER-VOL))
(defonce master-gain* (ref MASTER-GAIN))
(defonce bus-mixers*  (ref {:in [] :out []}))

(add-watch master-vol*
           ::update-vol-on-server
           (fn [k r old new-vol]
             (ctl (main-mixer-group) :master-volume new-vol)))

(add-watch master-gain*
           ::update-gain-on-server
           (fn [k r old new-gain]
             (ctl (main-input-group) :master-gain new-gain)))

(on-event "/server-audio-clipping-rogue-vol"
          (fn [msg]
            (println "TOO LOUD!! (clipped) Bus:"
                     (int (nth (:args msg) 2))
                     "- lower master vol") )
          ::server-audio-clipping-warner-vol)

(defonce __BUS-MIXERS__
  (do
    (defsynth out-bus-mixer [out-bus 0
                             volume 0.5 master-volume @master-vol*
                             safe-recovery-time 3]
      (let [source    (in out-bus)
            source    (* volume master-volume source)
            not-safe? (trig1 (a2k (> source 1)) safe-recovery-time)
            safe-snd  (limiter source 0.99 0.001)]
        (send-reply not-safe?
                    "/server-audio-clipping-rogue-vol"
                    out-bus)
        (replace-out out-bus safe-snd)))

    (comment defsynth out-bus-mixer [out-bus 0
                             volume 0.5 master-volume @master-vol*
                             safe-recovery-time 3]
      (let [source    (in out-bus)
            source    (* volume master-volume source)
            not-safe? (trig1 (a2k (> source 1)) safe-recovery-time)
            safe-vol  (+ 0.1 (abs (- 1 not-safe?)))
            safe-vol  (lag2-ud safe-vol 1 0.1)
            snd-idx   (< safe-vol 0.5)
            snd       (select snd-idx [source (pink-noise)])
            safe-snd  (* safe-vol (clip2 snd 1))]
        (send-reply not-safe?
                    "/server-audio-clipping-rogue-vol"
                    out-bus)
        (replace-out out-bus safe-snd)))

    (defsynth in-bus-mixer [in-bus 0
                            gain 1 master-gain @master-gain*]
      (let [source  (in in-bus)
            source  (* gain master-gain source)]
        (replace-out in-bus source)))))


(defn- start-mixers
  []
  (ensure-connected!)
  (let [in-cnt          (server-num-input-buses)
        out-cnt         (server-num-output-buses)
        out-mixers      (doall
                         (map
                          (fn [out-bus]
                            (out-bus-mixer :pos :head
                                           :target (main-mixer-group)
                                           :out-bus out-bus))
                          (range out-cnt)))
        in-mixers       (doall
                         (map
                          (fn [in-bus]
                            (in-bus-mixer :pos :head
                                          :target (main-input-group)
                                          :in-bus (+ out-cnt in-bus)))
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

(defonce __RECORDER__
  (defsynth master-recorder
    [out-buf 0]
    (disk-out out-buf (in 0 2))))

(defonce recorder-info* (ref nil))

(defn recording-start
  "Start recording a wav file to a new file at wav-path. Be careful - may
  generate very large files."
  [path & args]
  (if-let [info @recorder-info*]
    (throw (Exception. (str "Recording already taking place to: "
                            (get-in info [:buf-stream :path])))))

  (let [path (resolve-tilde-path path)
        bs   (apply buffer-stream path args)
        rec  (master-recorder :target (main-monitor-group) bs)]
    (dosync
     (ref-set recorder-info* {:rec-id rec
                              :buf-stream bs}))
    :recording-started))

(defn recording-stop
  "Stop system-wide recording. This frees the file and writes the wav headers.
  Returns the path of the file created."
  []
  (when-let [info (dosync
                   (let [old @recorder-info*]
                     (ref-set recorder-info* nil)
                     old))]
    (kill (:rec-id info))
    (buffer-stream-close (:buf-stream info))
    (get-in info [:buf-stream :path])))

(defn recording?
  []
  (not (nil? @recorder-info*)))
