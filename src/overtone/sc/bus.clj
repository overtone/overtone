(ns overtone.sc.bus
  (:use
    [overtone.sc core allocator]))

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
   (let [id (alloc-id :control-bus)]
     (with-meta {:id id
                 :n-channels n-channels
                 :rate :control}
                {:type ::control-bus}))))

(defn audio-bus
  "Allocate one ore more audio busses."
  ([] (audio-bus 1))
  ([n-channels]
   (let [id (alloc-id :audio-bus)]
     (with-meta {:id id
                 :n-channels n-channels
                 :rate :audio}
                {:type ::audio-bus}))))

(defn free-bus
  [b]
  (case (type b)
    ::audio-bus   (free-id :audio-bus (:id b))
    ::control-bus (free-id :control-bus (:id b))))

; Reserve the first 11 busses for audio I/O and mixer, forever.
(dotimes [i 11]
  (audio-bus))

(defn reset-busses
  []
  nil)

;(on-sync-event :reset :reset-busses reset-busses)
