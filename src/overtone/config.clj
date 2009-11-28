(ns overtone.config)

(def config* (ref {}))

(defn config [key] 
  (@config* key))

(defn set-config [k v] 
  (dosync (alter config* assoc k v)))

(defn- get-os []
  (let [os (System/getProperty "os.name")]
    (cond 
      (re-find #"[Ww]indows" os) :windows
      (re-find #"[Ll]inux" os)   :linux
      (re-find #"[Mm]ac" os)     :mac)))

(defn- setup []
  (set-config :os (get-os)))

(setup)
