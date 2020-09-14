; a slide-guitar example using the stringed synthesizer
(def sg (guitar)) ; our guitar :)
(doc slide-string)

; let's put some effects on it
(ctl sg :pre-amp 5.0 :distort 0.96
     :lp-freq 5000 :lp-rq 0.25
     :rvb-mix 0.5 :rvb-room 0.7 :rvb-damp 0.4)

(defn playat [time delta offset]
  (+ time (* offset delta))
)

;complete open slide
(let [time (now) delta 280]
  (slide-string sg 3 16 18 (playat time delta 0) 75 1)
  (guitar-pick sg 4 17 (playat time delta 3)) ;you need to take the time above into account (trial and error atm)
  (guitar-pick sg 4 19 (playat time delta 4))
  (guitar-pick sg 4 17 (playat time delta 5))
  (guitar-pick sg 3 18 (playat time delta 6))
  (guitar-pick sg 3 16 (playat time delta 7))
  (guitar-pick sg 3 18 (playat time delta 8))
  (guitar-pick sg 3 16 (playat time delta 9))
  (guitar-pick sg 4 17 (playat time delta 10))
  (guitar-pick sg 3 18 (playat time delta 11))
)

;open slide muted after some time
(let [time (now) delta 280]
  (guitar-pick sg 0 9 (playat time delta 0))
  (guitar-pick sg 0 -1 (playat time delta 1))
  (guitar-pick sg 1 7 (playat time delta 1))
  (guitar-pick sg 1 -1 (playat time delta 2))
  (guitar-pick sg 1 9 (playat time delta 2))
  (slide-string sg 1 9 11 (playat time delta 3) 75 1)
  (guitar-pick sg 1 -1 (playat time delta 5)) ;compensating time for slide again
  (guitar-pick sg 0 9 (playat time delta 5))
)

;now something else that resembles a melody - with open and closed slides
(let [time (now) delta 300]
  (slide-string sg 2 2 3 (playat time delta 0) 100 1)
  (guitar-pick sg 2 5 (playat time delta 1.5))
  (slide-string sg 2 5 7 (playat time delta 2.5) 100 0)
  (guitar-pick sg 2 -1 (playat time delta 4))
  (guitar-pick sg 3 5 (playat time delta 4))
  (guitar-pick sg 3 -1 (playat time delta 5))
  (guitar-pick sg 2 3 (playat time delta 6))
  (guitar-pick sg 2 5 (playat time delta 7))
  (slide-string sg 2 5 7 (playat time delta 8) 100 1)
)

