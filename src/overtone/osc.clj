(ns overtone.osc
  (:use [overtone.osc.util]
        [overtone.osc.peer]
        [overtone.osc.dyn-vars]))

(defn osc-send-msg
  "Send OSC msg to peer.

  (osc-send-msg client {:path \"foo\" :type-tag \"i\" :args [42]})
  "
  [peer msg]
  (let [msg (with-meta msg {:type :osc-msg})]
    (if *osc-msg-bundle*
      (swap! *osc-msg-bundle* #(conj %1 msg))
      (peer-send-msg peer msg))))

(defn osc-reply-msg
  "Send OSC msg to peer as a reply.

  (osc-reply-msg client {:path \"foo\" :type-tag \"i\" :args [42]} prev-msg)
  "
  [peer msg msg-to-reply-to]
  (peer-reply-msg peer (with-meta msg {:type :osc-msg}) msg-to-reply-to))

(defn osc-listen
  "Attach a generic listener function that will be called with every incoming
  osc message. An optional key allows you to specifically refer to this listener
  at a later point in time. If no key is passed, the listener itself will also
  serve as the key.

  (osc-listen s (fn [msg] (println \"listener: \" msg)) :foo)."
  ([peer listener] (osc-listen peer listener listener))
  ([peer listener key]
   (dosync
    (alter (:listeners peer) assoc key listener))
   peer))

(defn osc-listeners
  "Return a seq of the keys of all registered listeners. This may be the
  listener fns themselves if no key was explicitly specified when the listener
  was registered."
  [peer]
  (keys @(:listeners peer)))

(defn osc-rm-listener
  "Remove the generic listener associated with the specific key
  (osc-rm-listener s :foo)"
  [peer key]
  (dosync
   (alter (:listeners peer) dissoc key))
  peer)

(defn osc-rm-all-listeners
  "Remove all generic listeners associated with server"
  [peer]
  (dosync
   (ref-set (:listeners peer) {}))
  peer)

(defn osc-handle
  "Add a handle fn (a method in OSC parlance) to the specified OSC path
  (container). This handle will be called when an incoming OSC message matches
  the supplied path. This may either be a direct match, or a pattern match if
  the incoming OSC message uses wild card chars in its path.  The path you
  specify may not contain any of the OSC reserved chars:
  # * , ? [ ] { } and whitespace

  Will override and remove any handler already associated with the supplied
  path. If the handler-fn returns :done it will automatically remove itself."
  [peer path handler]
  (peer-handle peer path  handler)
  peer)

(defn osc-handlers
  "Returns a seq of all the paths containing a handler for the server. If a
  path is specified, the result will be scoped within that subtree."
  ([peer] (osc-handlers peer "/"))
  ([peer path]
   (peer-handler-paths peer path)))

(defn osc-rm-handler
  "Remove the handler at the specified path.
  specific handler (if found)"
  [peer path]
  (peer-rm-handler peer path)
  peer)

(defn osc-rm-all-handlers
  "Remove all registered handlers for the supplied path (defaulting to /)
  This not only removes the handler associated with the specified path
  but also all handlers further down in the path tree. i.e. if handlers
  have been registered for both /foo/bar and /foo/bar/baz and
  osc-rm-all-handlers is called with /foo/bar, then the handlers associated
  with both /foo/bar and /foo/bar/baz will be removed."
  ([peer] (osc-rm-all-handlers peer "/"))
  ([peer path]
   (peer-rm-all-handlers peer path)
   peer))

(defn osc-recv
  "Register a one-shot handler which will remove itself once called. If a
  timeout is specified, it will return nil if a message matching the path
  is not received within timeout milliseconds. Otherwise, it will block
  the current thread until a message has been received.

  Will override and remove any handler already associated with the supplied
  path."
  [peer path handler & [timeout]]
  (peer-recv peer path handler timeout))

(defn osc-reply
  "Similar to osc-send except ignores the peer's target address and instead
  sends the OSC message to the sender of msg-to-reply-to. It is not currently
  possible to implicitly build OSC bundles as a reply to an OSC msg."
  [peer msg-to-reply-to path & args]
  (osc-reply-msg peer (apply mk-osc-msg path (osc-type-tag args) args) msg-to-reply-to))

(defn osc-send
  "Creates an OSC message and either sends it to the server immediately
  or if a bundle is currently being formed it adds it to the list of messages."
  [client path & args]
  (osc-send-msg client (apply mk-osc-msg path (osc-type-tag args) args)))

(defn osc-msg
  "Returns a map representing an OSC message with the specified path and args."
  [path & args]
  (apply mk-osc-msg path (osc-type-tag args) args))

(defn osc-bundle
  "Returns an OSC bundle, which is a timestamped set of OSC messages and/or bundles."
  [timestamp & items]
  (mk-osc-bundle timestamp items))

(defn osc-send-bundle
  "Send OSC bundle to client."
  [client bundle]
  (peer-send-bundle client bundle))

(defmacro in-osc-bundle
  "Runs body and intercepts any inner calls to osc-send-msg and instead
  of sending the OSC message, aggregates them and wraps them in an OSC
  bundle. When the body has finished, the bundle is then sent with the
  associated timestamp to the client. Handles nested calls to
  in-osc-bundle - resulting in a nested set of bundles."
  [client timestamp & body]
  `(let [[bundle# body-res#] (binding [*osc-msg-bundle* (atom [])]
                               (let [res# (do ~@body)]
                                 [(mk-osc-bundle ~timestamp @*osc-msg-bundle*) res#]))]
     (if *osc-msg-bundle*
       (swap! *osc-msg-bundle* conj bundle#)
       (osc-send-bundle ~client bundle#))
     body-res#))

(defmacro in-unested-osc-bundle
  "Runs body and intercepts any inner calls to osc-send-msg and instead
  of sending the OSC message, aggregates them and wraps them in an OSC
  bundle. When the body has finished, the bundle is then sent with the
  associated timestamp to the client.

  Does not nest OSC bundles, it sends all completed OSC bundles
  immediately."
  [client timestamp & body]
  `(let [[bundle# body-res#] (binding [*osc-msg-bundle* (atom [])]
                               (let [res# (do ~@body)]
                                 [(mk-osc-bundle ~timestamp @*osc-msg-bundle*) res#]))]
     (osc-send-bundle ~client bundle#)
     body-res#))

(defmacro without-osc-bundle
  "Runs body and ensures that any inner calls to osc-send-msg are sent
  immediately. This is useful in the rare case you need to bypass the
  bundling of OSC messages when code may be wrapped within
  in-osc-bundle."
  [& body]
  `(binding [*osc-msg-bundle* nil]
     ~@body))

(defn osc-client
  "Returns an OSC client ready to communicate with a host on a given port via UDP"
  ([host port] (osc-client host port true))
  ([host port send-nested-osc-bundles?]
   (client-peer host port send-nested-osc-bundles?)))

(defn osc-peer
  "Returns a generic OSC peer. You will need to configure it to make
  it act either as a server or client."
  ([] (peer))
  ([listen? send-nested-osc-bundles?] (peer listen? send-nested-osc-bundles?)))

(defn osc-target
  "Update the target address of an OSC client so future calls to osc-send
  will go to a new destination. Automatically updates zeroconf if necessary."
  [client host port]
  (update-peer-target client host port)
  client)

(defn osc-server
  "Returns a live OSC server ready to register handler functions. By default
  this also registers the server with zeroconf. The name used to register
  can be passed as an optional param. If the zero-conf-name is set to nil
  zeroconf wont' be used."
  ([port] (osc-server port "osc-clj"))
  ([port zero-conf-name] (osc-server port zero-conf-name true))
  ([port zero-conf-name send-nested-osc-bundles?]
   (server-peer port zero-conf-name send-nested-osc-bundles?)))

(defn osc-close
  "Close an osc-peer, works for both clients and servers. If peer has been
  registered with zeroconf, it will automatically remove it."
  [peer & wait]
  (apply close-peer peer wait)
  peer)

(defn osc-debug
  [& [on-off]]
  (let [on-off (if (= on-off false) false true)]
    (dosync (ref-set osc-debug* on-off))))

(defn zero-conf-on
  "Turn zeroconf on. Will automatically register all running servers with their
  specified service names (defaulting to \"osc-clj\" if none was specified).
  Asynchronous."
  []
  (turn-zero-conf-on))

(defn zero-conf-off
  "Turn zeroconf off. Will unregister all registered services and close zeroconf
  down. Asynchronous."
  []
  (turn-zero-conf-off))

(defn zero-conf?
  "Returns true if zeroconf is running, false otherwise."
  []
  (zero-conf-running?))

(defn osc-now
  "Return the current time in milliseconds"
  []
  (System/currentTimeMillis))
