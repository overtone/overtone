(ns ^{:doc "Namespace containing fns to generate UGens, or Unit
    Generators. These are the functions that act as DSP nodes in the
    synthesizer definitions used by SuperCollider.  We generate the UGen
    functions based on hand written metadata about each ugen (ugen
    directory). (Eventually we hope to get this information dynamically
    from the server.)"
      :author "Jeff Rose & Christophe McKeon"}
  overtone.sc.ugens
  (:use [overtone.sc.machinery.ugen fn-gen]))

;; Done actions are typically executed when an envelope ends, or a sample ends
(def NO-ACTION
  "Do nothing when the ugen is finished"
  0)

(def PAUSE
  "Pause the enclosing synth, but do not free it"
  1)

(def FREE
  "Free the enclosing synth"
  2)

(def FREE-AND-BEFORE
  "Free both this synth and the preceding node"
  3)

(def FREE-AND-AFTER
  "Free both this synth and the following node"
  4)

(def FREE-AND-GROUP-BEFORE
  "Free this synth; if the preceding node is a group then do g_freeAll
   on it, else free it"
  5)

(def FREE-AND-GROUP-AFTER
  "Free this synth; if the following node is a group then do g_freeAll
   on it, else free it"
  6)

(def FREE-UPTO-THIS
  "Free this synth and all preceding nodes in this group"
  7)

(def FREE-FROM-THIS-ON
  "Free this synth and all following nodes in this group"
  8)

(def FREE-PAUSE-BEFORE
  "Free this synth and pause the preceding node"
  9)

(def FREE-PAUSE-AFTER
  "Free this synth and pause the following node"
  10)

(def FREE-AND-GROUP-BEFORE-DEEP
  "Free this synth and if the preceding node is a group then do
  g_deepFree on it, else free it"
  11)

(def FREE-AND-GROUP-AFTER-DEEP
  "Free this synth and if the following node is a group then do
 g_deepFree on it, else free it"
  12)

(def FREE-CHILDREN
  "Free this synth and all other nodes in this group (before and after)"
  13)

(def FREE-GROUP
  "Free the enclosing group and all nodes within it (including this
   synth)"
  14)

;;FFT Windows
(def SINE        0)
(def HANN        1)
(def RECT       -1)

;;LINES
(def LINEAR      0)
(def LIN         0)
(def EXPONENTIAL 1)
(def EXP         1)

;;Onset analysis functions
(def POWER       0)
(def MAGSUM      1)
(def COMPLEX     2)
(def RCOMPLEX    3)
(def PHASE       4)
(def WPHASE      5)
(def MKL         6)

(def INFINITE
  "Positive infinity - abbreviation for Float/POSITIVE_INFINITY"
  Float/POSITIVE_INFINITY)

(def INF
  "Positive infinity - abbreviation for Float/POSITIVE_INFINITY"
  Float/POSITIVE_INFINITY)


(defmacro with-overloaded-ugens
  "Bind symbols for all overloaded ugens (i.e. + - / etc.) to the
  overloaded fn in the ns overtone.sc.ugen-colliders. These fns will
  revert back to original
  (Clojure) semantics if not passed with ugen args. "
  [& body]
  (let [bindings (flatten (map (fn [[orig overload]]
                                 [orig (symbol ugen-collide-ns-str (str overload))])
                               @overloaded-ugens*))]
    `(let [~@bindings]
       ~@body)))

;; We refer all the ugen functions here so they can be access by other
;; parts of the Overtone system using a fixed namespace.  For example,
;; to automatically stick an Out ugen on synths that don't explicitly
;; use one.
(defonce __INTERN-UGENS__ (intern-ugens))
