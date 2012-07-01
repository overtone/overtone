;; Demo of find-chord
;; find-chord will identify chords from a set of midi notes. To see it in action compile this file.
;; You will probably need to modify it to read notes from your keyboard.
;; Change this line :
;;
;; (def kb (midi-in "nanoKEY"))
;;
;; to match your keyboard type. You can find the necessary changes by typing (midi-in) in the repl.
;; You can see the output by monitoring the logfile using this command:
;;
;; tail -f  ~/.overtone/log/log.log


(ns pitch.chord
  (:use [overtone.live]
        [overtone.config.log]))

(definst beep [note 60]
  (let [src (sin-osc (midicps note))
        env (env-gen (perc 0.1 0.2) :action FREE)]
    (* src env)))

(beep 86)

(def current-notes (ref #{}))

(defn add-to-current-notes
  [new-note]
  (dosync (ref-set current-notes (set (cons new-note @current-notes)))))

(defn remove-from-current-notes
  [old-note]
  (dosync (ref-set current-notes (set (disj @current-notes old-note)))))

(def kb (midi-in "nanoKEY"))

(defn midi-player [event ts]
  (do (info event)
      (cond
       (= :note-on (:cmd event))
       (do (beep (:note event))
           (add-to-current-notes (:note event)))
       (= :note-off (:cmd event))
       (remove-from-current-notes (:note event)))
      (info "chord "
            (find-chord @current-notes))))

(midi-handle-events kb #'midi-player)
