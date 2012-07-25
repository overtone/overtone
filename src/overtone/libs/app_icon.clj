(ns overtone.libs.app-icon
  (:use [clojure.java.io]
        [overtone.helpers.system :only [get-os]])
  (:import [com.apple.eawt.Application]
           [java.awt.Toolkit]))

(defn- set-osx-icon
  [icon]
  (try
    (.setDockIconImage (com.apple.eawt.Application/getApplication) icon)
    (catch Exception e
      false))
  true)

(defn- setup-icon
  []
  (let [icon-url (clojure.java.io/resource "overtone-logo.png")
        icon     (.createImage (java.awt.Toolkit/getDefaultToolkit) icon-url)]
    (cond
      (= :mac (get-os)) (set-osx-icon icon))))

(defonce __INIT-ICON__
  (setup-icon))
