(ns midi-test
  (:use (overtone midi rhythm)))

(def recvr (midi-out "vir"))
;(def txer (midi-in "axiom"))

(comment
(play recvr [57 57 69 64 45 52 57] [50 60 70 70 50 60 70] [200 200 200 200 200 200 400])
(periodic 
  #(play recvr [57 57 69 64 45 52 57] [50 60 70 70 50 60 70] [200 200 200 200 200 200 400])
  1600)

(route-midi txer recvr)
)


