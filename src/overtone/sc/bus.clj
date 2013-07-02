(ns overtone.sc.bus
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc synth ugens defaults server node]
        [overtone.helpers lib]
        [overtone.sc.foundation-groups :only [foundation-monitor-group]]
        [overtone.libs.deps            :only [on-deps]])
  (:require [overtone.at-at :as at-at]))

;; ## Buses
;;
;; Synthesizers can be connected to I/O devices (e.g. sound cards) and
;; other synthesizers by using buses.  Conceptually they are like
;; plugging a cable from the output of one unit to the input of another,
;; but in SC they are implemented using a simple integer referenced
;; array of float values.

(defonce ^{:private true} bus-monitors* (atom {}))
(defonce ^{:private true} bus-monitor-pool (at-at/mk-pool))
(defonce ^{:private true} audio-bus-monitor-group* (atom nil))

(defonce ^{:private true} __PROTOCOLS__
  (do
    (defprotocol IBus
      (free-bus [bus] "Free this control or audio bus - enabling the resource to be re-allocated"))))

(defrecord AudioBus [id n-channels rate name]
  to-sc-id*
  (to-sc-id [this] (:id this))

  IBus
  (free-bus [this] (free-id :audio-bus (:id this) (:n-channels this))))

(defrecord ControlBus [id n-channels rate name]
  to-sc-id*
  (to-sc-id [this] (:id this))

  IBus
  (free-bus [this] (free-id :control-bus (:id this) (:n-channels this))))

(defmethod print-method AudioBus [b w]
  (.write w (format "#<audio-bus: %s, %s, id %d>"
                    (if (empty? (:name b))
                      "No Name"
                      (:name b))
                    (cond
                     (= 1 (:n-channels b)) "mono"
                     (= 2 (:n-channels b)) "stereo"
                     :else (str (:n-channels b) " channels"))
                    (:id b))))

(defmethod print-method ControlBus [b w]
  (.write w (format "#<control-bus: %s, %s, id %d>"
                    (if (empty? (:name b))
                      "No Name"
                      (:name b))
                    (cond
                     (= 1 (:n-channels b)) "1 channel"
                     :else (str (:n-channels b) " channels"))
                    (:id b))))

(derive AudioBus ::bus)
(derive ControlBus ::bus)

(defn bus?
  "Returns true if the specified bus is a map representing a bus (either control
  or audio) "
  [bus]
  (isa? (type bus) ::bus))

(defn control-bus?
  "Returns true if the specified bus is a map representing a control bus."
  [bus]
  (isa? (type bus) ControlBus))

(defn audio-bus?
  "Returns true if the specified bus is a map representing a control bus."
  [bus]
  (isa? (type bus) AudioBus))

(defn control-bus
  "Allocate one or more successive control buses. By default, just one
   bus is allocated. However, if you specify a number of channels, a
   successive range of that length will be allocated.

   You may also specify a name for the bus for labelling purposes."
  ([] (control-bus 1 ""))
  ([n-channels-or-name]  (if (string? n-channels-or-name)
                          (control-bus 1 n-channels-or-name)
                          (control-bus n-channels-or-name "")))
  ([n-channels name]
     (let [id (alloc-id :control-bus n-channels)]
       (ControlBus. id n-channels :control name))))

(defn audio-bus
  "Allocate one or more successive audio buses. By default, just one
   bus is allocated. However, if you specify a number of channels, a
   successive range of that length will be allocated.

   For example, to allocate a stereo bus: (audio-bus 2)

   You may also specify a name for the bus for labelling purposes."
  ([] (audio-bus 1 ""))
  ([n-channels-or-name] (if (string? n-channels-or-name)
                          (audio-bus 1 n-channels-or-name)
                          (audio-bus n-channels-or-name "")))
  ([n-channels name]
     (let [id (alloc-id :audio-bus n-channels)]
       (AudioBus. id n-channels :audio name))))

;; Reserve busses for overtone
(defonce ___reserve-overtone-busses____
  (dotimes [i AUDIO-BUS-RESERVE-COUNT]
    (audio-bus)))

(defn reset-buses
  [event-info]
  nil)

;(on-sync-event :reset reset-buses ::reset-buses)

(defn control-bus-set!
  "Updates bus to new val. Modification takes place on the server asynchronously.

  (control-bus-set! my-bus 3) ;=> Sets my-bus to the value 3"
  [bus val]
  (let [id  (to-sc-id bus)
        val (double val)]
    (snd "/c_set" id val)))

(defn control-bus-get
  "Get the current value of a control bus."
  [bus]
  (let [id (to-sc-id bus)
        p  (server-recv "/c_set" (fn [info] (= id (first (:args info)))))]
    (snd "/c_get" id)
    (second (:args (deref! p (str "attempting to read the current value of bus " (with-out-str (pr bus))))))))

(defn control-bus-set-range!
  "Set a range of consecutive control buses to the supplied values."
  [bus start len vals]
  (let [id   (to-sc-id bus)
        vals (floatify vals)]
      (apply snd "/c_setn" id start len vals)))

(defn control-bus-get-range
  "Get a range of consecutive control bus values."
  [bus len]
  (let [id (to-sc-id bus)
        p  (server-recv "/c_setn" (fn [info] (and (= id (first (:args info)))
                                                 (= len (second (:args info))))))]
    (snd "/c_getn" id len)
    (drop 2 (:args (deref! p (str "attempting to get a range of consecutive control bus values of length " len " from bus " (with-out-str (pr bus))))))))

(defn- create-monitor-group
  "Creates a group for the audio bus monitor synths. Designed to be
   called in a dependency callback after :foundation-groups-created."
  []
  (ensure-connected!)
  (assert (foundation-monitor-group) "Couldn't find monitor group")
  (let [g (with-server-sync
            #(group "Audio Bus Monitors" :tail (foundation-monitor-group))
            "whilst creating the audio bus monitor group")]
    (reset! audio-bus-monitor-group* g)))

(on-deps :foundation-groups-created ::create-monitor-group create-monitor-group)

(defonce __BUS-MONITOR-SYNTH__
  (defsynth mono-audio-bus-level [in-a-bus 0 out-c-bus 0]
    (let [sig   (in:ar in-a-bus 1)
          level (amplitude sig)
          level (lag-ud level 0.1 0.3)]
      (out:kr out-c-bus [(a2k level)]))))

(defn audio-bus-monitor
  "Mono bus amplitude monitor. Returns an atom containing the current
   amplitude of the monitor. For multi-channel buses, an offset may be
   specified. Current amplitude is updated within the returned atom
   every 50 ms.

   Note - only creates one monitor per audio bus - subsequent calls for
   the same audio bus idx will return a cached monitor."
  ([audio-bus] (audio-bus-monitor audio-bus 0))
  ([audio-bus chan-offset]
     (ensure-connected!)
     (assert @audio-bus-monitor-group* "Couldn't find audio bus monitor group")
     (let [bus-idx (to-sc-id audio-bus)
           bus-idx (+ chan-offset bus-idx)]
       (if-let [[monitor _] (get @bus-monitors* bus-idx)]
         monitor
         (let [monitor (atom 0)
               cb      (control-bus (str "audio-bus-level [" bus-idx "]"))
               m-synth (mono-audio-bus-level [:tail @audio-bus-monitor-group*]
                                             bus-idx
                                             cb)]

           (at-at/every 50
                        #(reset! monitor (control-bus-get bus-idx))
                        bus-monitor-pool
                        :initial-delay 0
                        :desc (str "bus-monitor [" bus-idx "]"))
           (swap! bus-monitors* assoc bus-idx [monitor m-synth])
           monitor)))))
