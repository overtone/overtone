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
