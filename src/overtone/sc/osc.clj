(ns
    ^{:doc "The Overtone <-> scsynth OSC messaging infrasructure.
            Contains type signatures and checkers for all major scsynth
            OSC messages."
      :author "Sam Aaron"}
  overtone.sc.osc
  (:require [overtone.log :as log])
  (:use  osc))

(def TYPES
  {
   :anything         {:desc "any type"
                      :validator (fn [val] true)}

   :int              {:desc "an integer"
                      :validator (fn [val] (integer? val))}

   :string           {:desc "a string"
                      :validator (fn [val] (string? val))}

   :bytes            {:desc "a buffer of bytes"
                      :validator (fn [val] true)}

   :zero-to-three    {:desc "an integer in the range 0 -> 3 inclusive"
                      :validator (fn [val] (and (integer? val)
                                               (> val -1)
                                               (< val 4)))}

   :minus-two-to-one {:desc "an integer in the range -2 -> 1 inclusive"
                      :validator (fn [val] (and (integer? val)
                                              (> val -3)
                                              (< val 2)))}
   })

(defn description
  [type]
  (:desc (TYPES type)))

(defn valid?
  [type val]
  (if-let [type-info (TYPES type)]
    (apply (:validator type-info) [val])
    (throw (IllegalArgumentException. (str "Unknown OSC arg type " type ". Expecting one of " (keys TYPES))))))

(def OSC-TYPE-SIGNATURES
  {
   "/quit"       []
   "/notify"     [:int]
   "/status"     []

   "/cmd"        [:int :anything*]
   "/dumpOSC"    [:zero-to-three]
   "/sync"       [:int]
   "/clearSched" []
   "/error"      [:minus-two-to-one]
   "/d_recv"     [:bytes]
   "/d_load"     [:string]

   "/n_free"     [:int]
   })

(defn many-type?
  [type]
  (.endsWith (str type) "*"))

(defn many-type->type
  [many-type]
  (let [n (name many-type)]
    (keyword (.substring n 0 (dec (.length n))))))

(defn expand-type-sig
  [sig]
  (flatten (map #(if (many-type? %) (repeat (many-type->type %)) %) sig)) )

(defn sig-arity
  [name]
  (let [sig (OSC-TYPE-SIGNATURES name)]
    (if (some many-type? sig)
      Float/POSITIVE_INFINITY
      (count sig))))

(defn arity-mismatch?
  [s-arity a-arity]

  (and (not (= Float/POSITIVE_INFINITY s-arity))
       (not (= s-arity a-arity))))

(defn error-args
  [sig args]

  (some (fn [[type arg]] (if (not (valid? type arg))
                          [type arg]
                          false))
        (partition 2 (interleave (expand-type-sig sig) args))))

(defn checked-snd
  [sig host path & args]
  (let [s-arity (sig-arity path)
        a-arity (count args)]

    (when (arity-mismatch? s-arity a-arity)
      (let [err-string (str "You didn't provide the correct number of arguments for OSC message " path ". Expected " s-arity ", got " a-arity " Type sig: " sig " Arg list: " args)]
        (log/error err-string)
        (throw (IllegalArgumentException. err-string))))

    (when-let [[err-type err-arg] (error-args sig args)]
      (let [err-string (str "Incorrect arglist type in scsynth message. Expected " (description err-type) " found " (type err-arg) ". Message name: " path " Type sig: " sig " Arg list: " args)]
        (log/error err-string)
        (throw (IllegalArgumentException. err-string))))

    (apply osc-send host path args)))


(defn snd
  "Send an osc message using the osc library"
  [host path & args]
  (if-let [sig (OSC-TYPE-SIGNATURES path)]
    (apply checked-snd sig host path args)
    (apply osc-send host path args)))
