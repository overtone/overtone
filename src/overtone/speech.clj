(ns overtone.speech
  (:use
    [overtone.sc.sample :only (sample)]
    [overtone.config.store :only (OVERTONE-DIRS)]
    [clojure.java.shell :only (sh)]
    [overtone.helpers.system :only (get-os)]
    [overtone.util.lib :only (uuid)]))

(def SPEECH-DIR (:speech OVERTONE-DIRS))

(def VOICES
  {:agnes "Agnes"
   :kathy "Kathy"
   :princess "Princess"
   :vicki "Vicki"
   :victoria "Victoria"

   :bruce "Bruce"
   :fred "Fred"
   :junior "Junior"
   :ralph "Ralph"
   :alex "Alex" ; probably most realistic sounding

   :albert "Albert"
   :zarvox "Zarvox"
   :trinoids "Trinoids"
   :whisper "Whisper"

   :bahh "Bahh"
   :boing "Boing"
   :bubbles "Bubbles"
   :bad-news "Bad News"
   :good-news "Good News"
   :deranged "Deranged"
   :hysterical "Hysterical"

   :bells "Bells"
   :cellos "Cellos"
   :pipe-organ "Pipe Organ"        ; awesome ;-)
   })


(defmulti say (fn [& args] (get-os)))

(defmethod say :mac
  [text {:keys [voice]
         :or {voice :vicki}
         :as options}]
  (let [voice (get VOICES voice)
        out-file (str SPEECH-DIR "/" (uuid) ".aiff")]
    (sh "say" "-v" voice "-o" out-file text)
    out-file))

(defn speech-buffer
  [text & {:as options}]
  (let [file (say text options)]
    (sample file)))

