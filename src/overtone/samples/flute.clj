(ns overtone.samples.flute
  (:use [overtone.core]))

(defn- registered-vibrato-samples     [] (registered-assets ::TransverseFluteTenutoVibrato))
(defn- registered-non-vibrato-samples [] (registered-assets ::TransverseFluteTenutoNonVibrato))

(def FREESOUND-VIBRATO-FLUTE-SAMPLES
  {154274 :C7  154273 :B6 154272 :A#6 154271 :A6 154270 :G#6 154269 :G6  154268 :F#6
   154267 :F6  154266 :E6 154265 :D#6 154264 :D6 154263 :C#6 154262 :C6  154261 :B5
   154260 :A#5 154259 :A5 154258 :G#5 154257 :G5 154256 :F#5 154255 :F5  154254 :E5
   154253 :D#5 154252 :D5 154251 :C#5 154250 :C5 154249 :B4  154248 :A#4 154247 :A4
   154246 :G#4 154245 :G4 154244 :F#4 154243 :E4 154242 :F4  154241 :D#4 154240 :D4
   154239 :C#4 154238 :C4})

(def FREESOUND-NON-VIBRATO-FLUTE-SAMPLES
  {154237 :C4 154236 :C#4 154235 :D4 154234 :D#4 154233 :E4 154232 :F4 154231 :F#4
   154230 :G4 154229 :G#4 154228 :A4 154227 :G#6 154226 :G6 154225 :F#6 154224 :F6
   154223 :E6 154222 :D#6 154221 :D6 154220 :C#6 154219 :C6 154218 :B5 154217 :A#5
   154216 :A5 154215 :G#5 154214 :G5 154213 :F#5 154212 :F5 154211 :E5 154210 :D#5
   154209 :D5 154208 :C#5 154207 :C5 154206 :B4 154205 :A#4 154204 :A4})

(def VIBRATO-FLUTE-SAMPLE-IDS     (keys FREESOUND-VIBRATO-FLUTE-SAMPLES))
(def NON-VIBRATO-FLUTE-SAMPLE-IDS (keys FREESOUND-NON-VIBRATO-FLUTE-SAMPLES))

(def vibrato-flute-samples     (apply freesound-samples VIBRATO-FLUTE-SAMPLE-IDS))
(def non-vibato-flute-samples  (apply freesound-samples NON-VIBRATO-FLUTE-SAMPLE-IDS))

(defn- buffer->midi-note [buf note-map] (-> buf :freesound-id note-map name note))

(defn- note-index [buffers note-map]
  (reduce (fn [index buf]
            (let [id (:id buf)
                  note (buffer->midi-note buf note-map)]
              (assoc index note id)))
          {}
          buffers))

(defonce ^:private silent-buffer (buffer 0))

(defonce vibrato-index-buffer
  (let [tab (note-index vibrato-flute-samples FREESOUND-VIBRATO-FLUTE-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
        buf))

(defonce non-vibrato-index-buffer
  (let [tab (note-index non-vibato-flute-samples FREESOUND-NON-VIBRATO-FLUTE-SAMPLES)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
        buf))
