(ns overtone.gui.repl
  (:gen-class)

  (:import
   (java.util.concurrent TimeUnit TimeoutException)
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (clojure.lang IDeref))

  (:import
   (javax.swing JEditorPane JScrollPane  SwingUtilities))
  
  (:require [clojure.main :as r])
    
  (:use clojure.contrib.seq-utils)
  (:use [clojure.stacktrace :only (e)])
    
  (:use (overtone.core time-utils sc ugen synth synthdef envelope event)
        (overtone.music rhythm pitch tuning))

  (:use clj-scenegraph.core))

;; Non-graphical part

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
        (.get (future @p) 1000 TimeUnit/MILLISECONDS)
        (catch TimeoutException t 
          false))))

(comment
  (repl-thread-running?)
  (start-repl-thread))

(comment
  (start-repl-thread)

  (on ::repl-exception #(println (str "exception: " (:text %))))
  (on ::repl-prompt #(println (str "prompt: " (:text %))))
  (on ::repl-print #(println (str "print: " (:text %))))

  (on ::repl-exited #(println "repl exited"))

  (on ::repl-ready #(println "repl ready"))
  (event ::repl-exit)

  (event ::repl-write :text "(println \"hello world\")")) 

;; GUI part

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

(defmacro EDT
  "runs body on the Event-Dispatch-Thread (Swing)"
  [& body]
  `(SwingUtilities/invokeLater (fn [] ~@body)))


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
        input (javax.swing.JEditorPane.)
        scroll (JScrollPane. history)

        println-history #(EDT (let [ doc (.getDocument history)
                                    length (.getLength doc)]
                                (.insertString doc length % nil)))]

    (EDT (.setEditable input false)   
         (.setEditable history false)
         (.setVerticalScrollBarPolicy scroll (JScrollPane/VERTICAL_SCROLLBAR_ALWAYS))
         (.setHorizontalScrollBarPolicy scroll (JScrollPane/HORIZONTAL_SCROLLBAR_NEVER)))
    
    (doto group
      (add! (sg-component scroll 500 300))
      (add! (translate 0 305 (sg-component input 500 25))))

    (doto (.getKeymap input)
      (.addActionForKeyStroke
       (javax.swing.KeyStroke/getKeyStroke (java.awt.event.KeyEvent/VK_ENTER) 0)
       (proxy [javax.swing.AbstractAction] [] (actionPerformed [evt]  (let [text (.getText input)]
                                                                        (if (and (not (empty? text))
                                                                                 (balanced? text))
                                                                          (do (event ::repl-write :text text)
                                                                              (EDT
                                                                               (.setText input "")
                                                                               (.requestFocus input)))))))))

    (on ::repl-print #(println-history (:text %) ))
    
    (on ::repl-ready #((println-history "repl ready\r\n")
                       (event ::repl-write :text "(in-ns 'user)\r\n(use 'overtone.live)")
                       (EDT (.setEditable input true))))

    (on ::repl-exited #((println-history "repl exited\r\n")
                        (EDT (.setEditable input false))))
    
    (if (repl-thread-running?)
      (do (EDT (.setEditable input true))
          (println-history "repl thread allready running\r\n"))
      (start-repl-thread))
      
    group))

(defn repl-panel []
  (let [panel (sg-panel)]))