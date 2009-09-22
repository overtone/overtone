(ns sc-test
  (:use overtone
     clj-backtrace.repl))

(start-synth)
;(def echo (effect "echo"))
(def bass (synth-voice "vintage-bass"))
(def kick (synth-voice "big-kick"))
(def hat (synth-voice "hat"))
;(drum (synth-voice "kick") [1 0 0 0 1 0 0 0])

(doseq [n [50 54 57]]
  (play-note bass n 2000))

(def m (metronome 120))

(defn foo []
  (doseq [n [40 44 47]]
    (play-note bass n 200))
  (callback (+ (now) (m)) #'foo))
  
(foo)

(defn asdf []
  (doseq [n [57 65 68]]
    (play-note bass n 200))
  (note kick 57 200)
  (callback (+ 250 (now)) #'note hat 57 200)
  (callback (+ (now) (m)) #'asdf))
;(asdf)


(defn bass-line [pattern durs index]
  (let [index (mod index (count pattern))
        n (nth pattern index) 
        d (nth durs index)]
    (note bass n (* 0.8 d))
    (callback (m) #'bass-line pattern durs (inc index))))

(bass-line [40 40 38 47] [500 500 500 1000] 0)
(reset-studio)

(play-note hat 50 200)
