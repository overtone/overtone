(ns overtone.inst.sampled-piano
  (:use [overtone.core]))

(defn- registered-samples []
  (->> (registered-assets ::MISStereoPiano)
       (filter #(.contains % "LOUD"))))

(defn- download-samples []
  (->> (freesound-searchm [:id] "LOUD" :f "pack:MISStereoPiano")
       (map freesound-path)))

(defonce piano-samples
  (doall (map load-sample (or (registered-samples)
                              (download-samples)))))

(defn- note-index
  "Returns a map of midi-note values [0-127] to buffer id's. Uses the provided
  note-fn to determine the midi-note value of a buffer."
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

(defonce ^:private index-buffer
  (let [tab (note-index piano-samples piano-note-fn)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(definst sampled-piano
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1 curve -4 gate 1]
  (let [buf (index:kr (:id index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (* env (scaled-play-buf 2 buf :level level :loop loop? :action FREE))))
