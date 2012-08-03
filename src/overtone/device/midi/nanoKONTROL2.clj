(ns overtone.device.midi.nanoKONTROL2
  (:use [overtone.midi]
        [overtone.libs.event :only [event]]))

(defrecord NanoKontrol2 [name in out interfaces state])

(def event-handle "/overtone/midi/nanokontrol2")

(def config
  {:name "nanoKONTROL2"
   :interfaces {:input-controls {:name "Input Controls"
                                 :type :midi-in
                                 :midi-handle "nanoKONTROL2"
                                 :control-defaults {:chan 0 :cmd 176 :type :button}
                                 :controls {:track-left {:note 58}
                                            :track-right {:note 59}
                                            :cycle {:note 46}
                                            :marker-set {:note 60}
                                            :marker-left {:note 61}
                                            :marker-right {:note 62}
                                            :rewind {:note 43}
                                            :fast-forward {:note 44}
                                            :stop {:note 42}
                                            :play {:note 41}
                                            :record {:note 45}
                                            :s0 {:note 32}
                                            :m0 {:note 48}
                                            :r0 {:note 64}
                                            :slider0 {:note 0 :type :slider}
                                            :pot0 {:note 16 :type :pot}

                                            :s1 {:note 33}
                                            :m1 {:note 49}
                                            :r1 {:note 65}
                                            :slider1 {:note 1 :type :slider}
                                            :pot1 {:note 17 :type :pot}

                                            :s2 {:note 34}
                                            :m2 {:note 50}
                                            :r2 {:note 66}
                                            :slider2 {:note 2 :type :slider}
                                            :pot2 {:note 18 :type :pot}

                                            :s3 {:note 35}
                                            :m3 {:note 51}
                                            :r3 {:note 67}
                                            :slider3 {:note 3 :type :slider}
                                            :pot3 {:note 19 :type :pot}

                                            :s4 {:note 36}
                                            :m4 {:note 52}
                                            :r4 {:note 68}
                                            :slider4 {:note 4 :type :slider}
                                            :pot4 {:note 20 :type :pot}

                                            :s5 {:note 37}
                                            :m5 {:note 53}
                                            :r5 {:note 69}
                                            :slider5 {:note 5 :type :slider}
                                            :pot5 {:note 21 :type :pot}

                                            :s6 {:note 38}
                                            :m6 {:note 54}
                                            :r6 {:note 70}
                                            :slider6 {:note 6 :type :slider}
                                            :pot6 {:note 22 :type :pot}

                                            :s7 {:note 39}
                                            :m7 {:note 55}
                                            :r7 {:note 71}
                                            :slider7 {:note 7 :type :slider}
                                            :pot7 {:note 23 :type :pot}}}
                :leds {:name "LEDs"
                       :type :midi-out
                       :midi-handle "nanoKONTROL2"
                       :control-defaults {:type :led}
                       :controls {:cycle {:note 46}
                                  :rewind {:note 43}
                                  :fast-forward {:note 44}
                                  :stop {:note 42}
                                  :play {:note 41}
                                  :record {:note 45}
                                  :s0 {:note 32}
                                  :m0 {:note 48}
                                  :r0 {:note 64}
                                  :s1 {:note 33}
                                  :m1 {:note 49}
                                  :r1 {:note 65}
                                  :s2 {:note 34}
                                  :m2 {:note 50}
                                  :r2 {:note 66}
                                  :s3 {:note 35}
                                  :m3 {:note 51}
                                  :r3 {:note 67}
                                  :s4 {:note 36}
                                  :m4 {:note 52}
                                  :r4 {:note 68}
                                  :s5 {:note 37}
                                  :m5 {:note 53}
                                  :r5 {:note 69}
                                  :s6 {:note 38}
                                  :m6 {:note 54}
                                  :r6 {:note 70}
                                  :s7 {:note 39}
                                  :m7 {:note 55}
                                  :r7 {:note 71}}}}})


(defn- byte-seq-to-array
  "Turn a seq of bytes into a native byte-array."
  [bseq]
  (let [ary (byte-array (count bseq))]
    (doseq [i (range (count bseq))]
      (aset-byte ary i (nth bseq i)))
    ary))

;; Magic sysex messages (recorded from Korg Kontrol Editor output)
(def start-sysex [-16 126 127 6 1 -9])
(def second-sysex [-16 66 64 0 1 19 0 31 18 0 -9])
(def main-sysex [-16 66 64 0 1 19 0 127 127 2 3 5 64 0 0 0 1 16 1 0 0
                 0 0 127 0 1 0 16 0 0 127 0 1 0 32 0 127 0 0 1 0 48 0
                 127 0 0 1 0 64 0 127 0 16 0 1 0 1 0 127 0 1 0 0 17 0
                 127 0 1 0 0 33 0 127 0 1 0 49 0 0 127 0 1 0 65 0 0
                 127 0 16 1 0 2 0 0 127 0 1 0 18 0 127 0 0 1 0 34 0
                 127 0 0 1 0 50 0 127 0 1 0 0 66 0 127 0 16 1 0 0 3 0
                 127 0 1 0 0 19 0 127 0 1 0 35 0 0 127 0 1 0 51 0 0
                 127 0 1 0 67 0 127 0 0 16 1 0 4 0 127 0 0 1 0 20 0
                 127 0 0 1 0 36 0 127 0 1 0 0 52 0 127 0 1 0 0 68 0
                 127 0 16 1 0 0 5 0 127 0 1 0 21 0 0 127 0 1 0 37 0 0
                 127 0 1 0 53 0 127 0 0 1 0 69 0 127 0 0 16 1 0 6 0
                 127 0 0 1 0 22 0 127 0 1 0 0 38 0 127 0 1 0 0 54 0
                 127 0 1 0 70 0 0 127 0 16 1 0 7 0 0 127 0 1 0 23 0 0
                 127 0 1 0 39 0 127 0 0 1 0 55 0 127 0 0 1 0 71 0 127
                 0 16 0 1 0 58 0 127 0 1 0 0 59 0 127 0 1 0 0 46 0 127
                 0 1 0 60 0 0 127 0 1 0 61 0 0 127 0 1 0 62 0 127 0 0
                 1 0 43 0 127 0 0 1 0 44 0 127 0 1 0 0 42 0 127 0 1 0
                 0 41 0 127 0 1 0 45 0 0 127 0 127 127 127 127 0 127 0
                 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -9])
