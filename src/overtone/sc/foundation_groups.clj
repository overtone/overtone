(ns
    ^{:doc "Foundation Group Structure"
      :author "Sam Aaron"}
  overtone.sc.foundation-groups
  (use [overtone.libs.deps                 :only [on-deps satisfy-deps]]
       [overtone.libs.event                :only [on-sync-event]]
       [overtone.sc.node                   :only [group group-deep-clear]]
       [overtone.sc.server                 :only [ensure-connected!]]
       [overtone.sc.defaults               :only [foundation-groups* empty-foundation-groups]]
       [overtone.sc.server                 :only [clear-msg-queue]]
       [overtone.sc.machinery.server.comms :only [with-server-sync]]))

(defn- setup-foundation-groups
  []
  (let [overtone-group
        (with-server-sync #(group "Overtone" :head 0))

        input-group
        (with-server-sync #(group "Overtone Inputs" :head overtone-group))

        root-group
        (with-server-sync #(group "Overtone Root" :after input-group))

        user-group
        (with-server-sync #(group "Overtone User" :head root-group))

        safe-pre-default-group
        (with-server-sync #(group "Overtone Safe Pre Default" :head user-group))

        default-group
        (with-server-sync #(group "Overtone Default" :after safe-pre-default-group))

        safe-post-default-group
        (with-server-sync #(group "Overtone Safe Post Default" :after default-group))

        output-group
        (with-server-sync #(group "Overtone Output" :after root-group))

        monitor-group
        (with-server-sync #(group "Overtone Monitor" :after output-group))]
    (swap! foundation-groups* assoc
           :overtone-group          overtone-group
           :input-group             input-group
           :root-group              root-group
           :user-group              user-group
           :safe-pre-default-group  safe-pre-default-group
           :default-group           default-group
           :safe-post-default-group safe-post-default-group
           :output-group            output-group
           :monitor-group           monitor-group)
    (satisfy-deps :foundation-groups-created)))

(on-deps :server-connected ::setup-foundation-groups setup-foundation-groups)

(defn foundation-overtone-group
  "Returns the node id for the container group for the whole of the Overtone
   foundational infrastructure. All of Overtone's groups and nodes will
   be a child of this node.

   This group should not typically be used. Prefer a group within
   foundation-user-group such as foundation-default-group or
   foundation-safe-group."
  []
  (ensure-connected!)
  (:overtone-group @foundation-groups*))

(defn foundation-output-group
  "Returns the node id for the Overtone output group used for the
   default output mixers.

   This group should not typically be used. Prefer a group within
   foundation-user-group such as foundation-default-group or
   foundation-safe-group."
  []
  (ensure-connected!)
  (:output-group @foundation-groups*))

(defn foundation-monitor-group
  "Returns the node id for the Overtone output group for the default
   monitors i.e. the recording synths.

   This group should not typically be used. Prefer a group within
   foundation-user-group such as foundation-default-group or
   foundation-safe-group."
  []
  (ensure-connected!)
  (:monitor-group @foundation-groups*))

(defn foundation-input-group
  "Returns the node id for the Overtone output group for the default
   input mixers.

   This group should not typically be used. Prefer a group within
   foundation-user-group such as foundation-default-group or
   foundation-safe-group."
  []
  (ensure-connected!)
  (:input-group @foundation-groups*))

(defn foundation-root-group
  "Returns the node id for the main Overtone group for synth activity.

   This group should not typically be used. Prefer a group within
   foundation-user-group such as foundation-default-group or
   foundation-safe-group."
  []
  (ensure-connected!)
  (:root-group @foundation-groups*))

(defn foundation-user-group
  "Returns the node id for the main Overtone user group. This is where
   you should place your activity. This group already contains three
   convenience groups which you should prefer to using this group
   directly:

  * foundation-safe-pre-default-group
  * foundation-default-group
  * foundation-safe-post-default-group

  See the docstrings for these groups for more details."
  []
  (ensure-connected!)
  (:user-group @foundation-groups*))

(defn foundation-default-group
  "Returns the node id for the default Overtone group. This is where the
   majority of user activity should take place. This group is the target
   of a deep clear when the stop fn is called."
  []
  (ensure-connected!)
  (:default-group @foundation-groups*))

(defn foundation-safe-group
  "Synonym for foundation-safe-post-default-group.

  Returns the node id for a safe Overtone group. This is similar to
  the default group only it isn't the target of deep clear when the stop
  fn is called. Therefore synths in this group will *not* be
  automatically stopped on execution of the stop fn.

  This returns the safe group which is positioned *after* the default
  group. For a safe group that is positioned before the default group
  see foundation-safe-pre-default-group."
  []
  (ensure-connected!)
  (:safe-post-default-group @foundation-groups*))

(defn foundation-safe-post-default-group
  "Returns the node id for a safe Overtone group. This is similar to
  the default group only it isn't the target of deep clear when the stop
  fn is called. Therefore synths in this group will *not* be
  automatically stopped on execution of the stop fn.

  This returns the safe group which is positioned *after* the default
  group. For a safe group that is positioned before the default group
  see foundation-safe-pre-default-group."
  []
  (ensure-connected!)
  (:safe-post-default-group @foundation-groups*))

(defn foundation-safe-pre-default-group
  "Returns the node id for a safe Overtone group. This is similar to
  the default group only it isn't the target of deep clear when the stop
  fn is called. Therefore synths in this group will *not* be
  automatically stopped on execution of the stop fn.

  This returns the safe group which is positioned *after* the default
  group. For a safe group that is positioned after the default group
  see foundation-safe-post-default-group."
  []
  (ensure-connected!)
  (:safe-pre-default-group @foundation-groups*))

(on-sync-event :reset
               (fn [event-info]
                 (ensure-connected!)
                 (clear-msg-queue)
                 (group-deep-clear (foundation-default-group)))
               ::deep-clear-foundation-default-group)

(on-sync-event :shutdown
               (fn [event-info]
                 (reset! foundation-groups* empty-foundation-groups))
                ::reset-foundation-groups)
