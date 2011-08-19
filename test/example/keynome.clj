(ns example.keynome
  (:use overtone.live
        [overtone.studio keynome]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; inspired by these guys playing Zelda Song of Storms,
;;; (http://www.youtube.com/watch?v=ziUSQKcURBE)
;;; we create a little keyboard for the melody and
;;; button driven sequence for the harmony

;; set up a new keynome
(def kbd (keynome :title "zelda"))

;; a basic instrument, sounds like a marimba in the lower freqs
(definst marimba [freq 300]
  (let [src (sin-osc freq)
        env (env-gen (perc 0.01 1.0) :action FREE)]
    (* 0.9 src env)))

;; set up a mini-piano on the keyboard, with black keys on the
;; qwery row, and white keys on home row, starting with Q -> Eb4
(let [keys [:q :a :s :e :d :r :f :t :g :h :u :j :i :k :l :p
            (keyword (str ";")) (keyword (str "["))]
      notes (for [i (range (count keys))] (midi->hz (+ (resolve-note :eb4) i)))]
  (doall (for [[key note] (map list keys notes)]
           (kbd :map key #(marimba note)))))

;; for the harmony,  we use a simple sequence
;; of notes, triggered by z for bass only or
;; x for highs and bass (press repeatedly!)
(let [hit-count (atom 0), base-note (resolve-note :f3)
      bass  [[0] [3 7] [3 7], [2] [5 9] [], [3] [7 10] [7 10], [2] [5 9] []]
      highs-too [[0 36 43] [3 7 36 43]  [3 7 36 43],  [2 38 45] [5 9 38 45] [ 38 45]
                [3 39 46] [7 10 39 46] [7 10 39 46], [2 38 45] [5 9 38 45] [ 38 45]]
      play-seq (fn [sequ] (fn [] (do (doall (map #(marimba (midi->hz (+ base-note %)))
                                               (nth sequ (mod @hit-count 12))))
                                   (reset! hit-count (inc @hit-count)))))]
  (kbd :map
       :x (play-seq bass)
       :z (play-seq highs-too)
       :c #(reset! hit-count 0) ;; to reset sequences
       ))

;; We haven't put in place everything needed to place the song
;; the rest is left as an exercise to the coder..
