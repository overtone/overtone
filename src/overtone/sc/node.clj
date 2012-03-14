(ns overtone.sc.node
  (:use [overtone.util lib]
        [overtone.libs event deps]
        [overtone.sc bus server]
        [overtone.sc.machinery defaults allocator]
        [overtone.sc.machinery.server comms]
        [overtone.sc.util :only [id-mapper]])
  (:require [overtone.util.log :as log]))

;; ## Node and Group Management

;; Synths, Busses, Controls and Groups are all Nodes.  Groups are linked lists
;; and group zero is the root of the graph.  Nodes can be added to a group in
;; one of these 5 positions relative to either the full list, or a specified node.

(def POSITION
  {:head         0
   :tail         1
   :before       2
   :after        3
   :replace      4})

(defn- bus->id
  "if val is a bus, return its ref id otherwise return val"
  [val]
  (if (bus? val)
    (:id val)
    val))

(defn- map-and-check-node-args
  [arg-map]
  (let [name-fn (fn [name]
                  (let [name (to-str name)]
                    (when (not (string? name))
                      (throw (IllegalArgumentException. (str "Incorrect arg. Was expecting a string and found " name ". Full arg map: " arg-map))))
                    name))
        val-fn (fn [val]
                 (let [val (bus->id val)
                       val (to-float val)]
                   (when (not (float? val))
                     (throw (IllegalArgumentException. (str "Incorrect arg. Was expecting a float and found " val ". Full arg map: " arg-map))))
                   val))]

    (zipmap (map name-fn (keys arg-map))
            (map val-fn (vals arg-map)))))


(defprotocol to-synth-id* (to-synth-id [v]))

(extend-type java.lang.Long to-synth-id*    (to-synth-id [v] v))
(extend-type java.lang.Integer to-synth-id* (to-synth-id [v] v))

(defprotocol ISynthNode
  (node-free   [this])
  (node-pause  [this])
  (node-start  [this])
  (node-place  [this position dest-node]))

