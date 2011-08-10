(ns example.keynome
  (:use overtone.live
        [overtone.studio keynome]))

;; net sample steps
;; 1. define url
;; 2. pull file with java.net.URL.openStream
;; 3. open tempfile, write
;; 4. pass file to overtone's sample
;; http://www.davidreilly.com/java/java_network_programming/#2.3
;; http://www.roseindia.net/java/example/java/io/create-temp-file.shtml
(def url "https://github.com/downloads/mmwoodman/overtone/sound-of-threads.wav")
;;
;; withthis, coudl make a nouveau swing thing with a bouncing bass line
;; then swing drums and clarinet....


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
        env (env-gen (perc 0.01 1.0) :action :free)]
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
;; the rest is left as an exercise to the hacker..
