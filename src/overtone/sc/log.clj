(ns overtone.sc.log
  (:require [overtone.log :as log])
  (:use [overtone event core]
        osc))

;; TODO: Setup some logging infrastructure with functions to enable and
;; disable logging of osc messages and other useful info.

(def osc-log* (atom []))

(defn osc-log [on?]
  (if on?
    (on-sync-event :osc-msg-received ::osc-logger
                   (fn [{:keys [path args] :as msg}]
                     (swap! osc-log* #(conj % msg))))
    (remove-handler :osc-msg-received ::osc-logger)))

(defn debug
  "Control debug output from both the Overtone and the audio server."
  [on?]
  (if on?
    (do
      (log/level :debug)
      (osc-debug true)
      (snd "/dumpOSC" 1))
    (do
      (log/level :error)
      (osc-debug false)
      (snd "/dumpOSC" 0))))

