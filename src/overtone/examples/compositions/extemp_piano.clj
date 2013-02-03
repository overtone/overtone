(ns overtone.examples.compositions.extemp-piano
  (:use [overtone.live]
        [overtone.inst synth sampled-piano]))

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

(def chord-prog
  [#{[2 :minor7] [7 :minor7] [10 :major7]}
   #{[0 :minor7] [8 :major7]}])

(def beat-offsets [0 0.1 0.2 1/3  0.7 0.9])

(def metro (metronome 20))

(def root 40)
(def max-range 35)
(def range-variation 10)
(def range-period 8)

(defn beat-loop
  [metro beat chord-idx]
  (let [[tonic chord-name] (choose (seq (nth chord-prog chord-idx)))
        nxt-chord-idx      (mod (inc chord-idx) (count chord-prog))
        note-range         (cosr beat range-variation  max-range range-period)
        notes-to-play      (rand-chord (+ root tonic)
                                       chord-name
                                       (count beat-offsets)
                                       note-range)]
    (dorun
     (map (fn [note offset]
            (at (metro (+ beat offset)) (sampled-piano note 0.3)))
          notes-to-play
          beat-offsets))
    (apply-by (metro (inc beat)) #'beat-loop [metro (inc beat) nxt-chord-idx])))

;;start the music:
(beat-loop metro (metro) 0)

;;try changing the beat-offsets on the fly
;(def beat-offsets [0 0.2 1/3  0.5 0.8])
;(def beat-offsets [0 0.2 0.4  0.6 0.8])
;(def beat-offsets [0 0.1 0.2  0.3 0.4])
;(def beat-offsets [0 0.1 0.11 0.13 0.15 0.17 0.2 0.4 0.5 0.55 0.6 0.8])

;;to stop call (stop)
;(stop)
