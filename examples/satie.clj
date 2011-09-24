(ns examples.satie
  (:use [overtone.live]
        [overtone.inst piano])
  (:require [polynome.core :as poly]))


(def phrase1a [:iii :v :iv# :iii :iii :ii# :iii :ii#])
(def phrase1b [:iii :v :iv# :iii :v# :vi :v# :vi])
(def phrase1c [:iii :v :iv# :iii :iii :ii# :i :vii- :vi- :vii- :vi- :vii- :i :vii- :vii- :vi-])

(def phrase2 [:i :ii :i :vii- :i :ii :i :vii- :i :vii- :vii- :vi-])

(def phrase3 [:iii :iv# :v# :vi :vii :ii#+ :vii :vi :vii :vi :vii :vi :vi :v# :iv :iii :iii :ii# :i :vii- :vii- :vi-])

(def phrase1a-reprise [:iii :v :iv# :iii :iii :ii#])
(def phrase1b-reprise [:iii :v :iv# :iii :v# :vi])


(def degrees (concat phrase1a phrase1b phrase1c
                     phrase1a phrase1b phrase1c
                     phrase2
                     phrase2
                     phrase3
                     phrase3
                     phrase2
                     phrase2
                     phrase1a-reprise
                     phrase1b-reprise
                     phrase1a-reprise
                     phrase1b-reprise
                     phrase2
                     phrase2
                     phrase3
                     phrase3
                     phrase2
                     phrase2))

(def pitches (degrees->pitches degrees :major :Ab4))

(def cur-pitch (atom -1))

(defn play-next
  [vol]
  (let [idx (swap! cur-pitch inc)
        pitch (nth (cycle pitches) idx)]
    (piano :note pitch :vel vol)))

(play-next)

(def m (poly/init "/dev/tty.usbserial-m64-0790"))

(poly/on-press m (fn [x y s] (play-next (* 10 (+ x 4)))))
(poly/remove-all-callbacks m)

(nth (cycle pitches) 10)
