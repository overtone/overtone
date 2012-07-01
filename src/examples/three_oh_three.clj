(ns examples.three-oh-three
  (:use overtone.live
        [overtone.gui.surface core button monome fader dial])
  (:require [overtone.gui.sg :as sg]))

;; requires GUI stuff which is still in development

(defsynth tb303 [note 60 wave 1
                 cutoff 100 r 0.9
                 attack 0.101 decay 1.8 sustain 0.2 release 0.2
                 env 200 gate 0 vol 0.8]
  (let [freq (midicps note)
        freqs [freq (* 1.01 freq)]
        vol-env (env-gen (adsr attack decay sustain release)
                         (line:kr 1 0 (+ attack decay release))
                         :action FREE)
        fil-env (env-gen (perc))
        fil-cutoff (+ cutoff (* env fil-env))
        waves [(* vol-env (saw freqs))
               (* vol-env [(pulse (first freqs) 0.5) (lf-tri (second freqs))])]]
    (out 0 (* [vol vol] (rlpf (select wave (apply + waves)) fil-cutoff r)))))

(definst kick [amp 0.6 freq 100 dur 0.3 width 0.5]
  (let [freq-env (* freq (env-gen (perc 0 (* 0.9 dur))))
        env (env-gen (perc 0.01 dur) 1 1 0 1 FREE)
        sqr (* 0.8 (env-gen (perc 0 0.01)) (pulse (* 2 freq) width))
        src (sin-osc freq-env)
        drum (+ sqr (* env src))]
    (* amp drum)))

(defonce wave (atom 1))
(defonce cutoff* (atom 579))
(defonce r (atom 0.309))
(defonce attack (atom 0.059))
(defonce decay* (atom 0.039))
(defonce sustain (atom 0.269))
(defonce release (atom 3.6))
(defonce bass-release (atom 0.1))
(defonce bass-rate (atom 0.38))

(defn- tb303-gui []
  (let [s (surface "Overtone-303" 300 200)]
    (surface-add-widget s (fader  #(reset! cutoff* (* % 2000))) 20 15)
    (surface-add-widget s (button #(reset! wave (if % 1 0))) 50 85)
    (surface-add-widget s (fader  #(reset! r %)) 80 15)
    (surface-add-widget s (dial #(reset! attack %))  120 10)
    (surface-add-widget s (dial #(reset! decay* (* 4 %)))  120 50)
    (surface-add-widget s (dial #(reset! sustain %))       120 90)
    (surface-add-widget s (dial #(reset! release (* 6 %))) 120 130)
    (surface-add-widget s (fader #(reset! bass-rate %))    160 15)
    (surface-add-widget s (fader #(reset! bass-release %)) 200 15)
    s))

(defn- tb303-gui []
  (-> (surface "Overtone-303" 300 200)
    (fader  :cutoff       :x 20  :y 15 :scale 2000)
    (button :wave         :x 50  :y 85 :type :boolean)
    (fader  :r            :x 80  :y 15)
    (dial   :attack       :x 120 :y 10)
    (dial   :decay*       :x 120 :y 50 :scale 4)
    (dial   :sustain      :x 120 :y 90)
    (dial   :release      :x 120 :y 130 :scale 6)
    (fader  :bass-rate    :x 160 :y 15)
    (fader  :bass-release :x 200 :y 15)))

(defn tb3 []
  (let [p (promise)]
    (sg/in-swing (deliver p (tb303-gui)))
    @p))

(def pitches [64 66 71 73 74 66 64 73 71 66 74 73])
(def vols    [1  4  3  1  7  3  5  7  4  6  2  7])

(def p:v (map (fn [p v] [p v]) pitches vols))

(defn kicker [time sep]
  (let [tick (+ time sep)]
    (at time (kick))
    (apply-at tick #'kicker [tick sep])))

(defn play-scale [time notes:vols sep]
  (let [next-tick (+ time sep)
        [note vol] (first notes:vols)
        vol (/ vol 50)]
    (at time (tb303 :note note :vol (/ vol 3.0) :wave @wave :cutoff @cutoff* :r @r
                    :attack @attack :decay @decay* :sustain @sustain :release @release))
    (if (> (rand) (- 1 @bass-rate))
      (at time (tb303 :note (- note 24) :vol vol :wave @wave :cutoff @cutoff* :r @r
                      :attack @attack :decay (+ 0.001 (* (rand-int 10) @decay*))
                      :sustain @sustain :release (* 10 @bass-release))))
    (apply-at next-tick #'play-scale [next-tick (next notes:vols) sep])))

(defn reich [tempo phase]
  (let [time (+ 400 (now))]
    (kicker time (* 2 tempo))
    (play-scale time
                (cycle p:v)
                tempo)

    (play-scale time
                (cycle p:v)
                (+ tempo phase))))

(tb303)
(reich 270 6)

(stop)
