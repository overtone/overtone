(ns example.extemp-piano
  (:use [overtone.live]
        [overtone.inst piano synth]))

;; This example has been translated from the Extempore code demonstrated in
;; http://vimeo.com/21956071 (found around the 10 minute mark)

;; Original Extempore code:
;; (load-sampler sampler "/home/andrew/Documents/samples/piano")
;; (define scale (pc:scale 0 'aeolian))
;; (define loop
;;   (lambda (beat dur root)
;;      (for-each (lambda (p offset)
;;                   (play (+ offset) sampler p 100 (* 2.0 dur)))
;;                (pc:make-chord 40 (cosr 75 10 1/32) 5
;;                               (pc:chord root (if (member root '(10 8))
;;                                                '^7
;;                                                '-7)))
;;                '(1/3 1 3/2 1 2 3))
;;      (callback (*metro* (+ beat (* 0.5 dur))) 'loop (+ dur beat)
;;                dur
;;                (if (member root '(0 8))
;;                  (random '(2 7 10))
;;                  (random '(0 8))))))


(def beat-offsets [0 0.1 1/3  0.7 0.9])
(def instrument piano)
(def metro (metronome 20))

;;this assumes you have the mda-piano available. Feel fre to eplace piano with
;;a different synth which accepts a MIDI note as its first arg such as tb303.
;;(def instrument tb303)

(defn beat-loop
  [metro beat root]
  (let [chord-name (if (some #{10 8} [root])
                     :major7
                     :minor7)
        next-root  (if (some #{0 8} [root])
                     (choose [2 7 10])
                     (choose [0 8]))]
    (dorun (map (fn [note offset]
                  (at (metro (+ beat offset)) (instrument note)))
                (rand-chord (+ 40 root) chord-name (count beat-offsets) (cosr beat 35 10 32) )
                beat-offsets))
    (apply-at (metro (inc beat)) #'beat-loop [metro (inc beat) next-root])))

;;start the music:
(beat-loop metro (metro) 0)

;;try changing the beat-offsets on the fly
;;(def beat-offsets [0 0.2 1/3  0.5 0.8])
;;(def beat-offsets [0 0.2 0.4  0.6 0.8])
;;(def beat-offsets [0 0.1 0.11 0.13 0.15 0.17 0.2 0.4 0.5 0.55 0.6 0.8 0.8 0.8])

;;to stop, define beat-loop to not schedule another callback:
;;(defn beat-loop [m b r]nil)
