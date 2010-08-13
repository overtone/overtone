(ns devices.novation
  (:use midi)
  (:require [overtone.core.log :as log]))

(declare NOV-OUT)
(declare NOV-IN)

(defn start []
  (def NOV-OUT (midi-out "ReMOTE"))
  (def NOV-IN (midi-in)))

(def PID 0x03)
(def MSG-PREFIX [0xF0 0x00 0x20 0x29 0x03 0x03 0x20 0x02 PID])
(def ONLINE [0x00 0x01 0x01 0xF7])
(def OFFLINE [0x00 0x01 0x00 0xF7])

(def CLEAR-LEFT [0x00 0x02 0x02 0x04 0xF7])
(def CLEAR-RIGHT [0x00 0x02 0x02 0x05 0xF7])

(def WRITE-TXT [0x00 0x02 0x01])

(def msg-log* (atom []))

;(defn log-input []
;  (.setReceiver (:transmitter NOV-IN) (midi-handler
;                                        (fn [msg t]
;                                          (swap! msg-log* conj msg)))))
;
(defn online []
  (midi-sysex NOV-OUT (concat MSG-PREFIX ONLINE)))

(defn offline []
  (midi-sysex NOV-OUT (concat MSG-PREFIX OFFLINE)))

(defn write-text [row pos txt]
  (let [data (map int (seq txt))]
  (midi-sysex NOV-OUT (concat MSG-PREFIX WRITE-TXT [pos row 0x04] data [0xF7]))))

(defn clear-screens []
  (midi-sysex NOV-OUT CLEAR-LEFT)
  (midi-sysex NOV-OUT CLEAR-RIGHT))
