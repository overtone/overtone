(ns overtone.sc.allocator)

;; ## SCSynth limits
(def MAX-NODES 4000)
(def MAX-BUFFERS 4000)
(def MAX-SDEFS 2000)
(def MAX-AUDIO-BUS 1000)
(def MAX-CONTROL-BUS 4000)

;; ## Allocators
;;
;; We use bit sets to store the allocation state of resources
;; on the audio server.  These typically get allocated on usage by the client,
;; and then freed either by client request or automatically by receiving
;; notifications from the server.  (e.g. When an envelope trigger fires to
;; free a synth.)

(defn mk-bitset [size]
  (ref (vec (repeat size false))))

(defonce allocator-bits
  {:node         (mk-bitset MAX-NODES)
   :audio-buffer (mk-bitset MAX-BUFFERS)
   :audio-bus    (mk-bitset MAX-NODES)
   :control-bus  (mk-bitset MAX-NODES)})

(defonce allocator-limits
  {:node         MAX-NODES
   :audio-buffer MAX-BUFFERS
   :audio-bus    MAX-AUDIO-BUS
   :control-bus  MAX-CONTROL-BUS})

(defn- fill-gaps
  "Fill a given vector bs with size consecutive vals from idx"
  [bs idx size val]
  (loop [bs bs
         idx idx
         size size]
    (if (> size 0)
      (recur (assoc bs idx val) (inc idx) (dec size))
      bs)))

(defn- find-gap
  [bs size idx gap-found limit]
  (when (> idx limit)
    (throw (Exception. (str "No more ids! Unable to allocate a sequence of ids of length" size))))
  (if (= gap-found size)
    (- idx gap-found)
    (let [gap-found (if (not (get bs idx)) (inc gap-found) 0)]
      (find-gap bs size (inc idx) gap-found limit))))

(defn alloc-id
  "Allocate a new ID for the type corresponding to key."
  ([k] (alloc-id k 1))
  ([k size]
     (let [bits  (get allocator-bits k)
           limit (get allocator-limits k)]
       (when-not bits
         (throw (Exception. (str "Unable to get allocator bits for keyword " k))))
       (when-not limit
         (throw (Exception. (str "Unable to get allocator limit for keyword " k))))
       (dosync
        (let [id (find-gap @bits size 0 0 limit)]
          (alter bits fill-gaps id size true)
          id)))))

; The root group is implicitly allocated
(defonce _root-group_ (alloc-id :node))

(defn free-id
  "Free the id of type key."
  ([k id] (free-id k id 1))
  ([k id size]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (dosync
     (alter bits fill-gaps id size false)))))


(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (dosync
     (ref-set bits (mk-bitset limit)))))
