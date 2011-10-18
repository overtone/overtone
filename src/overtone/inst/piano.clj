(ns overtone.inst.piano
  (:use [overtone.sc gens envelope]
        [overtone.studio rig]))

;; IMPORTANT: requires the mda-piano ugen to be available on your system

(definst piano [note 60
                gate 1
                vel 100
                decay 0.8
                release 0.8
                hard 0.8
                velhard 0.8
                muffle 0.8
                velmuff 0.8
                velcurve 0.8
                stereo 0.2
                tune 0.5
                random 0.1
                stretch 0.1
                sustain 0.1]
  (let [snd (mda-piano {:freq (midicps note)
                        :gate gate
                        :vel vel
                        :decay decay
                        :release release
                        :hard hard
                        :velhard velhard
                        :muffle muffle
                        :velmuff velmuff
                        :velcurve velcurve
                        :stereo stereo
                        :tune tune
                        :random random
                        :stretch stretch
                        :sustain sustain})]
    (detect-silence snd 0.005 :action FREE)
    (* 1 snd))) ;;TODO: figure out why this mul is required
