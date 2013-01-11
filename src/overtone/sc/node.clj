(ns overtone.sc.node
  (:use [overtone.helpers lib]
        [overtone.helpers.seq :only [zipper-seq]]
        [overtone.libs event deps]
        [overtone.sc server defaults]
        [overtone.sc.machinery allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc.util :only [id-mapper]]
        [overtone.sc.defaults :only [foundation-groups* INTERNAL-POOL]])
  (:require [clojure.zip :as zip]
            [overtone.config.log :as log]
            [overtone.at-at :as at-at]))

(defonce ^{:dynamic true} *inactive-node-modification-error* :exception)

(defn- inactive-node-modification-error
  "The default error behaviour triggered when a user attempts to either
  control or kill an inactive node."
  [node err-msg]
  (condp = *inactive-node-modification-error*
    :silent    nil ;;do nothing
    :warning   (println "Warning - " err-msg node " " (with-out-str (print node)))
    :exception (throw (Exception. (str "Error - " err-msg " " (with-out-str (print node)))))
    (throw
     (IllegalArgumentException.
      (str "Unexpected value for *inactive-node-modification-error*: "
           *inactive-node-modification-error*
           "Expected one of :silent, :warning, :exception.")))))

(defonce ^{:private true} __PROTOCOLS__

  (do
    (defprotocol to-sc-id* (to-sc-id [v]))

    (defprotocol ISynthNodeStatus
      (node-status [this])
      (node-block-until-ready [this]))

    (defprotocol ISynthNode
      (node-free   [this])
      (node-pause  [this])
      (node-start  [this])
      (node-place  [this position dest-node]))

    (defprotocol IControllableNode
      (node-control         [this params]
        "Modify control parameters of the synth node.")
      (node-control-range   [this ctl-start ctl-vals]
        "Modify a range of control parameters of the synth node.")      (node-map-controls    [this names-busses]
        "Connect a node's controls to a control bus.")
      (node-map-n-controls  [this start-control start-bus n]
        "Connect N controls of a node to a set of sequential control
        busses, starting at the given control name."))

    (defprotocol IKillable
      (kill* [this] "Kill a synth element (node, or group, or ...)."))

    (defprotocol ISynthGroup
      (group-prepend-node [group node]
        "Adds the node to the head (first to be executed) of the group.")
      (group-append-node  [group node]
        "Adds the node to the tail (last to be executed) of the group.")
      (group-clear        [group]
        "Nukes all nodes in the group. This completely clears out all
         subgroups and frees all subsynths." )
      (group-deep-clear   [group]
        "Traverses all groups below this group and frees all the
         synths. Group structure is left unaffected." )
      (group-post-tree    [group with-args?]
        "Posts a representation of this group's node subtree, i.e. all
         the groups and synths contained within it, optionally including
         the current control values for synths." )
      (group-node-tree    [group]
        "Request a representation of this group's node subtree, i.e. all
         the groups and synths contained within it.

         Low-level functionality. See node-tree for something more
         usable." ))))

(extend-type java.lang.Long to-sc-id*    (to-sc-id [v] v))
(extend-type java.lang.Integer to-sc-id* (to-sc-id [v] v))
(extend-type java.lang.Float to-sc-id* (to-sc-id [v] v))

(defn to-id
  "If object can be converted to an sc id, then return the sc id,
   otherwise returns the object unchanged."
  [obj]
    (try
    (to-sc-id obj)
    (catch IllegalArgumentException e
      obj)))

(defn idify
  "Attempts to convert all objs in col to a sc id. Mapjs objs to
   themselves if a conversion wasn't possible."
  [col]
  (map to-id col))

;; ## Node and Group Management

;; Synths, Busses, Controls and Groups are all Nodes.  Groups are linked lists
;; and group zero is the root of the graph.  Nodes can be added to a group in
;; one of these 5 positions relative to either the full list, or a specified node.

(def NODE-POSITION
  {:head         0
   :tail         1
   :before       2
   :after        3
   :replace      4})

(defn- map-and-check-node-args
  [arg-map]
  (let [name-fn (fn [name]
                  (let [name (to-str name)]
                    (when (not (string? name))
                      (throw (IllegalArgumentException. (str "Incorrect arg. Was expecting a string and found " name ". Full arg map: " arg-map))))
                    name))
        val-fn (fn [val]
                 (let [val (to-id val)
                       val (to-float val)]
                   (when (not (float? val))
                     (throw (IllegalArgumentException. (str "Incorrect arg. Was expecting a float and found " val ". Full arg map: " arg-map))))
                   val))]

    (zipmap (map name-fn (keys arg-map))
            (map val-fn (vals arg-map)))))

(defrecord SynthNode [synth id target position args sdef status loaded?]
  to-sc-id*
  (to-sc-id [this] (:id this)))

(derive SynthNode ::node)

(defmethod print-method SynthNode [s-node w]
  (.write w (format "#<synth-node[%s]: %s %d>"
                    (name @(:status s-node)) (:synth s-node) (:id s-node))))

(defonce active-synth-nodes* (atom {}))

;; ### Node
;;
;; A Node is an addressable node in a tree of nodes run by the synth engine.
;; There are two types, Synths and Groups. The tree defines the order of
;; execution of all Synths. All nodes have an integer ID.

;; Sending a synth-id of -1 lets the server choose an ID
(defn node
  "Asynchronously instantiate a synth node on the audio server.  Takes
  the synth name and a map of argument name/value pairs.  Optionally use
  target <node/group-id> and position <pos> to specify where the node
  should be located.  The position can be one
  of :head, :tail :before, :after, or :replace.

  (node \"foo\")
  (node \"foo\" {:pitch 60})
  (node \"foo\" {:pitch 60} {:target 0})
  (node \"foo\" {:pitch 60} {:position :tail :target 2})
  "
  ([synth-name] (node synth-name {} {:position :tail, :target 0}))
  ([synth-name arg-map] (node synth-name arg-map {:position :tail, :target 0}))
  ([synth-name arg-map location] (node synth-name arg-map location nil))
  ([synth-name arg-map location sdef]
     (if (not (server-connected?))
       (throw (Exception. "Not connected to synthesis engine.  Please boot or connect server.")))
     (let [id       (alloc-id :node)
           position (get location :position :tail)
           pos-id   (get NODE-POSITION position 1)
           target   (to-sc-id (get location :target 0))
           arg-map  (map-and-check-node-args arg-map)
           args     (flatten (seq arg-map))
           snode    (SynthNode. synth-name id target position arg-map sdef (atom :loading) (promise))]
       (swap! active-synth-nodes* assoc id snode)
       (apply snd "/s_new" synth-name id pos-id target args)
       snode)))

;; ### Synth node callbacks
;;
;; The synth server sends an n_go event when a synth node is created and an
;; n_end event when a synth node is destroyed.

(declare synth-group?)

(defn node?
  "Returns true if obj is a synth node i.e. a SynthNode or a SynthGroup
   object which has a type which derives
   from :overtone.sc.node/synth-node"
  [obj]
  (isa? (type obj) ::node))

(defn node-live?
  "Returns true if n is a running synth node."
  [n]
  (and (node? n)
       (= :live @(:status n))))

(defn node-paused?
  "Returns true if n is a paused synth node."
  [n]
  (and (node? n)
       (= :paused @(:status n))))

(defn node-loading?
  "Returns true if n is a loading synth node."
  [n]
  (and (node? n) (= :loading @(:status n))))

(defn node-active?
  "Returns true if n is an active synth node."
  [n]
  (or (node-live? n)
      (node-paused? n)))

(defn- ensure-node-active!
  ([node] (ensure-node-active! "Trying to modify an inactive node."))
  ([node err-msg]
     (when (node? node)
       (node-block-until-ready node))

     (when (and (node? node)
                (not (node-active? node)))
       (inactive-node-modification-error node err-msg))))


(defn node-free*
  "Free the specified nodes on the server. The allocated id is
  subsequently freed from the allocator via a callback fn listening
  for /n_end which will call node-destroyed."
  [node]
  {:pre [(server-connected?)]}
  (ensure-node-active! node "Trying to free a synth node that has been destroyed")
  (snd "/n_free" (to-sc-id node))
  node)

(defn- node-destroyed
  "Frees up a synth node to keep in sync with the server. Delays the
  freeing of the node-id for 1 second to avoid race conditions in the
  case where the id has been recycled and used before the node status
  has been set to :destroyed."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (log/debug (format "node-destroyed: %d - synth-node: %s" id snode))
    (if snode
      (reset! (:status snode) :destroyed)
      (log/warn (format "ERROR: The fn node-destroyed can't find synth node: %d" id)))
    (at-at/after 1000 #(free-id :node id 1) INTERNAL-POOL)
    (swap! active-synth-nodes* dissoc id)))

(defn- node-created
  "Called when a node is created on the synth."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (log/debug (format "node-created: %d\nsynth-node: %s" id snode))
    (if snode
      (do
        (reset! (:status snode) :live)
        (deliver (:loaded? snode) true))
      (log/warn (format "ERROR: The fn node-created can't find synth node: %d" id)))))

(defn- node-paused
  "Called when a node is turned off, but not deallocated."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (log/debug (format "node-paused: %d\nsynth-node: %s" id snode))
    (if snode
      (reset! (:status snode) :paused)
      (log/warn (format "ERROR: The fn node-paused can't find synth node: %d" id)))))

(defn- node-started
  "Called when a node is turned on."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (log/debug (format "node-started: %d\nsynth-node: %s" id snode))
    (if snode
      (reset! (:status snode) :live)
      (log/warn (format "ERROR: The fn node-started can't find synth node: %d" id)))))

;; Setup the feedback handlers with the audio server.
(on-event "/n_end" (fn [info] (node-destroyed (first (:args info)))) ::node-destroyer)
(on-event "/n_go"  (fn [info] (node-created   (first (:args info)))) ::node-creator)
(on-event "/n_off" (fn [info] (node-paused    (first (:args info)))) ::node-pauser)
(on-event "/n_on"  (fn [info] (node-started   (first (:args info)))) ::node-starter)

;; ### Group
;;
;; A Group is a collection of Nodes represented as a linked list. A new Node
;; may be added to the head or tail of the group. The Nodes within a Group
;; may be controlled together. The Nodes in a Group may be both Synths and
;; other Groups. At startup there is a top level group with an ID of zero
;; that defines the root of the tree. If the server was booted from within
;; SCLang (as opposed to from the command line) there will also be a 'default
;; group' with an ID of 1 which is the default target for all new Nodes. See
;; RootNode and default_group for more info.

(defrecord SynthGroup [group id target position status loaded?]
  to-sc-id*
  (to-sc-id [_] id))

(derive SynthGroup ::node)

(defmethod print-method SynthGroup [s-group w]
  (.write w (format "#<synth-group[%s]: %s %d>" (name @(:status s-group)) (:group s-group) (:id s-group))))

(defn- synth-group? [obj]
  (= overtone.sc.node.SynthGroup (type obj)))

(defn group
  "Create a new synth group as a child of the target group. By default
  creates a new group at the tail of the root group.

  The position can be one of :head, :tail :before, :after, or :replace.

  (group)                  ;=> Creates a new group at the tail of the
                               foundation-default-group
  (group \"foo\")            ;=> Creates a group named foo
  (group :tail my-g)       ;=> Creates a group at the tail of group
                               my-g
  (group \"bar\" :head my-g) ;=> Creates a named group at the head of
                               group my-g"
  ([]
     (group :tail (:default-group @foundation-groups*)))

  ([name-or-position]
     (let [id (alloc-id :node)]
       (if (string? name-or-position)
         (group name-or-position id :tail (:default-group @foundation-groups*))
         (group (str "Group-" id) id name-or-position (:default-group @foundation-groups*)))))

  ([name-or-position position-or-target]
     (let [id (alloc-id :node)]
       (if (string? name-or-position)
         (group name-or-position id position-or-target (:default-group @foundation-groups*))
         (group (str "Group-" id) id name-or-position position-or-target))))

  ([name position target]
     (group name (alloc-id :node) position target))

  ([name id position target]
     (ensure-connected!)
     (when-not target
       (throw (IllegalArgumentException. (str "The target for this group must exist."))))
     (let [pos    (if (keyword? position) (get NODE-POSITION position) position)
           target (to-sc-id target)
           pos    (or pos 1)
           snode  (SynthGroup. name id target position (atom :loading) (promise))]
       (swap! active-synth-nodes* assoc id snode)
       (snd "/g_new" id pos target)
       snode)))

(defn- group-free*
  "Free synth groups, releasing their resources."
  [& group-ids]
  (ensure-connected!)
  (apply node-free group-ids))

(defn node-pause*
  "Pause a running synth node."
  [node]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to pause a node that has been destroyed.")
  (snd "/n_run" (to-sc-id node) 0)
  node)

(defn node-start*
  "Start a paused synth node."
  [node]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to start a node that has been destroyed.")
  (snd "/n_run" (to-sc-id node) 1)
  node)

(defn node-place*
  "Place a node :before or :after another node."
  [node position target]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to relocate a node that has been destroyed.")
  (let [node-id   (to-sc-id node)
        target-id (to-sc-id target)]
    (cond
     (= :before position) (snd "/n_before" node-id target-id)
     (= :after  position) (snd "/n_after" node-id target-id))
    node))

(defn node-control*
  "Set control values for a node."
  [node name-values]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to control a node that has been destroyed.")
  (apply snd "/n_set" (to-sc-id node) (floatify (stringify (idify name-values))))
  node)

(defn node-get-control
  "Get one or more synth control values by name.  Returns a map of
  key/value pairs, for example:

  {:freq 440.0 :attack 0.2}"
  [node names]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to get control values of a node that has been destroyed.")
  (let [res   (recv "/n_set")
        cvals (do (apply snd "/s_get" (to-sc-id node) (stringify names))
                  (:args (deref! res)))]
    (apply hash-map (keywordify (drop 1 cvals)))))

;; This can be extended to support setting multiple ranges at once if necessary...
(defn node-control-range*
  "Set a range of controls all at once, or if node is a group control
  all nodes in the group."
  [node ctl-start ctl-vals]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to control a node that has been destroyed.")
  (let [node-id (to-sc-id node)]
    (apply snd "/n_setn" node-id ctl-start (count ctl-vals) ctl-vals))
  node)

(defn node-get-control-range
  "Get a range of n controls starting at a given name or index.
  Returns a vector of values."
  [node name-index n]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to access a node that has been destroyed.")
  (let [res   (recv "/n_setn")
        cvals (do (snd "/s_getn" (to-sc-id node) (to-str name-index) n)
                  (:args (deref! res)))]
    (vec (drop 3 cvals))))

(defn node-map-controls*
  "Connect a node's controls to a control bus."
  [node names-busses]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to map the controls of a node that has been destroyed.")
  (let [node-id      (to-sc-id node)
        names-busses (map idify (stringify names-busses))]
    (apply snd "/n_map" node-id names-busses))
  node)

(defn node-map-n-controls*
  "Connect N controls of a node to a set of sequential control busses,
  starting at the given control name."
  [node start-control start-bus n]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to map the controls of a node that has been destroyed.")
  (assert (isa? (type start-bus) :overtone.sc.bus/bus) "Invalid start-bus")
  (let [node-id (to-sc-id node)]
    (snd "/n_mapn" node-id (first (stringify [start-control])) (to-sc-id start-bus) n))
  node)

(defn- group-post-tree*
  "Posts a representation of this group's node subtree, i.e. all the
  groups and synths contained within it, optionally including the
  current control values for synths."
  [node with-args?]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to view a node that has been destroyed.")
  (snd "/g_dumpTree" (to-sc-id node) with-args?)
  node)

(defn- group-prepend-node*
  "Add a synth node to the end of a group list."
  [group node]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to move a node that has been destroyed.")
  (ensure-node-active! group "Attempting to use a node that has been destroyed.")
  (let [group-id (to-sc-id group)
        node-id  (to-sc-id node)]
    (snd "/g_head" group-id node-id))
  group)

(defn- group-append-node*
  "Add a synth node to the end of a group list."
  [group node]
  (ensure-connected!)
  (ensure-node-active! node "Attempting to move a node that has been destroyed.")
  (ensure-node-active! group "Attempting to move a node that has been destroyed.")
  (let [group-id (to-sc-id group)
        node-id  (to-sc-id node)]
    (snd "/g_tail" group-id node-id))
  group)

(defn- group-clear*
  "Free all child synth nodes in a group."
  [group]
  (ensure-connected!)
  (ensure-node-active! group "Attempting to clear a node that has been destroyed.")
  (snd "/g_freeAll" (to-sc-id group))
  group)

(defn- group-deep-clear*
  "Free all child synth nodes in and below this group in other child
  groups."
  [group]
  (ensure-connected!)
  (ensure-node-active! group "Attempting to deep clear a node that has been destroyed.")
  (snd "/g_deepFree" (to-sc-id group))
  group)

(defn node-status*
  "Get the current status of node."
  [node]
  @(:status node))

(defn node-block-until-ready*
  "Block the current thread until the node is no longer loading."
  [node]
  (deref! (:loaded? node)))

(extend java.lang.Long
  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control           node-control*
   :node-control-range     node-control-range*
   :node-map-controls      node-map-controls*
   :node-map-n-controls    node-map-n-controls*})

(extend java.lang.Integer
  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control           node-control*
   :node-control-range     node-control-range*
   :node-map-controls      node-map-controls*
   :node-map-n-controls    node-map-n-controls*})

(extend SynthNode
  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control           node-control*
   :node-control-range     node-control-range*
   :node-map-controls      node-map-controls*
   :node-map-n-controls    node-map-n-controls*}

  ISynthNodeStatus
  {:node-status            node-status*
   :node-block-until-ready node-block-until-ready*})

(extend SynthGroup
  IControllableNode
  {:node-control           node-control*
   :node-control-range     node-control-range*
   :node-map-controls      node-map-controls*
   :node-map-n-controls    node-map-n-controls*}

  ISynthNodeStatus
  {:node-status            node-status*
   :node-block-until-ready node-block-until-ready*})

(defn ctl
  "Send a node control messages specified in pairs of :arg-name val. It
  is possible to pass a sequence of nodes in which case the same control
  messages will be sent to all nodes.  i.e.
  (ctl 34 :freq 440 :vol 0.2)
  (ctl [34 37] :freq 440 :vol 0.2)"
  [node & args]
  (ensure-connected!)
  (if (sequential? node)
    (doseq [n node]
      (node-control n args))
    (node-control node args)))



(defn kill
  "Free one or more synth nodes.
  Functions that create instance of synth definitions, such as hit,
  return a handle for the synth node that was created.
  (let [handle (hit :sin)] ; returns => synth-handle
  (kill (+ 1000 (now)) handle))

  ; a single handle without a time kills immediately
  (kill handle)

  ; or a bunch of synth handles can be removed at once
  (kill (hit) (hit) (hit))

  ; or a seq of synth handles can be removed at once
  (kill [(hit) (hit) (hit)])
  "
  [& nodes]
  (ensure-connected!)
  (doseq [node (flatten nodes)]
    (kill* node)))

(extend SynthNode
  IKillable
  {:kill* node-free*})

(extend java.lang.Long
  IKillable
  {:kill* node-free*})

;;/g_queryTree				get a representation of this group's node subtree.
;;	[
;;		int - group ID
;;		int - flag: if not 0 the current control (arg) values for synths will be included
;;	] * N
;;
;; Request a representation of this group's node subtree, i.e. all the groups and
;; synths contained within it. Replies to the sender with a /g_queryTree.reply
;; message listing all of the nodes contained within the group in the following
;; format:
;;
;;	int - flag: if synth control values are included 1, else 0
;;	int - node ID of the requested group
;;	int - number of child nodes contained within the requested group
;;	then for each node in the subtree:
;;	[
;;		int - node ID
;;		int - number of child nodes contained within this node. If -1this is a synth, if >=0 it's a group
;;		then, if this node is a synth:
;;		symbol - the SynthDef name for this node.
;;		then, if flag (see above) is true:
;;		int - numControls for this synth (M)
;;		[
;;			symbol or int: control name or index
;;			float or symbol: value or control bus mapping symbol (e.g. 'c1')
;;		] * M
;;	] * the number of nodes in the subtree

(defonce ^{:dynamic true} *node-tree-data* nil)

(defn- parse-synth-tree
  [id ctls?]
  (let [sname (first *node-tree-data*)]
    (if ctls?
      (let [n-ctls              (second *node-tree-data*)
            [ctl-data new-data] (split-at (* 2 n-ctls) (nnext *node-tree-data*))
            ctls                (apply hash-map ctl-data)]
        (set! *node-tree-data* new-data)
        {:type :synth
         :name sname
         :id id
         :controls ctls})
      (do
        (set! *node-tree-data* (next *node-tree-data*))
        {:type :synth
         :name sname
         :id id}))))

(defn- parse-node-tree-helper [ctls?]
  (let [[id n-children & new-data] *node-tree-data*]
    (set! *node-tree-data* new-data)
    (cond
     (neg? n-children)
     (parse-synth-tree id ctls?)        ; synth

     (= 0 n-children)
     {:type :group
      :id id
      :name (get-in @active-synth-nodes* [id :group] "Unknown Group")
      :children nil}

     (pos? n-children)
     {:type     :group
      :id       id
      :name     (get-in @active-synth-nodes* [id :group] "Unknown Group")
      :children (doall (map (fn [i] (parse-node-tree-helper ctls?))
                            (range n-children)))})))

(defn- parse-node-tree
  [data]
  (let [ctls? (= 1 (first data))]
    (binding [*node-tree-data* (next data)]
      (parse-node-tree-helper ctls?))))

(defn- group-node-tree*
  "Returns a data structure representing the current arrangement of
  groups and synthesizer instances residing on the audio server."
  ([] (group-node-tree* 0))
  ([node & [ctls?]]
     (ensure-connected!)
     (ensure-node-active! node "Attempting to view a node that has been destroyed.")
     (let [ctls? (if (or (= 1 ctls?) (= true ctls?)) 1 0)
           id    (to-sc-id node)]
       (let [reply-p (recv "/g_queryTree.reply")
             _       (snd "/g_queryTree" id ctls?)
             tree    (:args (deref! reply-p))]
         (with-meta (parse-node-tree tree)
           {:type ::node-tree})))))

(extend SynthGroup
  ISynthGroup
  {:group-prepend-node group-prepend-node*
   :group-append-node  group-append-node*
   :group-clear        group-clear*
   :group-deep-clear   group-deep-clear*
   :group-post-tree    group-post-tree*
   :group-node-tree    group-node-tree*}

  IKillable
  {:kill* group-deep-clear*})


(extend java.lang.Long
  ISynthGroup
  {:group-prepend-node group-prepend-node*
   :group-append-node  group-append-node*
   :group-clear        group-clear*
   :group-deep-clear   group-deep-clear*
   :group-post-tree    group-post-tree*
   :group-node-tree    group-node-tree*})

(defn node-tree
  "Returns a data representation of the synth node tree starting at
  the root group."
  ([] (node-tree (:root-group @foundation-groups*)))
  ([root]
     (ensure-connected!)
     (group-node-tree (to-sc-id root))))

(defn node-tree-zipper
  "Returns a zipper representing the tree of the specified node or
  defaults to the current node tree"
  ([] (node-tree-zipper (:root-group @foundation-groups*)))
  ([root]
     (zip/zipper map? :children #(assoc %1 :children %2) (group-node-tree root))))

(defn node-tree-seq
  "Returns a lazy seq of a depth-first traversal of the tree of the
  specified node defaulting to the current node tree"
  ([] (node-tree-zipper (:root-group @foundation-groups*)))
  ([root] (zipper-seq (node-tree-zipper root))))

(defn node-tree-matching-synth-ids
  "Returns a seq of synth ids in the node tree with specific
  root (defaulting to the entire node tree) that match regexp or
  strign."
  ([re-or-str] (node-tree-matching-synth-ids re-or-str (:root-group @foundation-groups*)))
  ([re-or-str root]
     (let [matcher-fn (if (string? re-or-str)
                        =
                        re-matches)]
       (map :id
            (filter #(and (:name %)
                          (matcher-fn re-or-str (:name %)))
                    (node-tree-seq root))))))
