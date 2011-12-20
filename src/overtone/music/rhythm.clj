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
  (start [this] [this b]
    "Start or restart the metronome at beat number 'b' or 0 if none is given. Returns the next beat number")
  (tick [this]
    "Returns the duration of one metronome 'tick' in milleseconds.")
  (beat [this] [this b]
    "Returns the number of the next beat or the timestamp (in milliseconds) of the
     given beat number 'b'.")
  (bpm [this] [this new-bpm]
    "Get the current bpm or change the bpm to 'new-bpm'."))

(defrecord Metronome [start bpm]
  IMetronome
  (start [this] (do (reset! start (now))
                    (beat this)))
  (start [this b] (let [new-start (- (now) (* b (tick this)))]
                        (reset! start new-start)
                        (beat this)))
  (tick  [this] (beat-ms 1 @bpm))
  (beat  [this] (inc (long (/ (- (now) @start) (tick this)))))
  (beat  [this b] (+ (* b (tick this)) @start))
  (bpm   [this] @bpm)
  (bpm   [this new-bpm]
    (let [cur-beat (beat this)
          new-tick (beat-ms 1 new-bpm)
          new-start (- (now) (* new-tick cur-beat))]
      (reset! start new-start)
      (reset! bpm new-bpm))
    [:bpm new-bpm])
  clojure.lang.IFn
  (invoke [this] (beat this))
  (invoke [this arg]
    (cond
     (number? arg) (beat this arg)
     (= :bpm arg) (.bpm this) ;; (bpm this) fails.
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
        bpm   (atom bpm)]
    (Metronome. start bpm)))

;== Grooves
;
; A groove represents a pattern of velocities and timing modifications that is
; applied to a sequence of notes to adjust the feel.
;
; * swing
; * jazz groove, latin groove
; * techno grooves (hard on beat one)
; * make something more driving, or more layed back...
