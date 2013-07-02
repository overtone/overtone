(ns overtone.examples.compositions.bells
  (:use [overtone.live]))

;;http://computermusicresource.com/Simple.bell.tutorial.html
(def dull-partials
  [
   0.56
   0.92
   1.19
   1.71
   2
   2.74
   3
   3.76
   4.07])

;; http://www.soundonsound.com/sos/Aug02/articles/synthsecrets0802.asp
;; (fig 8)
(def partials
  [
   0.5
   1
   3
   4.2
   5.4
   6.8])

;; we make a bell by combining a set of sine waves at the given
;; proportions of the frequency. Technically not really partials
;; as for the 'pretty bell' I stuck mainly with harmonics.
;; Each partial is mixed down proportional to its number - so 1 is
;; louder than 6. Higher partials are also supposed to attenuate
;; quicker but setting the release didn't appear to do much.

(defcgen bell-partials
  "Bell partial generator"
  [freq {:default 440 :doc "The fundamental frequency for the partials"}
   dur  {:default 1.0 :doc "Duration multiplier. Length of longest partial will
                            be dur seconds"}
   partials {:default [0.5 1 2 4] :doc "sequence of frequencies which are
                                        multiples of freq"}]
  "Generates a series of progressively shorter and quieter enveloped sine waves
  for each of the partials specified. The length of the envolope is proportional
  to dur and the fundamental frequency is specified with freq."
  (:ar
   (apply +
          (map
           (fn [partial proportion]
             (let [env      (env-gen (perc 0.01 (* dur proportion)))
                   vol      (/ proportion 2)
                   overtone (* partial freq)]
               (* env vol (sin-osc overtone))))
           partials ;; current partial
           (iterate #(/ % 2) 1.0)  ;; proportions (1.0  0.5 0.25)  etc
           ))))


(definst dull-bell [freq 220 dur 1.0 amp 1.0]
  (let [snd (* amp (bell-partials freq dur dull-partials))]
    (detect-silence snd :action FREE)
    snd))

(definst pretty-bell [freq 220 dur 1.0 amp 1.0]
  (let [snd (* amp (bell-partials freq dur partials))]
    (detect-silence snd :action FREE)
    snd))

;; TUNE - Troika from Lieutenant Kije by Sergei Prokofiev
;; AKA the Sleigh song
;; AKA that tune they play in most Christmas adverts

(def bell-metro  (metronome 400))

;; Two lines - the i-v loop that sort of sounds right
;; and the melody. _ indidcates a rest, we don't have to worry
;; about durations as this is percussion!
(def kije-troika-intervals
  (let [_ nil]
    [[ :i++ :v++ ]
     [ :i :i ]
     [_     _    _     _    _     _   _   _
      _     _    _     _    _     _  :v   _
      :i+  :vii  :vi  :vii  :i+   _  :vi  _
      :v    _     :vi  _   :iii   _  :v   _
      :vi  :v     :iv  _   :i+   _   :vii :i+
      :v   _      _    _   _     _   :iv  :iii
      :ii  _      :vi  _  :v     _   :iv  _   :v :iv
      :iii :iv    :v   _  :i+   :vi :iv  _   :iii  :iv :v _ :v _ :i ]]))

;; Playing in C major
(def troika-hz
  "Map all nested kije troika intervals to hz using the major scale with root C5"
  (let [scale [:major :C5]]
    (letfn [(intervals->hz [intervals]
              (map #(when % (midi->hz %)) (apply degrees->pitches intervals scale)))]
      (map intervals->hz kije-troika-intervals))))

;; Plays the tune endlessly
(defn play-bells
  "Recursion through time over an sequence of infinite sequences of hz notes
  (or nils representing rests) to play with the pretty bell at the specific
  time indicated by the metronome"
  [beat notes]
  (let [next-beat     (inc beat)
        notes-to-play (remove nil? (map first notes))]
    (at (bell-metro beat)
        (dorun
         (map #(pretty-bell % :amp 0.5) notes-to-play)))
    (apply-by (bell-metro next-beat) #'play-bells [next-beat (map rest notes)])))

;; Start the bells ringing...
(defn runner
  "Start up the play-bells recursion with a repeating troika melody and baseline"
  []
  (play-bells (bell-metro) (map cycle troika-hz)))

;; (pretty-bell 440) ;; sounds a bit woodblock
;; (pretty-bell 2000 7.00) ;; diiiiiiiiinnng
;; (dull-bell 600 5.0) ;;  ddddddonnnngg
;; (runner) ;; happy xmas
;; (stop)
