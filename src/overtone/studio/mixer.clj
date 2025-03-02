(ns overtone.studio.mixer
  "A virtual studio mixing table."
  {:author "Jeff Rose & Sam Aaron"}
  (:use [overtone.music.rhythm]
        [overtone.music.pitch]
        [overtone.libs.event]
        [overtone.libs.deps]
        [overtone.helpers.lib]
        [overtone.helpers.file]
        [overtone.helpers.system]
        [overtone.sc.defaults]
        [overtone.sc.synth]
        [overtone.sc.server]
        [overtone.sc.info]
        [overtone.sc.ugens]
        [overtone.sc.envelope]
        [overtone.sc.node]
        [overtone.sc.bus]
        [overtone.sc.buffer]
        [overtone.sc.foundation-groups :only [foundation-input-group
                                              foundation-output-group
                                              foundation-root-group
                                              foundation-monitor-group]]
        [overtone.sc.machinery.synthdef]
        [overtone.sc.machinery.ugen.fn-gen]
        [overtone.sc.machinery.ugen.defaults]
        [overtone.sc.machinery.ugen.sc-ugen]
        [overtone.sc.machinery.server.comms]
        [overtone.sc.util]
        [overtone.music.time]
        overtone.studio.core)
  (:require [overtone.studio fx]
            [overtone.config.log :as log]))

;; An instrument abstracts the more basic concept of a synthesizer used by
;; SuperCollider.  Every instance of an instrument will be placed in the same
;; group, so if you later call (kill my-inst) it will be able to stop all the
;; instances of that group.  (Likewise for controlling them...)

(on-event "/server-audio-clipping-rogue-vol"
          (fn [msg]
            (println "TOO LOUD!! (clipped) Bus:"
                     (int (nth (:args msg) 2))
                     "- lower master vol") )
          ::server-audio-clipping-warner-vol)

(defonce __BUS-MIXERS__
  (do
    (defsynth out-bus-mixer [out-bus 0
                             volume 0.5 master-volume (:master-volume @studio*)
                             safe-recovery-time 3]
      (let [source    (in out-bus)
            source    (* volume master-volume source)
            not-safe? (trig1 (a2k (> source 1)) safe-recovery-time)
            safe-snd  (limiter source 0.99 0.001)]
        (send-reply not-safe?
                    "/server-audio-clipping-rogue-vol"
                    out-bus)
        (replace-out out-bus safe-snd)))

    #_(defsynth out-bus-mixer [out-bus 0
                               volume 0.5 master-volume (volume)
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
                            gain 1 master-gain (:input-gain @studio*)]
      (let [source  (in in-bus)
            source  (* gain master-gain source)]
        (replace-out in-bus source)))))


(defn- clear-msg-queue-and-groups
  "Clear message queue and ALL groups. Catches exceptions in case the
  server has died. Meant for use in a :shutdown callback"
  [_event-info]
  (try
    (clear-msg-queue)
    (group-deep-clear 0)
    (catch Exception _
      (log/error "Can't clear message queue and groups - server might have died."))))

