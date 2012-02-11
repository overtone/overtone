(ns overtone.gui.toolbox
  (:use [seesaw core
         [selection :only [Selection]]
         [make-widget :only [MakeWidget]]
         [to-widget :only [ToWidget]]
         [value :only [Value]]])
  (:import java.awt.MouseInfo))

(defrecord Toolbox [tools primary secondary])

(extend-type Toolbox

  ToWidget
  (to-widget* [tbox] tbox)

  Selection
  (get-selection [tbox]
    (list @(:primary tbox)))
  (set-selection [tbox [v]]
    (reset! (:primary tbox) v))

  MakeWidget
  (make-widget* [tbox]
    (invoke-now
     (flow-panel :align :left :items (:tools tbox))))

  Value
  (container?* [tbox] false)
  (value* [tbox]
    (let [{:keys [primary secondary]} tbox]
      {:primary @primary :secondary @secondary}))
  (value!* [tbox v]
    (let [{:keys [primary secondary]} v]
      (reset! (:primary tbox) primary)
      (reset! (:secondary tbox) secondary)
      (value tbox))))

(defn tool
  "Returns a new tool (a button for now) for the given tool-spec."
  [& tool-spec]
  (let [tool-spec (if (= (count tool-spec) 1)
                    (first tool-spec)
                    tool-spec)
        tool-spec ((comp flatten seq) tool-spec)]
    (apply button tool-spec)))

(def ^:private pointer-spec
  {:id :pointer
   :text "Pointer"
   :cursor :default
   :selected? true})

(def ^:private hand-spec
  {:id :hand
   :text "Hand"
   :cursor :hand})

(defn default-tools
  []
  (map tool [pointer-spec hand-spec]))

(defn- add-behavior
  ([tbox]
     (let [tools (:tools tbox)]
       (listen tools :mouse-clicked
               (fn [e]
                 (let [id (id-of e)]
                   (selection! tbox [(to-widget e)])
                   (return-from-dialog e (id-of e)))))
       tbox)))

(defn toolbox
  "Create a Toolbox instance with a set of 'clickable' tools and initial primary
   and secondary tool selections."
  ([]
     (toolbox (default-tools)))
  ([tools]
     (let [primary   (or (first (filter :selected? tools))
                         (first tools))
           secondary (or (second (filter :selected? tools))
                         primary)]

       (add-behavior
        (Toolbox. tools (atom primary) (atom secondary))))))

(def ^:dynamic *toolbox* (toolbox))

;;TODO: move to seesaw? or overtone.gui.util?
(defn mouse-location [] (.getLocation (MouseInfo/getPointerInfo)))

(defn current-tool
  "Returns the id of the currently selected tool."
  ([]
     (current-tool *toolbox*))
  ([tbox]
     (id-of (selection tbox))))

(defn popup-toolbox
  "Show the toolbox until clicked."
  ([]
     (popup-toolbox *toolbox* :to (mouse-location)))
  ([tbox]
     (popup-toolbox tbox :to (mouse-location)))
  ([tbox & opts]
     (let [popup (custom-dialog :modal? true :content tbox)]
       (apply move! popup opts)
       (-> popup
           pack!
           show!))))

(comment
  ;; global toolbox
  (popup-toolbox)
  (current-tool)

  ;; thread-local toolbox.
  (binding [*toolbox* (toolbox)]
    (popup-toolbox))
  )
