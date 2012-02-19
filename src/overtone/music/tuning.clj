(ns
  ^{:doc "Functions that define tuning systems from various musical traditions and theories."
     :author "Jeff Rose"}
  overtone.music.tuning
  (:use [overtone.music pitch]
        [clojure.math.numeric-tower]))

;; TODO: Not only should we pre-compute the frequency values for standard tunings,
;; but it would be cool to let people explore different tunings while creating
;; new synth instruments.  So, if they have a base frequency chosen we should be
;; able to generate tunings, scales, and arpeggios to let them hear their
;; instrument in "western mode", "arabic mode", "indian mode", "chinese mode", etc.


;; Diatonic Just --> Indian music


;; Equal Temperament
;;
;;   The octave is divided into a fixed number of notes, where the ratio of
;; one note to the next is constant.  Typically the A at 440hz is used as the
;; stationary point around which all other notes can be calculated.

;; 12-tone equal temperament --> Western music
;; ratio = (Math/pow 2 (/ 1 12)) => 1.0594630943592953
;; (perform '((:edo 12 100 440) 73 76 79))
;; (perform '(:midi 60 69 81))

;; 24-tone equal temperament --> Arabic music
;; ratio = (Math/pow 2 (/ 1 24)) => 1.029302236643492
;; (perform '((:arabic 100 440) 73 76 79))

(defmulti perfn first)

(defmethod perfn :ed [[symb divisions multiplier initial freq]]
    (fn [note]
        (* freq (Math/pow multiplier (/ (- note initial) divisions)))))

(defmethod perfn :edo [[symb divisions initial freq]]
    (perfn (list :ed divisions 2 initial freq)))

(defmethod perfn :midi [[symb]]
    (perfn (list :edo 12 69 440)))

(defmethod perfn :arabic [[symb initial freq]]
    (perfn (list :edo 24 initial freq)))

(def qcmeantone-list
    (let [x (expt 5 1/4)]
        '(1
         (* 8/25 (sqrt 5) x)
         (* 1/2 (sqrt 5))
         (* 4/5 x)
         5/4
         (* 2/5 (sqrt 5) x)
         (* 5/8 (sqrt 5)) ; Could also use (* 16/25 (sqrt 5))
         x
         8/5
         (* 1/2 (sqrt 5) x)
         (* 4/5 (sqrt 5))
         (* 5/4 x))))

(def qcmeantone-hack
    (let [x (expt 5 1/4)]
        (sort
            (map #(if (< % 1) (* 2 %) %) ; Dear DAemon. What the hell. Love DAemon.
                 (for [expnt (range -5 7)]
                     (* (expt x expnt)
                        (expt 2 (ceil (* expnt -0.5)))))))))

(defn collapse-to-ntave [number ntave]
    (condp > number
             1 (recur (* number ntave) ntave)
             ntave number
             (recur (/ number ntave) ntave)))

(defn notesetfromgenerator [generator initpower finpower ntave]
    (let [tempset (for [exponent (range initpower finpower)] (expt generator exponent))]
        (sort (map #(collapse-to-ntave % ntave) tempset))))

(def qcmeantone
    (notesetfromgenerator (expt 5 1/4) -5 7 2))

(defn perfmap [note initial freq notemap]
    (let [pos (mod (- note initial) (count notemap))
          octave (quot (- note initial (- (count notemap) 1 )) (count notemap))]
        (* freq (nth notemap pos) (expt (ceil (reduce max notemap)) octave))))

(defmethod perfn :qcmeantone [[symb initial freq]]
    (fn [note]
        (perfmap note initial freq qcmeantone)))



(defn perform [[opts & notes]] (map (perfn (flatten (list opts))) notes))
