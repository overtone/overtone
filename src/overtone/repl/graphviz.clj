(ns overtone.repl.graphviz
  (:use [overtone.repl.debug :only [unified-sdef]]
        [overtone.sc.machinery.ugen.metadata.unaryopugen]
        [overtone.sc.machinery.ugen.metadata.binaryopugen]
        [overtone.helpers.system :only [mac-os? linux-os?]]
        [overtone.helpers.file :only [mk-tmp-dir! mk-path]]
        [clojure.java.shell]))

(defn- safe-name
  [n]
  (let [n (name n)]
    (.replaceAll (.toLowerCase (str n)) "[-]" "____")))

(defn- simple-ugen?
  [ug]
  (or
   (some #{(:name ug)} (keys unnormalized-unaryopugen-docspecs))
   (some #{(:name ug)} (keys unnormalized-binaryopugen-docspecs))))

(defn- chop-last-char
  [s]
  (if (empty? s)
    s
    (subs s 0 (dec (count s)))))

(defn- generate-vec-arg-info
  [in-name in-vals]
  (chop-last-char
   (with-out-str
     (dorun (map (fn [idx v]
                   (cond
                    (associative? v) (print (str "<" (safe-name in-name) "___" (safe-name (:name v)) "___" idx ">"))
                    (number? v) (print v)
                    :else (println "Unkown Input Val"))
                   (print "|"))
                 (range)
                 in-vals)))))

(defn- graphviz-escape-chars
  [s]
  (let [esc-chars "<>{}\\"
        char->esc (zipmap esc-chars
                          (map #(str "\\" %) esc-chars))]
    (apply str (replace char->esc s))))


(defn- generate-node-info
  [ugs]
  (with-out-str
    (doseq [ug ugs]
      (if (:control-param ug)
        (let [style (case (-> ug :control-param :rate)
                          :ir "\" shape=invhouse style=\"rounded, dashed, filled, bold\" fillcolor=white fontcolor=black ]; "
                          "\" shape=invhouse style=\"rounded, filled, bold\" fillcolor=black fontcolor=white ]; ")]
          (println (str (:id ug) " [label = \"" (-> ug :name) "\n " (keyword (-> ug :control-param :name))  "\n default: " (-> ug :control-param :default) style)))
        (println (str (:id ug)
                      (with-out-str
                        (print " [label = \"{")
                        (when-not (empty? (:inputs ug ))
                          (print "{ ")
                          (print
                           (chop-last-char
                            (with-out-str
                              (doseq [[in-name in-val] (reverse (:inputs ug))]
                                (print (str (if (vector? in-val)
                                              "{{"
                                              (str "<" (safe-name in-name) "> "))
                                            (cond
                                             (simple-ugen? ug) (if (number? in-val)
                                                                 in-val
                                                                 "")

                                             (number? in-val) (str (name in-name) " " in-val)
                                             (vector? in-val) (generate-vec-arg-info in-name in-val)
                                             :else (name in-name))
                                            (when (vector? in-val)
                                              (str "}|" (name in-name) "}"))
                                            "|"))))))
                          (print "} |")))
                      "<__UG_NAME__>"(graphviz-escape-chars (:name ug))

                      " }\" style=" (cond
                                     (= :ar (:rate ug)) "\"filled, bold, rounded\" "
                                     (= :kr (:rate ug)) "\"bold, rounded\""
                                     (= :dr (:rate ug)) "\"bold, diagonals\" "
                                     (= :ir (:rate ug)) "\"dashed, rounded\""
                                     :else "\"\"") " shape=record rankdir=LR];"))))))

(defn- print-connection
  [n input ug]
  (cond
   (vector? input)      (dorun (map (fn [idx i]
                                      (when (associative? i)
                                        (println (str (:id i) ":__UG_NAME__ -> " (:id ug) ":" (safe-name n) "___" (safe-name (:name i)) "___" idx " ;"))))
                                    (range)
                                    input))
   (associative? input) (println (str (:id input) ":__UG_NAME__ -> " (:id ug) ":" (safe-name n) " ;"))))

(defn- generate-node-connections
  [ugs]
  (with-out-str
    (doseq [ug ugs]
      (doseq [[n input] (:inputs ug)]
        (print-connection n input ug))))) ;

(defn- generate-unified-synth-gv
  [unified-synth]
  (let [ugs (:ugens unified-synth)]
    (str (generate-node-info ugs)
         "\n"
         (generate-node-connections ugs))))

(defn graphviz
  "Generate dot notation for synth design.
   (see overtone.repl.debug/unified-sdef)"
  [s]
  (str "digraph synthdef {\n"
       (generate-unified-synth-gv (unified-sdef s))
       "\n}"))

(defn show-graphviz-synth
  "Generate pdf of design for synth s. This assumes that graphviz has
   been installed and the dot program is available on the system's PATH.

   On OS X, a simple way to install graphviz is with homebrew: brew
   install graphviz

   Also opens pdf on Mac OS X (with open) and Linux (with xdg-open)."
  [s]
  (let [dir       (mk-tmp-dir!)
        f-name    (str (or (:name s) (gensym)) ".pdf")
        full-path (mk-path dir f-name) ]
    (do
      (sh "dot" "-Tpdf" (str "-o" full-path) :in (graphviz s))
      (cond
       (mac-os?)   (sh "open" full-path)
       (linux-os?) (sh "xdg-open" full-path)))
    (str "PDF generated in " full-path)))
