(ns examples.satie
  (:use [clojure.core.match :only [match]]
        [overtone.live]
        [overtone.inst sampled-piano])
  (:require [polynome.core :as poly]))

;;Erik Satie Gnossienne No. 1
(def phrase1a [:iii :v :iv# :iii :iii :ii# :iii :ii#])
(def phrase1b [:iii :v :iv# :iii :v# :vi :v# :vi])
(def phrase1c [:iii :v :iv# :iii :iii :ii# :i :vii- :vi- :vii- :vi- :vii- :i :vii- :vii- :vi-])

(def phrase2 [:i :ii :i :vii- :i :ii :i :vii- :i :vii- :vii- :vi-])

(def phrase3 [:iii :iv# :v# :vi :vii :ii#+ :vii :vi :vii :vi :vii :vi :vi :v# :iv :iii :iii :ii# :i :vii- :vii- :vi-])

(def phrase1a-reprise [:iii :v :iv# :iii :iii :ii#])
(def phrase1b-reprise [:iii :v :iv# :iii :v# :vi])

(def phrase1-bass [:vi--- [:vi- :iii- :i-] [:vi- :iii- :i-]])
(def phrase2-bass [:iii-- [:iii- :vii-- :v--] [:iii- :vii-- :v--]])

(def phrase3-bass [:ii--- [:vi-- :ii- :iv-] [:vi-- :ii- :iv-]])


(def right-hand-degrees (concat phrase1a phrase1b phrase1c
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


(def left-hand-degrees (concat (apply concat (repeat 6 phrase1-bass))  ;;A
                               phrase2-bass                            ;;B
                               (apply concat (repeat 8 phrase1-bass))  ;;C
                               phrase2-bass                            ;;D
                               (apply concat (repeat 2 phrase1-bass))  ;;E
                               (apply concat (repeat 2 phrase3-bass))  ;;F
                               (apply concat (repeat 2 phrase1-bass))  ;;G
                               (apply concat (repeat 2 phrase3-bass))  ;;H
                               (apply concat (repeat 14 phrase1-bass)) ;;I
                               (apply concat (repeat 2 phrase3-bass))  ;;J
                               (apply concat (repeat 2 phrase1-bass))  ;;K
                               (apply concat (repeat 2 phrase3-bass))  ;;L
                               (apply concat (repeat 10 phrase1-bass)) ;;M
                               (apply concat (repeat 2 phrase3-bass))  ;;N
                               (apply concat (repeat 2 phrase1-bass))  ;;O
                               (apply concat (repeat 2 phrase3-bass))  ;;P
                               (apply concat (repeat 14 phrase1-bass)) ;;Q
                               (apply concat (repeat 2 phrase3-bass))  ;;R
                               (apply concat (repeat 2 phrase1-bass))  ;;S
                               (apply concat (repeat 2 phrase3-bass))  ;;T
                               phrase1-bass                            ;;U
                               ))

(def lh-pitches (degrees->pitches left-hand-degrees :major :Ab4))
(def rh-pitches (degrees->pitches right-hand-degrees :major :Ab4))

(def cur-pitch-rh (atom -1))
(def cur-pitch-lh (atom -1))

(defn reset-pos
  []
  (reset! cur-pitch-rh -1)
  (reset! cur-pitch-lh -1))

(defn vol-mul
  [vol]
  (* vol 0.002))

(defn play-next-rh
  [vol]
  (let [idx (swap! cur-pitch-rh inc)
        pitch (nth (cycle rh-pitches) idx)]
    (sampled-piano pitch (vol-mul vol))))

(defn play-next-lh
  [vol]
  (let [idx (swap! cur-pitch-lh inc)
        pitch (nth (cycle lh-pitches) idx)]
    (if (sequential? pitch)
      (doseq [p pitch]
        (sampled-piano p (vol-mul vol)))
      (sampled-piano pitch (vol-mul vol)))))

(defonce m (poly/init "/dev/tty.usbserial-m64-0790"))

(poly/on-press m (fn [x y s]
                   (match [x y]
                          [7 _] (reset-pos)
                          [_ 0] (play-next-lh (+ (rand-int 5) (* 12 (+ x 4))))
                          [_ 7] (play-next-rh (+ (rand-int 5) (* 12 (+ x 4)))))))

;;(poly/remove-all-callbacks m)
;;(poly/disconnect m)
