(ns
  ^{:doc "Swing helper functions"
     :author "Fabian Aussems & Jeff Rose"}
  overtone.gui.swing
  (:import (clojure.lang RT)
           (java.awt Toolkit GraphicsEnvironment)
           (java.awt.event ActionListener)
           (javax.swing.text TextAction JTextComponent)
           (javax.swing JFrame JPanel JSplitPane JLabel
                        JButton SwingUtilities BorderFactory
                        ImageIcon KeyStroke JOptionPane))
  (:use clojure.stacktrace
        (clojure.contrib swing-utils)
        (overtone.core util)))

(defmacro in-swing [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))

(defn resource-url [path]
  (.getResource (RT/baseLoader) path))

(defn icon [path]
  (ImageIcon. (resource-url path)))

(defn button
  "Creates a JButton with the associated text and handler or icon, tooltip,
  and handler.

  (button \"Save\" #(save-file ...))
  (button \"Save the current file\"
          \"path/to/my/icon/save.png\"
          #(save-file ...))
  "
  ([text handler]
   (doto (JButton. text)
     (add-action-listener (fn [event]
                            (run-handler handler event)))))
  ([tooltip icon-path handler]
   (doto (JButton. (icon icon-path))
     (.setToolTipText tooltip)
     (add-action-listener (fn [event]
                            (run-handler handler event))))))

(defn screen-dim []
  (.getScreenSize (Toolkit/getDefaultToolkit)))

(defn screen-size []
  (let [dim (screen-dim)]
    [(.width dim) (.height dim)]))

;TODO: Fix me
; It undecorates, but it doesn't seem to change the size of the frame...
(defn fullscreen-frame [f]
    (.setExtendedState f JFrame/MAXIMIZED_BOTH)
    (.setUndecorated f true))

; API not supported yet
;(Desktop/browse my-uri)

(defn on-action
  "Adds a handler to a component."
  [component handler]
  (let [listener (proxy [ActionListener] []
                   (actionPerformed [event] (run-handler handler event)))]
    (.addActionListener component listener)
    listener))

(def KEY-MODIFIERS {:none  0
                    :shift java.awt.event.InputEvent/SHIFT_MASK
                    :ctrl java.awt.event.InputEvent/CTRL_MASK
                    :meta java.awt.event.InputEvent/META_MASK
                    :alt java.awt.event.InputEvent/ALT_MASK})

(defn text-action [handler]
  (proxy [TextAction] ["EVAL"]
    (actionPerformed [e] (run-handler handler e))))

(defn key-stroke [k & mods]
  (if (empty? mods)
    (KeyStroke/getKeyStroke k)
    (KeyStroke/getKeyStroke k (apply + (map KEY-MODIFIERS mods)))))

(defn add-keystroke! [editorpane stroke mods performed-fn]
  (let [stroke-key (eval `(. java.awt.event.KeyEvent ~stroke))]
    (doto (.getKeymap editorpane)
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke stroke-key (if (vector? mods)
                                                        (apply + (map #(KEY-MODIFIERS %) mods))
                                                        (KEY-MODIFIERS mods)))
       (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt] (performed-fn)))))))

(defn set-default-keymap-action! [editorpane performed-fn]
  (.setDefaultAction (.getKeymap editorpane) (proxy [javax.swing.Action] []
                                               (isEnabled [] true)
                                               (getValue [k])
                                               (actionPerformed [e]
                                                                (performed-fn e)))))

(defn confirm [title msg]
  (= JOptionPane/OK_OPTION
     (JOptionPane/showConfirmDialog (JFrame.)
                                    msg
                                    title
                                    JOptionPane/OK_CANCEL_OPTION)))


(comment do
  (.removeBindings (.getKeymap edit))
  (set-default-keymap-action! edit (fn [e] (let [content (str (.getActionCommand e))
                                                 c (.charAt content 0)
                                                 i (int c)]

                                             (cond (= 8 i)     (event ::jline-command :id :backspace)
                                                   (= 127 i)   (event ::jline-command :id :delete-prev-char))

                                             (if (>= i 0x20)
                                               (if (not (= i 127))
                                                 (event ::jline-write :text (str c)))))))
  (add-keystroke! edit 'VK_DELETE :none #(event ::jline-command :id :delete-next-char))
  (add-keystroke! edit 'VK_LEFT :none #(event ::jline-command :id :prev-char))
  (add-keystroke! edit 'VK_HOME :none #(event ::jline-command :id :move-to-beg))
  (add-keystroke! edit 'VK_END :none #(event ::jline-command :id :move-to-end))
  (add-keystroke! edit 'VK_ENTER :none #(event ::jline-command :id :newline))
)

