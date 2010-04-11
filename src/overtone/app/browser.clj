(ns overtone.app.browser
  (:import 
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout 
               GridBagLayout GridBagConstraints Insets)
     (javax.swing.tree TreeModel DefaultTreeCellRenderer)
     (javax.swing.table TableModel AbstractTableModel)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane 
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities))
  (:use clojure.contrib.seq-utils))

(def test-synths* (ref {:root 
                   {"Instruments"
                    {"Bass" :bass-obj
                     "Pad" :pad-obj
                     "Sizzler" :sizzler-obj}
                    "Effects"
                    {"Echo" :echo-obj
                     "Compressor" :compressor-obj
                     "Reverb" :reverb-obj
                     "Chorus" :chors-obj}}}))

(defn- tree-cell-renderer []
  (proxy [DefaultTreeCellRenderer] []
    (getTreeCellRendererComponenet [tree value selected? expanded? 
                                    leaf? row has-focus?]
      ;(let [cell (proxy-super tree value selected? expanded? leaf? row has-focus?)]
      ;  (.setText cell (str (first value)))
      ;  cell))))
      ;(JLabel. (str (first value))))))
      (JLabel. (str "asdf")))))

(defn- find-in [col item]
  (first (filter #(= %1 item) col)))

(defn- path-get [node path]
  (cond
    (= node (first path)) node
    (empty? path) nil
    :default (recur (find-in (second node) (first path)) (next path))))

(defn- tree-model [root*]
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

(defn browser-panel []
  (let [tree (JTree. (tree-model test-synths*))
        scroller (JScrollPane. tree)]
    (doto tree
      (.setCellRenderer (tree-cell-renderer))
      (.setRootVisible false)
      (.putClientProperty "JTree.lineStyle", "None"))
    scroller))

