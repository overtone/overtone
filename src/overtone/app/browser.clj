(ns overtone.app.browser
  (:import
     (java.awt Toolkit EventQueue Color Font FontMetrics Dimension BorderLayout
               GridBagLayout GridBagConstraints Insets)
     (javax.swing.tree TreeModel TreeCellRenderer DefaultTreeModel)
     (javax.swing.table TableModel AbstractTableModel)
     (javax.swing.event TreeModelEvent)
     (javax.swing JFrame JPanel JLabel JTree JEditorPane JScrollPane JTextPane
                  JSplitPane JMenuBar JMenu JMenuItem SwingUtilities))
  (:use (overtone.core event time-utils sc)
        [clojure.contrib.seq-utils :only (indexed)])) ;;TODO replace this with clojure.core/keep-indexed or map-indexed


(def synth-tree* (ref {:group 0
                       :children [{:group 1
                                   :children [{:synth "fat-bass"}]}
                                  {:group 2
                                   :children [{:synth "sizzle-pad"}]}
                                  {:group 3
                                   :children [{:synth "kick"}]}
                                  {:group 4
                                   :children [{:group 1
                                               :children [{:synth "back-beater"}]}]}]}))

(defn- tree-cell-renderer []
  (proxy [TreeCellRenderer] []
    (getTreeCellRendererComponent [tree value selected? expanded?
                                    leaf? row has-focus?]
                                   (cond
                                     (contains? value :synth) (JLabel. (str (:synth value)))
                                     (contains? value :group) (JLabel. (str "Group: " (:group value)))))))

(defn- find-in [col item]
  (first (filter #(= %1 item) col)))

(defn- path-get [node path]
  (cond
    (= node (first path)) node
    (empty? path) nil
    :default (recur (find-in (second node) (first path)) (next path))))

(def listeners* (ref []))

(defn update-synth-tree []
  (let [tree (node-tree)]
    (when (not (= tree @synth-tree*))
      (dosync (ref-set synth-tree* tree))
      (doseq [l @listeners*]
        (.treeStructureChanged l (TreeModelEvent. @synth-tree* @synth-tree*))))))

(on :connected  #(periodic update-synth-tree 1000))

(defn- tree-model [root]
  (proxy [TreeModel] []
    (addTreeModelListener [l] (dosync (alter listeners* conj l)))
    (removeTreeModelListener [l] (dosync (ref-set listeners* (remove #(= l %) @listeners*))))
    (getRoot [] root)
    (isLeaf [node] (contains? node :synth))
    (getChild [parent index] (nth (:children parent) index))
    (getChildCount [parent] (count (:children parent)))
    (valueForPathChanged [path new-val])
    (getIndexOfChild [parent child]
                     (first (filter (fn [[idx v]] (= child v))
                                    (indexed (second parent)))))))

(defn browser-panel []
  (let [tree (JTree. (tree-model @synth-tree*))
        scroller (JScrollPane. tree)]
    (doto tree
      (.setRootVisible true)
      (.setCellRenderer (tree-cell-renderer)))
      ;(.putClientProperty "JTree.lineStyle", "None"))
    scroller))

