(ns overtone.examples.midi.basic
  (:use [overtone.live]
        [overtone.synth.sts :only [prophet]]))

;; Now, we know we can trigger synths manually with code:

(prophet :freq 110 :decay 5 :rq 0.6 :cutoff-freq 2000)
(prophet :freq 130 :decay 5 :rq 0.6 :cutoff-freq 2000)

;; It's super easy to trigger synths with attached MIDI devices using
;; Overtone's event system. By default all note-making MIDI devices will
;; emit a [:midi :note-on] event. Make sure you attach a MIDI piano-like
;; device *before* booting Overtone and the following will work:

(on-event [:midi :note-on]
          (fn [m]
            (let [note (:note m)]
              (prophet :freq (midi->hz note)
                       :decay 5
                       :rq 0.6
                       :cutoff-freq 1000)))
          ::prophet-midi)

;; You also have access to velocity information:

(on-event [:midi :note-on]
          (fn [m]
            (let [note (:note m)]
              (prophet :freq (midi->hz note)
                       :decay 5
                       :rq 0.6
                       :cutoff-freq 1000
                       :amp (:velocity-f m))))
          ::prophet-midi)

;; To see all the information passed to the event system:

(on-event [:midi :note-on]
          (fn [m]
            (println (type (:velocity-f m))))
          ::midi-debug)

;; We can remove our event handlers with:

(remove-event-handler ::prophet-midi)
(remove-event-handler ::midi-debug)

;; Useful keys are:
;;
;; * :note (MIDI note)
;; * :timestamp
;; * :velocity (MIDI value i.e. 0->127)
;; * :velocity-f (float 0->1)

;; We can also easily receive note off events with the event key [:midi
;; :note-off]. So, if we have a gated synth that we want to sustain
;; whilst we hold the MIDI key down, this is pretty easy to build.
;; First, let's define a suitable synth:

(defsynth pad1 [freq 110 amp 1 gate 1 out-bus 0]
  (out out-bus
       (* (saw [freq (* freq 1.01)])
          (env-gen (adsr 0.01 0.1 0.7 0.5) :gate gate :action FREE))))


;; Of course, we could also build a more sophisticated synth with the
;; same properties:

(defsynth pad2 [freq 440 amp 0.4 amt 0.3 gate 1.0 out-bus 0]
  (let [vel        (+ 0.5 (* 0.5 amp))
        env        (env-gen (adsr 0.01 0.1 0.7 0.5) gate 1 0 1 FREE)
        f-env      (env-gen (perc 1 3))
        src        (saw [freq (* freq 1.01)])
        signal     (rlpf (* 0.3 src)
                         (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)
        k          (/ (* 2 amt) (- 1 amt))
        distort    (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
        gate       (pulse (* 2 (+ 1 (sin-osc:kr 0.05))))
        compressor (compander distort gate 0.01 1 0.5 0.01 0.01)
        dampener   (+ 1 (* 0.5 (sin-osc:kr 0.5)))
        reverb     (free-verb compressor 0.5 0.5 dampener)
        echo       (comb-n reverb 0.4 0.3 0.5)]
    (out out-bus
         (* vel env echo))))

;; We can trigger our synth like this:

(def pad-s (pad2))

;; and kill it by cutting off the gate (thus completing the envelope):

(ctl pad-s :gate 0)

;; We can easily trigger this with our Keyboard

;; Create a map to store the note/proc-number tuples so we can
;; switch off the correct note when a note-off is recieved

(defonce memory (agent {}))

(on-event [:midi :note-on]
          (fn [m]
            (send memory
                  (fn [mem]
                    (let [n (:note m)
                          s (pad2 :freq (midi->hz n))]
                      (assoc mem n s)))))
          ::play-note)

(on-event [:midi :note-off]
          (fn [m]
            (send memory
                  (fn [mem]
                    (let [n (:note m)]
                      (when-let [s (get mem n)]
                        (ctl s :gate 0))
                      (dissoc mem n))))
)
          ::release-note)


;; And again for tidiness:

(remove-event-handler ::play-note)
(remove-event-handler ::release-note)

;; See overtone.examples.midi.keyboard for a built-in way of achieving
;; exactly this behaviour.


;; So, what if you have multiple MIDI devices capable of generating note
;; on and off events? Clearly the approach above won't work. Luckily,
;; we've got you covered.

;; All attached MIDI devices are registered when Overtone booted.
;; You can look at the list of attached devices with:

(midi-connected-devices)

;; The MIDI devices are not guaranteed to be registered in the same
;; order every time you boot Overtone, so you can't rely on the index of
;; the device you want to be consistent. However, you can search for
;; your device usign a string or regexp. For example, the following
;; finds my Korg nanoKEY2 keyboard:

(def nk (midi-find-connected-device "nanoKEY2"))

;; If I happen to have more than one Kort nanoKEY2 keyboard, I can
;; search for them all with:

(def nks (midi-find-connected-devices "nanoKEY2"))

;; Once I have my device, I can find the unique key that Overtone's
;; event system uses to manage events from the MIDI device:

(midi-mk-full-device-key nk)
;;=> [:midi-device "KORG INC." "KEYBOARD" "nanoKEY2 KEYBOARD" 0]

;; our note on and off events are also sent with this unique device
;; id. All you need to do is append either :note-on or :note-off. The
;; following code will only work with a connected nanoKEY2 device:

(on-event (conj (midi-mk-full-device-key nk) :note-on)
          (fn [m]
            (let [note (:note m)]
              (prophet :freq (midi->hz note)
                       :decay 5
                       :rq 0.6
                       :cutoff-freq 1000)))
          ::prophet-midi)

(remove-event-handler ::prophet-midi)

;; In addition to :note-on and :note-off, other available midi events
;; are:
;; * :channel-pressure
;; * :control-change
;; * :pitch-bend
;; * :poly-pressure
;; * :program-change

;; Enjoy playing notes with your MIDI devices!
