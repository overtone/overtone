; SyntaxDocument functions
;
; For find action
; (.getMatcher doc pattern start)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; File Operations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn file-new
  []
  (doto (:editor @editor*)
    (.moveCaretPosition 0)
    (.setText ""))
  (dosync
    (alter editor* assoc :current-path :new-file)))

(defn- file-open-dialog [parent & [path]]
  (let [chooser (if path
                  (JFileChooser. path)
                  (JFileChooser.))
        ret (.showOpenDialog chooser parent)]
    (case ret
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getAbsolutePath))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn file-open
  ([] (if-let [path (file-open-dialog (:editor @editor*) (:current-dir @editor*))]
        (file-open path)))
  ([path]
   (let [f (File. path)
         dir (.getParent (java.io.File. path))]
     (doto (:editor @editor*)
       (.moveCaretPosition 0)
       (.setText (slurp path)))
     (dosync
       (alter editor* assoc
              :current-path path
              :current-dir  dir)
       (alter config* assoc
              :last-open-file path)))))

(defn- file-save-dialog [parent & [path]]
  (let [chooser (if path
                  (JFileChooser. path)
                  (JFileChooser.))
        ret (.showSaveDialog chooser parent)]
    (case ret
      JFileChooser/APPROVE_OPTION (-> chooser (.getSelectedFile) (.getAbsolutePath))
      JFileChooser/CANCEL_OPTION nil
      JFileChooser/ERROR_OPTION  nil)))

(defn file-save-as
  ([] (if-let [path (file-save-dialog (:editor @editor*))]
        (file-save-as path)))
  ([path] (let [f (java.io.File. path)
                dir (.getParent (java.io.File. "/home/rosejn/studio/samples/kit/boom.wav"))]
            (if (or (not (.exists f))
                    (and (.exists f) (confirm "File Save As"
                                              (str "Are you sure you want to replace this file? "
                                                   (.getCanonicalPath f)))))
              (do
                (spit path (.getText (:editor @editor*)))
                (dosync (alter editor* assoc
                               :current-path path
                               :current-dir  dir)))))))

(defn file-save []
  (if (= :new-file (:current-path @editor*))
    (file-save-as)
    (file-save-as (:current-path @editor*))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Editor view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- font-mod [mod-fn]
  (let [editor (:editor @editor*)
        cur (.getFont editor)
        f (Font. (.getName cur) (.getStyle cur) (mod-fn (.getSize cur)))]
    (in-swing (.setFont editor f))))

(defn font-grow [] (font-mod inc))
(defn font-shrink [] (font-mod dec))

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

