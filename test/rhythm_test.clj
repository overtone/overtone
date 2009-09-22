(ns rhythm-test
  (:use overtone))

(def piano (midi-out "vir"))
(def metro (metronome 120))

; A generic player action for voice agents
(defn player [[voice notes]]
  (when-let [note (first notes)]
    (apply midi-note voice note)
    [voice (rest notes)]))

(def pitches (chosen-from [60 62 65 69 67 55 48 72]))
(def vels  (chosen-from [30 60 20 80]))
(def durs  (cycle [250 250 500 250]))

(defn generator [length notes vels durs]
  (lazy-seq
    (when (pos? length)
      (cons [(first notes)
             (first vels)
             (first durs)]
            (generator (dec length) (rest notes) (rest vels) (rest durs))))))

(def g (generator 20 pitches vels durs))

(defn forever [notes vels durs]
  (lazy-seq
    (cons [(first notes)
           (first vels)
           (first durs)]
          (forever (rest notes) (rest vels) (rest durs)))))

(def f (forever pitches vels durs))
(def piano-voice (agent [piano f]))

(on-tick metro #(send piano-voice player))
(stop metro)