(def end-sysex [-16 66 64 0 1 19 0 31 17 0 -9])

(defn set-external-led-mode!
  "External led mode allows you to control each of the nanoKontrol2's
  leds independently to button presses. This gives you much more
  control over the device.

  To enable external led mode, we need to send send the magical sysex
  incantation to the nanoKontrol2 midi-out object nko (this was
  determined by recording the sysex messages that the official Korg
  Kontrol Editor sends to perform this task.)"
  [nko]
  (midi-sysex nko (byte-seq-to-array start-sysex))
  (midi-sysex nko (byte-seq-to-array second-sysex))
  (midi-sysex nko (byte-seq-to-array start-sysex))
  (midi-sysex nko (byte-seq-to-array main-sysex))
  (midi-sysex nko (byte-seq-to-array start-sysex))
  (midi-sysex nko (byte-seq-to-array end-sysex)))

(defn leds-on-test
  [nko]
  (dotimes [n 100] (midi-control nko n 127)))

(defn- led-on-
  [out id]
  (let [led-id  (-> config :interfaces :leds :controls id :note)]
    (midi-control out led-id 127)))

(defn- led-off-
  [out id]
  (let [led-id (-> config :interfaces :leds :controls id :note)]
    (midi-control out led-id 0)))

(defn led-on
  "Turn a led on. Usage: (led-on nk :r2)"
  [nk id]
  (let [out (:out nk)]
    (led-on- out id)))

(defn led-off
  "Turn a led off. Usage: (led-off nk :r2)"
  [nk id]
  (let [out (:out nk)]
    (led-off- out id)))

(defn- smr-col-on
  [out col-num]
  (let [s (keyword (str "s" col-num))
        m (keyword (str "m" col-num))
        r (keyword (str "r" col-num))]
    (led-on- out s)
    (led-on- out m)
    (led-on- out r)))

(defn- smr-col-off
  [out col-num]
  (let [s (keyword (str "s" col-num))
        m (keyword (str "m" col-num))
        r (keyword (str "r" col-num))]
    (led-off- out s)
    (led-off- out m)
    (led-off- out r)))

(defn intromation
  ([out]
     (let [intro-times (repeat 75)]
       (doseq [id (range 8)]
         (smr-col-on out id)
         (Thread/sleep (nth intro-times id)))
       (Thread/sleep 750)
       (doseq [id (reverse (range 8))]
         (smr-col-off out id)
         (Thread/sleep (nth intro-times id))))))

(defn- merge-control-defaults
  "Returns config map where control info maps are merged
  with :control-defaults map."
  [config]
  (assoc config
    :interfaces
    (into {}
          (map (fn [[i-name i-info]]
                 [i-name (assoc (dissoc i-info :control-defaults)
                           :controls (into {}
                                           (map (fn [[c-name c-info]]
                                                  [c-name (merge (:control-defaults i-info)
                                                                 c-info)])
                                                (:controls i-info))))])
               (:interfaces config)))))

(defn note-controls-map
  [config]
  (let [controls (-> config :interfaces :input-controls :controls)]
    (into {}
          (map (fn [[k v]] [(:note v) k])
               controls))))

(defn mk-midi-in-handler
  [config state event-name]
  (let [ncm (note-controls-map config)]
    (fn [msg ts]
      (let [note   (:note msg)
            vel    (:vel msg)
            at-key (get ncm note)
            atm    (get state at-key)]
        (reset! atm vel)
        (event event-name :msg msg :ts ts :val vel :id at-key)))))

(defn connect
  "Connect to a connected nanoKONTROL2 midi device. By default, it
  places the deviced into 'external led mode' which allows you to
  control the leds remotely giving you more control. If your device
  doesn't have the midi-handle \"nanoKONTROL2\" for both the input
  controls and leds (perhaps if you are connecting more than one
  simulataneously) you may also specify these identifiers directly."
  ([] (connect true))
  ([force-external-led-mode?]
     (connect force-external-led-mode?
              (-> config :interfaces :input-controls :midi-handle)
              (-> config :interfaces :leds :midi-handle)))
  ([midi-in-str midi-out-str] (connect true midi-in-str midi-out-str))
  ([force-external-led-mode? midi-in-str midi-out-str]
     (let [in         (midi-in midi-in-str)
           out        (midi-out midi-out-str)
           config     (merge-control-defaults config)
           interfaces (-> config :interfaces)
           state      (into {} (map (fn [[k v]] [k (atom nil)]) (-> config :interfaces :input-controls :controls)))]
       (when force-external-led-mode?
         (set-external-led-mode! out))
       (midi-handle-events in (mk-midi-in-handler config state event-handle))
       (intromation out)
       (NanoKontrol2. (:name config) in out interfaces state))))
