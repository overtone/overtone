(ns
    ^{:doc "Useful system information fns"
      :author "Sam Aaron and Jeff Rose"}
  overtone.helpers.system)

(defn system-user-name
  "returns the name of the current user"
  []
  (System/getProperty "user.name"))

(defn get-os
  "Return the OS as a keyword. One of :windows :linux :max"
  []
  (let [os (System/getProperty "os.name")]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn get-cpu-bits
  "Return either 32 or 64 for the JVM architecture currently used."
  []
  (Integer. (System/getProperty "sun.arch.data.model")))

(defn classpath-seq
  "Return the the classpath as a seq"
  []
  (map (memfn getPath)
       (seq (.getURLs (.getClassLoader clojure.lang.RT)))))

(defn windows-os?
  "Returns true if the current os is windows based"
  []
  (= :windows (get-os)))

(defn linux-os?
  "Returns true if the current os is mac based"
  []
  (= :linux (get-os)))

(defn mac-os?
  "Returns true if the current os is bac based"
  []
  (= :mac (get-os)))
