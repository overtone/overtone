(ns
    ^{:doc "The Overtone <-> scsynth OSC messaging infrastructure.
            Contains type signatures and checkers for all major scsynth
            OSC messages to reduce the number of bogus messages that may
            potentially crash the server."
      :author "Sam Aaron"}
  overtone.sc.osc-validator
  (:use [overtone.osc])
  (:require [overtone.util.log :as log]))

(def TYPES
  {
   :anything         {:desc "any type"
                      :validator (fn [val] true)}

   :int              {:desc "an integer"
                      :validator (fn [val] (integer? val))}

   :sample-idx       {:desc "an integer 0 or greater representing the index of a sample"
                      :validator (fn [val] (and (integer? val)
                                               (>= val 0)))}

   :sample-val       {:desc "a float representing the value of a sample"
                      :validator (fn [val] (float? val))}

   :frame-start      {:desc "an integer 0 or greater representing the starting frame"
                      :validator (fn [val] (and (integer? val)
                                               (>=  val 0)))}

   :chan-idx         {:desc "an integer 0 or greater representing a channel index"
                      :validator (fn [val] (and (integer? val)
                                               (>=  val 0)))}

   :num-frames       {:desc "an integer -1 or greater representing the number of frames"
                      :validator (fn [val] (and (integer? val)
                                               (>=  val -1)))}

   :buf-num          {:desc "an integer representing a buffer number"
                      :validator (fn [val] (integer? val))}

   :ugen-idx         {:desc "an integer representing the index of a ugen"
                      :validator (fn [val] (integer? val))}

   :count            {:desc "a positive integer representing the cardinality of elements"
                      :validator (fn [val] (and (integer? val)
                                               (> val 0)))}

   :node-id          {:desc "an integer (-1 upwards) representing a node id."
                      :validator (fn [val] (and (integer? val)
                                               (>= val -1)))}

   :group-id          {:desc "an integer (-1 upwards) representing a group id."
                      :validator (fn [val] (and (integer? val)
                                               (>= val -1)))}

   :synth-id         {:desc "an integer (-1 upwards) representing a synth id."
                      :validator (fn [val] (and (integer? val)
                                               (>= val -1)))}

   :ctl-val          {:desc "a float representing a control value"
                      :validator (fn [val] (float? val ))}

   :string           {:desc "a string"
                      :validator (fn [val] (string? val))}

   :cmd-name         {:desc "a string representing a command name"
                      :validator (fn [val] (string? val))}

   :ctl-handle       {:desc "an integer or a string representing a control"
                      :validator (fn [val] (or (integer? val)
                                              (string? val)))}

   :ctl-bus-idx      {:desc "an integer (-1 or greater) representing a control bus index"
                      :validator (fn [val] (and (integer? val)
                                               (>= val -1)))}

   :pathname         {:desc "a string representing a pathname"
                      :validator (fn [val] (string? val))}

   :synthdef-name    {:desc "a string representing the name of a synthdef"
                      :validator (fn [val] (string? val))}

   :bytes            {:desc "a buffer of bytes"
                      :validator (fn [val] (= (type (byte-array 0)) (type val)))}

   :zero-to-three    {:desc "an integer in the range 0 -> 3 inclusive"
                      :validator (fn [val] (and (integer? val)
                                               (>= val 0)
                                               (<= val 3)))}

   :zero-to-four     {:desc "an integer in the range 0 -> 4 inclusive"
                      :validator (fn [val] (and (integer? val)
                                               (>= val 0)
                                               (<= val 4)))}

   :minus-two-to-one {:desc "an integer in the range -2 -> 1 inclusive"
                      :validator (fn [val] (and (integer? val)
                                               (>= val -2)
                                               (<= val 1)))}

   :zero-or-one      {:desc "an integer that's either 0 or 1"
                      :validator (fn [val] (or (= 0 val)
                                              (= 1 val)))}

   :header-format    {:desc "a string representing the sound header format - one of \"aiff\", \"next\", \"wav\", \"ircam\"\", \"raw\""
                      :validator (fn [val] (or (= val "aiff")
                                              (= val "next")
                                              (= val "wav")
                                              (= val "ircam")
                                              (- val "raw")))}

   :sample-format    {:desc "a string representing the sound sample format - one of \"int8\", \"int16\", \"int24\", \"int32\", \"float\", \"double\", \"mulaw\", \"alaw\""
                      :validator (fn [val] (or (= val "int8")
                                              (= val "int16")
                                              (= val "int24")
                                              (= val "int32")
                                              (= val "float")
                                              (= val "double")
                                              (= val "mulaw")
                                              (= val "alaw")))}})

