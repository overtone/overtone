(ns overtone.ugen
;  (:refer-clojure :exclude [+ - * / = < > <= >=])
  (:use (overtone util ops)
     clojure.contrib.seq-utils))

;; Outputs have a specified calculation rate
;;   0 = scalar rate - one sample is computed at initialization time only. 
;;   1 = control rate - one sample is computed each control period.
;;   2 = audio rate - one sample is computed for each sample of audio output.
(def RATES {:scalar  0
            :control 1
            :audio   2
            :demand  3})

(def REVERSE-RATES (invert-map RATES))

(defn- normalize-name [n]
  (.replaceAll (.toLowerCase (str n)) "[-|_]" ""))

;; Done actions are typically executed when an envelope ends, or a sample ends
;; 0	do nothing when the UGen is finished
;; 1	pause the enclosing synth, but do not free it
;; 2	free the enclosing synth
;; 3	free both this synth and the preceding node
;; 4	free both this synth and the following node
;; 5	free this synth; if the preceding node is a group then do g_freeAll on it, else free it
;; 6	free this synth; if the following node is a group then do g_freeAll on it, else free it
;; 7	free this synth and all preceding nodes in this group
;; 8	free this synth and all following nodes in this group
;; 9	free this synth and pause the preceding node
;; 10	free this synth and pause the following node
;; 11	free this synth and if the preceding node is a group then do g_deepFree on it, else free it
;; 12	free this synth and if the following node is a group then do g_deepFree on it, else free it
;; 13	free this synth and all other nodes in this group (before and after)
;; 14	free the enclosing group and all nodes within it (including this synth)
(def DONE-ACTIONS  
  {:done-nothing 0	
   :done-pause 1	
   :done-free 2	
   :done-free-and-before 3	
   :done-free-and-after 4
   :done-free-and-group-before 5	
   :done-free-and-group-after 6
   :done-free-upto-this 7	
   :done-free-from-this-on 8	
   :done-free-pause-before 9
   :done-free-pause-after 10
   :done-free-and-group-before-deep 11
   :done-free-and-group-after-deep 12	
   :done-free-children 13	
   :done-free-group 14})

(def UGENS (read-string (slurp "src/overtone/ugen-data.clj")))

(def UGEN-MAP (reduce (fn [mem ugen] 
                        (assoc mem (normalize-name (:name ugen)) ugen)) 
                      UGENS))

(defn get-ugen [word]
  (get UGEN-MAP (normalize-name word)))

(defn find-ugen [regexp]
  (filter (fn [[k v]] (re-find (re-pattern regexp) (str k))) UGEN-MAP))

