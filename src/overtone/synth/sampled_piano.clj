(ns overtone.synth.sampled-piano
  (:use [overtone.core]
        [overtone.samples.piano :only [index-buffer]]))

(defsynth sampled-piano
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1 curve -4 gate 1 out-bus 0]
  (let [buf (index:kr (:id index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (out out-bus (* env (scaled-play-buf 2 buf :level level :loop loop? :action FREE)))))
