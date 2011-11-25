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

(defn classpath-seq
  "Return the the classpath as a seq"
  []
  (map (memfn getPath)
       (seq (.getURLs (.getClassLoader clojure.lang.RT)))))