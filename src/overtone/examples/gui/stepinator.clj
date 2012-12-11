(ns overtone.examples.gui.stepinator
  (:use overtone.live
        overtone.gui.stepinator))

(def example-steps [2 4 -5 -10 -10 2 -3 0])
(def pstep (stepinator :steps 8 :values example-steps))

; Evaluate this form to hear the notes
(demo 2
      (let [note (duty (dseq [0.3 0.2] INF)
                       0
                       (dseq (map #(+ 60 %) (:steps @(:state pstep)))))
            freq (midicps note)
            src  (sync-saw (* 0.5 freq) [freq (* 0.97 freq)])
            filt (moog-ladder src (* freq 4) 0.3)]
        (* filt)))

; Or give the stepinator a func to call and it will show a Stepinate button
(stepinator
  :steps   32
  :slices  50
  :width   640
  :height  480
  :stepper (fn [steps]
             (demo 5
                   (let [note (duty (dseq [0.2 0.1] INF)
                                    0
                                    (dseq (map #(+ 60 %) steps)))
                         a (saw (midicps note))
                         b (sin-osc (midicps (+ note 7)))]
                     [(* 0.2 a) (* 0.2 b)]))))
