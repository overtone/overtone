(ns overtone.log
  (:import (java.util.logging Logger Level ConsoleHandler FileHandler
                              SimpleFormatter)))

; Sets up some basic logging infrastructure and helpers for the project.

(defonce LOGGER (Logger/getLogger "overtone"))
(defonce LOG-FILE "log")

(defonce LOG-APPEND false)
(defonce LOG-CONSOLE (ConsoleHandler.))
(defonce LOG-FILE-HANDLER (FileHandler. LOG-FILE LOG-APPEND))

(def LEVELS {:debug Level/FINE
             :info  Level/INFO
             :warn  Level/WARNING
             :error Level/SEVERE})

(def DEFAULT-LEVEL :info)

(defn level [& [lvl]]
  (if (nil? lvl)
    (.getLevel LOGGER)
    (do
      (assert (contains? LEVELS lvl))
      (.setLevel LOGGER (lvl LEVELS)))))


(defonce LOG-SETUP?
  (do 
    (level DEFAULT-LEVEL)
    
    ;(.setOutputStream LOG-CONSOLE *out*)
    (.setFormatter LOG-FILE-HANDLER (SimpleFormatter.))
    ;(.addHandler LOGGER LOG-CONSOLE)
    (.addHandler LOGGER LOG-FILE-HANDLER)
    true))

(defn debug [& msg]
  (.log LOGGER Level/FINE (apply str msg)))

(defn info [& msg]
  (.log LOGGER Level/INFO (apply str msg)))

(defn warning [& msg]
  (.log LOGGER Level/WARNING (apply str msg)))

(defn error [& msg]
  (.log LOGGER Level/SEVERE (apply str msg)))

