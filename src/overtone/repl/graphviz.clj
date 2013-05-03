(ns overtone.repl.graphviz
  (:use [overtone.repl.debug :only [unify-synthdef]]
        [overtone.sc.machinery.ugen.metadata.unaryopugen]
        [overtone.sc.machinery.ugen.metadata.binaryopugen]
        [overtone.helpers.system :only [mac-os?]]
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
  (subs s 0 (dec (count s))))

(defn- generate-node-info
  [ugs]
  (with-out-str
    (doseq [ug ugs]
      (if (= "control" (:name ug))
        (println (str (:id ug) " [label = \"" "Control"  "\" shape=doubleoctagon ]; "))
        (println (str (:id ug)
                      (with-out-str
                        (print " [label = \"{{ ")
                        (print
                         (chop-last-char
                          (with-out-str
                            (doseq [[in-name in-val] (reverse (:inputs ug))]
                              (print (str "<"
                                          (safe-name in-name)
                                          "> "
                                          (cond
                                           (simple-ugen? ug) (if (number? in-val)
                                                               in-val
                                                               "")

                                           (number? in-val) (str (name in-name) " " in-val)

                                           :else (name in-name))
                                          "|"))))))
                        (print "} |"))
                      (:name ug)

                      " }\" style=" (cond
                                     (= :ar (:rate ug)) "rounded"
                                     :else "solid") " shape=record rankdir=LR];"))))))

(defn- print-connection
  [n input ug]
  (cond
   (vector? input)      (doseq [i input] (print-connection n i ug))
   (associative? input) (println (str (:id input) " -> " (:id ug) ":" (safe-name n) " ;"))))

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
   (see overtone.repl.deub/unify-synthdef)"
  [s]
  (str "digraph synthdef {\n"
       (generate-unified-synth-gv (unify-synthdef s))
       "\n}"))

(defn show-graphviz-synth
  "Generate pdf of synth design. On Mac OSX also opens pdf."
  [s]
  (let [f-name (str "/tmp/" (or (:name s) (gensym)) ".pdf") ]
    (do
      (sh "dot" "-Tpdf" "-o" f-name  :in (graphviz s))
      (when (mac-os?)
        (sh "open" f-name)))
    (str "PDF generated in " f-name)))
