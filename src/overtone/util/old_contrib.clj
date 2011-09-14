(ns
    ^{:doc "Temporary namespace for functions and macros we depend on from the old Clojure 1.2 contrib. This namespace should be removed when these have been relocated to the new contrib structure."}
  overtone.util.old-contrib
  (:import [java.util.regex Pattern]
           [java.io InputStreamReader OutputStreamWriter]))

;; name-with-attributes by Konrad Hinsen:
;;http://code.google.com/p/clojure-contrib/source/browse/trunk/src/clojure/contrib/def.clj?r=889
(defn name-with-attributes
  "To be used in macro definitions.
   Handles optional docstrings and attribute maps for a name to be defined
   in a list of macro arguments. If the first macro argument is a string,
   it is added as a docstring to name and removed from the macro argument
   list. If afterwards the first macro argument is a map, its entries are
   added to the name's metadata map and the map is removed from the
   macro argument list. The return value is a vector containing the name
   with its extended metadata map and the list of unprocessed macro
   arguments."
  [name macro-args]
  (let [[docstring macro-args] (if (string? (first macro-args))
                 [(first macro-args) (next macro-args)]
                 [nil macro-args])
    [attr macro-args]      (if (map? (first macro-args))
                 [(first macro-args) (next macro-args)]
                 [{} macro-args])
    attr                   (if docstring
                 (assoc attr :doc docstring)
                 attr)
    attr                   (if (meta name)
                 (conj (meta name) attr)
                 attr)]
    [(with-meta name attr) macro-args]))


;;String util fns from:
;;https://github.com/richhickey/clojure-contrib/blob/bacf49256673242bb7ce09b9f5983c27163e5bfc/src/main/clojure/clojure/contrib/string.clj
(defn split
  "Splits string on a regular expression.  Optional argument limit is
  the maximum number of splits."
  ([#^Pattern re #^String s] (seq (.split re s)))
  ([#^Pattern re limit #^String s] (seq (.split re s limit))))

(defn replace-str
  "Replaces all instances of substring a with b in s."
  [#^String a #^String b #^String s]
  (.replace s a b))

(defn replace-re
  "Replaces all matches of re with replacement in s."
  [re replacement #^String s]
  (.replaceAll (re-matcher re s) replacement))


;;from str-utils2
;;https://github.com/richhickey/clojure-contrib/blob/a1c66df5287776b4397cf3929a5f498fbb34ea32/src/main/clojure/clojure/contrib/str_utils2.clj
(defn #^String chop
  "Removes the last character of string, does nothing on a zero-length
  string."
  [#^String s]
  (let [size (count s)]
    (if (zero? size)
      s
      (subs s 0 (dec (count s))))))


;;shell-out
;;https://raw.github.com/richhickey/clojure-contrib/a1c66df5287776b4397cf3929a5f498fbb34ea32/src/main/clojure/clojure/contrib/shell_out.clj
;   Copyright (c) Chris Houser, Jan 2009. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

; :dir and :env options added by Stuart Halloway

; Conveniently launch a sub-process providing to its stdin and
; collecting its stdout


(def *sh-dir* nil)
(def *sh-env* nil)

(defmacro with-sh-dir [dir & forms]
  "Sets the directory for use with sh, see sh for details."
  `(binding [*sh-dir* ~dir]
     ~@forms))

(defmacro with-sh-env [env & forms]
  "Sets the environment for use with sh, see sh for details."
  `(binding [*sh-env* ~env]
     ~@forms))

(defn- stream-seq
  "Takes an InputStream and returns a lazy seq of integers from the stream."
  [stream]
  (take-while #(>= % 0) (repeatedly #(.read stream))))

(defn- aconcat
  "Concatenates arrays of given type."
  [type & xs]
  (let [target (make-array type (apply + (map count xs)))]
    (loop [i 0 idx 0]
      (when-let [a (nth xs i nil)]
        (System/arraycopy a 0 target idx (count a))
        (recur (inc i) (+ idx (count a)))))
    target))

(defn- parse-args
  "Takes a seq of 'sh' arguments and returns a map of option keywords
  to option values."
  [args]
  (loop [[arg :as args] args opts {:cmd [] :out "UTF-8" :dir *sh-dir* :env *sh-env*}]
    (if-not args
      opts
      (if (keyword? arg)
        (recur (nnext args) (assoc opts arg (second args)))
        (recur (next args) (update-in opts [:cmd] conj arg))))))

(defn- as-env-key [arg]
  "Helper so that callers can use symbols, keywords, or strings
   when building an environment map."
  (cond
   (symbol? arg) (name arg)
   (keyword? arg) (name arg)
   (string? arg) arg))

(defn- as-file [arg]
  "Helper so that callers can pass a String for the :dir to sh."
  (cond
   (string? arg) (java.io.File. arg)
   (nil? arg) nil
   (instance? java.io.File arg) arg))

(defn- as-env-string [arg]
  "Helper so that callers can pass a Clojure map for the :env to sh."
  (cond
   (nil? arg) nil
   (map? arg) (into-array String (map (fn [[k v]] (str (as-env-key k) "=" v)) arg))
   true arg))


(defn sh
  "Passes the given strings to Runtime.exec() to launch a sub-process.

  Options are

  :in    may be given followed by a String specifying text to be fed to the
         sub-process's stdin.
  :out   option may be given followed by :bytes or a String. If a String
         is given, it will be used as a character encoding name (for
         example \"UTF-8\" or \"ISO-8859-1\") to convert the
         sub-process's stdout to a String which is returned.
         If :bytes is given, the sub-process's stdout will be stored in
         a byte array and returned.  Defaults to UTF-8.
  :return-map
         when followed by boolean true, sh returns a map of
           :exit => sub-process's exit code
           :out  => sub-process's stdout (as byte[] or String)
           :err  => sub-process's stderr (as byte[] or String)
         when not given or followed by false, sh returns a single
         array or String of the sub-process's stdout followed by its
         stderr
  :env   override the process env with a map (or the underlying Java
         String[] if you are a masochist).
  :dir   override the process dir with a String or java.io.File.

  You can bind :env or :dir for multiple operations using with-sh-env
  and with-sh-dir."
  [& args]
  (let [opts (parse-args args)
        proc (.exec (Runtime/getRuntime)
		    (into-array (:cmd opts))
		    (as-env-string (:env opts))
		    (as-file (:dir opts)))]
    (if (:in opts)
      (with-open [osw (OutputStreamWriter. (.getOutputStream proc))]
        (.write osw (:in opts)))
      (.close (.getOutputStream proc)))
    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (let [[[out err] combine-fn]
                (if (= (:out opts) :bytes)
                  [(for [strm [stdout stderr]]
                    (into-array Byte/TYPE (map byte (stream-seq strm))))
                  #(aconcat Byte/TYPE %1 %2)]
                  [(for [strm [stdout stderr]]
                    (apply str (map char (stream-seq
                                            (InputStreamReader. strm (:out opts))))))
                  str])
              exit-code (.waitFor proc)]
        (if (:return-map opts)
          {:exit exit-code :out out :err err}
          (combine-fn out err))))))

(comment

(println (sh "ls" "-l"))
(println (sh "ls" "-l" "/no-such-thing"))
(println (sh "sed" "s/[aeiou]/oo/g" :in "hello there\n"))
(println (sh "cat" :in "x\u25bax\n"))
(println (sh "echo" "x\u25bax"))
(println (sh "echo" "x\u25bax" :out "ISO-8859-1")) ; reads 4 single-byte chars
(println (sh "cat" "myimage.png" :out :bytes)) ; reads binary file into bytes[]

)
