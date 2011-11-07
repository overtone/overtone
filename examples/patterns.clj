(ns examples.patterns
  (:use [overtone.live]))

(defn- play-sample
  [samp time vol]
  (at time (stereo-player samp :vol vol)))

(defn determine-time
 [onset-time b-idx beat-dur num-beats]
 (+ onset-time (* b-idx beat-dur)))

(defn- schedule-all-beats
  [bar samp onset-time bar-dur]
  (let [num-beats (count bar)
        beat-dur (/ bar-dur num-beats)]
    (doall
     (map-indexed (fn [idx beat]
                    (cond
                     (= true beat)
                     (play-sample samp (determine-time onset-time idx beat-dur num-beats) 1)

                     (number? beat)
                     (play-sample samp (determine-time onset-time idx beat-dur num-beats) (/ beat 10))

                     (sequential? beat)
                     (schedule-all-beats beat
                                         samp
                                         (determine-time onset-time idx beat-dur num-beats)
                                         beat-dur)))
                  bar))))

(defn play-rhythm
  ([patterns* bar-dur*] (play-rhythm patterns* bar-dur* (+ 500 (now)) 0))
  ([patterns* bar-dur* start-time beat-num]
     (let [patterns @patterns*
           bar-dur @bar-dur*]
       (doall
        (map (fn [[key [samp pat]]]
               (let [idx (mod beat-num (count pat))]
                 (schedule-all-beats (nth pat idx) samp start-time bar-dur)))
             patterns))
       (apply-at (+ start-time bar-dur) #'play-rhythm [patterns*
                                                       bar-dur*
                                                       (+ start-time bar-dur)
                                                       (inc beat-num)]))))

(def piano-samples (load-samples "~/Desktop/samples/MIS_Stereo_Piano/Piano/*LOUD*"))
(def ice-samples (load-samples "~/Desktop/samples/1083-7_Bram_breaking_ice/*.wav"))
(def p (nth piano-samples 18))
(def s (nth piano-samples 20))
(def t (nth piano-samples 21))

(stereo-player t :vol 0.4)

(def bar-dur (atom 1000))


(def _ false)
(def X true)

(def patterns* (atom   {:low-piano [p [[_]]]
                        :hi-piano  [s [[_]]]
                        :ice1 [(nth ice-samples 1) [[_]]]
                        :ice2 [(nth ice-samples 2) [[_]]]
                        :ice3 [(nth ice-samples 3) [[_]]]
                        :ice4 [(nth ice-samples 4) [[_]]]
                        :ice5 [(nth ice-samples 5) [[_]]]}))

(play-rhythm patterns* bar-dur)

(defn update-pat!
  [key pat]
  (swap! patterns* (fn [patterns key new-pat]
                     (let [[samp pat] (get patterns key)]
                       (assoc patterns key [samp new-pat])))
         key pat))

(update-pat! :low-piano [[_]])
(update-pat! :hi-piano  [[_] [_]])
(update-pat! :ice1  [[X] [_] [X [_ X] X]])
(update-pat! :ice2  [[X] [X]])
(update-pat! :ice3  [[X] [_ [X _]]])
(update-pat! :ice4  [[X] [_ [_ X]]])
(update-pat! :ice5  [[X] [_] [_ X _]])

;;(stop)

(def server (osc-server 7800))

(defn- handle-msg
  [samp-name pat]
  (let [pat (read-string pat)]
    (println "samp-name: " samp-name ", pat: " pat ", count: " (count pat))
    (update-pat! (keyword samp-name) pat)))

(osc-handle server "/new-pat" (fn [msg]
                                (apply #'handle-msg [(first (:args msg)) (second (:args msg))])))

(def cl (osc-client "localhost" 7800))
(osc-send cl "/new-pat" "hi-piano" (prn-str [[2 5 7 1]]))
