(ns overtone.music.pitch-macros)

(defmacro defratio [rname ratio]
  `(defn ~rname [freq#] (* freq# ~ratio)))
