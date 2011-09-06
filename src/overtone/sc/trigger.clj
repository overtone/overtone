(ns overtone.sc.trigger
  (:use [overtone.sc core]
        [overtone.lib event]))

; Trigger Notifications
;
; This command is the mechanism that synths can use to trigger events in
; clients.  The node ID is the node that is sending the trigger. The trigger ID
; and value are determined by inputs to the SendTrig unit generator which is
; the originator of this message.
;
; /tr a trigger message
;
;   int - node ID
;   int - trigger ID
;   float - trigger value
(defonce trigger-handlers* (ref {}))

(defn on-trigger [node-id trig-id f]
  (dosync (alter trigger-handlers* assoc [node-id trig-id] f)))

(defn remove-trigger [node-id trig-id]
  (dosync (alter trigger-handlers* dissoc [node-id trig-id])))

(on-event "/tr"
          (fn [msg]
            (let [[node-id trig-id value] (:args msg)
                  handler (get @trigger-handlers* [node-id trig-id])]
              (if handler
                (handler node-id trig-id value))))
          ::trig-handler)
