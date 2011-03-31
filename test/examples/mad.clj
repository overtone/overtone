(ns examples.mad
  (:use overtone.live
        overtone.inst.synth))

; Adapted from the music as data project, cool stuff!
; http://mad.emotionull.com/

(definst tone [note 60 amp 0.3 dur 0.4]
  (let [snd (sin-osc (midicps note))
        env (env-gen (perc 0.01 dur) :action :free)]
    (* env snd amp)))

(defrecord note
  [synth vol pitch dur data])

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
            note (octave-note octave (get NOTE (keyword (str n-char))))]
        (defnote n-sym note)
        (when-let [sharp (get NOTE (keyword (str n-char "#")))]
          (defnote (symbol (str n-char "#" octave))
                   (octave-note octave sharp)))
        (when-let [flat (get NOTE (keyword (str n-char "b")))]
          (defnote (symbol (str n-char "b" octave)) 
                   (octave-note octave flat)))))))

(def-notes)

(def derezzed [[E4 G4 E4] [E5 B4 G4 D4 A4 E4 G4 A4]])

; run this to play the pattern
;(p (pattern derezzed 2))

; or this to play it forever
;(p (cycle (pattern derezzed 2)))

; call stop to kill the loop
; (stop)

; uncomment this one and move the mouse around
(comment p (cycle (map
            #(assoc % :synth ks1-demo)
            (pattern derezzed 2))))

; throw some distortion on there
; (inst-fx ks1-demo fx-distortion)

; Clear fx
; (clear-fx ks1-demo)

; (stop)

; Ok, now try this one slow, and add the echo effect
(comment p (cycle (map
            #(assoc % :synth ks1-demo)
            (pattern derezzed 4))))
;(inst-fx ks1-demo fx-echo)

(comment p (cycle
     (map
       #(assoc % :pitch (- (:pitch %) 24))
     (map
       #(assoc % :synth grunge-bass)
       (pattern derezzed 2)))))

;(stop)

; Now for a Bach challenge:
; http://www.sheetmusic1.com/new.great.music/bach.minuet.gmajor/bach.1.demo.gif
(def g-minuet-right-hand [[D5 D5 D5] 
                         [B4 [A4 B4] G4] 
                         [A4 D5 C5] 
                         [B4 B4 A4] ; the two B4's should be tied together (ie. they should be one note). I don't think it's possible to express that they are one note using the []-dividing notation
                         [D5 [C5 B4] [A4 G4]]
                         [E5 [C5 B4] [A4 G4]]
                         [F4 [E4 D4] F4]
                         [G4]
                         [B4 E5 E5]
                         [C#5 [B4 C5] A4]
                         [D5 E5 F5]
                         [[E5 D5] [C#5 B4] A4]])

(def g-minuet-left-hand [[G3 F#3 D3]
                        [G3 D3 G2]
                        [G3 [F#3 E3] [F#3 D3]]
                        [G3 G2 [D3 C3]]
                        [B2]
                        [C3] ; I made up some harmonies here...
                        [D3] ; ...as the sheet music gif omitted them
                        [G2]
                        [G3 G3 E3]
                        [A3 E3 A2]
                        [F#3 E3 D3]
                        [A2 E3 [A3 G3]]) 

; now make it play both hands at once! I don't know how.
(comment p (map
     #(assoc % :synth ks1-demo)
     (pattern g-minuet 10)))
