(ns overtone.studio-test
  (:use overtone.live))

(comment
  (defn inst-test []
    (definst bar [freq 200]
      (* (env-gen (perc 0.1 0.8) 1 1 0 1 FREE)
         (rlpf (saw freq) (* 1.1 freq) 0.3)
         0.4))

    (definst buz [freq 200]
      (* (env-gen (perc 0.1 0.8) 1 1 0 1 FREE)
         (+ (sin-osc (/ freq 2))
            (rlpf (saw freq) (* 1.1 freq) 0.3))
         0.4)))


  (def metro (metronome 128))

  (defonce sequences (atom {}))

  (defn sequence-pattern [inst pat]
    (swap! sequences assoc inst pat))

  (defn sequencer-player [beat]
    (doseq [[inst pat] @sequences]
      (pat inst))
    (apply-at #'sequence-player (@sequencer-metro* (inc beat)) (inc beat)))

  (defn sequencer-play []
    (sequencer-player (metro)))

  (def _ nil)
  (def X 440)
  (def x 220)

  (definst foo [freq 440]
    (* 0.8
       (env-gen (perc 0.1 0.4) :action FREE)
       (rlpf (saw [freq (* 0.98 freq)])
             (mul-add (sin-osc:kr 30) 100 (* 1.8 freq)) 0.2)))

  (definst kick [freq 240]
    (* 0.8
       (env-gen (perc 0.01 0.3) :action FREE)
       (sin-osc freq)))

  (defn test-session []
    (track :kick kick)
    (track-fn :kick (fn [] [220]))
    (session-play))

  )
;  (track :foo #'foo)
;  (track-fn :foo #(if (> (rand) 0.7) (+ 300 (rand-int 500))))
