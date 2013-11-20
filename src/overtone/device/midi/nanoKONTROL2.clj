(ns overtone.device.midi.nanoKONTROL2
  (:use [overtone.midi]
        [overtone.libs.event :only [event on-event on-latest-event]]
        [overtone.core :only [control-bus control-bus-set!]]))

(defrecord NanoKontrol2 [name out interfaces state buses])

(def event-handle [:midi-device "KORG INC." "SLIDER/KNOB" "nanoKONTROL2 SLIDER/KNOB" 0 :control-change])

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

(def default-event-type
  {:button :on-event
   :slider :on-latest-event
   :pot    :on-latest-event})

(def config
  (merge-control-defaults
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
                                   :r7 {:note 71}}}}}))

;; Magic sysex messages (recorded from Korg Kontrol Editor output)
(def sysex-1 "F0 7E 7F 06 01 F7")
(def sysex-2 "F0 42 40 00 01 13 00 1F  12 00 F7")
(def sysex-3 "F0 7E 7F 06 01 F7")
(def sysex-4 "F0 42 40 00 01 13 00 7F  7F 02 03 05 40 00 00 00
              01 10 01 00 00 00 00 7F  00 01 00 10 00 00 7F 00
              01 00 20 00 7F 00 00 01  00 30 00 7F 00 00 01 00
              40 00 7F 00 10 00 01 00  01 00 7F 00 01 00 00 11
              00 7F 00 01 00 00 21 00  7F 00 01 00 31 00 00 7F
              00 01 00 41 00 00 7F 00  10 01 00 02 00 00 7F 00
              01 00 12 00 7F 00 00 01  00 22 00 7F 00 00 01 00
              32 00 7F 00 01 00 00 42  00 7F 00 10 01 00 00 03
              00 7F 00 01 00 00 13 00  7F 00 01 00 23 00 00 7F
              00 01 00 33 00 00 7F 00  01 00 43 00 7F 00 00 10
              01 00 04 00 7F 00 00 01  00 14 00 7F 00 00 01 00
              24 00 7F 00 01 00 00 34  00 7F 00 01 00 00 44 00
              7F 00 10 01 00 00 05 00  7F 00 01 00 15 00 00 7F
              00 01 00 25 00 00 7F 00  01 00 35 00 7F 00 00 01
              00 45 00 7F 00 00 10 01  00 06 00 7F 00 00 01 00
              16 00 7F 00 01 00 00 26  00 7F 00 01 00 00 36 00
              7F 00 01 00 46 00 00 7F  00 10 01 00 07 00 00 7F
              00 01 00 17 00 00 7F 00  01 00 27 00 7F 00 00 01
              00 37 00 7F 00 00 01 00  47 00 7F 00 10 00 01 00
              3A 00 7F 00 01 00 00 3B  00 7F 00 01 00 00 2E 00
              7F 00 01 00 3C 00 00 7F  00 01 00 3D 00 00 7F 00
              01 00 3E 00 7F 00 00 01  00 2B 00 7F 00 00 01 00
              2C 00 7F 00 01 00 00 2A  00 7F 00 01 00 00 29 00
              7F 00 01 00 2D 00 00 7F  00 7F 7F 7F 7F 00 7F 00
              00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00
              00 F7")
(def sysex-5 "F0 7E 7F 06 01 F7")
(def sysex-6 "F0 42 40 00 01 13 00 1F  11 00 F7")

(defn set-external-led-mode!
  "External led mode allows you to control each of the nanoKontrol2's
  leds independently to button presses. This gives you much more
  control over the device.

  To enable external led mode, we need to send send the magical sysex
  incantation to the nanoKontrol2 midi-out object nko (this was
  determined by recording the sysex messages that the official Korg
  Kontrol Editor sends to perform this task.)

  On OS X, this is currently broken due to broken Java support for MIDI
  sysex messages."
  [nko]
   (doseq [m [sysex-1 sysex-2 sysex-3 sysex-4 sysex-5 sysex-6]]
     (midi-sysex (:out nko) m)))

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

(defn note-controls-map
  [config]
  (let [controls (-> config :interfaces :input-controls :controls)]
    (into {}
          (map (fn [[k v]] [(:note v) k])
               controls))))

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
     (let [out        (midi-out midi-out-str)
           interfaces (-> config :interfaces)
           state      (into {}
                            (map (fn [[k v]] [k (atom nil)])
                                 (-> config :interfaces :input-controls :controls)))
           buses     (into {}
                            (map (fn [[k v]] [k (control-bus)])
                                 (-> config :interfaces :input-controls :controls)))]
       (when force-external-led-mode?
         (set-external-led-mode! out))

       (doseq [[k v] (-> config :interfaces :input-controls :controls)]
         (let [type      (:type v)
               note      (:note v)
               handle    (concat event-handle [note])
               update-fn (fn [{:keys [data2-f]}]
                           (control-bus-set! (buses k) data2-f)

                           (reset! (state k) data2-f))]
           (cond
            (= :on-event (default-event-type type))
            (on-event handle update-fn (str "update-state-for" handle))

            (= :on-latest-event (default-event-type type))
            (on-latest-event handle update-fn (str "update-state-for" handle)))))

       (intromation out)
       (NanoKontrol2. (:name config) out interfaces state buses))))
