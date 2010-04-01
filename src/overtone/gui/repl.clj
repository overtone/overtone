(ns overtone.gui.repl
  (:gen-class)

  (:import                  
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (clojure.lang IDeref))
 
  (:require [clojure.main :as r])
    
  (:use clojure.contrib.seq-utils)
  (:use [clojure.stacktrace :only (e)])
    
  (:use overtone.core.event)

  (:use clj-scenegraph.core))

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

(comment
  (def temp (ref ""))                   
  (capture-out-write #(dosync (ref-set temp (str "from callback: " %))) (println "capture me please"))
  (println @temp))

(defn start-repl-thread []
  (future (try
           (capture-out-write
            #(event ::repl-print :text %)
            (let [read-q (LinkedBlockingQueue.)
                  exit (ref false)]
              
              (on ::repl-write #(.put read-q (PushbackReader. (StringReader. (:text %)))))
              (on ::repl-exit #((dosync (ref-set exit true))
                                (Thread/sleep 300)
                                (event ::repl-write :text "(println \"exit hack\")")))
              
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

(comment

  (start-repl-thread)

  (on ::repl-exception #(println (str "exception: " (:text %))))
  (on ::repl-prompt #(println (str "prompt: " (:text %))))
  (on ::repl-print #(println (str "print: " (:text %))))

  (on ::repl-exited #(println "repl exited"))

  (on ::repl-ready #(println "repl ready"))
  (event ::repl-exit)

  (event ::repl-write :text "(println \"hello world\")"))

(import '(com.sun.scenario.scenegraph SGComponent))

(defn sg-component
  ([] (SGComponent.))
  ([comp]
     (doto (sg-component)
       (.setComponent comp)))
  ([comp w h]
     (doto (sg-component comp)
       (.setSize w h))))

(def #^{:doc "balanced pairs"}
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
 

(defn repl []
  (let [group (sg-group)
        history (javax.swing.JEditorPane.)
        input (javax.swing.JEditorPane.)]

    (doto history
      (.setEditable false))
    
    (doto group
      (add! (sg-component history 400 100))
      (add! (translate 0 105 (sg-component input 400 25))))

    (doto (.getKeymap input)
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_ENTER) 0)
       (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (let [text (.getText input)]
                                                                        (if (and (not (empty? text))
                                                                                 (balanced? text))
                                                                          (do (event ::repl-write :text text)
                                                                              (.setText input "")
                                                                              (.requestFocus input))))))))

    (on ::repl-print #( (let [ doc (.getDocument history)
                              length (.getLength doc)]
                          (.insertString doc length (:text %) nil))))
    
    group))

(comment
  (start-repl-thread)
  (def panel (sg-panel 640 480))
  (sg-window panel)
  (set-scene! panel (translate 30 30 (repl))))