(def OSC-TYPE-SIGNATURES
  {
   ;;Master Controls
   "/quit"               []
   "/notify"             [:zero-or-one]
   "/status"             []
   "/cmd"                [:int :anything*]
   "/dumpOSC"            [:zero-to-three]
   "/sync"               [:int]
   "/clearSched"         []
   "/error"              [:minus-two-to-one]

   ;;Synth Definition Commands
   "/d_recv"             [:bytes]
   "/d_load"             [:pathname]
   "/d_loadDir"          [:pathname]
   "/d_free"             [:synthdef-name]

   ;;Node Commands
   "/n_free"             [:node-id]
   "/n_run"              [:node-id :zero-or-one]
   "/n_set"              [:node-id :ALTERNATING-ctl-handle-THEN-ctl-val*]
   "/n_setn"             [:node-id :ctl-handle :count :ctl-val*]
   "/n_fill"             [:node-id :ctl-handle :count :ctl-val]
   "/n_map"              [:node-id :ctl-handle :ctl-bus-idx]
   "/n_mapn"             [:node-id :ctl-handle :ctl-bus-idx :count]
   "/n_mapa"             [:node-id :ctl-handle :ctl-bus-idx]
   "/n_mapan"            [:node-id :ctl-handle :ctl-bus-idx :count]
   "/n_before"           [:node-id :node-id]
   "/n_after"            [:node-id :node-id]
   "/n_query"            [:node-id]
   "/n_trace"            [:node-id]
   "/n_order"            [:zero-to-three :node-id :node-id]

   ;;Synth Commands
   "/s_new"              [:synthdef-name :synth-id :zero-to-four :node-id :ALTERNATING-ctl-handle-THEN-ctl-val*]
   "/s_get"              [:synth-id :ctl-handle]
   "/s_getn"             [:synth-id :ctl-handle :count]
   "/s_noid"             [:synth-id]

   ;;Group Commands
   "/g_new"              [:group-id :zero-to-four :node-id]
   "/g_head"             [:group-id :node-id]
   "/g_tail"             [:group-id :node-id]
   "/g_freeAll"          [:group-id]
   "/g_deepFree"         [:group-id]
   "/g_dumpTree"         [:group-id :zero-or-one]
   "/g_queryTree"        [:group-id :zero-or-one]

   ;;Unit Generator Commands
   "/u_cmd"              [:node-id :ugen-idx :cmd-name :anything*]

   ;;Buffer Commands
   "/b_alloc"            [:buf-num :num-frames :count]
   "/b_allocRead"        [:buf-num :pathname :frame-start :num-frames]
   "/b_allocReadChannel" [:buf-num :pathname :frame-start :num-frames :chan-idx*]
   "/b_read"             [:buf-num :pathname :frame-start :num-frames :frame-start :zero-or-one]
   "/b_readChannel"      [:buf-num :pathname :frame-start :num-frames :frame-start :zero-or-one :chan-idx*]
   "/b_write"            [:buf-num :pathname :header-format :sample-format :num-frames :frame-start :zero-or-one]
   "/b_free"             [:buf-num]
   "/b_zero"             [:buf-num]
   "/b_set"              [:buf-num :sample-idx :sample-val]
   "/b_setn"             [:buf-num :sample-idx :count :sample-val*]
   "/b_fill"             [:buf-num :sample-idx :count :sample-val]
   "/b_gen"              [:buf-num :cmd-name :anything*]
   "/b_close"            [:buf-num]
   "/b_query"            [:buf-num]
   "/b_get"              [:buf-num :sample-idx]
   "/b_getn"             [:buf-num :sample-idx :count]

   ;;Control Bus Commands
   "/c_set"              [:ctl-bus-idx :ctl-val]
   "/c_setn"             [:ctl-bus-idx :count :ctl-val*]
   "/c_fill"             [:ctl-bus-idx :count :ctl-val]
   "/c_get"              [:ctl-bus-idx]
   "/c_getn"             [:ctl-bus-idx :count]})