(defprotocol IControllableNode
  (node-control         [this & params]
    "Modify control parameters of the synth node.")
  (node-control-range   [this ctl-start & ctl-vals]
    "Modify a range of control parameters of the synth node.")
  (node-map-controls    [this & names-busses]
    "Connect a node's controls to a control bus.")
  (node-map-n-controls  [this start-control start-bus n]
    "Connect N controls of a node to a set of sequential control busses,
    starting at the given control name."))

(defrecord SynthNode [synth id target position status]
  to-synth-id*
  (to-synth-id [this] (:id this)))

(def active-synth-nodes* (atom {}))

;; ### Node
;;
;; A Node is an addressable node in a tree of nodes run by the synth engine.
;; There are two types, Synths and Groups. The tree defines the order of
;; execution of all Synths. All nodes have an integer ID.

;; Sending a synth-id of -1 lets the server choose an ID
(defn node
  "Instantiate a synth node on the audio server.  Takes the synth name and a
  map of argument name/value pairs.  Optionally use target <node/group-id>
  and position <pos> to specify where the node should be located.  The
  position can be one of :head, :tail :before, :after, or :replace.

  (node \"foo\")
  (node \"foo\" {:pitch 60})
  (node \"foo\" {:pitch 60} {:target 0})
  (node \"foo\" {:pitch 60} {:position :tail :target 2})
  "
  ([synth-name] (node synth-name {} {:position :tail, :target 0}))
  ([synth-name arg-map] (node synth-name arg-map {:position :tail, :target 0}))
  ([synth-name arg-map location]
     (if (not (server-connected?))
       (throw (Exception. "Not connected to synthesis engine.  Please boot or connect server.")))
     (let [id       (alloc-id :node)
           position (or ((get location :position :tail) POSITION) 1)
           target   (to-synth-id (get location :target 0))
           arg-map  (map-and-check-node-args arg-map)
           args     (flatten (seq arg-map))
           snode    (SynthNode. synth-name id target position (atom :loading))]
       (apply snd "/s_new" synth-name id position (to-synth-id target) args)
       (swap! active-synth-nodes* assoc id snode)
       snode)))

;; ### Synth node callbacks
;;
;; The synth server sends an n_go event when a synth node is created and an
;; n_end event when a synth node is destroyed.

(defn node-free*
  "Free the specified nodes on the server. The allocated id is subsequently
  freed from the allocator via a callback fn listening for /n_end which will
  call node-destroyed."
  [node]
  {:pre [(server-connected?)]}
  (snd "/n_free" (to-synth-id node)))

(defn- node-destroyed
  "Frees up a synth node to keep in sync with the server."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    ;(log/info (format "node-destroyed: %d\nsynth-node: %s" id snode))
    (free-id :node id 1 #(log/debug (format "node-destroyed: %d" id)))
    (if snode
      (reset! (:status snode) :destroyed)
      (log/warning (format "ERROR: node-destroyed can't find synth node: %d" id)))
    (swap! active-synth-nodes* dissoc id)))

(defn- node-created
  "Called when a node is created on the synth."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    ;(log/info (format "node-created: %d\nsynth-node: %s" id snode))
    (if snode
      (reset! (:status snode) :live)
      (log/warning (format "ERROR: node-created can't find synth node: %d" id)))))

(defn- node-paused
  "Called when a node is turned off, but not deallocated."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (if snode
      (reset! (:status snode) :paused)
      (log/warning (format "ERROR: node-paused can't find synth node: %d" id)))))

(defn- node-started
  "Called whena a node is turned on."
  [id]
  (let [snode (get @active-synth-nodes* id)]
    (if snode
      (reset! (:status snode) :running)
      (log/warning (format "ERROR: node-started can't find synth node: %d" id)))))

; Setup the feedback handlers with the audio server.
(on-event "/n_end" #(node-destroyed (first (:args %))) ::node-destroyer)
(on-event "/n_go"  #(node-created   (first (:args %))) ::node-creator)
(on-event "/n_off" #(node-paused    (first (:args %))) ::node-pauser)
(on-event "/n_on"  #(node-started   (first (:args %))) ::node-starter)

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

(defrecord SynthGroup [id target position status]
  to-synth-id*
  (to-synth-id [_] id))

(defn group
  "Create a new synth group as a child of the target group. By default creates
  a new group at the tail of the root group."
  ([] (group :tail (root-group)))
  ([position target] (group (alloc-id :node) position target))
  ([id position target]
     (let [pos (if (keyword? position) (get POSITION position) position)
           target (to-synth-id target)
           pos (or pos 1)
           snode    (SynthGroup. id target position (atom :loading))]
       (snd "/g_new" id pos target)
       (swap! active-synth-nodes* assoc id snode)
       snode)))

(defn- group-free*
  "Free synth groups, releasing their resources."
  [& group-ids]
  {:pre [(server-connected?)]}
  (apply node-free group-ids))

(defn node-pause*
  "Pause a running synth node."
  {:pre [(server-connected?)]}
  [node]
  (snd "/n_run" (to-synth-id node) 0))

(defn node-start*
  "Start a paused synth node."
  [node]
  {:pre [(server-connected?)]}
  (snd "/n_run" (to-synth-id node) 1))

(defn node-place*
  "Place a node :before or :after another node."
  [node position target]
  {:pre [(server-connected?)]}
  (let [node-id (to-synth-id node)
        target-id (to-synth-id target)]
    (cond
      (= :before position) (snd "/n_before" node-id target-id)
      (= :after  position) (snd "/n_after" node-id target-id))))

(defn node-control*
  "Set control values for a node."
  [node & name-values]
  {:pre [(server-connected?)]}
  (let [node-id (to-synth-id node)]
        (apply snd "/n_set" node-id (floatify (stringify (bus->id name-values))))
        node-id))

; This can be extended to support setting multiple ranges at once if necessary...
(defn node-control-range*
  "Set a range of controls all at once, or if node is a group control
  all nodes in the group."
  [node ctl-start & ctl-vals]
  {:pre [(server-connected?)]}
  (let [node-id (to-synth-id node)]
    (apply snd "/n_setn" node-id ctl-start (count ctl-vals) ctl-vals)))

(defn- bussify
  "Convert busses in a col to bus ids."
  [col]
  (map #(if (bus? %)
          (bus-id %)
          %)
       col))

(defn node-map-controls*
  "Connect a node's controls to a control bus."
  [node & names-busses]
  {:pre [(server-connected?)]}
  (let [node-id (to-synth-id node)
        names-busses (bussify (stringify names-busses))]
    (apply snd "/n_map" node-id names-busses)))

(defn node-map-n-controls*
  "Connect N controls of a node to a set of sequential control busses,
  starting at the given control name."
  [node start-control start-bus n]
  {:pre [(server-connected?)]}
  (assert (bus? start-bus) "Invalid start-bus")
  (let [node-id (to-synth-id node)]
    (snd "/n_mapn" node-id (first (stringify [start-control])) (bus-id start-bus) n)))

(defn- group-post-tree*
  "Posts a representation of this group's node subtree, i.e. all the groups and
  synths contained within it, optionally including the current control values
  for synths."
  [id & [with-args?]]
  {:pre [(server-connected?)]}
  (snd "/g_dumpTree" id with-args?))

(defn- group-prepend-node*
  "Add a synth node to the end of a group list."
  [group node]
  (let [group-id (to-synth-id group)
        node-id (to-synth-id node)]
    (snd "/g_head" group-id node-id)))

(defn- group-append-node*
  "Add a synth node to the end of a group list."
  [group node]
  (let [group-id (to-synth-id group)
        node-id (to-synth-id node)]
  (snd "/g_tail" group-id node-id)))

(defn- group-clear*
  "Free all child synth nodes in a group."
  [group]
  (snd "/g_freeAll" (to-synth-id group))
  :clear)

(defn- group-deep-clear*
  "Free all child synth nodes in and below this group in other child groups."
  [group]
  (snd "/g_deepFree" (to-synth-id group))
  :clear)

(extend java.lang.Long
  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control        node-control*
   :node-control-range  node-control-range*
   :node-map-controls   node-map-controls*
   :node-map-n-controls node-map-n-controls*})

(extend SynthNode
  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control        node-control*
   :node-control-range  node-control-range*
   :node-map-controls   node-map-controls*
   :node-map-n-controls node-map-n-controls*})

(extend SynthGroup
  IControllableNode
  {:node-control        node-control*
   :node-control-range  node-control-range*
   :node-map-controls   node-map-controls*
   :node-map-n-controls node-map-n-controls*})

(defn ctl [node & args]
  (apply node-control node args))

(defprotocol IKillable
  (kill* [this] "Kill a synth element (node, or group, or ...)."))

(defn kill
  "Free one or more synth nodes.
  Functions that create instance of synth definitions, such as hit, return
  a handle for the synth node that was created.
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
  (doseq [node (flatten nodes)]
    (kill* node)))

(extend SynthNode
  IKillable
  {:kill* node-free*})

(extend java.lang.Number
  IKillable
  {:kill* node-free*})

;/g_queryTree				get a representation of this group's node subtree.
;	[
;		int - group ID
;		int - flag: if not 0 the current control (arg) values for synths will be included
;	] * N
;
; Request a representation of this group's node subtree, i.e. all the groups and
; synths contained within it. Replies to the sender with a /g_queryTree.reply
; message listing all of the nodes contained within the group in the following
; format:
;
;	int - flag: if synth control values are included 1, else 0
;	int - node ID of the requested group
;	int - number of child nodes contained within the requested group
;	then for each node in the subtree:
;	[
;		int - node ID
;		int - number of child nodes contained within this node. If -1this is a synth, if >=0 it's a group
;		then, if this node is a synth:
;		symbol - the SynthDef name for this node.
;		then, if flag (see above) is true:
;		int - numControls for this synth (M)
;		[
;			symbol or int: control name or index
;			float or symbol: value or control bus mapping symbol (e.g. 'c1')
;		] * M
;	] * the number of nodes in the subtree

(def ^{:dynamic true} *node-tree-data* nil)

(defn- parse-synth-tree
  [id ctls?]
  (let [sname (first *node-tree-data*)]
    (if ctls?
      (let [n-ctls (second *node-tree-data*)
            [ctl-data new-data] (split-at (* 2 n-ctls) (nnext *node-tree-data*))
            ctls (apply hash-map ctl-data)]
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
      (neg? n-children) (parse-synth-tree id ctls?) ; synth
      (= 0 n-children) {:type :group :id id :children nil}
      (pos? n-children)
      {:type :group :id id
       :children (doall (map (fn [i] (parse-node-tree-helper ctls?)) (range n-children)))})))

(defn- parse-node-tree
  [data]
  (let [ctls? (= 1 (first data))]
    (binding [*node-tree-data* (next data)]
      (parse-node-tree-helper ctls?))))

(defn- group-node-tree*
  "Returns a data structure representing the current arrangement of groups and
  synthesizer instances residing on the audio server."
  ([] (group-node-tree* 0))
  ([id & [ctls?]]
   (let [ctls? (if (or (= 1 ctls?) (= true ctls?)) 1 0)]
     (let [reply-p (recv "/g_queryTree.reply")
           _ (snd "/g_queryTree" id ctls?)
          tree (:args (deref! reply-p))]
       (with-meta (parse-node-tree tree)
         {:type ::node-tree})))))

(defprotocol ISynthGroup
  (group-prepend-node [group node])
  (group-append-node  [group node])
  (group-clear        [group])
  (group-deep-clear   [group])
  (group-post-tree    [group & [with-args?]])
  (group-node-tree    [group]))

(extend SynthGroup
  ISynthGroup
  {:group-prepend-node group-prepend-node*
   :group-append-node  group-append-node*
   :group-clear        group-clear*
   :group-deep-clear   group-deep-clear*
   :group-post-tree    group-post-tree*
   :group-node-tree    group-node-tree*})

(extend java.lang.Long
  ISynthGroup
  {:group-prepend-node group-prepend-node*
   :group-append-node  group-append-node*
   :group-clear        group-clear*
   :group-deep-clear   group-deep-clear*
   :group-post-tree    group-post-tree*
   :group-node-tree    group-node-tree*})

(defn node-tree
  "Returns a data representation of the synth node tree starting at the root group."
  []
  (group-node-tree 0))

(on-deps :core-groups-created ::create-synth-group #(dosync
                                                     (log/debug (str "Creating synth group at head of group with id: " (root-group)))
                                                     (ref-set synth-group* (group :head (root-group)))))

(on-sync-event :reset
  (fn []
    (clear-msg-queue)
    (group-clear @synth-group*)) ; clear the synth group
  ::reset-base)

(defn- clear-msg-queue-and-groups
  "Clear message queue and groups. Catches exceptions in case the server
  has died. Meant for use in a :shutdown callback"
  []
  (try
    (clear-msg-queue)
    (group-clear 0)
    (catch Exception e
      (log/error "Can't clear message queue and groups - server might have died."))))

(defn- setup-core-groups
  []
  (let [input-group   (with-server-sync #(group 0 0))
        root-group    (with-server-sync #(group 3 input-group))
        mixer-group   (with-server-sync #(group 3 root-group))
        monitor-group (with-server-sync #(group 3 mixer-group))]
    (dosync
      (alter core-groups* assoc
             :input   input-group
             :root    root-group
             :mixer   mixer-group
             :monitor monitor-group))
    (satisfy-deps :core-groups-created)))

(on-deps :server-connected ::setup-core-groups setup-core-groups)
(on-sync-event :shutdown ::reset-core-groups #(dosync
                                               (ref-set core-groups* {})))

(on-deps [:server-connected :core-groups-created] ::signal-server-ready #(satisfy-deps :server-ready))
(on-sync-event :shutdown
               clear-msg-queue-and-groups
               ::free-all-nodes)
