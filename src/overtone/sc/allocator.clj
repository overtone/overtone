(ns overtone.sc.allocator
  (:import 
    [java.util BitSet]))

;; ## SCSynth limits
(def MAX-NODES 1024)
(def MAX-BUFFERS 1024)
(def MAX-SDEFS 1024)
(def MAX-AUDIO-BUS 128)
(def MAX-CONTROL-BUS 4096)

;; ## Allocators
;;
;; We use bit sets to store the allocation state of resources
;; on the audio server.  These typically get allocated on usage by the client,
;; and then freed either by client request or automatically by receiving
;; notifications from the server.  (e.g. When an envelope trigger fires to
;; free a synth.)
(defonce allocator-bits 
  {:node         (BitSet. MAX-NODES) 
   :audio-buffer (BitSet. MAX-BUFFERS) 
   :audio-bus    (BitSet. MAX-NODES)
   :control-bus  (BitSet. MAX-NODES)})

(defonce allocator-limits
  {:node    MAX-NODES
   :sdefs     MAX-SDEFS
   :audio-bus   MAX-AUDIO-BUS
   :control-bus MAX-CONTROL-BUS})

(defn alloc-id
  "Allocate a new ID for the type corresponding to key."
  [k]
  (let [bits  (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (let [id (.nextClearBit bits 0)]
        (if (= limit id)
          (throw (Exception. (str "No more " (name k) " ids available!")))
          (do
            (.set bits id)
            id))))))

; The root group is implicitly allocated
(defonce _root-group_ (alloc-id :node))

(defn free-id
  "Free the id of type key."
  [k id]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (.clear bits id))))

(defn all-ids
  "Get all of the currently allocated ids for key."
  [k]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (locking bits
      (loop [ids []
             idx 0]
        (let [id (.nextSetBit bits idx)]
          (if (and (> id -1) (< idx limit))
            (recur (conj ids id) (inc id))
            ids))))))

(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (locking (get allocator-bits k)
    (doseq [id (all-ids k)]
      (free-id k id))))

