(ns overtone.studio.eq
  ^{:doc "Audio equalizer effect synths."
    :author "Jeff Rose"}
  (:use [overtone.sc gens envelope synth]))

(defsynth eq-3
  [bus 0
   low-freq {:default 80 :min 40 :max 160 :step 1}
   mid-freq {:default 800 :min 200 :max 1800 :step 1}
   hi-freq  {:default 2000 :min 1000 :max 10000 :step 1}
   low-gain {:default -45 :min -60 :max 20 :step 1}
   mid-gain {:default -45 :min -60 :max 20 :step 1}
   hi-gain  {:default -45 :min -60 :max 20 :step 1}]
  (let [dry (in bus)
        wet (b-low-shelf dry low-freq 1 low-gain)
        wet (b-peak-eq wet mid-freq 1 mid-gain)
        wet (b-hi-shelf wet hi-freq 1 hi-gain)]
    (replace-out bus wet)))

(defsynth eq-7
  [bus 0
   freq0 90 freq1 250 freq2 500 freq3 1500 freq4 3000 freq5 5000 freq6 8000
   gain0 -45 gain1 -45 gain2 -45 gain3 -45 gain4 -45 gain5 -45 gain6 -45]
  (let [dry (in bus)
        wet (b-low-shelf dry freq0 1 gain0)
        wet (b-peak-eq wet freq1 1 gain1)
        wet (b-peak-eq wet freq2 1 gain2)
        wet (b-peak-eq wet freq3 1 gain3)
        wet (b-peak-eq wet freq4 1 gain4)
        wet (b-peak-eq wet freq5 1 gain5)
        wet (b-hi-shelf wet freq6 gain6)]
    (replace-out bus wet)))

(defsynth eq-7b
  [bus 0
   freq0 90 freq1 250 freq2 500 freq3 1500 freq4 3000 freq5 5000 freq6 8000
   gain0 -45 gain1 -45 gain2 -45 gain3 -45 gain4 -45 gain5 -45 gain6 -45]
  (let [dry (in bus)
        wet (b-low-shelf dry freq0 1 gain0)
        wet (mid-eq wet freq1 1 gain1)
        wet (mid-eq wet freq2 1 gain2)
        wet (mid-eq wet freq3 1 gain3)
        wet (mid-eq wet freq4 1 gain4)
        wet (mid-eq wet freq5 1 gain5)
        wet (b-hi-shelf wet freq6 gain6)]
    (replace-out bus wet)))