(defn print-ugen [& ugens]
  (doseq [ugen ugens]
    (println "ugen:" (str "\"" (:name ugen) "\"")
             "\nargs: " (map #(:name %1) (:args ugen))
             "\nrates: " (seq (:rates ugen))
             "\nouts: " (:fixed-outs ugen) (str "(" (:out-type ugen) ")")
             "\n- - - - - - - - - -\n")))

(defn ugen-doc [word]
  (apply print-ugen (find-ugen word)))

(defn inf! [sq]
  "users use this to tag infinite sequences for use as
   expanded arguments. needed or else multichannel
   expansion will blow up"
  (with-meta sq
    (merge (meta sq) {:infinite-sequence true})))

(defn inf? [obj]
  (:infinite-sequence (meta obj)))

(defn mapply [f coll-coll]
  (map #(apply f %) coll-coll))

(defn parallel-seqs [& seqs]
  "takes n seqs and returns a seq of vectors of length n, lazily
   (take 4 (parallel-seqs (repeat 5) (cycle [1 2 3]))) ->
     ([5 1] [5 2] [5 3] [5 1])"
  (apply map vector seqs))

(defn cycle-vals [coll]
  (cycle (if (map? coll) (vals coll) coll)))

; because i am using maps as csproxies (cient side ugens)
; and because i wanted to expand maps, it checks if it's a csproxy before it expands.
; to test this code you could just use this dummy function 
(defn csproxy? [_] false)

(defn multichannel-expand [& args]
  "does sc style multichannel expansion checks for any seqs flagged infinite by
   the user so as not to try expand them. note that this returns a list even
   in the case of a single expansion. see fn expansive which implements the full
   expansion semantics."
  (if (= (count args) 0)
    [[]]
    (let [gc-seqs (fn [[gcount seqs] arg]
                    (cond (inf? arg)
                          [gcount (conj seqs arg)]
                          (and (coll? arg)
                               (not (csproxy? arg)))
                          [(max gcount (count arg))
                           (conj seqs (cycle-vals arg))]
                          :else
                          [gcount (conj seqs (repeat arg))]))
          [greatest-count seqs] (reduce gc-seqs [1 []] args)]
      (take greatest-count (apply parallel-seqs seqs)))))

(defn expansive [f]
  "takes a function and returns a multichannel-expand version of the function"
  (fn [& args]
    (let [expanded (apply multichannel-expand args)
          applied (mapply f expanded)]
      (if (= (count applied) 1)
        (first applied)
        applied))))

(defn- replace-action-args [args]
  (map #(get DONE-ACTIONS %1 %1) args))

(defn- envelope-args [spec args]
  (replace-action-args 
    (let [env-ary (first args)
          args (next args) 
          defaults (drop-last (map #(%1 :default) (:args spec)))
          with-defs (cond 
                      (and (< (count args) (count defaults))
                           (contains? DONE-ACTIONS (last args)))
                      (concat (drop-last args) 
                              (drop (dec (count args)) (drop-last defaults)) 
                              [(last args)])

                      (< (count args) (count defaults))
                      (concat args (drop (count args) defaults))

                      :default args)]
      (flatten (concat with-defs [env-ary])))))

(defn- add-default-args [spec args]
  (let [defaults (map #(:default %1) (:args spec))]
    ;(println (:name spec) " defaults: " defaults "\nargs: " args)
    (cond 
      ; Many ugens (e.g. EnvGen) have an array of values as their last argument,
      ; so when the last arg is a coll? we insert missing defaults between the passed
      ; args and the array.
      (and (< (count args) (count defaults))
           (and (coll? (last args)) (:array? (last (:args spec)))))
      (concat (drop-last args) (drop (count args) (drop-last defaults)) (last args))

      ; Replace regular missing args as long as they are all valid numbers
      (and (< (count args) (count defaults)) 
           (not-any? #(= Float/NaN %1) args))
      (concat args (drop (count args) defaults))

      ; Otherwise we just missed something
      (< (count args) (count defaults))
      (throw (IllegalArgumentException. 
        (str "Missing arguments to ugen: " (:name spec) " => "  
             (doall (drop (count args) (map #(%1 :name) (:args spec)))))))

      :default args)))

(defn make-ugen 
  "Returns a function representing the given ugen that will fill in default arguments, rates, etc."
  [spec]
  (fn [& args]
    (let [[rate args] (if (keyword? (first args))
                        [(first args) (rest args)]
                        [:audio args])
          uname (:name spec)
          [uname special] (cond
                            (unary-op-num uname) 
                            ["UnaryOpUgen" (unary-op-num uname)]

                            (binary-op-num uname) 
                            ["BinaryOpUgen" (binary-op-num uname)]

                            :default [uname 0])]
      (with-meta {:id (next-id :ugen)
                  :name uname 
                  :rate (rate RATES)
                  :special special
                  :args (if (= "EnvGen" (:name spec))
                          (envelope-args spec args)
                          (add-default-args spec args))}
                 {:type ::ugen}))))

(defn clojurify-ugen-name 
  "A basic camelCase to with-dash name converter.  Most likely needs improvement."
  [n]
  (let [n (.replaceAll n "([a-z])([A-Z])" "$1-$2") 
        n (.replaceAll n "([A-Z])([A-Z][a-z])" "$1-$2") 
        n (.toLowerCase n)]
  n))

(defn ugen? [obj] (= ::ugen (type obj)))

(defn overload-ugen-op [ns ugen-name ugen-fn]
  (let [original-fn (ns-resolve ns ugen-name)]
    (ns-unmap ns ugen-name)
    (intern ns ugen-name (fn [& args]
                           (if (some #(ugen? %1) args)
                             (apply ugen-fn args)
                             (apply original-fn args))))))
  
(defn refer-ugens [ns]
  (doseq [ugen UGENS]
    (let [ugen-name (symbol (clojurify-ugen-name (:name ugen)))
          ugen-fn (make-ugen ugen)]
      (if (ns-resolve ns ugen-name)
        (overload-ugen-op ns ugen-name ugen-fn)
        (intern ns ugen-name ugen-fn)))))

(refer-ugens (create-ns 'overtone.ugens))

(defn old-ugen [name rate special & args]
  (let [spec (if-let [ug (get-ugen name)]
               ug
               (throw (IllegalArgumentException. (str "Unknown ugen: " name))))
        args (envelope-args spec args)
        args (add-default-args spec args)]
    (println "envelope args...")
    (with-meta {:id (next-id :ugen)
                :name (:name spec)
                :rate (rate RATES)
                :special special
                :args args}
               {:type :ugen})))

;; TODO: Figure out the complete list of control types
;; This is used to determine the controls we list in the synthdef, so we need
;; all ugens which should be specified as external controls.
(def CONTROLS #{"control"})

(defn control-ugen [rate n-outputs]
  (with-meta {:id (next-id :ugen)
              :name "Control"
              :rate (rate RATES)
              :special 0
              :args nil
              :n-outputs n-outputs
              :outputs (repeat n-outputs {:rate (rate RATES)})
              :n-inputs 0
              :inputs []}
            {:type :ugen}))

(defn control-ugen? [ugen]
  (and (map? ugen)
       (CONTROLS (normalize-name (:name ugen)))))

