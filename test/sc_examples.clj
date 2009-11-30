(ns sc-examples
  (:use overtone
     clojure.contrib.seq-utils
     clj-backtrace.repl))

(def _ false)
(def x true)

(boot)

(def BPM 120)
(def TICK (/ (/ 60000 BPM) 4))

(defsynth sin-chord {:out 0 :pitch 52 :dur 500 :amp 0.2}
  (let [dur-half (/ (/ :dur 1000.0) 2)
        env (env-gen.ar (perc dur-half dur-half) :done-free)]
  (out.ar :out (* env :amp (+ (sin-osc.ar (midicps :pitch))
                              (sin-osc.ar (midicps (+ :pitch 4)))
                              (sin-osc.ar (midicps (+ :pitch 7))))))))

(defn chords [t n]
  (hit t sin-chord :pitch (choose [52 55 57 61 63]) :amp 0.2 :dur 500)
  (callback (+ (now) 260) #'chords (+ t 125) n))

(chords (now) [52 57 59 64])

(def hat-buf (load-sample "/home/rosejn/projects/overtone/instruments/samples/kit/open-hat.wav"))

(def house-drums {:kick "kick" 
                  :hat hat-buf
                  :snare "clap"
                  :tom "tom"})

(def house-beat {:kick  [x _ _ _ x _ _ _ x _ _ _ x _ _ _]
                 :hat   [_ _ x _ _ _ x _ _ _ x _ _ _ x _]
                 :snare [_ _ _ _ x _ _ _ _ _ _ _ x _ _ _]
                 :tom   [_ _ _ _ _ _ x _ _ _ x _ x x _ x]})

(defn get-tick [tick beat]
  (map (fn [[inst pat]] [inst (nth pat (mod tick (count pat)))]) beat))

(defn play-beat [voices beat num-ticks]
  (let [t (now)]
    (doseq [i (range num-ticks)]
      (doseq [[inst note] (get-tick i beat)]
        (if note
          (hit (+ (* i TICK) t) (inst voices)))))))

(play-beat house-drums house-beat 500)

(defn beats [t cnt]
  (cond
    (zero? (mod cnt 4)) (hit t "kick")
    (zero? (mod cnt 8)) (hit t "snare"))
  (callback (+ (now) 125) #'beats  (+ t 125) (inc cnt)))

;(beats (now) 0)

(defn make-beat [pattern voice-map]
  (let [t (now)]
    (for [[inst pat] pattern]
      (for [[index note] (indexed pat)]
        (if note
          (hit (+ (* index TICK) t) (get voice-map inst)))))))

;(make-beat house-beat house-drums)
  
(defn sin-man [start]
  (if (< 2 (rand-int 5))
    (doseq [i (range (rand-int 5))]
      (hit (+ (* i 120) (now)) "sin" :pitch (+ 20 start (- (rand-int 6) 3)) :dur (* 0.29 i)))
    (hit (now) "sin" :pitch (+ 20 start (- (rand-int 6) 3)) :dur (* 0.1 (+ 1 (rand-int 2)))))
  (callback (+ 500 (now)) #'sin-man start))

(sin-man 40)

;(defn forever [tick pat cur]
;  (lazy-seq
;    (cons [(first notes)
;           (first vels)
;           (first durs)]
;          (forever (rest notes) (rest vels) (rest durs)))))


;;(def echo (effect "echo"))
;(def bass (synth-voice "vintage-bass"))
;(def kick (synth-voice "big-kick"))
;(def hat (synth-voice "hat"))
;;(drum (synth-voice "kick") [1 0 0 0 1 0 0 0])
;
;(doseq [n [50 54 57]]
;  (play-note bass n 2000))
;
;(def m (metronome 120))
;
;(defn foo []
;  (doseq [n [40 44 47]]
;    (play-note bass n 200))
;  (callback (+ (now) (m)) #'foo))
;  
;(foo)
;
;(defn asdf []
;  (doseq [n [57 65 68]]
;    (play-note bass n 200))
;  (note kick 57 200)
;  (callback (+ 250 (now)) #'note hat 57 200)
;  (callback (+ (now) (m)) #'asdf))
;;(asdf)
;
;
;(defn bass-line [pattern durs index]
;  (let [index (mod index (count pattern))
;        n (nth pattern index) 
;        d (nth durs index)]
;    (note bass n (* 0.8 d))
;    (callback (m) #'bass-line pattern durs (inc index))))
;
;(bass-line [40 40 38 47] [500 500 500 1000] 0)
;(reset-studio)
;
;(play-note hat 50 200)
