(ns studio-test
  (:use overtone.studio
     clj-backtrace.repl))

(reset-studio)
(def echo (effect "echo"))
(def bass (voice "vintage-bass"))
(note bass 50 200)

;(note bass 50 200)
