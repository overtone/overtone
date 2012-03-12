(ns overtone.sc.cgens.mix
  (:use [overtone.sc defcgen ugens]
        [overtone.util lib]
        [overtone.helpers stereo]))

(defcgen mix
  "Mix down (sum) a list of input channels into a single channel."
  [ins {:default [] :doc "list of input channels to mix"}]
  "Mixes down the list of input channels by summing them together. Be careful about mixing too many channels together as the resulting signal will be progressively amplified."
  (:ar (apply + ins)))

(defcgen splay
  "Spread input channels across a stereo field"
  [in-array {:default [] :doc "List of input channels to splay."}
   spread {:default 1 :doc "The audio spread width."}
   level {:default 1 :doc "Ampilitude level of each individual spread channel (only used if level-comp is false)."}
   center {:default 0 :doc "Center point of audio spread."}
   level-comp? {:default true :doc "Boolean switch to determine whether automatic level compensation should be used."}]
  "Spread input channels across a stereo field, with control over the center point
  and spread width of the target field, and level compensation that lowers the volume
  for each additional input channel."
  (:ar (let [n         (count in-array)
             level     (if level-comp?
                         (* level (Math/sqrt (/ 1 (dec n))))
                         level)
             positions (splay-pan n center spread)
             pans      (pan2 in-array positions level)]
         (mix pans))))
