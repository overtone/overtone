(ns overtone.gui.toolbox
  (:use [seesaw.core])
  (:import javax.swing.JWindow
           java.awt.MouseInfo))

(defn tool
  "Returns a new tool (a tqoggle-button for now) for the given tool-spec."
  [& tool-spec]
  (let [tool-spec (if (= (count tool-spec) 1)
                    (first tool-spec)
                    tool-spec)
        tool-spec ((comp flatten seq) tool-spec)]

  (apply toggle tool-spec)))

(def ^:private pointer-spec
  {:id :pointer
   :text "Pointer"
   :selected? true})

(def ^:private hand-spec
  {:id :hand
   :text "Hand"})

(defn toolbox-bg
  [& tool-specs]
  (let [tool-specs (or (not-empty tool-specs)
                       [pointer-spec hand-spec])
        tools      (map tool tool-specs)]

    (button-group :buttons tools)))

(defn toolbox
  "Create a temporary popup window containing a set of selectable tools."
  ([]
     (toolbox (toolbox-bg)))
  ([tools-bg]
     (invoke-now
      (let [popup (JWindow.)
            tools (enumeration-seq (.getElements tools-bg))
            body (flow-panel :align :left :items tools)]

        (listen tools-bg :mouse-clicked (fn [e] (hide! popup)))

        (doto popup
          (.add body)
          (.pack))
        popup))))

(def ^:dynamic *toolbox* (toolbox))

;;TODO: move to seesaw? or overtone.gui.util?
(defn mouse-location [] (.getLocation (MouseInfo/getPointerInfo)))

(defn current-tool
  "Returns the id of the currently selected tool."
  ([]
     (current-tool *toolbox*))
  ([tbox]
     ((comp key first) (filter (comp true? val)
                               (value tbox)))))

(defn popup-toolbox
  "Show the toolbox until clicked."
  ([]
     (popup-toolbox *toolbox* :to (mouse-location)))
  ([tbox]
     (popup-toolbox tbox :to (mouse-location)))
  ([tbox & opts]
     (apply move! tbox opts)
     (show! tbox)))

(comment

  ;; global toolbox
  (popup-toolbox)
  (current-tool)

  ;;local toolbox
  (toolbox (toolbox-bg))

  ;; thread-local toolbox.
  (binding [*toolbox* (toolbox)]
    (popup-toolbox))
  )
