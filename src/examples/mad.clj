(ns examples.mad
  (:use overtone.live
        overtone.inst.synth))

; Adapted from the music as data project, cool stuff!
; http://mad.emotionull.com/

(definst tone [note 60 amp 0.3 dur 0.4]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc 0.01 dur) :action FREE)]
    (* env snd amp)))

(defn p
  ([elements]
   (p elements (now)))
  ([[{:keys [synth vol pitch dur data]} & elements] t]
   (let [next-t (+ t (int (* 1000 dur)))]
     (at t
         (synth pitch vol dur))
     (when elements
       (apply-at next-t #'p elements [next-t])))))

(declare calc-duration)

(defn pattern
  ([m-element] (pattern m-element 1))
  ([m-element duration]
   (if (= (type []) (type m-element))
     (flatten
       (calc-duration m-element duration (count m-element)))
     (assoc m-element :dur (float duration)))))

(defn calc-duration
  [elements duration count]
  (map #(pattern % (/ duration count))
       elements))

(defn defnote
  [n-sym pitch]
  (intern *ns* n-sym
          {:synth tone
           :vol 0.2
           :pitch pitch
           :dur 0.1
           :data []}))

(defn def-notes
  "Define vars for all notes."
  []
  (doseq [octave (range 8)]
    (doseq [n (range 7)]
      (let [n-char (char (+ 65 n))
            n-sym (symbol (str n-char octave))
            note (octave-note octave (get NOTES (keyword (str n-char))))]
        (defnote n-sym note)
        (when-let [sharp (get NOTES (keyword (str n-char "#")))]
          (defnote (symbol (str n-char "#" octave))
                   (octave-note octave sharp)))
        (when-let [flat (get NOTES (keyword (str n-char "b")))]
          (defnote (symbol (str n-char "b" octave))
                   (octave-note octave flat)))))))

(def-notes)

(def derezzed [[E4 G4 E4] [E5 B4 G4 D4 A4 E4 G4 A4]])

; run this to play the pattern
;(p (pattern derezzed 2))

; or this to play it forever
;(p (cycle (pattern derezzed 2)))

; before you stop, add some reverb
;(inst-fx tone fx-reverb)

; call stop to kill the loop
;(stop)

; try it slow with an echo effect
;(inst-fx tone fx-echo)
;(p (cycle (pattern derezzed 6)))

; clear the fx for this instrument like so
;(clear-fx tone)

;;(stop)


; uncomment this one and move the mouse around
(comment (p (cycle (map
            #(assoc % :synth ks1-demo)
            (pattern derezzed 2))))
         )

; throw some distortion on there
; (inst-fx ks1-demo fx-distortion)

; Clear fx
; (clear-fx ks1-demo)

; (stop)

; Ok, now try this one slow, and add the echo effect
(comment
  (p (cycle (map
            #(assoc % :synth ks1-demo)
            (pattern derezzed 4)))))
;(inst-fx ks1-demo fx-echo)

(comment
  (p (cycle
     (map
       #(assoc % :pitch (- (:pitch %) 24))
     (map
       #(assoc % :synth grunge-bass)
       (pattern derezzed 2)))))
  )

;(stop)

; Bach - Minuet in G Major
; Go here for the sheet music:
;; http://www.sheetmusic1.com/new.great.music/bach.minuet.gmajor/bach.1.demo.gif
;; http://www.sheetmusic1.com/new.great.music/bach.minuet.gmajor/bach.2.demo.gif
(def g-minuet-right-hand [[D5 D5 D5]
                         [B4 [A4 B4] G4]
                         [A4 D5 C5]
                         [B4 B4 A4] ; NOTE: two B4's should be tied together
                         [D5 [C5 B4] [A4 G4]]
                         [E5 [C5 B4] [A4 G4]]
                         [F#4 [E4 D4] F#4]
                         [G4]
                         [B4 E5 E5]
                         [C#5 [B4 C5] A4]
                         [D5 E5 F5]
                         [[E5 D5] [C#5 B4] A4]

                         [A6 [G5 F#5 E5 D5]]
                         [B6 [G5 F#5 E5 D5]]
                         [C#5 A5 C#5]
                         [D5]
                         [D5 [C5 B5] A5]
                         [B5 [A5 B5] [G4]]
                         [C5 C5 [C5 B5]]
                         [A5]
                         [D5 [C5 B5 A5 G4]]
                         [E5 [C5 B5 A5 G4]]
                         [F#4 [E4 D4] F#4]
                         [G4]])

(def g-minuet-left-hand [[G3 F#3 D3]
                        [G3 D3 G2]
                        [G3 [F#3 E3] [F#3 D3]]
                        [G3 G2 [D3 C3]]
                        [B2]
                        [C3]
                        [D3]
                        [G2]
                        [G3 G3 E3]
                        [A3 E3 A2]
                        [F#3 E3 D3]
                        [A2 E3 [A4 G3]]

                        [F#3]
                        [G3]
                        [A4 A4 A3]
                        [D3 [D4 C4 B4 A4]]
                        [G3 G3 F#3]
                        [G3 D3 G2]
                        [A4 F#3 G3]
                        [D3 D2 [D3 C3]]
                        [B3]
                        [C3]
                        [D3 D3 D2]
                        [G3]])

(comment
  (do
  (p (map
       #(assoc % :synth ks1-demo)
       (pattern g-minuet-left-hand 25)))

  (p (map
       #(assoc % :synth ks1-demo)
       (pattern g-minuet-right-hand 25))))
  )


; Grrrrrrr! ;-)
;(inst-fx ks1-demo fx-distortion)
;(clear-fx ks1-demo)

(defn glp
  [t]
  (map
    #(assoc % :synth ks1-demo)
    (pattern g-minuet-left-hand t)))

(defn grp
  [t]
  (map
    #(assoc % :synth ks1-demo)
    (pattern g-minuet-right-hand t)))

(defn transpose
  [pat shift]
  (map #(assoc % :pitch (+ (:pitch %) shift)) pat))

; Bach would have had a blast :-)
(comment
(do
    (p (concat
        (glp 20)
        (reverse (glp 20))
        (transpose (glp 20) 4)
        (glp 20)
        ))
    (p (concat
        (grp 20)
        (transpose (reverse (grp 20)) -12)
        (grp 20)
        (grp 20)
        )))
  )
