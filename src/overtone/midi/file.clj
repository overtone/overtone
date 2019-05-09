(ns overtone.midi.file
  (:import java.io.File
           java.net.URL
           [javax.sound.midi MidiSystem MidiFileFormat Sequence Track
            MetaMessage ShortMessage])
  (:use [overtone.midi :only (midi-msg)]))

(defn- midi-division-type
  [info]
  (case (.getDivisionType info)
    Sequence/PPQ          :ppq
    Sequence/SMPTE_24     :smpte-24fps
    Sequence/SMPTE_25     :smpte-25fps
    Sequence/SMPTE_30DROP :smpte-30drop
    Sequence/SMPTE_30     :smpte-30fps
    :unknown))

                                        ; TODO: Figure out how to detect the strange end-of-track msg
                                        ; TODO: Find better documentation for the meta messages so we can
                                        ; either make sense of them or disregard them if unimportant.
(defn- midi-event
  [event]
  (let [msg (.getMessage event)
        msg (cond
              (= (type msg) MetaMessage) {:type :meta-message}
              (instance? ShortMessage msg) (midi-msg msg)
              :default {:type :end-of-track})]
    (assoc msg :timestamp (.getTick event))))

(defn- midi-track
  [track]
  (let [size (.size track)]
    {:type :midi-track
     :size size
     :events (for [i (range size)] (midi-event (.get track i)))}))

(defn midi-sequence
  [src]
  (let [mseq (MidiSystem/getSequence src)
        tracks (.getTracks mseq)]
    {:type :midi-sequence
     :tracks (map midi-track tracks)}))

(defn midi-info
  [src]
  (let [info (MidiSystem/getMidiFileFormat src)
        div-type (midi-division-type info)
        res-type (if (= div-type :ppq)
                   " ticks per beat"
                   " ticks per frame")
        resolution (str (.getResolution info) res-type)
        mseq       (MidiSystem/getSequence src)
        usecs      (.getMicrosecondLength info)
        props      (into {} (.properties info))
        midi-seq   (midi-sequence src)]
    {:type :midi-sequence
     :division-type div-type
     :resolution    resolution
     :sequence      mseq
     :usecs         usecs
     :properties props}))

(defn- midi-src
  [src]
  (merge
   (midi-info src)
   (midi-sequence src)))

(defn midi-file
  [path]
  (let [f (File. path)]
    (midi-src f)))

(defn midi-url
  [url]
  (let [src (URL. url)]
    (midi-src src)))
