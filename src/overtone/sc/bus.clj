(ns overtone.sc.bus
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc defaults server node]
        [overtone.helpers lib]
        [overtone.sc server]))

;; ## Busses
;;
;; Synthesizers can be connected to I/O devices (e.g. sound cards) and
;; other synthesizers by using busses.  Conceptually they are like
;; plugging a cable from the output of one unit to the input of another,
;; but in SC they are implemented using a simple integer referenced
;; array of float values.

(defonce ^{:private true} __PROTOCOLS__
  (do
    (defprotocol IBus
      (free-bus [this]))))

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
  (.write w (format "#<audio-bus: %s %s %d>"
                    (:name b)
                    (cond
                     (= 1 (:n-channels b)) "mono"
                     (= 2 (:n-channels b)) "stereo"
                     :else (str (:n-channels b) " channels"))
                    (:id b))))

(defmethod print-method ControlBus [b w]
  (.write w (format "#<control-bus: %s %s %d>"
                    (:name b)
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
  "Allocate one or more successive control busses. By default, just one
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
  "Allocate one or more successive audio busses. By default, just one
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

; Reserve busses for overtone
(defonce ___reserve-overtone-busses____
  (dotimes [i AUDIO-BUS-RESERVE-COUNT]
    (audio-bus)))

(defn reset-busses
  [event-info]
  nil)

;(on-sync-event :reset reset-busses ::reset-busses)

(defn bus-set!
  "Updates bus to new val. Modification takes place on the server asynchronously.

  (bus-set! my-bus 3) ;=> Sets my-bus to the value 3"
  [bus val]
  (let [id  (to-sc-id bus)
        val (double val)]
    (snd "/c_set" id val)))

(defn bus-get
  "Get the current value of a control bus."
  [bus]
  (let [id (to-sc-id bus)
        p  (server-recv "/c_set")]
    (snd "/c_get" id)
    (second (:args (deref! p (str "attempting to read the current value of bus " (with-out-str (pr bus))))))))

(defn bus-set-range!
  "Set a range of consecutive control busses to the supplied values."
  [bus start len vals]
  (let [id   (to-sc-id bus)
        vals (floatify vals)]
      (apply snd "/c_setn" id start len vals)))

(defn bus-get-range
  "Get a range of consecutive control bus values."
  [bus len]
  (let [id (to-sc-id bus)
        p  (server-recv "/c_setn")]
    (snd "/c_getn" id len)
    (drop 2 (:args (deref! p (str "attempting to get a range of consecutive control bus values of length " len " from bus " (with-out-str (pr bus))))))))
