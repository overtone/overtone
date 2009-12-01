(ns extra.getting-started
  (:use overtone))

(comment 
(boot)
(hit) ; makes a test noise
(hit (now) "kick") ; hits the kick drum right now
(hit (+ (now) 1000) "kick") ; hits the kick drum in 1 second (1,000 ms)
(quit)

)
