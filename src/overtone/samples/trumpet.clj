(ns overtone.samples.trumpet
  {:author "Karl Thorssen"}
  (:require [overtone.core :as overtone]))

(def TRUMPET-SAMPLES
  {487469 :A#5
   487464 :A5
   487474 :G#5
   487479 :G5
   487476 :F#5
   487477 :F5
   487492 :E5
   487489 :D#5
   487483 :D5
   487485 :C#5
   487471 :C5
   487467 :B4
   487470 :A#4
   487465 :A4
   487473 :G#4
   487482 :G4
   487475 :F#4
   487491 :F4
   487487 :E4
   487490 :D#4
   487484 :D4
   487486 :C#4
   487472 :C4
   487468 :B3
   487463 :A#3
   487466 :A3
   487480 :G#3
   487481 :G3
   487478 :F#3
   487488 :E3})

(def notes-samples
  (into {}
        (for [[freesound-id notename] TRUMPET-SAMPLES]
          [(overtone/note notename)
           (overtone/freesound-sample freesound-id)])))

(defonce ^:private silent-buffer (overtone/buffer 0))

(def trumpet-index-buffer
  (let [buf (overtone/buffer 128 1)]
    (overtone/buffer-fill! buf (:id silent-buffer))
    (doseq [[idx note-sample] notes-samples]
      (overtone/buffer-set! buf idx (:id note-sample)))
    buf))
