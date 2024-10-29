(ns overtone.osc.dyn-vars)

;; We use binding to *osc-msg-bundle* to bundle messages
;; and send combined with an OSC timestamp.
(defonce ^{:dynamic true} *osc-msg-bundle* nil)

;; Timestamp of currently building `at` message bundle, if any.
;; Used by `at-offset` to schedule messages relative an enclosing `at`.
(defonce ^{:dynamic true} *at-time* nil)
