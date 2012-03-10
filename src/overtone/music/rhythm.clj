(ns
  ^{:doc "Functions to help work with musical time."
     :author "Jeff Rose"}
  overtone.music.rhythm
  (:use [overtone.music time]))

; Rhythm

; * a resting heart rate is 60-80 bpm
; * around 150 induces an excited state


; A rhythm system should let us refer to time in terms of rhythmic units like beat, bar, measure,
; and it should convert these units to real time units (ms) based on the current BPM and signature settings.

(defn beat-ms
  "Convert 'b' beats to milliseconds at the given 'bpm'."
  [b bpm] (* (/ 60000.0 bpm) b))

;(defn bar-ms
;  "Convert b bars to milliseconds at the current bpm."
;  ([] (bar 1))
;  ([b] (* (bar 1) (first @*signature) b)))

(defprotocol IMetronome
  (start [this] [this start-beat]
    "Returns the start time of the metronome. Also restart's the metronome at
     'start-beat' if given.")
  (bar-start [this] [this start-bar])
  (tick [this]
    "Returns the duration of one metronome 'tick' in milleseconds.")
  (tock [this]
    "Returns the duration of one bar in milliseconds.")
  (beat [this] [this beat]
    "Returns the next beat number or the timestamp (in milliseconds) of the
     given beat.")
  (bar [this] [this new-bar]
    "Returns the next bar number or the timestamp (in milliseconds) of the
     given bar")
  (bpb [this] [this new-bpb]
    "Get the current beats per bar or change it to new-bpb")
  (bpm [this] [this new-bpm]
    "Get the current bpm or change the bpm to 'new-bpm'."))

(deftype Metronome [start bar-start bpm bpb]

  IMetronome
  (start [this] @start)
  (start [this start-beat]
    (let [new-start (- (now) (* start-beat (tick this)))]
      (reset! start new-start)
      new-start))
  (bar-start [this] @bar-start)
  (bar-start [this start-bar]
    (let [new-bar-start (- (now) (* start-bar (tock this)))]
      (reset! bar-start new-bar-start)
      new-bar-start))
  (tick  [this] (beat-ms 1 @bpm))
  (tock  [this] (beat-ms @bpb @bpm))
  (beat  [this] (inc (long (/ (- (now) @start) (tick this)))))
  (beat  [this b] (+ (* b (tick this)) @start))
  (bar   [this] (inc (long (/ (- (now) @bar-start) (tock this)))))
  (bar   [this b] (+ (* b (tock this)) @bar-start))
  (bpm   [this] @bpm)
  (bpm   [this new-bpm]
    (let [cur-beat (beat this)
          cur-bar  (bar this)
          new-tick (beat-ms 1 new-bpm)
          new-tock (* @bpb new-tick)
          new-start (- (beat this cur-beat) (* new-tick cur-beat))
          new-bar-start ( - (bar this cur-bar) (* new-tock cur-bar))]
      (reset! start new-start)
      (reset! bpm new-bpm))
    [:bpm new-bpm])
  (bpb   [this] @bpb)
  (bpb   [this new-bpb]
    (let [cur-bar (bar this)
          new-tock (beat-ms new-bpb @bpm)
          new-bar-start (- (bar this cur-bar) (* new-tock cur-bar))]
      (reset! bar-start new-bar-start)
      (reset! bpb new-bpb))
    [:bpb new-bpb])

  clojure.lang.ILookup
  (valAt [this key] (.valAt this key nil))
  (valAt [this key not-found]
    (cond (= key :start) @start
          (= key :bar-start) @start
          (= key :bpm) @bpm
          (= key :bpb) @bpb
          :else not-found))

  clojure.lang.IFn
  (invoke [this] (beat this))
  (invoke [this arg]
    (cond
     (number? arg) (beat this arg)
     (= :bpm arg) (.bpm this) ;; (bpm this) fails.
     (= :bpb arg) (.bpb this)
     :else (throw (Exception. (str "Unsupported metronome arg: " arg)))))
  (invoke [this _ new-bpm] (.bpm this new-bpm)))

(defn metronome
  "A metronome is a beat management function.  Tell it what BPM you want,
  and it will output beat timestamps accordingly.  Call the returned function
  with no arguments to get the next beat number, or pass it a beat number
  to get the timestamp to play a note at that beat.

  (def m (metronome 128))
  (m)          ; => <next beat number>
  (m 200)      ; => <timestamp of beat 200>
  (m :bpm)     ; => return the current bpm val
  (m :bpm 140) ; => set bpm to 140"
  [bpm]
  (let [start (atom (now))
        bar-start (atom @start)
        bpm   (atom bpm)
        bpb   (atom 4)]
    (Metronome. start bar-start bpm bpb)))

;== Grooves
;
; A groove represents a pattern of velocities and timing modifications that is
; applied to a sequence of notes to adjust the feel.
;
; * swing
; * jazz groove, latin groove
; * techno grooves (hard on beat one)
; * make something more driving, or more layed back...


















