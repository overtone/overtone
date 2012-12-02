(ns overtone.examples.gui.stepinator
  (:use overtone.live
        overtone.gui.stepinator))

(def pstep (stepinator))

;; Before evaluating this form, click around in the stepinator to create some
;; settings.
(demo 2
      (let [note (duty (dseq [0.2 0.1] INF)
                       0
                       (dseq (map #(+ 60 %) (:steps @(:state pstep)))))
            src (saw (midicps note))]
        (* [0.2 0.2] src)))

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
