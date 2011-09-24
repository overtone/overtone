(ns examples.bassline
  (:use overtone.core))

(definst z []
  (let [snd (saw 110)]
    (* 0.2 snd)))

(definst z [freq 110 amp 0.2]
  (let [snd (saw freq)]
    (* amp snd)))

(definst z [note 45 amp 0.2]
  (let [freq (midicps note)
        snd (saw freq)]
    (* amp snd)))

(definst z [note 45 amp 0.2]
  (let [freq (midicps note)
        snd (saw [freq (* 0.987 freq) (* 2.013 freq)])]
    (* amp snd)))


(z)
(stop)

(definst grunge-bass [freq 120 a 0.1 d 0.01 s 0.4 r 0.4 amp 0.8 gate 1]
  (let [env (env-gen (adsr a d s r) gate :action :free)
        src (saw [freq (* 0.98 freq) (* 2.015 freq)])
        src (clip2 (* 1.3 src) 0.9)
        sub (sin-osc (/ freq 2))
        filt (resonz (rlpf src (* 8.4 freq) 0.29) (* 2.0 freq) 2.9)
        meat (ring4 filt sub)
        sliced (rlpf meat (* 2 freq) 0.1)
        bounced (free-verb sliced 0.8 0.9 0.2)]
    (* env bounced)))
