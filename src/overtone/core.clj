(ns overtone.core
  (:require [overtone.api]))

(overtone.api/immigrate-overtone-api)

(defonce __PRINT-CONNECT-HELP__
  (when-not (server-connected?)
    (println "--> Please boot a server to start making noise:
    * (boot-server)             ; boot default server (honours config)
    * (boot-internal-server)    ; boot an internal server
    * (boot-external-server)    ; boot an external server
    * (connect-external-server) ; connect to an existing external server
")))
