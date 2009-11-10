(ns overtone.log
  (:import (java.util.logging Logger Level ConsoleHandler FileHandler
                              SimpleFormatter)))

(defonce LOGGER (Logger/getLogger "overtone"))
(defonce LOG_FILE "log")

(defonce LOG_APPEND false)
(defonce LOG_CONSOLE (ConsoleHandler.))
(defonce LOG_FILE_HANDLER (FileHandler. LOG_FILE LOG_APPEND))

(defonce LOG_SETUP?
  (do 
    ;(.setOutputStream LOG_CONSOLE *out*)
    (.setFormatter LOG_FILE_HANDLER (SimpleFormatter.))
    (.addHandler LOGGER LOG_CONSOLE)
    (.addHandler LOGGER LOG_FILE_HANDLER)
    true))

(defn debug [& msg]
  (.log LOGGER Level/FINE (str msg)))

(defn info [& msg]
  (.log LOGGER Level/INFO (str msg)))

(defn warning [& msg]
  (.log LOGGER Level/WARNING (str msg)))

(defn error [& msg]
  (.log LOGGER Level/SEVERE (str msg)))

(def LEVELS {:debug Level/FINE
             :info  Level/INFO
             :warn  Level/WARNING
             :error Level/SEVERE})

(defn level [lvl]
  (assert (contains? LEVELS lvl))
  (.setLevel LOGGER (lvl LEVELS)))