(defn- description
  [type]
  (:desc (TYPES type)))

(defn- valid?
  [type val]
  (if-let [type-info (TYPES type)]
    (apply (:validator type-info) [val])
    (let [err-str (str "Unknown OSC arg type " type ". Expecting one of " (keys TYPES))]
      (log/error err-str)
      (throw (IllegalArgumentException. err-str)))))

(defn- many-type?
  [type]
  (.endsWith (name type) "*"))

(defn- many-type->type
  [many-type]
  (let [n (name many-type)]
    (keyword (.substring n 0 (dec (.length n))))))

(defn- expand-many
  [many-type]
  (repeat (many-type->type many-type)))

(defn- expand-alternating
  [type]
  (let [[[_ a b]] (re-seq #"ALTERNATING-(.*)-THEN-(.*)?\*" (name type))]
    (cycle [(keyword a) (keyword b)])))

(defn- alternating?
  [type]
  (.startsWith (name type) "ALTERNATING"))

(defn- expand-type
  [type]
  (cond
   (alternating? type) (expand-alternating type)
   (many-type? type) (expand-many type)
   :else type))

(defn- expand-type-sig
  [sig]
  (flatten (map #(expand-type %) sig)) )

(defn- sig-arity
  [name]
  (let [sig (OSC-TYPE-SIGNATURES name)]
    (if (some many-type? sig)
      Float/POSITIVE_INFINITY
      (count sig))))

(defn- arity-mismatch?
  [s-arity a-arity]

  (and (not (= Float/POSITIVE_INFINITY s-arity))
       (not (= s-arity a-arity))))

(defn- error-args
  [sig args]

  (some (fn [[type arg]] (if (not (valid? type arg))
                          [type arg]
                          false))
        (partition 2 (interleave (expand-type-sig sig) args))))

(defn- checked-snd
  [sig host path & args]
  (let [s-arity (sig-arity path)
        a-arity (count args)]

    (when (arity-mismatch? s-arity a-arity)
      (let [err-string (str "Failed attempting to send an OSC message to SuperCollider server. Reason: you didn't provide the correct number of arguments for OSC message " path ". Expected " s-arity ", got " a-arity " Type sig: " sig " Arg list: " args)]
        (log/error err-string)
        (throw (IllegalArgumentException. err-string))))

    (when-let [[err-type err-arg] (error-args sig args)]
      (let [err-string (str "Failed attempting to send an OSC message to SuperCollider server. Reason: incorrect arglist type in OSC message " path ". Expected " (description err-type) " found " (type err-arg) ". Message name: " path " Type sig: " sig " Arg list: " args)]
        (log/error err-string)
        (throw (IllegalArgumentException. err-string))))

    (apply osc-send host path args)))


(defn validated-snd
  "Send an scsynth osc message. Validates message. Raises an exception if the
  message is unknown or is not well formed according to the message's type
OP  signature."
  [host path & args]
  (if-let [sig (OSC-TYPE-SIGNATURES path)]
    (apply checked-snd sig host path args)
    (let [err-string (str "Unknown scsynth OSC path" path)]
      (log/error err-string)
      (throw (Exception. err-string)))))
