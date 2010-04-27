; SyntaxDocument functions
;
; For find action
; (.getMatcher doc pattern start)

(println "actions ns: " *ns*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; File Operations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn file-open [path]
  (let [f (java.io.File. path)
        dir (.getParent (java.io.File. path))]
  (.setText (:editor @editor*) (slurp path))
  (dosync (alter editor* assoc 
                 :current-path path
                 :current-dir  dir))))

(defn file-save []
  (spit (:current-path @editor*) (.getText (:editor @editor*))))

(defn verify-msg [arg] arg)

(defn file-save-as [path]
  (let [f (java.io.File. path)
        dir (.getParent (java.io.File. "/home/rosejn/studio/samples/kit/boom.wav"))]
    (if (or (not (.exists f))
            (and (.exists f) (verify-msg (.getCanonicalPath f))))
      (do
        (spit path (.getText (:editor @editor*)))
        (dosync (alter editor* assoc 
                       :current-path path
                       :current-dir  dir))))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Text Selection
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn selected-text
  "Returns the currently selected text."
  []
  (.getSelectedText (:editor @editor*)))

(defn select-all
  "Selects all of the text."
  []
  (.selectAll (:editor @editor*)))

(defn select-start
  "Get the starting point of the current selection."
  []
  (.getSelectionStart (:editor @editor*)))

(defn set-select-start
  "Set the starting point of the current selection."
  [start]
  (.setSelectionStart (:editor @editor*) start))

(defn select-end
  "Get the end point of the current selection."
  []
  (.getSelectionEnd (:editor @editor*)))

(defn set-select-end
  "Set the end point of the current selection."
  [end]
  (.setSelectionEnd (:editor @editor*) end))

(defn select-range
  "Set the current selection range."
  ([start end]
   (.select (:editor @editor*) start end)))

(defn selected-text-color
  "Get the selection color, a java.awt.Color."
  []
  (.getSelectedTextColor (:editor @editor*)))

(defn set-selected-text-color
  "Set the selection color with a java.awt.Color."
  [c]
  (.setSelectedTextColor (:editor @editor*) c))

(defn highlight-color
  "Get the selection color, a java.awt.Color."
  []
  (.getSelectionColor (:editor @editor*)))

(defn set-highlight-color
  "Set the selection color with a java.awt.Color."
  [c]
  (.setSelectionTextColor (:editor @editor*) c))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Selection operations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn text-cut
  "cut"
  []
  (.cut (:editor @editor*)))

(defn text-copy
  "copy"
  []
  (.copy (:editor @editor*)))

(defn text-paste
  "paste"
  []
  (.paste (:editor @editor*)))

(defn text-replace
  "Replace the current selection text."
  [s]
  (.replaceSelection (:editor @editor*) s))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The caret
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn caret-color
  "Get the caret's java.awt.Color."
  []
  (.getCaretColor (:editor @editor*)))

(defn set-caret-color
  "Set the caret's java.awt.Color."
  [c]
  (.setCaretColor (:editor @editor*) c))

(defn caret-position
  "Get the caret's position."
  []
  (.getCaretPosition (:editor @editor*)))

(defn set-caret-position
  "Set the caret's position."
  [index]
  (.setCaretPosition (:editor @editor*) index))

(defn move-caret
  "Shift the caret's position."
  [amount]
  (.moveCaretPosition (:editor @editor*) amount))

(defn current-line
  "The line of text containing the caret."
  []
  (.getLineAt (.getDocument (:editor @editor*)) (caret-position)))

(defn remove-current-line
  "Delete the current line of text."
  []
  (.removeLineAt (.getDocument (:editor @editor*)) (caret-position)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Search
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

