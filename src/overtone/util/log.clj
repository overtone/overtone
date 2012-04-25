(ns
  ^{:doc "Basic logging functionality."
     :author "Jeff Rose"}
  overtone.util.log
  (:import [java.util.logging Logger Level ConsoleHandler FileHandler
            StreamHandler Formatter LogRecord]
           [java.util Date])
  (:use [overtone.config store]
        [clojure.pprint :only (pprint)]))

; Sets up some basic logging infrastructure and helpers for the project.

(defonce LOGGER (Logger/getLogger "overtone"))

(defonce LOG-APPEND true)
(defonce LOG-CONSOLE (ConsoleHandler.))

(def LOG-FILE-HANDLER (FileHandler. OVERTONE-LOG-FILE LOG-APPEND))

(def LEVELS {:debug Level/FINE
             :info  Level/INFO
             :warn  Level/WARNING
             :error Level/SEVERE})

(def REVERSE-LEVELS (apply hash-map (flatten (map reverse LEVELS))))

(def DEFAULT-LEVEL :warn)

(defn level [& [lvl]]
  (if (nil? lvl)
    (.getLevel LOGGER)
    (do
      (assert (contains? LEVELS lvl))
      (.setLevel LOGGER (lvl LEVELS)))))

(defn- log-formatter []
  (proxy [Formatter] []
    (format [^LogRecord log-rec]
      (let [lvl (REVERSE-LEVELS (.getLevel log-rec))
            msg (.getMessage log-rec)
            ts (.getMillis log-rec)]
        (with-out-str (pprint {:level lvl
                               :timestamp ts
                               :date (Date. (long ts))
                               :msg msg}))))))

(defn- print-handler []
  (let [formatter (log-formatter)]
    (proxy [StreamHandler] []
      (publish [msg] (println (.format formatter msg))))))

(defn console []
  (.addHandler LOGGER (print-handler)))

(defonce LOG-SETUP?
  (do
    (level DEFAULT-LEVEL)
    (.setFormatter LOG-FILE-HANDLER (log-formatter))
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

(defmacro with-error-log
  "Wrap body with a try/catch form, and log exceptions (using warning)."
  [message & body]
  `(try
     ~@body
     (catch Exception ex#
       (println "Exception: " ex#)
       (warning (str ~message "\nException: " ex#
                     (with-out-str (clojure.stacktrace/print-stack-trace ex#)))))))


