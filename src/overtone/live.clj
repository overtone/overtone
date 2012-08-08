(ns overtone.live
  (:require [overtone.core]))

(overtone.core/immigrate-overtone-core)

(defonce __AUTO-BOOT__ (boot-server-and-mixer))
