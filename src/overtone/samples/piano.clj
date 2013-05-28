(ns overtone.samples.piano
  (:use [overtone.core]))

(defn- registered-samples
  "Fetch piano samples from the asset store if they have been manually
  registered"
  []
  (filter #(.contains % "LOUD")
          (registered-assets ::MISStereoPiano)))

;;(freesound-searchm [:id] "LOUD" :f "pack:MISStereoPiano")
(def FREESOUND-PIANO-SAMPLES
  "Freesound ids and matching notes for all the loud samples in the MISStereoPiano pack"
  (sorted-map
   148401 :BB5 148402 :BB6 148403 :B7  148404 :BB0 148405 :B5  148406 :B6  148407 :BB3
   148408 :BB4 148423 :BB1 148424 :BB2 148425 :D1  148426 :C8  148427 :C1  148428 :BB7
   148429 :C3  148430 :C2  148431 :C5  148432 :C4  148433 :C7  148434 :C6  148435 :GB4
   148436 :GB5 148437 :GB6 148438 :GB7 148439 :G7  148440 :GB1 148441 :GB2 148442 :GB3
   148471 :AB6 148472 :AB5 148473 :AB4 148474 :AB3 148475 :B2  148476 :B1  148477 :B0
   148478 :AB7 148479 :B4  148480 :B3  148481 :A3  148482 :A2  148483 :A1  148484 :A0
   148485 :A7  148486 :A6  148487 :A5  148488 :A4  148489 :AB2 148490 :AB1 148491 :EB7
   148492 :F1  148493 :EB5 148494 :EB6 148495 :EB3 148496 :EB4 148497 :EB1 148498 :EB2
   148499 :F2  148500 :F3  148501 :G2  148502 :G1  148503 :G4  148504 :G3  148505 :F5
   148506 :F4  148507 :F7  148508 :F6  148509 :G6  148510 :G5  148511 :D2  148512 :D3
   148513 :D4  148514 :D5  148515 :D6  148516 :D7  148517 :DB1 148518 :DB2 148519 :DB3
   148520 :DB4 148521 :E7  148522 :E6  148523 :E5  148524 :E4  148525 :E3  148526 :E2
   148527 :E1  148528 :DB7 148529 :DB6 148530 :DB5))

(def FREESOUND-PIANO-SAMPLE-IDS
  (keys FREESOUND-PIANO-SAMPLES))

(def FREESOUND-PIANO-SAMPLE-NOTE-NAMES
  (vals FREESOUND-PIANO-SAMPLES))

(defn- download-samples
  "Download piano samples from freesound and store them in the asset
  store."
  []
  (map freesound-path FREESOUND-PIANO-SAMPLE-IDS))

(defn- get-samples
  "Either fetch samples from the registered store or download them if
  necessary"
  []
  (let [samples (registered-samples)]
    (if (empty? samples)
      (download-samples)
      samples)))

(defonce piano-samples
  (doall (map load-sample (get-samples))))

(defn- buffer->midi-note [buf]
  (let [buffer-id (-> buf :id)
        note (nth FREESOUND-PIANO-SAMPLE-NOTE-NAMES buffer-id)]
    (-> note name match-note :midi-note)))

(defn- note-index
  "Returns a map of midi-note values [0-127] to buffer ids."
  [buffers]
  (reduce (fn [index buf]
            (let [note (buffer->midi-note buf)
                  id   (-> buf :id)]
              (assoc index note id)))
          {}
          buffers))

;; Silent buffer used to fill in the gaps.
(defonce ^:private silent-buffer (buffer 0))

(defonce index-buffer
  (let [tab (note-index piano-samples)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))
