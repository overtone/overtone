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

(defrecord AudioBus [id n-channels rate]
  to-sc-id*
  (to-sc-id [this] (:id this)))

(defrecord ControlBus [id n-channels rate]
  to-sc-id*
  (to-sc-id [this] (:id this)))

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
  "Allocate one or more control busses."
  ([] (control-bus 1))
  ([n-channels]
     (let [id (alloc-id :control-bus n-channels)]
       (ControlBus. id n-channels :control))))

(defn audio-bus
  "Allocate one or more audio busses."
  ([] (audio-bus 1))
  ([n-channels]
     (let [id (alloc-id :audio-bus n-channels)]
       (AudioBus. id n-channels :audio))))

(defn free-bus
  "Free the id of specified bus for reuse."
  [bus]
  (assert (bus? bus))
  (case (type bus)
    ::audio-bus   (free-id :audio-bus (:id bus) (:n-channels bus))
    ::control-bus (free-id :control-bus (:id bus) (:n-channels bus)))
  :free)

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
    (second (:args (deref! p)))))

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
    (drop 2 (:args (deref! p)))))
