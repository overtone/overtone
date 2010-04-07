(ns overtone.gui.repl
  (:gen-class)

  (:import
   (java.util.concurrent TimeUnit TimeoutException)
   (java.io StringReader PushbackReader OutputStreamWriter PrintWriter)
   (java.util.concurrent LinkedBlockingQueue)
   (clojure.lang IDeref))

  (:import
   (jline ConsoleReader Terminal)
   (com.Ostermiller.util CircularByteBuffer)
   (java.io ByteArrayInputStream BufferedReader InputStreamReader DataOutputStream)
   (java.nio.charset Charset)
   )
  
  (:import
   (java.awt.BorderLayout)
   (javax.swing JEditorPane JScrollPane JPanel SwingUtilities)
   )
  
  (:require [clojure.main :as r])
    
  (:use clojure.contrib.seq-utils)
  (:use [clojure.stacktrace :only (e)])
    
  (:use (overtone.core time-utils sc ugen synth synthdef envelope event)
        (overtone.gui utils)
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


;; JLine part

(def jline-command-map {:unknown                    'UNKNOWN 
                        :move-to-beg                'MOVE_TO_BEG 
                        :move-to-end                'MOVE_TO_END 
                        :prev-char                  'PREV_CHAR 
                        :newline                    'NEWLINE 
                        :kill-line                  'KILL_LINE 
                        :clear-screen               'CLEAR_SCREEN 
                        :next-history               'NEXT_HISTORY 
                        :prev-history               'PREV_HISTORY 
                        :redisplay                  'REDISPLAY 
                        :kill-line-prev             'KILL_LINE_PREV 
                        :delete-prev-word           'DELETE_PREV_WORD 
                        :next-char                  'NEXT_CHAR 
                        :repeat-prev-char           'REPEAT_PREV_CHAR 
                        :search-prev                'SEARCH_PREV 
                        :repeat-next-char           'REPEAT_NEXT_CHAR 
                        :search-next                'SEARCH_NEXT 
                        :prev-space-word            'PREV_SPACE_WORD 
                        :to-end-word                'TO_END_WORD 
                        :repeat-search-prev         'REPEAT_SEARCH_PREV 
                        :paste-prev                 'PASTE_PREV 
                        :replace-mode               'REPLACE_MODE 
                        :substitute-line            'SUBSTITUTE_LINE 
                        :to-prev-char               'TO_PREV_CHAR 
                        :next-space-word            'NEXT_SPACE_WORD 
                        :delete-prev-char           'DELETE_PREV_CHAR 
                        :add                        'ADD 
                        :prev-word                  'PREV_WORD 
                        :change-meta                'CHANGE_META 
                        :delete-meta                'DELETE_META 
                        :end-word                   'END_WORD 
                        :insert                     'INSERT 
                        :repeat-search-next         'REPEAT_SEARCH_NEXT 
                        :paste-next                 'PASTE_NEXT 
                        :replace-char               'REPLACE_CHAR 
                        :substitute-char            'SUBSTITUTE_CHAR 
                        :to-next-char               'TO_NEXT_CHAR 
                        :undo                       'UNDO 
                        :next-word                  'NEXT_WORD 
                        :delete-next-char           'DELETE_NEXT_CHAR 
                        :change-case                'CHANGE_CASE 
                        :complete                   'COMPLETE 
                        :exit                       'EXIT 
                        :paste                      'PASTE 
                        :start-of-history           'START_OF_HISTORY 
                        :end-of-history             'END_OF_HISTORY 
                        :clear-line                 'CLEAR_LINE })


(def jline-command-map-ids (loop [i 4000
                                  m (keys jline-command-map)                        
                                  r {}]
                             (if (empty? m)
                               r
                               (let [key (first m)
                                     val (jline-command-map key)]
                                 (recur (+ i 1) (rest m)  (assoc r  key i ))))))

(def my-bindings (loop [m (keys jline-command-map)                        
                        r ""]
                        (if (empty? m)
                          r
                          (let [key (first m)
                                val (jline-command-map key)
                                i (jline-command-map-ids key)]
                            (recur (rest m) (str r "\n" i " : " val) )))))

(defn jline-init []
  (let [cbb (CircularByteBuffer.)
        output-stream (.getOutputStream cbb)
        data-output-stream (DataOutputStream. output-stream)
        input-stream (.getInputStream cbb)
        print-stream (java.io.PrintStream. output-stream true  "UTF-32")

        reader (BufferedReader. (InputStreamReader. input-stream "UTF-32"))
           
        terminal (proxy [Terminal] []
                   (isANSISupported [] true)
                   (initializeTerminal [] )
                   (getTerminalWidth [] 80)
                   (getTerminalHeight [] 25)
                   (isSupported [] true)
                   (getEcho [] false)
                   (isEchoEnabled [] false)
                   (enableEcho [])
                   (getDefaultBindings [] (-> my-bindings
                                              .getBytes
                                              java.io.ByteArrayInputStream.))
                   (readCharacter [in]
                                  (let [c (.read reader)]
                                    c))
                   (readVirtualKey [in]
                                   (let [c (.readCharacter this in)]                                  
                                     c))
                   (disableEcho []))
          
        print-writer (proxy [PrintWriter] [(System/out)]                         
                       (write [c]
                              (comment (proxy-super write c))
                              (if (isa? (type c) Integer)
                                (let [bb (java.nio.ByteBuffer/allocate 4)
                                      dec  (.newDecoder (Charset/forName "UTF-32"))]
                                  (.putInt bb c)
                                  (.flip bb)
                                  (let [cb (.decode dec bb)]
                                    (event ::jline-print :c (str (.get cb  0)))))))
                       (flush []
                              (comment (proxy-super flush))
                              (event ::jline-flush)))
          
        console-reader (ConsoleReader. input-stream print-writer nil terminal)

        cursor-buffer (.getCursorBuffer console-reader)

        running (ref false)
        
        read-loop (future (event ::jline-read-loop-init)
                          (dosync (ref-set running true))
                          (loop []
                            (let [line (.readLine console-reader)]
                              (event ::jline-readline :text (.toString cursor-buffer))
                              (if (not (nil? line))                                                   
                                (recur))))                             
                          (event ::jline-read-loop-exit)
                          (dosync (ref-set running false))
                          (future (.close cbb))
                          (future (.close input-stream))
                          (future (.close output-stream)))

        old-cursor-buf    (ref {:pos 0, :text ""})
        ]

    (on ::jline-write-int #(.writeInt data-output-stream (:val %)))

    (on ::jline-command #(.writeInt data-output-stream (jline-command-map-ids (:id %))))
       
    (on ::jline-exit  #((event ::jline-command :id :newline)
                        (event ::jline-command :id :exit)))

    (on ::jline-write #(.print print-stream (:text %)))

    (on ::jline-ping #(if (= @running true) (event ::jline-pong)))
    
    (on ::jline-flush #(let [pos (.cursor cursor-buffer)
                             text (.toString cursor-buffer)
                             old-pos (:pos @old-cursor-buf)
                             old-text (:text @old-cursor-buf)]

                         (if (not (= pos old-pos))
                           (event ::jline-update-pos :pos pos))

                         (if (not (= text old-text))
                           (event ::jline-update-text :text text))
                           
                         (dosync (ref-set old-cursor-buf {:pos pos :text text}))))))

(defn jline-running? []
     (let [p (promise)]
       (on ::jline-pong #(deliver p true))
       (event ::jline-ping)
       (try 
        (.get (future @p) 300 TimeUnit/MILLISECONDS)
        (catch TimeoutException t 
          false))))


;; GUI part

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

(comment
  (defn repl []
    (let [group (sg-group)
          history (javax.swing.JEditorPane.)
          input (javax.swing.JEditorPane.)
          scroll (JScrollPane. history)

          println-history #(EDT (let [ doc (.getDocument history)
                                      length (.getLength doc)]
                                  (.insertString doc length % nil)))

          bb (CircularByteBuffer.)
          ps (java.io.PrintStream. (.getOutputStream bb) true)
          cr (ConsoleReader. (.getInputStream bb) (PrintWriter. (System/out)))
          ]

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
      
      group)))


(defn repl-panel []
  (let [panel (javax.swing.JPanel.)

        history (javax.swing.JEditorPane.)
        input (javax.swing.JEditorPane.)
        scroll (javax.swing.JScrollPane. history)

        println-history #(in-swing (let [ doc (.getDocument history)
                                         length (.getLength doc)]
                                     (.insertString doc length % nil)))
        
        ]



    panel))
 
