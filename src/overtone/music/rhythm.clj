(ns
  ^{:doc "Functions to help work with musical time."
     :author "Jeff Rose"}
  overtone.music.rhythm
  (:use [overtone.music time]))

(defonce ^{:private true}
  _PROTOCOLS_
  (do
    (defprotocol IMetronome
      (metro-start [metro] [metro start-beat]
        "Returns the start time of the metronome. Also restarts the metronome at
     'start-beat' if given.")
      (metro-bar-start [metro] [metro start-bar])
      (metro-tick [metro]
        "Returns the duration of one metronome 'tick' in milleseconds.")
      (metro-tock [metro]
    "Returns the duration of one bar in milliseconds.")
      (metro-beat [metro] [metro beat]
        "Returns the next beat number or the timestamp (in milliseconds) of the
     given beat.")
      (metro-bar [metro] [metro  bar]
    "Returns the next bar number or the timestamp (in milliseconds) of the
     given bar")
      (metro-bpb [metro] [metro new-bpb]
    "Get the current beats per bar or change it to new-bpb")
      (metro-bpm [metro] [metro new-bpm]
        "Get the current bpm or change the bpm to 'new-bpm'."))))

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

(deftype Metronome [start bar-start bpm bpb]

  IMetronome
  (metro-start [metro] @start)
  (metro-start [metro start-beat]
    (let [new-start (- (now) (* start-beat (metro-tick metro)))]
      (reset! start new-start)
      new-start))
  (metro-bar-start [metro] @bar-start)
  (metro-bar-start [metro start-bar]
    (let [new-bar-start (- (now) (* start-bar (metro-tock metro)))]
      (reset! bar-start new-bar-start)
      new-bar-start))
  (metro-tick  [metro] (beat-ms 1 @bpm))
  (metro-tock  [metro] (beat-ms @bpb @bpm))
  (metro-beat  [metro] (inc (long (/ (- (now) @start) (metro-tick metro)))))
  (metro-beat  [metro b] (+ (* b (metro-tick metro)) @start))
  (metro-bar   [metro] (inc (long (/ (- (now) @bar-start) (metro-tock metro)))))
  (metro-bar   [metro b] (+ (* b (metro-tock metro)) @bar-start))
  (metro-bpm   [metro] @bpm)
  (metro-bpm   [metro new-bpm]
    (let [cur-beat      (metro-beat metro)
          cur-bar       (metro-bar metro)
          new-tick      (beat-ms 1 new-bpm)
          new-tock      (* @bpb new-tick)
          new-start     (- (metro-beat metro cur-beat) (* new-tick cur-beat))
          new-bar-start (- (metro-bar metro cur-bar) (* new-tock cur-bar))]
      (reset! start new-start)
      (reset! bar-start new-bar-start)
      (reset! bpm new-bpm))
    [:bpm new-bpm])
  (metro-bpb   [metro] @bpb)
  (metro-bpb   [metro new-bpb]
    (let [cur-bar       (metro-bar metro)
          new-tock      (beat-ms new-bpb @bpm)
          new-bar-start (- (metro-bar metro cur-bar) (* new-tock cur-bar))]
      (reset! bar-start new-bar-start)
      (reset! bpb new-bpb)))

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
     (= :bpm arg) (.bpm this) ;; (bpm this) fails.
     :else (throw (Exception. (str "Unsupported metronome arg: " arg)))))
  (invoke [this _ new-bpm] (.bpm this new-bpm)))

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
