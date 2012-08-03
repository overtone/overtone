(ns overtone.speech
  (:use
    [overtone.sc.sample :only (sample)]
    [overtone.config.store :only (OVERTONE-DIRS)]
    [clojure.java.shell :only (sh)]
    [overtone.helpers.system :only (get-os)]
    [overtone.helpers.lib :only (uuid)]))

(def ^:private SPEECH-DIR (:speech OVERTONE-DIRS))

(def ^:private VOICES
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

(defmulti ^:private say (fn [& args] (get-os)))

(defmethod ^:private say :mac
  [text {:keys [voice]
         :or {voice :vicki}
         :as options}]
  (let [voice (get VOICES voice)
        out-file (str SPEECH-DIR "/" (uuid) ".aiff")]
    (sh "say" "-v" voice "-o" out-file text)
    out-file))

(defn speech-buffer
  "Takes a string and returns an audio buffer with containing that text
  read by one of the available voices defaulting to vicki.

  (speech-buffer \"a nice cup of tea, please\" :voice :alex)

  Voices to choose from are
  :agnes, :kathy, :princess, :vicki, :victoria, :bruce, :fred, :junior,
  :ralph, :alex, :albert, :zarvox, :trinoids, :whisper, :bahh, :boing,
  :bubbles, :bad-news, :good-news, :deranged, :hysterical, :bells,
  :cellos, :pipe-organ.

  Currently available on OS X only."
  [text & {:as options}]
  (let [file (say text options)]
    (sample file)))
