(ns examples.midi
  (:use overtone.live))

;; Use (midi-in) with no parameters in the repl to find out the details
;; of your midi keyboard.
;; Try using the contents of :name or :description or a substring of either.

(def kb (midi-in "nanokey"))

;; Define an inst. If you want it to respond to note off messages
;; then ensure it responds to (ctl <proc number> :gate 0)
;; An adsr ugen will do this

(definst pad2 [freq 440 vel 0.4 amt 0.3 gate 1.0]
  (let [vel        (+ 0.5 (* 0.5 vel))
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
    (* vel env echo)))

;; Create a map to store the note/proc-number tuples so we can
;; switch off the correct note when a note-off is recieved

(def mem (atom {}))

(defn midi-responder [e t]
  (case (:cmd e)
        :note-on (let [id (pad2 (midi->hz (:note e)))]
                     (swap! mem assoc (:note e) id))

        :note-off (let [id (get @mem (:note e))]
                    (ctl id :gate 0))))

(midi-handle-events kb #'midi-responder)
