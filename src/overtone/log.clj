(ns overtone.log
  (:import (java.util.logging Logger Level ConsoleHandler FileHandler
                              SimpleFormatter)))

(defonce LOGGER (Logger/getLogger "overtone"))
(defonce LOG-FILE "log")

(defonce LOG-APPEND false)
(defonce LOG-CONSOLE (ConsoleHandler.))
(defonce LOG-FILE-HANDLER (FileHandler. LOG-FILE LOG-APPEND))

(def LEVELS {:debug Level/FINE
             :info  Level/INFO
             :warn  Level/WARNING
             :error Level/SEVERE})

(def DEFAULT-LEVEL :error)

(defn level [& [lvl]]
  (if (nil? lvl)
    (.getLevel LOGGER)
    (do
      (assert (contains? LEVELS lvl))
      (.setLevel LOGGER (lvl LEVELS)))))

(level DEFAULT-LEVEL)

(defonce LOG-SETUP?
  (do 
    ;(.setOutputStream LOG-CONSOLE *out*)
    (.setFormatter LOG-FILE-HANDLER (SimpleFormatter.))
    (.addHandler LOGGER LOG-CONSOLE)
    (.addHandler LOGGER LOG-FILE-HANDLER)
    true))

(defn debug [& msg]
  (.log LOGGER Level/FINE (str msg)))

(defn info [& msg]
  (.log LOGGER Level/INFO (str msg)))

(defn warning [& msg]
  (.log LOGGER Level/WARNING (str msg)))

(defn error [& msg]
  (.log LOGGER Level/SEVERE (str msg)))

