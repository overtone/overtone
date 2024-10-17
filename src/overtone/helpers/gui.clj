(ns overtone.helpers.gui
  (:import java.awt.event.ActionListener))

(set! *warn-on-reflection* true)

;; note that proxy creates a public var that could be interned
;; into the api namespaces, so it's safer to put this helper here
(defn action-listener ^ActionListener [->event]
  (proxy [ActionListener] []
    (actionPerformed [event]
      (->event event))))
