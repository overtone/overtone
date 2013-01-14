(ns
    ^{:doc "Useful system information fns"
      :author "Sam Aaron and Jeff Rose"}
  overtone.helpers.system)

(defn system-user-name
  "returns the name of the current user"
  []
  (System/getProperty "user.name"))

(defn os-name
  "Returns a string representing the current operating system. Useful
   for debugging, etc. Prefer get-os for os-specific logic."
  []
  (System/getProperty "os.name"))

(defn get-os
  "Return the OS as a keyword. One of :windows :linux :mac"
  []
  (let [os (os-name)]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn get-cpu-bits
  "Return either 32 or 64 for the JVM architecture currently used."
  []
  (Integer. (System/getProperty "sun.arch.data.model")))

(defn classpath-seq
  "Return the classpath as a seq"
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

(defn os-description
  "Returns a string describing the OS and archicture. Useful for user
   feedback and debugging.  Prefer get-os and get-cpu-bits for
   os/arch-specific logic."
  []
  (str (get-cpu-bits) " bit " (os-name)))
