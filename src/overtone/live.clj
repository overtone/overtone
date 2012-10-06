(ns overtone.live
  (:require [overtone.api]))

(overtone.api/immigrate-overtone-api)

(defonce __AUTO-BOOT__ (boot-server-and-mixer))
