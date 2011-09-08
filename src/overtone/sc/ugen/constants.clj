(ns ^{:doc "Handy constant shortcuts for common ugen args"
      :author "Sam Aaron"}
  overtone.sc.ugen.constants)

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

;;Done Actions
(def NO-ACTION 0)
(def PAUSE 1)
(def FREE 2)
(def FREE-AND-BEFORE 3)
(def FREE-AND-AFTER 4)
(def FREE-AND-GROUP-BEFORE 5)
(def FREE-AND-GROUP-AFTER 6)
(def FREE-UPTO-THIS 7)
(def FREE-FROM-THIS-ON 8)
(def FREE-PAUSE-BEFORE 9)
(def FREE-PAUSE-AFTER 10)
(def FREE-AND-GROUP-BEFORE-DEEP 11)
(def FREE-AND-GROUP-AFTER-DEEP 12)
(def FREE-CHILDREN 13)
(def FREE-GROUP 14)

;;FFT Windows
(def SINE 0)
(def HANN 1)
(def RECT -1)

;;LINES
(def LINEAR 0)
(def LIN 0)
(def EXPONENTIAL 1)
(def EXP 1)

;;Onset analysis functions
(def POWER 0)
(def MAGSUM 1)
(def COMPLEX 2)
(def RCOMPLEX 3)
(def PHASE 4)
(def WPHASE 5)
(def MKL 6)

(def INFINITE Float/POSITIVE_INFINITY)
(def INF Float/POSITIVE_INFINITY)
