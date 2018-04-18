(ns overtone.examples.midi.keyboard
  (:use overtone.live))

(definst ding
  [note 60 velocity 100]
  (let [freq (midicps note)
        snd  (sin-osc freq)
        env  (env-gen (perc 0.1 0.8) :action FREE)]
    (* velocity env snd)))

(defn midi-player [event]
  (ding (:note event) (/ (:velocity event) 127.0)))

; Calling midi-in without any arguments will bring up a swing dialog to choose
; from available midi devices.  Note that Java does not seem to detect midi devices
; after JVM startup (at least on OSX), so you USB midi device will need to be connected
; before starting Overtone.

;(def keyboard (midi-in))

; The low level midi handler mechanism from midi-clj uses midi-handle-events,
; which takes a device ; and a midi player function that will receive midi
; event maps.

;(midi-handle-events keyboard #'midi-player)

; overtone.studio.midi now includes the beginnings of a higher level midi interface
; that helps improve on this.  By default Overtone will detect and listen to all
; midi input devices.  These midi messages are then sent as events, which can be received
; with the Overtone event system.

; For example, print out all incoming note-on messages:

;(on-event [:midi :note-on] (fn [{note :note velocity :velocity}]
;                             (println "Note: " note ", Velocity: " velocity))
;          ::note-printer)

;(remove-event-handler ::note-printer)

; Other available midi events are:
; * :channel-pressure
; * :control-change
; * :note-off
; * :note-on
; * :pitch-bend
; * :poly-pressure
; * :program-change

; In order to play instruments that continue playing until a key is released,
; we need to keep track of each active synth instance once triggered by :note-on,
; and then send a control message to either kill it or close the gate on an
; envelope so the release starts.  This is what overtone.studio.midi/midi-poly-player
; does for you.  All it requires is that you have exposed an envelope gate as a synth
; parameter called "gate".

(definst poly-ding
  [note 60 amp 1 gate 1]
  (let [freq (midicps note)
        snd  (sin-osc freq)
        env  (env-gen (adsr 0.001 0.1 0.6 0.3) gate :action FREE)]
    (* amp env snd)))

; Create a polyphonic midi player:
;(def ding-player (midi-poly-player poly-ding))

; and stop it:
;(midi-player-stop ding-player)

; Below is a more sophisticated example that demonstrates how to
; control :gate and :sustain parameters, and pitch bend of an instrument based on MIDI
; events. inst-player can be used to handle input from a MIDI keyboard
; and its sustain pedal and pitch bend controller.

; This function transforms pitch-bend midi signal of 14 bits to a
; float between -1.0 and 1.0 where -1.0 means lowest bend,
; 0.0 means no bend, and 1.0 means highest bend.
; See https://www.midikits.net/midi_analyser/pitch_bend.htm for reference.
(defn midi-pitch-bend-to-float [data1 data2]
  (let [; midi two bytes to a number between 0 and 16383,
        ; 0 means lowest bend, 8192 means no bend, 16383 means highest bend
        unsigned (bit-or
                   (-> data2 (bit-flip 6) (bit-shift-left 7))
                   data1)
        ; to a number between -8192 and 8191
        signed (- unsigned 8192)
        ; correcting for having less values (8191) for pitch increase than decrease
        divisor (if (< signed 0) 8192 8191)]
    (float (/ signed divisor))))

(defn inst-player [inst]
  "Handle incoming midi events by playing notes on inst, updating
  the :gate and :sustain parameters of inst based on MIDI
  note-on/note-off and sustain pedal (control-change 64) events
  respectively."
  (let [notes* (atom {
                      ; Notes are active if the key is still being
                      ; pressed.
                      :active {}
                      ; Notes are finished if the key is no longer
                      ; being pressed (but it may still be generating
                      ; sound).
                      :finished {}})
        sustain* (atom 0)
        pitch-bend* (atom 0)
        on-id (keyword (gensym "on-handler"))
        off-id (keyword (gensym "off-handler"))
        cc-id (keyword (gensym "cc-handler"))
        pitch-bend-id (keyword (gensym "pitch-bend-handler"))]

    ; Handle note-on MIDI events.
    (on-event [:midi :note-on]
              (fn [{:keys [note velocity]}]
                (let [amp (float (/ velocity 127))]
                  ; Ignore the event if this note is already active.
                  (when (not (contains? (:active @notes*) note))
                    (let [sound (get-in @notes* [:finished note])]
                      ; If there is a "finished" version of this note,
                      ; set gate and sustain to zero to prevent
                      ; overlapping with the new note.
                      (when (and sound (node-active? sound))
                        (node-control sound [:gate 0 :sustain 0]))
                      ; Create a new note and set it to "active".
                      (swap! notes* assoc-in [:active note]
                             (inst :note note :amp amp :sustain @sustain*))))))
              on-id)

    ; Handle note-off MIDI events.
    (on-event [:midi :note-off]
              (fn [{:keys [note velocity]}]
                (let [velocity (float (/ velocity 127))]
                  (when-let [sound (get-in @notes* [:active note])]
                    ; Set the note's gate to 0, and move it
                    ; from "active" to "finished".
                    (with-inactive-node-modification-error :silent
                      (node-control sound [:gate 0 :after-touch velocity]))
                    (swap! notes* update-in [:active] dissoc note)
                    (swap! notes* assoc-in [:finished note] sound))))
              off-id)

    ; Handle control-change MIDI events.
    (on-event [:midi :control-change]
              (fn [{:keys [note data2]}]
                (case note
                  ; Note 64 = MIDI sustain pedal control-change
                  64 (let [sustain? (>= data2 64)
                           sustain (if sustain? 1 0)]
                       ; Update the sustain atom, and update the
                       ; sustain of all active notes.
                       (reset! sustain* sustain)
                       (if sustain?
                         (doseq [sound (:active @notes*)]
                           (ctl sound :sustain sustain))
                         (ctl inst :sustain sustain)))))
              cc-id)

    ; Handle pitch-bend MIDI events
    (on-event [:midi :pitch-bend]
              (fn [{:keys [data1 data2]}]
                (let [pitch-f (midi-pitch-bend-to-float data1 data2)]
                  ; Update pitch bend, even if no nodes are active.
                  (reset! pitch-bend* (* 2 pitch-f))
                  ; Ignore bending pitch if no nodes are active
                  (when (not (empty? (:active @notes*)))
                    (ctl inst :pitch-bend @pitch-bend*))))
              pitch-bend-id)

    ; Return the ids of the event handlers so they can be stopped
    ; later.
    [on-id off-id cc-id pitch-bend-id]))

(defn stop-inst-player [event-handler-ids]
  "Given a list of event-handler-ids returned by inst-player, remove
  all event handlers."
  (doseq [id event-handler-ids]
    (remove-event-handler id)))

; Create an instrument with a sustain parameter.
(definst sustain-ding
  [note 60 amp 1 gate 1 sustain 0 pitch-bend 0]
  (let [freq (midicps (+ note pitch-bend))
        snd  (sin-osc freq)
        env  (env-gen (adsr 0.001 0.1 0.6 0.3) (or gate sustain) :action FREE)]
    (* amp env snd)))

; Start an instrument player.
;(def sustain-ding-player (inst-player sustain-ding))

; Stop the instrument player.
;(stop-inst-player sustain-ding-player)
