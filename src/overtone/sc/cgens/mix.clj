(ns overtone.sc.cgens.mix
  (:use [overtone.sc defcgen ugens]
        [overtone.helpers lib]
        [overtone.helpers stereo]))

(defcgen sum
  "sum a list of input channels into a single channel."
  [ins {:default [] :doc "list of input channels to sum"}]
  "Sum the list of input channels by summing them together. Be careful
   about summing too many channels together as the resulting signal will
   be progressively amplified."
  (:ar (apply + ins)))

(defcgen mix
  "Mix a list of input channels into a single channel."
  [ins {:default [] :doc "list of input channels to mix"}]
  "Mix the list of input channels by summing them together and dividing
   by the number of input signals. See sum if you wish to just add the
   signals together."
  (:ar (* (apply + ins) (/ 1 (count ins)))))

(defcgen splay
  "Spread input channels across a stereo field"
  [in-array    {:default [] :doc "List of input channels to splay."}
   spread      {:default 1 :doc "The audio spread width."}
   level       {:default 1 :doc "Ampilitude level of each individual spread channel (only used if level-comp is false)."}
   center      {:default 0 :doc "Center point of audio spread."}
   level-comp? {:default true :doc "Boolean switch to determine whether automatic level compensation should be used."}]
  "Spread input channels across a stereo field, with control over the center point
  and spread width of the target field, and level compensation that lowers the volume
  for each additional input channel."
  (:ar (let [n         (count in-array)
             level     (if level-comp?
                         (* level (Math/sqrt (/ 1 (dec n))))
                         level)
             positions (for [i (range n)]
                         (+ center
                            (* spread
                               (- (* i
                                     (/ 2 (dec n)))
                                  1))))
             pans      (pan2 in-array positions level)]
         (mix pans))))
