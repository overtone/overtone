(ns overtone.examples.getting-started.video
  "Examples used in the video 'Quick Intro to Live Programming in Overtone'
   http://vimeo.com/22798433"
    (:use [overtone.live]
          [overtone.inst.sampled-piano]))

;; use the sampled piano
;; note: The piano samples take a long time to download, but
;; it will only happen once.
(def piano sampled-piano)

; play some notes on our piano
(piano)
(piano 72)

;; this is one possible implementation of play-chord,
;; it was not shown in the video
(defn play-chord [a-chord]
  (doseq [note a-chord] (piano note)))

;; play a chord progression on our piano
(let [time (now)]
  (at time (play-chord (chord :D3 :major7)))
  (at (+ 2000 time) (play-chord (chord :A3 :major)))
  (at (+ 3000 time) (play-chord (chord :A3 :major7)))
  (at (+ 4300 time) (play-chord (chord :F3 :major7))))

;; here's a different function that encapsulates the above
(defn play-progression [chords]
  (if (empty? chords) nil
    (doseq []
      (play-chord (first chords))
      (Thread/sleep 2000)
      (play-progression (rest chords)))))

;; define a simple instrument. in the video, sam uses :action :free,
;; but this is now :action FREE
(definst beep [note 60]
  (let [sound-src (sin-osc (midicps note))
        env       (env-gen (perc 0.01 1.0) :action FREE)] ; sam uses :free
    (* sound-src env)))

;; admire our beep :-)
(beep)

;; beep across a wide range of sounds
(for [i (range 110)] (at (+ (now) (* i 20)) (beep i)))

;; model a plucked string. this is really cool!
(definst plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1]
  (let [freq   (midicps note)
        noize  (* 0.8 (white-noise))
        dly    (/ 1.0 freq)
        plk    (pluck noize gate dly dly decay coef)
        dist   (distort plk)
        filt   (rlpf dist (* 12 freq) 0.6)
        clp    (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur)) reverb)))

;; ___|)_______________|\________________|\______________|\_______________|\________
;;|___/___||___________|_________________|_______________|________________|_________||
;;|__/|___||.________,-.___( )___o-;___,-.___o-;__( )__,-.________o-; __,-.___o-;__.||
;;|_/(|,\_||.___(_)__`-'___|______/____`-'____/___|____`-'___(_)___/____`-'____/___.||
;;|_\_|_/_||____|__________|______________________|__________|______________________||
;;    |         |          |/                     |/         |
;;  (_|         |/                                           |/

;; note: the underscores are rests
(def reich-degrees [:vi :vii :i+ :_ :vii :_ :i+ :vii :vi :_ :vii :_])
(def pitches (degrees->pitches reich-degrees :diatonic :C4))

;; temporal recursion: create a function that takes:
;; 1) time to play a note
;; 2) list of notes to play
;; 3) seperation of notes
;;
;; Armed with these parameters, we check if the note is a rest,
;; if it isn't, schedule it to be played. then, we schedule
;; a recursive call to be made to our function again at the new time,
;; calculated by adding the current time plus the separation.
(defn play
  [time notes sep]
  (let [note (first notes)]
    (when note
      (at time (plucked-string note)))
    (let [next-time (+ time sep)]
      (apply-by next-time play [next-time (rest notes) sep]))))

;; play some pitches
(play (now) pitches 200)

;; cycle through some pitches
;; this will loop indefinitely.
(let [t (+ 500 (now))]
  (play t (cycle pitches) 100)
  (play t (cycle pitches) 102))

(stop)
