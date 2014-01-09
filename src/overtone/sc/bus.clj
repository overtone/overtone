(ns overtone.sc.bus
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc synth ugens defaults server node]
        [overtone.sc.cgens.tap]
        [overtone.helpers lib]
        [overtone.sc.foundation-groups :only [foundation-monitor-group]]
        [overtone.libs.deps            :only [on-deps]])
  (:require [overtone.at-at :as at-at]
            [overtone.sc.info :refer [server-num-input-buses server-num-output-buses]]
            [overtone.libs.deps :refer [satisfy-deps]]))

;; ## Buses
;;
;; Synthesizers can be connected to I/O devices (e.g. sound cards) and
;; other synthesizers by using buses.  Conceptually they are like
;; plugging a cable from the output of one unit to the input of another,
;; but in SC they are implemented using a simple integer referenced
;; array of float values.

(defonce ^{:private true} audio-bus-monitors* (atom {}))
(defonce ^{:private true} control-bus-monitors* (atom {}))
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

;; Reserve first control bus. This is a precautionary measure to stop
;; synths which have control bus args with defaults of 0 which aren't
;; overridden on triggering from clobbering important bus data. By
;; reserving the first control bus (with an id of 0) we ensure that this
;; bus isn't used for important data.
(defonce ___reserve-overtone-first-control-bus___
  (control-bus "Reserved Bus 0"))

(defn- ensure-valid-bus-offset!
  [bus offset]
  (when-not (and (control-bus? bus)
                 (< offset (:n-channels bus)))
    (throw (Exception. (str "Invalid bus offset found. Offset was "
                            offset
                            ", which is greater or equal to the number of channels: "
                            (:n-channels bus) ". ")))))

(defn- ensure-valid-bus-offset-with-len!
  [bus offset len]
  (ensure-valid-bus-offset! bus offset)
  (when-not (and (control-bus? bus)
                 (<= (+ offset len) (:n-channels bus)))
    (throw (Exception. (str "Invalid value len found. len was "
                            len
                            ", which is greater or equal to the number of channels remaining after offset: "
                            offset
                            ", which is: "
                            (- (:n-channels bus) offset) ".")))))

(defn control-bus-set!
  "Asynchronously updates control bus to new val.

   (control-bus-set! my-bus 3) ;=> Sets my-bus to the value 3

   An optional offset may be supplied to access values within
   multi-channel buses.

   Modification takes place on the server asynchronously."
  ([bus val] (control-bus-set! bus val 0))
  ([bus val offset]
     (let [id  (to-sc-id bus)
           id  (+ offset id)
           val (float val)]
       (when (control-bus? bus)
         (ensure-valid-bus-offset! bus offset))
       (snd "/c_set" id val)
       val)))

(defn control-bus-get
  "Synchronously get the current value of a control bus.

   An optional offset may be supplied to access values within
   multi-channel buses."
  ([bus] (control-bus-get bus 0))
  ([bus offset]
     (let [id (to-sc-id bus)
           id (+ offset id)]
       (when (control-bus? bus)
         (ensure-valid-bus-offset! bus offset))
       (let [p  (server-recv "/c_set" (fn [info] (= id (first (:args info))))) ]
         (snd "/c_get" id)
         (second (:args (deref! p (str "attempting to read the current value of bus "
                                       (with-out-str (pr bus))))))))))

(defn control-bus-set-range!
  "Asynchronously set a range of consecutive control buses to the
   supplied vals.

   An optional offset may be supplied to set values within multi-channel
   buses.

   Modification takes place on the server asynchronously."
  ([bus vals] (control-bus-set-range! bus vals 0))
  ([bus vals offset]
     (let [len  (int (count vals))
           id   (+ offset (to-sc-id bus))
           vals (floatify vals)]
       (when (control-bus? bus)
         (ensure-valid-bus-offset-with-len! bus offset len))
       (apply snd "/c_setn" id len vals)
       vals)))