(on-deps [:server-connected :foundation-groups-created :synthdefs-loaded :hw-audio-buses-reserved] ::signal-server-ready
         #(satisfy-deps :server-ready))

(on-sync-event :shutdown clear-msg-queue-and-groups ::free-all-nodes)

(defn- start-io-mixers
  []
  (ensure-connected!)
  (let [in-cnt     (with-server-sync
                     #(server-num-input-buses)
		     "whilst discovering the number of server input buses")
        out-cnt    (with-server-sync
                     #(server-num-output-buses)
		     "whilst discovering the number of server output buses")
        out-mixers (doall
                    (map
                     (fn [out-bus]
                       (out-bus-mixer [:head (foundation-output-group)]
                                      :out-bus out-bus))
                     (range out-cnt)))
        in-mixers  (doall
                    (map
                     (fn [in-bus]
                       (in-bus-mixer [:head (foundation-input-group)]
                                     :in-bus (+ out-cnt in-bus)))
                     (range in-cnt)))]

    (swap! studio* assoc :bus-mixers {:in in-mixers :out out-mixers})))

(defn- clear-io-mixers
  []
  (swap! studio* assoc :bus-mixers {:in [] :out []}))

;; Setup mixers automatically when the base
(on-deps [:foundation-groups-created :synthdefs-loaded] ::start-bus-mixers start-io-mixers)
(on-sync-event :shutdown ::reset-bus-mixers (fn [_] (clear-io-mixers)))

(defn volume
  "Set the volume on the master mixer. When called with no params, retrieves the
   current value"
  ([] (:master-volume @studio*))
  ([vol]
   (ctl (foundation-output-group) :master-volume vol)
   (swap! studio* assoc :master-volume vol)
   vol))

(defn input-gain
  "Set the input gain on the master mixer. When called with no params, retrieves
  the current value"
  ([] (:master-gain @studio*))
  ([gain]
   (ctl (foundation-input-group) :input-gain gain)
   (swap! studio* assoc :input-gain gain)))

(defonce __RECORDER__
  (defsynth master-recorder
    [out-buf 0]
    (disk-out out-buf (in 0 2))))

(defn recording-start
  "Start recording a wav file to a new file at wav-path. Be careful -
  may generate very large files. See buffer-stream for a list of output
  options.

  Note, due to the size of the buffer used for transferring the audio
  from the audio server to the file, there will be 1.5s of silence at
  the start of the recording"
  [path & args]
  (if-let [info (:recorder @studio*)]
    (throw (Exception. (str "Recording already taking place to: "
                            (get-in info [:buf-stream :path])))))

  (let [path (resolve-tilde-path path)
        bs   (apply buffer-stream path args)
        rec  (master-recorder [:tail (foundation-monitor-group)] bs)]
    (swap! studio* assoc :recorder {:rec-id rec
                                    :buf-stream bs})
    :recording-started))

(defn recording-stop
  "Stop system-wide recording. This frees the file and writes the wav headers.
  Returns the path of the file created."
  []
  (when-let [info (:recorder @studio*)]
    (kill (:rec-id info))
    (buffer-stream-close (:buf-stream info))
    (swap! studio* assoc :recorder nil)
    (get-in info [:buf-stream :path])))

(defn recording?
  []
  (not (nil? (:recorder @studio*))))

(def MIXER-BOOT-DEPS   [:server-ready :studio-setup-completed])

(defn mixer-booted?
  "Check if the mixer has successfully booted yet."
  []
  (deps-satisfied? MIXER-BOOT-DEPS))

(defn wait-until-mixer-booted
  "Makes the current thread sleep until the mixer completed its boot
  process."
  []
  (wait-until-deps-satisfied MIXER-BOOT-DEPS))

(defn boot-server-and-mixer
  "Boots the server and waits until the studio mixer has complete set
  up"
  []
  (when-not (mixer-booted?)
    (boot-server)
    (wait-until-mixer-booted)))

(defn- setup-studio-groups
  "Setup the studio groups."
  []
  (log/info (str "Creating studio group  " (foundation-root-group)))
  (let [root              (foundation-root-group)
        g                 (with-server-sync
                            #(group "Studio" :head root)
                            "whilst creating the Studio group")
        insts-with-groups (map-vals (fn [val]
                                      (assoc val :group
                                             (atom
                                              (with-server-sync
                                                #(group (str "Recreated Inst Group") :tail g)
                                                "whist creating the Recreated Inst Group"))))
                                    (:instruments @studio*))]
    (swap! studio* assoc
           :instrument-group g
           :instruments insts-with-groups)
    (satisfy-deps :studio-setup-completed)))

(defn- setup-studio
  []
  (setup-studio-groups))

(on-deps :foundation-groups-created ::setup-studio-groups setup-studio)

(defn reset-instruments
  "Frees all synth notes for each of the current instruments"
  [_event-info]
  (doseq [[_name inst] (:instruments @studio*)]
    (group-clear (:instance-group inst))))

(on-sync-event :reset reset-instruments ::reset-instruments)

(defn add-instrument
  "Add an instrument to the session."
  [inst]
  (let [i-name (:full-name inst)]
    (swap! studio* assoc-in [:instruments i-name] inst)
    i-name))

(defn remove-instrument
  "Remove an instrument from the session."
  [full-name]
  (swap! studio* update :instruments dissoc full-name)
  (event :inst-removed :inst-name full-name))

(defn clear-instruments
  "Clear all instruments from the session."
  []
  (let [[{:keys [instruments]} _]
        (swap-vals! studio* assoc :instruments {})]
    (doseq [[_name inst] instruments]
      (group-free (:group inst)))))
