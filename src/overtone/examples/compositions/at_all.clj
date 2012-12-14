(ns overtone.examples.compositions.at-all
  (:use
    overtone.live
    [overtone.inst.sampled-piano :only [sampled-piano]]))

(defn from [metro offset]
  (fn [beat] (metro (+ beat offset))))

(defn speed-up [metro factor]
  (fn [beat] (metro (/ beat factor))))

(def base 60)

(defn play-note [relative-midi]
  (-> relative-midi (+ base) sampled-piano))

(defn play-chord [chord]
  (doseq [note (vals chord)] (play-note note)))

(defn ionian [degree]
  (let [interval (mod degree 7)
        note ([0 2 4 5 7 9 11] interval)
        octave (quot (- degree interval) 7)]
    (+ (* 12 octave) note)))

(defn triad [scale root]
  (zipmap [:i :iii :v]
          [(scale root)
           (scale (+ root 2))
           (scale (+ root 4))]))

(defn lower [note] (- note 12))
(defn raise [note] (+ note 12))

(defn with-base [chord]
  (assoc chord :base
         (lower (:i chord))))

(def I (with-base (triad ionian 0)))
(def II (with-base (triad ionian 1)))
(def V (with-base (triad ionian 4)))

(def progression [I I II II II V I (update-in V [:base] lower)])

(defn rhythm-n-bass [timing [chord1 chord2 & chords]]
  (do
    (at (timing 0) (play-note (:base chord1)))
    (at (timing 2) (play-chord (dissoc chord1 :base)))
    (at (timing 3) (play-note (:base chord1)))
    (at (timing 4) (play-note (:base chord2)))
    (at (timing 6) (play-chord (dissoc chord2 :base)))
    (let [next (from timing 8)]
      (if chords
        (rhythm-n-bass next chords)
        next))))

(defn even-melody [timing [note & notes]]
  (do
    (at (timing 0) (play-note note))
    (let [next (from timing 1)]
      (if notes
        (even-melody next notes)
        next))))

(defn intro [timing]
    (even-melody timing (take 32 (cycle (map ionian [5 4]))))
    (rhythm-n-bass timing (take 8 (cycle progression))))

(defn first-bit [timing]
  (-> timing
    (from -1)
    (speed-up 2)
    (even-melody (map ionian [2 4 5 4 4 2 4]))
    (from 9)
    (even-melody (map ionian [-2 1 2 1 1 -2 1]))
    (from 9)
    (even-melody (map ionian [-2 1 2 1 1 -2 1 2 3 4]))
    (from 6)
    (even-melody (map ionian [-1 -2 -3 0 0 -3 0 1 0 -3])))
  (rhythm-n-bass timing (take 8 (cycle progression))))

(defn variation [timing]
  (-> timing
    (speed-up 2)
    (from 9)
    (even-melody (map ionian [11 11 12 9 7]))
    (from 11)
    (even-melody (map ionian [8 8 9 8 3]))
    (from 11)
    (even-melody (map ionian [8 8 9 6 4]))
    (from 11)
    (even-melody (map ionian [11 11 12 11 8])))
  (first-bit timing))

(defn final-chord [timing]
  (-> timing
    (from -1)
    (speed-up 2)
    (even-melody (map ionian [11 13 14])))
  (at (timing 0)
      (play-chord (update-in I [:i] raise))))

(defn play [] (-> (metronome 160) (from 2) intro first-bit
                 (speed-up 3/2) variation final-chord))

(play)
