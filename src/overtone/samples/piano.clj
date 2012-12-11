(ns overtone.samples.piano
  (:use [overtone.core]))

(defn- registered-samples
  "Fetch piano samples from the asset store if they have been manually
  registered"
  []
  (filter #(.contains % "LOUD")
          (registered-assets ::MISStereoPiano)))

;;(freesound-searchm [:id] "LOUD" :f "pack:MISStereoPiano")
(def FREESOUND-PIANO-SAMPLE-IDS
  "Freesound ids for all the loud samples in the MISStereoPiano pack"
  [148408 148404 148431 148430 148492 148434 148433 148432 148405 148401 148525 148498 148497 148528 148527 148521 148505 148503 148477 148471 148440 148436 148429 148427 148425 148424 148423 148407 148526 148496 148530 148518 148517 148516 148515 148507 148506 148502 148501 148494 148491 148490 148487 148478 148476 148475 148474 148473 148472 148437 148428 148426 148406 148403 148402 148524 148523 148495 148529 148522 148520 148519 148514 148513 148512 148511 148510 148509 148508 148504 148500 148499 148493 148489 148488 148486 148485 148484 148483 148482 148481 148480 148479 148442 148441 148439 148438 148435])

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

(defn- note-index
  "Returns a map of midi-note values [0-127] to buffer ids. Uses the
  provided note-fn to determine the midi-note value of a buffer."
  [buffers note-fn]
  (reduce (fn [index buf]
            (let [note (note-fn buf)
                  id   (-> buf :id)]
              (assoc index note id)))
          {}
          buffers))

(defn- piano-note-fn [buf] (-> buf :name match-note :midi-note))

;; Silent buffer used to fill in the gaps.
(defonce ^:private silent-buffer (buffer 0))

(defonce index-buffer
  (let [tab (note-index piano-samples piano-note-fn)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))
