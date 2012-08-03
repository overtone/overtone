(ns
    ^{:doc "ID allocator system. Used to return new unique integer IDs in a threadsafe manner. IDs may be freed and therefore reused. Allows action fns to be executed in synchronisation with allocation and deallocation."
      :author "Sam Aaron"}
  overtone.sc.machinery.allocator
  (:use [overtone.sc defaults]
        [overtone.sc.machinery.server args])
  (:require [overtone.config.log :as log]))

;; ## Allocators
;;
;; We use bit sets to store the allocation state of resources
;; on the audio server.  These typically get allocated on usage by the client,
;; and then freed either by client request or automatically by receiving
;; notifications from the server.  (e.g. When an envelope trigger fires to
;; free a synth.)

(defn mk-bitset
  "Create a vector representation of a bitset"
  [size]
  (vec (repeat size false)))

(defonce allocator-bits
  {:node         (ref (mk-bitset (sc-arg-default :max-nodes)))
   :audio-buffer (ref (mk-bitset (sc-arg-default :max-buffers)))
   :audio-bus    (ref (mk-bitset (sc-arg-default :max-audio-bus)))
   :control-bus  (ref (mk-bitset (sc-arg-default :max-control-bus)))})

(defn- fill-gaps
  "Returns a new vector similar to bs except filled with with size consecutive vals from idx

  example: (fill with 3 from idx 1 for 2 vals)
  (fill-gaps [1 0 0 0 1] 1 2 3) ;=> [1 3 3 0 1]"
  [bs idx size val]
  (loop [bs bs
         idx idx
         size size]
    (if (> size 0)
      (recur (assoc bs idx val) (inc idx) (dec size))
      bs)))

(defn- find-gap
  "Returns index of the first gap in vector bs with specified size where gap is
  defined as a falsey value.

  example:
  (find-gap [true false true false false false true] 3) ;=> 3"
  ([bs size] (find-gap bs size 0 0))
  ([bs size idx gap-found]
     (let [limit (count bs)]
       (when (> idx limit)
         (throw (Exception. (str "No more ids! Unable to allocate a sequence of ids of length: " size))))
       (if (= gap-found size)
         (- idx gap-found)
         (let [gap-found (if (not (get bs idx)) (inc gap-found) 0)]
           (find-gap bs size (inc idx) gap-found))))))

(defonce action-fn-executor* (agent nil))

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
  ([k size] (alloc-id k size nil))
  ([k size action-fn]
     (let [bits  (get allocator-bits k)]
       (when-not bits
         (throw (Exception. (str "Unable to get allocator bits for keyword " k))))
       (dosync
        (let [id (find-gap @bits size)]
          (alter bits fill-gaps id size true)
          (when action-fn (execute-action-fn #(action-fn id) "alloc-id"))
          id)))))

; The root group is implicitly allocated
(defonce _root-group_ (alloc-id :node))

(defn free-id
  "Free the ID of type key. Takes an optional action-fn which it will evaluate
  in transaction with the freeing of the id. (Evaluation happens within an agent
  so there's no need to worry about the transaction retrying multiple times).
  Therefore there is no possibility of interleaving concurrent freeing of ids
  and execution of associated action-fns. Execution of action-fn is also
  synchronised with the execution of alloc-id action-fns (they all use the same
  agent)."
  ([k id] (free-id k id 1 nil))
  ([k id size] (free-id k id size nil))
  ([k id size action-fn]
     (let [bits (get allocator-bits k)]
       (dosync
        (alter bits fill-gaps id size false)
        (when action-fn (execute-action-fn action-fn "free-id"))))))

(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (let [bits (get allocator-bits k)]
    (dosync
     (let [new-bitset (mk-bitset (count @bits))]
       (ref-set bits new-bitset)))
    :cleared))
