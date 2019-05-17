(ns overtone.osc.dyn-vars)

;; We use binding to *osc-msg-bundle* to bundle messages
;; and send combined with an OSC timestamp.
(defonce ^{:dynamic true} *osc-msg-bundle* nil)
