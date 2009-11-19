(ns overtone.app.main
  (:import 
     (java.awt Toolkit EventQueue Font FontMetrics Dimension BorderLayout)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities) 
     (javax.swing.tree TreeModel DefaultTreeCellRenderer)
     (javax.swing.table TableModel AbstractTableModel)
     (org.enclojure.repl IReplWindow IReplWindowFactory IRepl)
     (com.raelity.jvi ViManager ColonCommands)
     (com.raelity.jvi.swing DefaultViFactory StatusDisplay TextView))
  (:require [clojure.inspector :as inspector])
  (:use clojure.contrib.seq-utils
     org.enclojure.ide.repl.factory))


(def APP-NAME "Overtone")
(def DEFAULT-FONT "Sans")
(def DEFAULT-FONT-SIZE 12)

(def TAB-STOP 4)

(defn screen-dim []
  (.getScreenSize (Toolkit/getDefaultToolkit)))

(defn screen-size []
  (let [dim (screen-dim)]
    [(.width dim) (.height dim)]))

;TODO: It undecorates, but it doesn't seem to change the size of the frame...
(defn fullscreen-frame [f]
    (.setExtendedState f JFrame/MAXIMIZED_BOTH)
    (.setUndecorated f true))

(defn app-menu-bar []
  (let [bar (JMenuBar.)
        file-menu (JMenu. "File")
        quit-item (JMenuItem. "Quit")
        help-menu (JMenu. "Help")
        about-item (JMenuItem. "About")]
    (doto file-menu
      (.add quit-item))
    (doto help-menu
      (.add about-item))
    (doto bar
      (.add file-menu)
      (.add help-menu))))

(def synths* (ref {:root 
                   {"Instruments"
                    {"Bass" :bass-obj
                     "Pad" :pad-obj
                     "Sizzler" :sizzler-obj}
                    "Effects"
                    {"Echo" :echo-obj
                     "Compressor" :compressor-obj
                     "Reverb" :reverb-obj
                     "Chorus" :chors-obj}}}))

(defn tree-cell-renderer []
  (proxy [DefaultTreeCellRenderer] []
    (getTreeCellRendererComponenet [tree value selected? expanded? 
                                    leaf? row has-focus?]
      ;(let [cell (proxy-super tree value selected? expanded? leaf? row has-focus?)]
      ;  (.setText cell (str (first value)))
      ;  cell))))
      ;(JLabel. (str (first value))))))
      (JLabel. (str "asdf")))))
                                   

(defn find-in [col item]
  (first (filter #(= %1 item) col)))

(defn path-get [node path]
  (cond
    (= node (first path)) node
    (empty? path) nil
    :default (recur (find-in (second node) (first path)) (next path))))

(defn tree-model [root*]
  (proxy [TreeModel] []
    (addTreeModelListener [treeModelListener])
    (removeTreeModelListener [treeModelListener]) 

    (getRoot [] ["root" @root*])

    (isLeaf [node]
      (not (coll? (second node))))

    (getChild [parent index]
      (nth (seq (second parent)) index))

    (getChildCount [node]
      (count (second node)))

    (valueForPathChanged [path new-val]
      (let [path (seq (.getPath path))
            old-val (path-get @root* path)
            path-keys (map #(first %1) path)]
        (if (not (= old-val new-val))
          (dosync (ref-set (assoc-in @root* path-keys
                                     (concat  
                                       [new-val]))))))) 

    (getIndexOfChild [parent child]
      (first (filter (fn [[idx v]] (= child v)) 
                     (indexed (second parent)))))))

(defn synth-tree []
  (let [tree (JTree. (tree-model synths*))
        scroller (JScrollPane. tree)]
    (doto tree
      (.setCellRenderer (tree-cell-renderer))
      (.setRootVisible false)
      (.putClientProperty "JTree.lineStyle", "None"))
    scroller))

(defn editor []
  (let [editor-pane (JPanel.)
        editor (JTextPane.)
        scroller (JScrollPane. editor)
        status-pane (JPanel.)
        status (JLabel. "Overtone Live!")
        font (Font. DEFAULT-FONT 
                    Font/PLAIN
                    DEFAULT-FONT-SIZE)
        _ (.setFont editor font)
        font (.getFont editor)
        fm (.getFontMetrics editor font)
        width (* 81 (.charWidth fm \space))
        height (* 30 (.getHeight fm))]
    (doto editor-pane
      (.setLayout (BorderLayout.))
      (.add scroller BorderLayout/CENTER)
      (.add status-pane BorderLayout/SOUTH))))

(defn make-repl []
  (.getReplPanel (create-in-proc-repl)))

(defn main []
  (let [app-frame (JFrame. APP-NAME)
        app-pane (.getContentPane app-frame)
        synth-tree (synth-tree)
        editor-pane (editor)
        split-pane (JSplitPane. JSplitPane/HORIZONTAL_SPLIT
                                synth-tree editor-pane)]

    (doto app-pane
      (.setLayout (BorderLayout.))
      ;(.add  BorderLayout/WEST)
      (.add split-pane BorderLayout/CENTER)
      (.add (make-repl) BorderLayout/SOUTH))
    
    ;(.. scroll-pane (getViewport) (setPreferredSize (Dimension. width height)))
    (doto app-frame
      (.setJMenuBar (app-menu-bar))
      (.pack)
      (.setSize (screen-dim)) ; Fullscreen by default
      (.setVisible true))

    (.setDividerLocation split-pane 0.1)))

