(ns sc-test
  (:use (overtone sc utils pitch)
     clojure.contrib.seq-utils
     clj-backtrace.repl))

(def _ false)
(def x true)

(defn setup [] (boot))

(def BPM 120)
(def TICK (/ (/ 60000 BPM) 4))

(defn make-beat [pattern voice-map]
  (let [t (now)]
    (for [[inst pat] pattern]
      (for [[index note] (indexed pat)]
        (if note
          (hit (+ (* index TICK) t) (get voice-map inst)))))))
  
(def house-beat {:kick  [x _ _ _ x _ _ _ x _ _ _ x _ _ _]
                 :hat   [_ _ x _ _ _ x _ _ _ x _ _ _ x _]
                 :snare [_ _ _ _ x _ _ _ _ _ _ _ x _ _ _]})
(def hat-buf (load-sample "/home/rosejn/projects/overtone/instruments/samples/kit/open-hat.wav"))

(def house-drums {:kick "kick" 
                  :hat hat-buf
;                  :hat "noise-hat"
                  :snare "clap"})

(defn get-tick [tick beat]
;  (println "tick: " tick " beat: " beat)
  (map (fn [[inst pat]] [inst (nth pat (mod tick (count pat)))]) beat))

(defn play-beat [voices beat num-ticks]
  (let [t (now)]
    (doseq [i (range num-ticks)]
      (doseq [[inst note] (get-tick i beat)]
        (if note
          (if (number? (inst voices))
            (hit (+ (* i TICK) t) "play-mono" :bufnum (inst voices))
            (hit (+ (* i TICK) t) (inst voices))))))))

;(defn forever [tick pat cur]
;  (lazy-seq
;    (cons [(first notes)
;           (first vels)
;           (first durs)]
;          (forever (rest notes) (rest vels) (rest durs)))))
;(make-beat house-beat house-drums)


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
