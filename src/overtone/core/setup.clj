(ns
  ^{:doc "Library initialization and configuration."
     :author "Jeff Rose"}
  overtone.core.setup
  (:import (java.io File))
  (:use (overtone.core config)))

(defn- get-os []
  (let [os (System/getProperty "os.name")]
    (cond
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(def OVERTONE-DIR (str (System/getProperty "user.home") "/.overtone"))
(def CONFIG-FILE (str OVERTONE-DIR "/config"))
(def OVERTONE-LOG-FILE (str OVERTONE-DIR "/log"))

(defn check-app-dir []
  (let [ot-dir (File. OVERTONE-DIR)]
    (when (not (.exists ot-dir))
      (.mkdir ot-dir)
      (save-config CONFIG-FILE {}))))

(check-app-dir)
(live-config CONFIG-FILE)

(if (not (contains? @config* :os))
  (dosync (alter config* assoc :os (get-os))))

;    (assoc config* :user.name "anonymous")))


