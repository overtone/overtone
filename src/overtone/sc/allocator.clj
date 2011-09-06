(ns overtone.sc.allocator
  (:use [overtone.sc defaults])
  (:require [overtone.util.log :as log]))

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

(def action-fn-executor* (agent nil))

(defn- execute-action-fn
  "Execute action-fn and catch all exceptions - outputting them to the error
  log. All actions are executed in sequence."
  [action-fn caller-name]
  (send action-fn-executor* (fn [_]
                              (try
                                (action-fn)
                                (catch Exception e
                                  (log/error "Exception in " caller-name " action-fn: " e "\nstacktrace: " (.printStackTrace e)))
                                (finally nil)))))

(defn alloc-id
  "Allocate a new ID for the type corresponding to key. Takes an optional
  action-fn which it will evaluate in transaction with the allocation of the id.
  Therefore there is no possibility of interleaving concurrent allocation of ids
  and the execution of associated action-fns. Execution of action-fn is also
  synchronised with the execution of free-id action-fns. Action-fn takes one
  param - the newly allocated id.

  Returns newly allocated id."
  ([k] (alloc-id k 1 nil))
  ([k size] (alloc-id k 1 nil))
  ([k size action-fn]
     (let [bits  (get allocator-bits k)
           limit (get allocator-limits k)]
       (when-not bits
         (throw (Exception. (str "Unable to get allocator bits for keyword " k))))
       (when-not limit
         (throw (Exception. (str "Unable to get allocator limit for keyword " k))))
       (dosync
        (let [id (find-gap @bits size 0 0 limit)]
          (alter bits fill-gaps id size true)
          (when action-fn (execute-action-fn #(action-fn id) "alloc-id"))
          id)))))

; The root group is implicitly allocated
(defonce _root-group_ (alloc-id :node))

(defn free-id
  "Free the id of type key. Takes an optional action-fn which it will evaluate
  in transaction with the freeing of the id. Therefore there is no possibility
  of interleaving concurrent freeing of ids and execution of associated
  action-fns. Execution of action-fn is also synchronised with the execution of
  alloc-id action-fns."
  ([k id] (free-id k id 1 nil))
  ([k id size] (free-id k id size nil))
  ([k id size action-fn]
     (let [bits (get allocator-bits k)
           limit (get allocator-limits k)]
       (dosync
        (alter bits fill-gaps id size false)
        (when action-fn (execute-action-fn action-fn "free-id"))))))

(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (let [bits (get allocator-bits k)
        limit (get allocator-limits k)]
    (dosync
     (ref-set bits (mk-bitset limit)))))
