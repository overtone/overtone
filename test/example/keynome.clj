(ns example.keynome
  (:use [[overtone.live]
         [overtone.studio.keynome]]))

;; make a short keyboard with Q key as Eb
(def marimba (keynome))
(definst marimba-note [freq 300]
  (let [src (sin-osc freq)
        env (env-gen (perc 0.01 1.0) :action :free)]
    (* 0.9 src env)))

(def note-diff (Math/pow 2.0 (/ 1.0 12.0)))
(defn change-note [note steps]
  (* note (Math/pow note-diff steps)))

;; keyboard like, though we aren't restricted to that
(def melody-kbd
  (let [base-note (change-note 440 -6)
        keys [:q :a :s :e :d :r :f :t :g :h :u :j :i :k :l :p
              (keyword (str ";")) (keyword (str "["))]
        notes (for [i (range (count keys))] (change-note base-note i))]
    (doall (for [[key note] (partition 2 (interleave keys notes))]
             (set-actions marimba key #(marimba-note note))))
    {:keys keys :notes notes}))

;; lazy harmony, press z for bass only, x  for highs and bass
(def hit-count (ref 0))
(def base-note (change-note 440 -16))

(def bass-sequence [[0] [3 7] [3 7]
                    [2] [5 9] []
                    [3] [7 10] [7 10]
                    [2] [5 9] []])

(def high-sequence [[0 36 43] [3 7 36 43] [3 7 36 43]
                    [2 38 45] [5 9 38 45] [ 38 45]
                    [3 39 46] [7 10 39 46] [7 10 39 46]
                    [2 38 45] [5 9 38 45] [ 38 45]])

(defn play-bass []
  (let [cc (mod @hit-count 12)
        notes (nth bass-sequence cc)]
    (do (dosync (ref-set hit-count (inc @hit-count)))
        (doall (map #(marimba-note (change-note base-note %)) notes)))))

(defn play-high []
  (let [cc (mod @hit-count 12)
        notes (nth high-sequence cc)]
    (do (dosync (ref-set hit-count (inc @hit-count)))
        (doall (map #(marimba-note (change-note base-note %)) notes)))))

(set-actions marimba
             :x play-high
             :z play-bass
             :1 #(dosync (ref-set hit-count 0)) ;; reset sequence
             )

;; which is enough to play a Zelda theme on marimba
;; I saw somewhere on youtube..






;;;;;;;;;;; new example stuff
;;
;; demo use of
;; 1. trigger functions, chords, samples
;; 2. on / off buttons for loops
;; 3. trigger up-for-playing, and off in metro loop
;; 4. paint FFT buffer to JPanel

;; changed to
(comment
  (foo :paint (fn [g2d] bla))
  (foo :map :a #(nty) etc.)
  )
(def foo (keynome)) ;; or (defkeynome foo)
(def sw (switch))
(set-actions foo
             :a #(nty)
             :b #(sot)
             :0 #(marimba-note)
             :1 #(new-keynome)
             :2 #(sw :swap))

(doall (for [i (range 1 10)]
         (set-actions foo
                      (keyword (str i))
                      #(do (marimba-note (midi->hz (+ 50 i)))
                           (marimba-note (midi->hz (+ 57 i)))))))

(use 'overtone.live)

(definst marimba-note [freq 300]
  (let [src (sin-osc freq)
        env (env-gen (perc 0.01 1.0) :action 2)]
    (* 0.9 src env)))


(def nty (sample "/home/duke/samples/were-not-done-yet.wav"))
(def sot (sample "/home/duke/samples/sound-of-threads.wav"))