(defn control-bus-get-range
  "Synchronously get a range (of length len) of consecutive control bus
   values.

   An optional offset may be supplied to access values within
   multi-channel buses."
  ([bus len] (control-bus-get-range bus len 0))
  ([bus len offset]
     (let [len (int len)
           id  (to-sc-id bus)
           id  (+ offset id)
           p   (server-recv "/c_setn" (fn [info] (and (= id (first (:args info)))
                                                     (= len (second (:args info))))))]
       (when (control-bus? bus)
         (ensure-valid-bus-offset-with-len! bus offset len))
       (snd "/c_getn" id len)
       (drop 2 (:args (deref! p (str "attempting to get a range of consecutive control bus values of length "
                                     len " from bus " (with-out-str (pr bus)))))))))

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
  (defsynth mono-audio-bus-level [in-a-bus 0]
    (let [sig   (in:ar in-a-bus 1)
          level (amplitude sig)
          level (lag-ud level 0.1 0.3)]
      (tap "level" 20 level))))

(defn audio-bus-monitor
  "Mono bus amplitude monitor. Returns an atom containing the current
   amplitude of the audio bus. Note that this isn't the current value,
   rather it's the peak amplitude of the signal within the audio bus.

   For multi-channel buses, an offset may be specified. Current
   amplitude is updated within the returned atom every 50 ms.

   Note - only creates one monitor per audio bus - subsequent calls for
   the same audio bus idx will return a cached monitor."
  ([audio-bus] (audio-bus-monitor audio-bus 0))
  ([audio-bus chan-offset]
     (ensure-connected!)
     (assert @audio-bus-monitor-group* "Couldn't find audio bus monitor group")
     (let [bus-idx (to-sc-id audio-bus)
           bus-idx (+ chan-offset bus-idx)]
       (if-let [[monitor _] (get @audio-bus-monitors* bus-idx)]
         monitor
         (let [m-synth (mono-audio-bus-level [:tail @audio-bus-monitor-group*]
                                             bus-idx)
               monitor (get-in m-synth [:taps "level"])]
           (swap! audio-bus-monitors* assoc bus-idx [monitor m-synth])
           monitor)))))

(defn control-bus-monitor
  "Control bus monitor. Returns an atom containing the current value of
   the control bus. Note that this isn't the peak amplitude, rather the
   direct value of the control bus.

   For multi-channel buses, an offset may be specified. Current
   amplitude is updated within the returned atom every 50 ms.

   Note - only creates one monitor per control bus - subsequent calls for
   the same control bus idx will return a cached monitor."
  ([control-bus] (control-bus-monitor control-bus 0))
  ([control-bus chan-offset]
     (ensure-connected!)
     (let [bus-idx (to-sc-id control-bus)
           bus-idx (+ chan-offset bus-idx)]
       (if-let [monitor (get @control-bus-monitors* bus-idx)]
         monitor
         (let [monitor (atom 0)]
           (at-at/every 50
                        #(reset! monitor (control-bus-get bus-idx))
                        bus-monitor-pool
                        :initial-delay 0
                        :desc (str "control-bus-monitor [" bus-idx "]"))
           (swap! control-bus-monitors* assoc bus-idx monitor)
           monitor)))))

(defn bus-monitor
  "Returns either a control or audio bus monitor depending on the rate
   of bus supplied. Returns an atom containing the current value of the
   control bus. Note that this isn't the peak amplitude, rather the
   direct value of the control bus.

   For multi-channel buses, an offset may be specified. Current
   amplitude is updated within the returned atom every 50 ms.

   Note - only creates one monitor per bus - subsequent calls for the
   same bus will return a cached monitor.

   See audio-bus-monitor and control-bus-monitor for specific details."
  ([bus] (bus-monitor bus 0))
  ([bus chan-offset]
     (assert (bus? bus))
     (cond
      (audio-bus? bus) (audio-bus-monitor bus chan-offset)
      (control-bus? bus) (control-bus-monitor bus chan-offset)
      :else (throw (Exception. (str "Unknown bus type: " bus))))))

(defn- allocate-hw-audio-buses
  []
  (let [n-buses (+ (server-num-input-buses)
                   (server-num-output-buses))]
    (audio-bus n-buses "Reserved Audio Busses")
    (satisfy-deps :hw-audio-buses-reserved)))

(on-deps :synthdefs-loaded ::allocate-hw-audio-busses allocate-hw-audio-buses)
