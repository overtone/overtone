(ns overtone.sc.dyn-vars)

(defonce ^{:dynamic true :private true} *inactive-node-modification-error* :exception)
(defonce ^{:dynamic true :private true} *inactive-buffer-modification-error* :exception)
(defonce ^{:dynamic true :private true} *block-node-until-ready?* true)

(defn inactive-node-modification-error
  "Returns the current value for the dynamic var
  *inactive-node-modification-error*"
  []
  *inactive-node-modification-error*)

(defn inactive-buffer-modification-error
  "Returns the current value for the dynamic var
  *inactive-buffer-modification-error*"
  []
  *inactive-buffer-modification-error*)

(defn block-node-until-ready?
  "Returns the current value for the dynamic var
  *block-node-until-ready?*"
  []
  *block-node-until-ready?*)

(defmacro with-inactive-node-modification-error
  "Specify the inactive node modification error for the specified
   block. Options are: :exception, :warning and :silent"
  [error-type & body]
  `(binding [*inactive-node-modification-error* ~error-type]
     ~@body))

(defmacro with-inactive-buffer-modification-error
  "Specify the inactive buffer modification error for the specified
   block. Options are: :exception, :warning and :silent"
  [error-type & body]
  `(binding [*inactive-buffer-modification-error* ~error-type]
     ~@body))

(defmacro with-inactive-modification-error
  "Specify the inactive modification error for both nodes and buffers
   within the specified block. Options are: :exception, :warning
   and :silent"
  [error-type & body]
  `(binding [*inactive-buffer-modification-error* ~error-type
             *inactive-node-modification-error* ~error-type]
     ~@body))

(defmacro without-node-blocking
  "Stops the current thread from being blocked if you send a
   modification message to a server node that hasn't completed
   loading. This may result in messages sent within the body of this
   macro being ignored by the server."
  [& body]
  `(binding [*block-node-until-ready?* false]
     ~@body))
