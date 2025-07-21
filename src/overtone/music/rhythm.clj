(ns overtone.music.rhythm
  "Functions to help work with musical time."
  {:author "Jeff Rose"}
  (:require [overtone.at-at :refer [now]]))

(defonce ^{:private true}
  _PROTOCOLS_
  (do
    (defprotocol IMetronome
      (metro-start [metro] [metro start-beat]
        "Returns the start time of the metronome. Also restarts the metronome at
         'start-beat' if given.")
      (metro-bar-start [metro] [metro start-bar])
      (metro-tick [metro]
        "Returns the duration of one metronome 'tick' in milliseconds.")
      (metro-tock [metro]
        "Returns the duration of one bar in milliseconds.")
      (metro-beat [metro] [metro beat]
        "Returns the next beat number or the timestamp (in milliseconds) of the
         given beat.")
      (metro-beat-phase [metro]
        "Returns the distance traveled into the current beat [0.0, 1.0)")
      (metro-bar [metro] [metro  bar]
        "Returns the next bar number or the timestamp (in milliseconds) of the
         given bar")
      (metro-bar-phase [metro]
        "Returns the distance traveled into the current bar [0.0, 1.0)")
      (metro-bpb [metro] [metro new-bpb]
        "Get the current beats per bar or change it to new-bpb")
      (metro-bpm [metro] [metro new-bpm]
        "Get the current bpm or change the bpm to 'new-bpm'."))))

;; Rhythm

;; * a resting heart rate is 60-80 bpm
;; * around 150 induces an excited state

;; A rhythm system should let us refer to time in terms of rhythmic units like beat, bar, measure,
;; and it should convert these units to real time units (ms) based on the current BPM and signature settings.

(defn beat-ms
  "Convert 'b' beats to milliseconds at the given 'bpm'."
  ([bpm] (beat-ms 1 bpm))
  ([b bpm] (* (/ 60000.0 bpm) b)))

(deftype Metronome [start bar-start bpm bpb]
  IMetronome
  (metro-start [metro] @start)
  (metro-start [metro start-beat]
    (dosync
     (ensure bpm)
     (ref-set start (- (now) (* start-beat (metro-tick metro))))))
  (metro-bar-start [metro] @bar-start)
  (metro-bar-start [metro start-bar]
    (dosync
     (ensure bpm)
     (ensure bpb)
     (ref-set bar-start (- (now) (* start-bar (metro-tock metro))))))
  (metro-tick  [metro] (beat-ms @bpm))
  (metro-tock  [metro] (dosync
                        (beat-ms (ensure bpb) (ensure bpm))))
  (metro-beat  [metro] (dosync
                        (ensure bpm)
                        (inc (long (/ (- (now) (ensure start)) (metro-tick metro))))))
  (metro-beat  [metro b] (dosync
                          (ensure bpm)
                          (+ (* b (metro-tick metro)) (ensure start))))
  (metro-beat-phase [metro]
    (dosync
     (ensure bpm)
     (let [ratio (/ (- (now) (ensure start)) (metro-tick metro))]
       (- (float ratio) (long ratio)))))
  (metro-bar [metro] (dosync
                      (ensure bpm)
                      (ensure bpb)
                      (inc (long (/ (- (now) (ensure bar-start)) (metro-tock metro))))))
  (metro-bar [metro b] (dosync
                        (ensure bpm)
                        (ensure bpb)
                        (+ (* b (metro-tock metro)) (ensure bar-start))))
  (metro-bar-phase [metro]
    (dosync
     (ensure bpm)
     (ensure bpb)
     (let [ratio (/ (- (now) (ensure start)) (metro-tock metro))]
       (- (float ratio) (long ratio)))))
  (metro-bpm [metro] @bpm)
  (metro-bpm [metro new-bpm]
    (dosync
     (let [cur-beat      (metro-beat metro)
           cur-bar       (metro-bar metro)
           new-tick      (beat-ms new-bpm)
           new-tock      (* (ensure bpb) new-tick)
           new-start     (- (metro-beat metro cur-beat) (* new-tick cur-beat))
           new-bar-start (- (metro-bar metro cur-bar) (* new-tock cur-bar))]
       (ref-set start new-start)
       (ref-set bar-start new-bar-start)
       (ref-set bpm new-bpm)))
    [:bpm new-bpm])
  (metro-bpb [metro] @bpb)
  (metro-bpb [metro new-bpb]
    (dosync
     (let [cur-bar       (metro-bar metro)
           new-tock      (beat-ms new-bpb (ensure bpm))
           new-bar-start (- (metro-bar metro cur-bar) (* new-tock cur-bar))]
       (ref-set bar-start new-bar-start)
       (ref-set bpb new-bpb))))

  clojure.lang.ILookup
  (valAt [this key] (.valAt this key nil))
  (valAt [this key not-found]
    (cond (= key :start) @start
          (= key :bpm) @bpm
          :else not-found))

  clojure.lang.IFn
  (invoke [this] (metro-beat this))
  (invoke [this arg]
    (cond
      (number? arg) (metro-beat this arg)
      (= :bpm arg) (metro-bpm this)
      :else (throw (Exception. (str "Unsupported metronome arg: " arg)))))
  (invoke [this kw new-val]
    (case kw
      :bpm (metro-bpm this new-val)
      :start (metro-start this new-val)
      :bar-start (metro-bar-start this new-val))))

(defn metronome
  "A metronome is a beat management function.  Tell it what BPM you want,
  and it will output beat timestamps accordingly.  Call the returned function
  with no arguments to get the next beat number, or pass it a beat number
  to get the timestamp to play a note at that beat.

  Metronome also works with bars. Set the number of beats per bar using
  metro-bpb (defaults to 4). metro-bar returns a timestamp that can be used
  to play a note relative to a specified bar.

  (def m (metronome 128))
  (m)          ; => <next beat number>
  (m 200)      ; => <timestamp of beat 200>
  (m :bpm)     ; => return the current bpm val
  (m :bpm 140) ; => set bpm to 140
  (m :start 80) ; => set start beat to 80"
  [bpm]
  (let [cur-now (now)
        start (ref cur-now)
        bar-start (ref cur-now)
        bpm   (ref bpm)
        bpb   (ref 4)]
    (Metronome. start bar-start bpm bpb)))

;; == Grooves

;; A groove represents a pattern of velocities and timing modifications that is
;; applied to a sequence of notes to adjust the feel.

;; * swing
;; * jazz groove, latin groove
;; * techno grooves (hard on beat one)
;; * make something more driving, or more layed back...
