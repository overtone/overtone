(ns studio-test
  (:use overtone.studio))

(reset-studio)
(def echo (effect "echo"))
(def bass (voice "vintage-bass"))
(note bass 50 200)

;(note bass 50 200)
