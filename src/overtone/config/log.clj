(ns overtone.config.log
  "Basic logging functionality."
  {:author "Jeff Rose"}
  (:require
   [clojure.pprint :refer [pprint]]
   [overtone.config.store :as store])
  (:import
   (java.time ZoneId)
   (java.time.format DateTimeFormatter)
   (java.util Date)
   (java.util.logging ConsoleHandler FileHandler Formatter Level Logger LogManager LogRecord StreamHandler)))

(set! *warn-on-reflection* true)

(def ^:private LOG-APPEND true)
(def ^:private LOG-LIMIT 5000000)
(def ^:private LOG-COUNT 2)
(def ^:private DEFAULT-LOG-LEVEL :warn)
(def ^:private LOG-LEVELS {:debug Level/FINE
                           :info  Level/INFO
                           :warn  Level/WARNING
                           :error Level/SEVERE})
(def ^:private REVERSE-LOG-LEVELS (apply hash-map (flatten (map reverse LOG-LEVELS))))

(defonce ^:private ^Logger ROOT-LOGGER (Logger/getLogger "overtone"))
(defonce ^:private ^FileHandler LOG-FILE-HANDLER (FileHandler. store/OVERTONE-LOG-FILE (int LOG-LIMIT) (int LOG-COUNT) ^boolean LOG-APPEND))
(defonce ^:private ^DateTimeFormatter TIME-FORMATTER (.withZone (DateTimeFormatter/ofPattern "HH:MM:SS")
                                                                (ZoneId/systemDefault)))

(defn- initial-log-level
  []
  (or (store/config-get :log-level)
      DEFAULT-LOG-LEVEL))

(defn- log-formatter ^Formatter []
  (proxy [Formatter] []
    (format [^LogRecord log-rec]
      (str #_(.format TIME-FORMATTER (.getInstant log-rec)) #_" "
           "[" (.getLoggerName log-rec) "] [" (.getLevel log-rec) "] " (.getMessage log-rec)
           "\n"))))

(defn- print-handler []
  (let [formatter (log-formatter)]
    (proxy [StreamHandler] []
      (publish [msg] (println (.format formatter msg))))))

(defn console []
  (.addHandler ROOT-LOGGER (print-handler)))

(defn log-level
  "Returns the current log level"
  []
  (.getLevel ROOT-LOGGER))

(defn set-level!
  "Set the log level for this session. Use one of :debug, :info, :warn
  or :error"
  [level]
  (assert (contains? LOG-LEVELS level))
  (.setLevel ROOT-LOGGER (get LOG-LEVELS level)))

(defn set-default-level!
  "Set the log level for this and future sessions - stores level in
  config. Use one of :debug, :info, :warn or :error"
  [level]
  (set-level! level)
  (store/config-set! :log-level level)
  level)

(defn ns-logger ^Logger [ns]
  (Logger/getLogger (name (ns-name ns))))

(defn log-msg [form level msg]
  (:ns (meta form)))

(defn debug
  "Log msg with level debug"
  [& msg]
  (.log (ns-logger *ns*) Level/FINE ^String (apply str msg)))

(defn info
  "Log msg with level info"
  [& msg]
  (.log (ns-logger *ns*) Level/INFO ^String (apply str msg)))

(defn warn
  "Log msg with level warn"
  [& msg]
  (.log (ns-logger *ns*) Level/WARNING ^String (apply str msg)))

(defn error
  "Log msg with level error"
  [& msg]
  (.log (ns-logger *ns*) Level/SEVERE ^String (apply str msg)))

(defmacro with-error-log
  "Wrap body with a try/catch form, and log exceptions (using warning)."
  [message & body]
  `(try
     ~@body
     (catch Exception ex#
       (println "Exception: " ex#)
       (warn (str ~message "\nException: " ex#
                  (with-out-str (clojure.stacktrace/print-stack-trace ex#)))))))

;;setup logger
(defonce ^:private __setup-logs__
  (do
    (.reset (LogManager/getLogManager))
    (set-level! (initial-log-level))
    (.setFormatter LOG-FILE-HANDLER (log-formatter))
    (console)
    (.addHandler ROOT-LOGGER LOG-FILE-HANDLER)))

(defonce ^:private __cleanup-logger-on-shutdown__
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (info "Shutting down - cleaning up logger")
                               (run! #(.removeHandler ROOT-LOGGER %)
                                     (.getHandlers ROOT-LOGGER))
                               (.close LOG-FILE-HANDLER)))))
