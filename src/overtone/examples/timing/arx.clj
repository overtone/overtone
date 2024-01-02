(ns overtone.examples.timing.arx
  "Drum patterns based on probabilities.
  https://github.com/gilesbowkett/arx/blob/master/src/arx/core.clj"
  {:author "Giles Bowkett"
   :license "Eclipse Public License 2.0"}
  (:require [overtone.live :refer :all]))

;; drum sounds (sampled)
(def snare (freesound 26903))
(def kick (freesound 2086))
(def cowbell (freesound 9780))
(def tom (freesound 184536))
(def tom2 (freesound 47700))
(def zg-hat (freesound 72526))
(def click-hat (freesound 183401))

;; drum sound (synthesized)
(definst hat [volume 1.0]
  (let [src (white-noise)
        env (env-gen (perc 0.001 0.1) :action FREE)]
    (* volume 0.7 src env)))

(def kick-beats (atom [0 1.5 3]))
(def snare-beats (atom [1 2.5]))
(def hat-beats (atom [0 0.5 1 1.5 2 2.5 3 3.5])) ;; FIXME: DRY?
(def cowbell-beats (atom []))
(def tom-beats (atom []))
(def tom2-beats (atom []))
(def zg-hat-beats (atom []))
(def click-hat-beats (atom []))

;; e.g., (mute snare-beats)
(defn mute [beats]
  (swap! beats (fn [_] [])))

(def kick-probabilities [1  0  0  0
                         0  0  1  0
                         0  0  0  0.1
                         1  0  0  0])

(def snare-probabilities  [0 0 0 0
                           1 0 0 0
                           0 0 1 0
                           0 0 0 0])

(def hat-probabilities  [0.55 0.15 0.55 0.15
                         0.55 0.15 0.55 0.15
                         0.55 0.15 0.55 0.15
                         0.55 0.15 0.55 0.15])

(def cowbell-probabilities [0   0   0.2 0
                            0   0   0   0.1
                            0   0.1 0   0
                            0   0   0.2 0])

(def tom-probabilities [0   0   0.2 0
                        0.4 0   0   0
                        0.2 0   0.3 0
                        0.3 0.1 0.3 0.4])

(def tom2-probabilities [0.2 0   0.3 0
                         0.3 0.1 0.3 0.4
                         0   0   0.2 0
                         0.4 0   0   0])

(def zg-hat-probabilities [0.65 0.65 0.65 0.65
                           0.65 0.65 0.65 0.65
                           0.65 0.65 0.65 0.65
                           0.65 0.65 0.65 0.65])

(def click-hat-probabilities [0.9 0.9 0.9 0.9
                              0.9 0.9 0.9 0.9
                              0.9 0.9 0.9 0.9
                              0.9 0.9 0.9 0.9])

(def sieve-function (atom rand))

(defn random-drums [probabilities]
  (->> probabilities
       (map-indexed (fn [idx prob]
                      (cond (< (@sieve-function) prob)
                            (* idx 0.25))))
       (filter (fn [value]
                 (not (nil? value))))))

(def tracks
  {kick-beats      kick-probabilities
   snare-beats     snare-probabilities
   hat-beats       hat-probabilities
   cowbell-beats   cowbell-probabilities
   tom-beats       tom-probabilities
   tom2-beats      tom2-probabilities
   zg-hat-beats    zg-hat-probabilities
   click-hat-beats click-hat-probabilities})

(defn random-beat []
  (doseq [[!track probabilities] tracks]
    (reset! !track (random-drums probabilities))))

(defn sieve [threshold]
  (reset! sieve-function (constantly threshold))
  (random-beat))

(defn random-sieve []
  (reset! sieve-function rand)
  (random-beat))

;; the following three functions enable live-coding. to plug in new patterns,
;; write code like this in the REPL:
                                        ;
;;   (kicks [0 2 3.75]) ;; for example
                                        ;
;; in addition to being a nice idiom for coding live, this is good setup for
;; the archaeopteryx-style generative breakbeats I have planned...

;; FIXME: DRY
;; might also be wiser to make these atoms too
(defn kicks [beats]
  (reset! kick-beats beats))

(defn snares [beats]
  (reset! snare-beats beats))

(defn hats [beats]
  (reset! hat-beats beats))

;; metronome
(def metro (atom (metronome 170)))

(defn generate-drum-series [drum beats beat-number]
  (doseq [beat beats]
    (at (@metro (+ beat beat-number)) (drum))))

;; FIXME: DRY. the following three functions are nearly identical, and highly
;; repetitious.
(defn generate-kicks [beat-number]
  (generate-drum-series kick @kick-beats beat-number))

(defn generate-snares [beat-number]
  (generate-drum-series snare @snare-beats beat-number))

(defn generate-hats [beat-number]
  (generate-drum-series hat @hat-beats beat-number))

(defn generate-cowbells [beat-number]
  (generate-drum-series cowbell @cowbell-beats beat-number))

(defn generate-toms [beat-number]
  (generate-drum-series tom @tom-beats beat-number)) ;; FIXME DRY ZOMGWTF again!

(defn generate-tom2s [beat-number]
  (generate-drum-series tom2 @tom2-beats beat-number)) ;; FIXME DRY ZOMGWTF again!

(defn generate-zg-hats [beat-number]
  (generate-drum-series zg-hat @zg-hat-beats beat-number)) ;; FIXME DRY ZOMGWTF again!

(defn generate-click-hats [beat-number]
  (generate-drum-series click-hat @click-hat-beats beat-number)) ;; FIXME DRY ZOMGWTF again!

(defn play-beat [beat-number]
  (doseq [generate-drums [generate-kicks
                          generate-snares
                          generate-hats
                          generate-cowbells
                          generate-toms
                          generate-zg-hats
                          generate-click-hats
                          generate-tom2s]] ;; UGH DRY FIXME
    (generate-drums beat-number))

  (apply-at (@metro (+ 4 beat-number)) play-beat (+ 4 beat-number) []))

(defn drums []
  (play-beat (@metro)))

;; use these to do paint-by-numbers live-coding; just fire off (variation)
;; or (main-loop) to switch from one to the other in the REPL
(defn main-loop []
  (kicks [0 1.5 3])
  (snares [1 2.5])
  (hats [0 0.5 1 1.5 2 2.5 3 3.5])) ;; FIXME: DRY?

(defn variation []
  (kicks [0 2 2.5])
  (snares [1 3])
  (hats [0 0.25 0.5 0.75 1 1.25 1.5 1.75 2 2.25 2.5 2.75 3 3.25 3.5 3.75])) ;; FIXME: DRY?

;; use this to change tempo live. restarts loop >.<
(defn change-tempo [bpm]
  (reset! metro (metronome bpm))
  (stop)
  (drums))

(comment
  ;; Start playing
  (drums)

;; Change the probabilities
  (main-loop)
  (variation)

;; Change the sieve
  (random-sieve)
  (sieve 0.5)

  (stop))
