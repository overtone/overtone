(ns overtone.live
  (:require [overtone.api]))

(overtone.api/immigrate-overtone-api)

(defonce __AUTO-BOOT__
  (when (overtone.sc.server/server-disconnected?)
    (overtone.studio.mixer/boot-server-and-mixer)))
