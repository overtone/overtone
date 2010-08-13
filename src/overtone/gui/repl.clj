(ns
  ^{:doc "The Overtone repl"
     :author "Fabian Aussems & Jeff Rose"}
  overtone.gui.repl
  (:gen-class)
  (:import
   (java.util.concurrent TimeUnit TimeoutException)
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (clojure.lang IDeref)
   (java.awt Color BorderLayout Dimension Insets)
   (javax.swing JEditorPane JFrame JScrollPane JPanel SwingUtilities AbstractAction JLabel)
   (javax.swing.border LineBorder)
   (javax.swing.text TextAction JTextComponent))
  (:require [clojure.main :as r])
  (:use [clojure.stacktrace :only (e)]
        (overtone.core time-utils sc ugen synth synthdef envelope event)
        (overtone.gui swing sg)
        (overtone.music rhythm pitch tuning)))

(defn outputstream-with-cb [cb]
  (let [buffer (StringBuffer.)]
    (proxy [java.io.OutputStream IDeref] []
      (deref [] buffer)
      (flush []
             (when (< 0 (.length buffer))
               (let [sb (.toString buffer)]
                 (cb sb)
                 (.setLength buffer 0))))
      (close [] (.flush this))
      (write
       ([i] (.append buffer (char i)))
       ([buf off len]
          (doseq [i (take len (drop off buf))]
            (.append buffer (char i))))))))

(defmacro capture-out-write [cb & body]
  `(binding [`~*out* (PrintWriter. (OutputStreamWriter. (outputstream-with-cb ~cb)))]
     ~@body))

(defn start-repl-thread []
  (on ::repl-exit #((event ::repl-write :text "(println \"exit hack\")")))
  (future (try
           (capture-out-write
            #(event ::repl-print :text %)
            (let [read-q (LinkedBlockingQueue.)
                  exit (atom false)]

              (on ::repl-write #(.put read-q (PushbackReader. (StringReader. (:text %)))))
              (on ::repl-exit #((compare-and-set! exit false true)))

              (on ::repl-ping #((if (= @exit false) (event ::repl-pong))))

              (clojure.main/repl
               :init (fn [] (event ::repl-ready))
               :caught (fn [x]
                         (binding [*e x] (e))
                         (.flush *out*)
                         (println ""))
               :need-prompt (constantly true)
               :prompt #(do (event ::repl-prompt :text (str (ns-name *ns*))))
               :read (fn [a b]
                       (if @exit
                         b
                         (binding [*in* (.take read-q)]
                           (r/repl-read a b))))))

            (event ::repl-exited))
           (catch Exception e
             (event ::repl-exception :text (pr-str e))))))

(defn repl-thread-running? []
     (let [p (promise)]
       (on ::repl-pong #(deliver p true))
       (event ::repl-ping)
       (try
        (.get (future @p) 300 TimeUnit/MILLISECONDS)
        (catch TimeoutException t
          false))))

(def ^{:doc "balanced pairs"}
     pairs '((\( \))
             (\[ \])
             (\" \")
             (\{ \})))

(defn balanced?
  "are all the pairs balanced in this string?"
  [string]
  ((comp not some)
   false?
   (map
    (fn [pair] (-> pair set (filter string) count (mod 2) zero?))
    pairs)))

(def history* (ref (list)))
(def history-index* (ref -1))

(defn history-up-action [input]
  (on ::repl-write #(dosync
                      (alter history* conj (:text %))
                      (ref-set history-index* -1)))
  (proxy [javax.swing.AbstractAction] []
    (actionPerformed [evt]
      (let [txt (nth @history*
                       (dosync (ref-set history-index*
                         (min (dec (count @history*)) (inc @history-index*)))))]
      (in-swing
        (.setText input txt))))))

(defn history-down-action [input]
  (proxy [javax.swing.AbstractAction] []
    (actionPerformed [evt]
                     (in-swing
                       (.setText input
                                 (if (< @history-index* 1)
                                   (do
                                     (dosync (ref-set history-index* -1))
                                     "")
                                   (nth @history* (dosync (alter history-index* dec)))))))))

(defn repl-enter-action [input]
  (proxy [javax.swing.AbstractAction] []
    (actionPerformed [evt]  (let [text (.getText input)]
                              (if (and (not (empty? text))
                                       (balanced? text))
                                (do
                                  (event ::repl-write :text text)
                                  (event ::repl-print :text (str text "\n"))
                                  (in-swing
                                    (.setText input "")
                                    (.requestFocus input))))))))

(def repl-input* (ref nil))

(defn repl-panel []
  (let [panel (JPanel.)
        history (JEditorPane.)
        input (JEditorPane.)
        scroll (JScrollPane. history)
        println-history #(in-swing (let [ doc (.getDocument history)
                                         length (.getLength doc)]
                                     (.insertString doc length % nil)))
        layout (BorderLayout.)
        key-map (JTextComponent/addKeymap "repl" (.getKeymap input))]
    (doto scroll
      (.setVerticalScrollBarPolicy (JScrollPane/VERTICAL_SCROLLBAR_ALWAYS))
      (.setHorizontalScrollBarPolicy (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER)))

    (doto key-map
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_UP) 0)
        (history-up-action input))
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_DOWN) 0)
        (history-down-action input))
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_ENTER) 0)
        (repl-enter-action input)))

    (dosync (ref-set repl-input* input))
    (doto input
      (.setKeymap key-map)
      (.setEditable false)
      (.setBorder (LineBorder. java.awt.Color/BLACK))
      (.setMargin (Insets. 0 20 0 1))
      (.setPreferredSize (Dimension. 400 20))
      (.setMinimumSize (Dimension. 100 10)))

    (doto history
      (.setEditable false)
      (.setBorder (LineBorder. Color/BLACK))
      (.setMinimumSize (Dimension. 100 40))
      (.setPreferredSize (Dimension. 400 400))
      )

    (doto panel
      (.setLayout layout)
      (.add scroll BorderLayout/CENTER)
      (.add input BorderLayout/SOUTH))

    (on ::repl-print #(println-history (:text %) ))
    (on ::repl-ready #((println-history "repl ready\r\n")
                       (event ::repl-write :text "(in-ns 'user)\r\n(use 'overtone.live)")
                       (in-swing (.setEditable input true))))
    (on ::repl-exited #((println-history "repl exited\r\n")
                        (in-swing (.setEditable input false))))

    (if (repl-thread-running?)
      (do (in-swing (.setEditable input true))
          (println-history "repl thread allready running\r\n"))
      (start-repl-thread))
    (in-swing (.requestFocus input))
    panel))
