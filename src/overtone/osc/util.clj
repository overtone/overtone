(ns overtone.osc.util
  (:require [clojure.string :as str]))

(defn print-debug [& msgs]
  (binding [*out* *err*]
    (apply println msgs)))

(def osc-debug* (ref false))

(def SEND-LOOP-TIMEOUT 10000) ; ms
(def OSC-SEND-Q-SIZE 512) ;;initial queue size
(def OSC-TIMETAG-NOW 1) ; Timetag representing right now.
(def BUFFER-SIZE 32768)
(def PAD (byte-array 4))
(def PATTERN-MATCH-CHARS [" " "#" "*" "," "?" "[" "]" "{" "}"])

(defn contains-pattern-match-chars?
  "Returns true if str contains any pattern-match characters"
  [str] (some #(.contains str %) PATTERN-MATCH-CHARS))

;; TODO: Figure out how to detect a byte array correctly...
(defn osc-type-tag
  "Generate a type tag for the argument list args. Each arg in args should be
  one of the following specific types and the type tag will consist of a series
  of consecutive chars representing the type of each arg in sequence.

  For example, an arg list of a string then three ints will generate a type tag
  of \"siii\"

  OSC Data Types:
  int => i
   * 32-bit big-endort an two's complement integer

  long => h
   * 64-bit big-endian two's complement integer

  float => f
   * 32-bit big-endian IEEE 754 floating point number

  string => s
   * A sequence of non-null ASCII characters followed by a null, followed by 0-3
     additional null characters to make the total number of bits a multiple of
     32.

  blob => b
   * An int32 size count, followed by that many 8-bit bytes of arbitrary binary
     data, followed by 0-3 additional zero bytes to make the total number of
     bits a multiple of 32.

  OSC-timetag
   * 64-bit big-endian fixed-point timestamp"
  [args]
  (apply str
         (map (fn [arg]
                (condp = (type arg)
                  Integer "i"
                  Long    "h"
                  Float   "f"
                  Double  "d"
                  (type PAD) "b" ; This is lame... what is a byte array an instance of?
                  String  "s"))
              args)))


(defn mk-osc-msg
  "Create an OSC message

  An OSC message consists of:
  * a path prefixed with /
  * a type tag prefixed with , (the , isn't stored in the map)
  * 0 or more args (where the number of args equals the number of types
    in the type tag"
  [path type-tag & args]
  (let [type-tag (if (and type-tag (.startsWith type-tag ","))
                   (.substring type-tag 1)
                   type-tag)]
    (with-meta {:path path
                :type-tag type-tag
                :args args}
      {:type :osc-msg})))

(defn split-path
  "Takes an osc path and splits it to a seq of names
  (split-path \"/foo/bar/baz\") ;=> [\"foo\" \"bar\" \"baz\"]"
  [path]
  (rest (str/split path #"/")))

(defn mk-osc-bundle
  "Create an osc bundle. Throws exceptions if the timestamp and items aren't the
  correct types (number and list respectively)."
  [timestamp items]
  (when-not (number? timestamp)
    (throw (IllegalArgumentException. (str "OSC bundle timestamp param needs to be a number. Got: " (type timestamp) " - " timestamp))))
  (when-not (sequential? items)
    (throw (IllegalArgumentException. (str "OSC bundle items param needs to be a list. Got: " (type items) " - " items))))
  (with-meta {:timestamp timestamp
              :items items}
    {:type :osc-bundle}))

(defn osc-msg?
  "Returns true if obj is an OSC message"
  [obj]
  (= :osc-msg (type obj)))

(defn osc-bundle?
  "Returns true if obj is an OSC Bundle"
  [obj]
  (= :osc-bundle (type obj)))

;; osc-type-tag defined in osc/internals
(defn osc-msg-infer-types
  "Returns an OSC message.  Infers the types of the args."
  [path & args]
  (apply mk-osc-msg path (osc-type-tag args) args))
