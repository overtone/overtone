;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Midi rotater                ;;
;;                             ;;
;; Inspired by this video:     ;;
;; http://youtu.be/4kBpxBJkknY ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns ^:hw overtone.examples.compositions.rotater
  (:use [overtone.live]))


;; Specify output device
(def synth-out (first (midi-connected-receivers)))

;; Rotate between these notes
(def rotation-notes (ref '(-10 -7 -14 -5)) )

;; TODO - use a pointer into the list mod list length instead
(defn next-rotate []
  (let [note (first @rotation-notes)]
    (ref-set rotation-notes (concat (rest @rotation-notes) (list note)))
    note))


;; Init a vector of 128 empty lists
(def notes-playing
  (ref (vec (repeat 128 '()))))

(defn add-notes [note notes]
  (ref-set notes-playing
           (assoc @notes-playing note notes)))

(defn rotater-on [note vel]
  (dosync ;; (next-rotate) and (add-notes) must be sync'ed
   (let [notes (map #(+ % note) [(next-rotate) 0 7])]
     (prn 'on notes)
     (add-notes note notes) ;; mapping note => notes
     (doseq [n notes] (midi-note-on synth-out n vel))
     )))

(defn rotater-off [note]
  (let [notes (@notes-playing note)]
    (prn 'off notes)
    (doseq [n notes] (midi-note-off synth-out n))
    ))

;; the rotater function to handle incoming midi
(defn rotater [event ts]
  (let [chan (:chan event)
        cmd (:command event)
        note (:note event)
        vel (:velocity event)]
    (case cmd
      :note_-on (rotater-on note vel)
      :note-off (rotater-off note))))
