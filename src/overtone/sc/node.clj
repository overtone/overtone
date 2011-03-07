(ns overtone.sc.node
  (:require
    [overtone.log :as log])
  (:use
    [overtone event util deps]
    [overtone.sc core allocator]))

;; ## Node and Group Management

;; Synths, Busses, Controls and Groups are all Nodes.  Groups are linked lists
;; and group zero is the root of the graph.  Nodes can be added to a group in
;; one of these 5 positions relative to either the full list, or a specified node.

(def POSITION
  {:head         0
   :tail         1
   :before-node  2
   :after-node   3
   :replace-node 4})

;; ### Node
;;
;; A Node is an addressable node in a tree of nodes run by the synth engine.
;; There are two types, Synths and Groups. The tree defines the order of
;; execution of all Synths. All nodes have an integer ID.

;; Sending a synth-id of -1 lets the server choose an ID
(defn node
  "Instantiate a synth node on the audio server.  Takes the synth name and a set of
  argument name/value pairs.  Optionally use :target <node/group-id> and :position <pos>
  to specify where the node should be located.  The position can be one of :head, :tail
  :before-node, :after-node, or :replace-node.

  (node \"foo\")
  (node \"foo\" :pitch 60)
  (node \"foo\" :pitch 60 :target 0)
  (node \"foo\" :pitch 60 :target 2 :position :tail)
  "
  [synth-name & args]
  (if (not (connected?))
    (throw (Exception. "Not connected to synthesis engine.  Please boot or connect.")))
  (let [id (alloc-id :node)
        argmap (apply hash-map args)
        position (or ((get argmap :position :tail) POSITION) 1)
        target (get argmap :target 0)
        args (flatten (seq (-> argmap (dissoc :position) (dissoc :target))))
        args (stringify (floatify args))]
    ;(println "node " synth-name id position target args)
    (apply snd "/s_new" synth-name id position target args)
    id))

;; ### Synth node callbacks
;;
;; The synth server sends an n_go event when a synth node is created and an
;; n_end event when a synth node is destroyed.

(defn node-free
  "Remove a synth node."
  [& node-ids]
  {:pre [(connected?)]}
  (apply snd "/n_free" node-ids)
  (doseq [id node-ids] (free-id :node id)))

(defn- node-destroyed
  "Frees up a synth node to keep in sync with the server."
  [id]
  (log/debug (format "node-destroyed: %d" id))
  (free-id :node id))

(defn- node-created
  "Called when a node is created on the synth."
  [id]
  (log/debug (format "node-created: %d" id)))

; Setup the feedback handlers with the audio server.
(on-event "/n_end" ::node-destroyer #(node-destroyed (first (:args %))))
(on-event "/n_go" ::node-creator #(node-created (first (:args %))))

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

(defn group
  "Create a new synth group as a child of the target group."
  [position target-id]
  {:pre [(connected?)]}
  (let [id (alloc-id :node)
        pos (if (keyword? position) (get POSITION position) position)
        pos (or pos 1)]
    (snd "/g_new" id pos target-id)
    id))

(defn group-free
  "Free synth groups, releasing their resources."
  [& group-ids]
  {:pre [(connected?)]}
  (apply node-free group-ids))

(defn node-run
  "Start a stopped synth node."
  [node-id]
  {:pre [(connected?)]}
  (snd "/n_run" node-id 1))

(defn node-stop
  "Stop a running synth node."
  {:pre [(connected?)]}
  [node-id]
  (snd "/n_run" node-id 0))

(defn node-place
  "Place a node :before or :after another node."
  [node-id position target-id]
  {:pre [(connected?)]}
  (cond
    (= :before position) (snd "/n_before" node-id target-id)
    (= :after  position) (snd "/n_after" node-id target-id)))

(defn node-control
  "Set control values for a node."
  [node-id & name-values]
  {:pre [(connected?)]}
  (apply snd "/n_set" node-id (floatify (stringify name-values)))
  node-id)

; This can be extended to support setting multiple ranges at once if necessary...
(defn node-control-range
  "Set a range of controls all at once, or if node-id is a group control
  all nodes in the group."
  [node-id ctl-start & ctl-vals]
  {:pre [(connected?)]}
  (apply snd "/n_setn" node-id ctl-start (count ctl-vals) ctl-vals))

(defn node-map-controls
  "Connect a node's controls to a control bus."
  [node-id & names-busses]
  {:pre [(connected?)]}
  (apply snd "/n_map" node-id names-busses))

(defn post-tree
  "Posts a representation of this group's node subtree, i.e. all the groups and
  synths contained within it, optionally including the current control values
  for synths."
  [id & [with-args?]]
  {:pre [(connected?)]}
  (snd "/g_dumpTree" id with-args?))

(defn prepend-node
  "Add a synth node to the end of a group list."
  [g n]
  (snd "/g_head" g n))

(defn append-node
  "Add a synth node to the end of a group list."
  [g n]
  (snd "/g_tail" g n))

(defn group-clear
  "Free all child synth nodes in a group."
  [group-id]
  (snd "/g_freeAll" group-id))

(defn- synth-kind
  "Resolve synth kind depending on type of arguments. Intended for use as a multimethod dispatch fn"
  [& args]
  (cond
   (number? (first args)) :number
   (associative? (first args)) (:type (first args))
   :else (type (first args))))

(defmulti ctl
  "Modify synth parameters for a synth node or group of nodes."
  synth-kind)

(defmethod ctl :number
  [synth-id & ctls]
  (apply node-control synth-id ctls))

(defmulti kill
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
  synth-kind)

(defmethod kill :number
  [& ids]
  (apply node-free (flatten ids))
  :killed)

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

(def *node-tree-data* nil)

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

(defn parse-node-tree
  [data]
  (let [ctls? (= 1 (first data))]
    (binding [*node-tree-data* (next data)]
      (parse-node-tree-helper ctls?))))

(defn node-tree
  "Returns a data structure representing the current arrangement of groups and
  synthesizer instances residing on the audio server."
  ([] (node-tree 0))
  ([id & [ctls?]]
   (let [ctls? (if (or (= 1 ctls?) (= true ctls?)) 1 0)]
     (let [reply-p (recv "/g_queryTree.reply")
           _ (snd "/g_queryTree" id ctls?)
          tree (:args (await-promise! reply-p))]
       (with-meta (parse-node-tree tree)
         {:type ::node-tree})))))

(with-deps :connected #(dosync (ref-set synth-group* (group :head ROOT-GROUP))))

(on-sync-event :reset :reset-base
  (fn []
    (clear-msg-queue)
    (group-clear @synth-group*))) ; clear the synth group

