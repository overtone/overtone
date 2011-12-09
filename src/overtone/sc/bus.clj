(ns overtone.sc.bus
  (:import [java.util.concurrent TimeoutException])
  (:use [overtone.sc.machinery defaults allocator]
        [overtone.sc.machinery.server comms]
        [overtone.util lib]
        [overtone.sc server]))

;; ## Busses
;;
;; Synthesizers can be connected to I/O devices (e.g. sound cards) and
;; other synthesizers by using busses.  Conceptually they are like
;; plugging a cable from the output of one unit to the input of another,
;; but in SC they are implemented using a simple integer referenced
;; array of float values.

(derive ::audio-bus ::bus)
(derive ::control-bus ::bus)

(defn bus?
  [b]
  (or (number? b)
      (and (associative? b)
       (or (isa? (type b) ::bus)))))

(defn bus-id
  [b]
  (if (number? b)
    b
    (:id b)))

; TODO: In order to allocate multi-channel busses we actually need to
; allocate multiple, adjacent busses, which the current bitset based
; allocator doesn't support.
(defn control-bus
  "Allocate one ore more control busses."
  ([] (control-bus 1))
  ([n-channels]
   (let [id (alloc-id :control-bus n-channels)]
     (with-meta {:id id
                 :n-channels n-channels
                 :rate :control}
                {:type ::control-bus}))))

(defn audio-bus
  "Allocate one ore more audio busses."
  ([] (audio-bus 1))
  ([n-channels]
   (let [id (alloc-id :audio-bus n-channels)]
     (with-meta {:id id
                 :n-channels n-channels
                 :rate :audio}
                {:type ::audio-bus}))))

(defn free-bus
  [b]
  (case (type b)
    ::audio-bus   (free-id :audio-bus (:id b) (:n-channels b))
    ::control-bus (free-id :control-bus (:id b) (:n-channels b)))
  :free)

; Reserve busses for overtone
(defonce ___reserve-overtone-busses____
  (dotimes [i AUDIO-BUS-RESERVE-COUNT]
    (audio-bus)))

(defn reset-busses
  []
  nil)

;(on-sync-event :reset reset-busses ::reset-busses)

(defn bus-set!
  "Takes a list of control bus indices and values and sets the buses to those values.
  Modifies bus(ses) in place on the server.

  (bus-set! my-bus 3) ;=> Sets my-bus to the value 3"
  [bus val]
  (assert (bus? bus))
  (snd "/c_set" (bus-id bus) (double val)))

(defn bus-get
  "Get the current value of a control bus."
  [bus]
  (assert (bus? bus))
  (let [p (server-recv "/c_set")]
    (snd "/c_get" (bus-id bus))
    (try
      (second (:args (deref! p)))
      (catch TimeoutException t
        :timeout))))

(defn bus-set-range!
  "Set a range of consecutive control busses to the supplied values."
  [bus start len vals]
  (assert (bus? bus))
  (apply snd "/c_setn" (bus-id bus) start len (floatify vals)))

(defn bus-get-range
  "Get a range of consecutive control bus values."
  [bus len]
  (assert (bus? bus))
  (let [p (server-recv "/c_setn")]
    (snd "/c_getn" (bus-id bus) len)
    (try
      (drop 2 (:args (deref! p)))
      (catch TimeoutException t
        :timeout))))
