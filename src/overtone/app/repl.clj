(ns overtone.app.repl
  (:gen-class)

  (:import                  
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (clojure.lang IDeref))
 
  (:require [clojure.main :as r])
    
  (:use clojure.contrib.seq-utils)
  (:use [clojure.stacktrace :only (e)])
    
  (:use overtone.core.event))

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
  (capture-out-write #(dosync (ref-set temp (str "from callback: " %))) (println "hiiii"))
  (println @temp))



(defn start-repl-thread [] (future (try
                                 (capture-out-write #(event ::repl-print :text %)
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

  (event ::repl-ready #(println "repl ready"))
  (event ::repl-exit)

  (event ::repl-write :text "(println \"hello world\")"))

;(defn repl []
;  )