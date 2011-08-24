(ns overtone.sc.examples.compander
  (:use [overtone.sc.ugen]
        [overtone.sc.ugen constants]
        [overtone.sc.cgen.audio-in]
        [overtone.sc.example]))

(defexamples amplitude
  (:saw-vol
   "Use input amplitude to control saw amplitude"
   "Here we have a basic multi-channel expanded saw wave (220hz in the left ear and 110hz in the right). We modulate its amplitude based on the output of the amplitude ugen which reads from sound-in (which defaults to the first mic). Make a noise (i.e. clap your hands) near the mic to hear the saw wave."
   rate :ar
   [attack {:default 0.01 :doc "Convergence time for following attacks. Increasing this should make it appear less sensitive to trigger."}
    release {:default 0.01 :doc "Convergence time for following decays. Increasing this should make it take longer to reset (i.e. noise will have a longer effect). "}]
   "
   (let [amp (amplitude (sound-in) attack release)]
     (* (saw [220 110]) amp))"
   contributed-by "Sam Aaron")
  (:sin-freq
   "Use input amplitude to control saw frequency"
   "Here we have a basic multi-channel expanded sine wave (110hz in the left ear and 55hz in the right). We modulate its frequency based on the output of the amplitude ugen which reads from sound-in (which defaults to the first mic). Make a noise (i.e. clap your hands) near the mic to change the pitch of the sine wave."
   rate :ar
   [attack {:default 0.01 :doc "Convergence time for following attacks. Increasing this should make it appear less sensitive to trigger."}
    release {:default 0.01 :doc "Convergence time for following decays. Increasing this should make it take longer to reset (i.e. noise will have a longer effect). "}]
   "
   (let [amp      (amplitude (sound-in) attack release)
         freq-mul (* 4 (+ 1 amp))]
     (* 0.5 (sin-osc (* [110 55] freq-mul)))))"
   contributed-by "Sam Aaron"))

(defexamples compander
  (:noise-gate
   "Use compander to create a noise gate"
   "Here we have a fairly interesting original sound which we pass through a noise gate created by a compander. This is created by having a steep slope-below curve which cuts off quiet input signals. The slope above the threshold is 1 which means that when the input signal is above the threshold, it isn't modified.

  Move the mouse from left to right to increase the threshold. Notice how more of the sound is affected the further right you go.

  Move the mouse from up to down to increase the slope below the threshold. Notice how the modification of the sound is increased the further down you go.

  Also, try setting mix-val to -1 to hear the original sound unmodified."
   rate :ar
   [mix-val {:default 1 :doc "Amount of original sound to play back. 1 means no original sound (and therefore all noise-gate sound) -1 means all original sound (and no noise gate). "}]
   "
   (let [orig-sound   (* (decay2:kr (* (impulse:kr 8 0)
                                     (+ 0.3 (* -0.3 (lf-saw:kr 0.3))))
                                  0.001
                                  0.3)
                       (mix (pulse [80 81] 0.3)))
         thresh-val (mouse-x 0.1 0.5)
         sb-val     (mouse-y 1 10)
         noise-gate (compander :in orig-sound
                               :control orig-sound
                               :thresh thresh-val
                               :slope-below (mouse-y 1 10)
                               :slope-above 1
                               :clamp-time 0.01
                               :relax-time 0.1)
         trig       (impulse:kr 5)
         poll-x (poll trig thresh-val \"thresh\")
         poll-y (poll trig sb-val \"slope-below\")

         mixed (x-fade2 orig-sound noise-gate mix-val)]
       [mixed mixed])"))
