(ns overtone.sc.bus
  (:use [overtone.sc.machinery defaults allocator]
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
  (and (associative? b)
       (or (isa? (type b) ::bus))))

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
  "Takes a list of bus indices and values and sets the buses to those values.
  Modifies bus(ses) in place on the server.

  (bus-set! my-bus 3) ;=> Sets my-bus to the value 3"
  [bus val]
  (assert (bus? bus))

  (send "/c_set" (:id bus) (double val)))

(defn bus-set-range!
  "Set a range of consecutive busses to the supplied vals"
  [bus start len data]
  (assert (bus? bus))

  (apply snd "/c_setn" (:id bus) start len (map double data)))
