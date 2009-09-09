(ns overtone.midi
  (:import 
     (javax.sound.midi 
       MidiSystem MidiDevice Sequencer Synthesizer 
       Receiver Transmitter Sequence MidiEvent 
       MidiMessage ShortMessage SysexMessage
       InvalidMidiDataException MidiUnavailableException)
     (javax.swing JFrame JScrollPane JList 
                  DefaultListModel ListSelectionModel)
     (java.awt.event MouseAdapter))
  (:use clojure.set))

(defn devices []
  "Get all of the currently available midi devices."
  (for [info (MidiSystem/getMidiDeviceInfo)]
    (let [device (MidiSystem/getMidiDevice info)]
      {:name         (.getName info)
       :description  (.getDescription info)
       :vendor       (.getVendor info)
       :version      (.getVersion info)
       :sources      (.getMaxTransmitters device)
       :sinks        (.getMaxReceivers device)
       :info         info
       :device       device})))

(defn sequencers 
  "Get the available midi sequencers."
  []
  (filter #(instance? Sequencer (:device %1)) (devices)))

(defn synthesizers 
  "Get the available midi synthesizers."
  []
  (filter #(instance? Synthesizer (:device %1)) (devices)))

(defn ports 
  "Get the available midi ports (hardware sound-card and virtual ports)."
  []
  (filter #(and (not (instance? Sequencer   (:device %1)))
                (not (instance? Synthesizer (:device %1))))
          (devices)))

;; NOTE: devices use -1 to signify unlimited sources or sinks

(defn sources []
  "Get the midi input sources."
  (filter #(not (zero? (:sources %1))) (ports)))

(defn sinks 
  "Get the midi output sinks."
  []
  (filter #(not (zero? (:sinks %1))) (ports)))

(defn- list-model [items]
  (let [model (DefaultListModel.)]
    (doseq [item items]
      (.addElement model item))
    model))

(defn port-chooser [ports handler]
  (let [frame   (JFrame. "Midi Port Chooser")
        model   (list-model (for [port ports] 
                              (str (:name port) " - " (:description port))))
        options (JList. model)
        pane    (JScrollPane. options)
        listener (proxy [MouseAdapter] []
                   (mouseClicked 
                     [event] 
                     (println "clicked: " event)
                     (if (= (.getClickCount event) 2)
                       (.setVisible frame false)
                       (dosync (ref-set *midi-out-port* 
                                        (nth ports (.getSelectedIndex options)))))))]
    (doto options
      (.addMouseListener listener)
      (.setSelectionMode ListSelectionModel/SINGLE_SELECTION))
    (doto frame 
      (.add pane) 
      (.pack) 
      (.setSize 400 600)
      (.setVisible true))))

(defn sink-chooser [handler]
  (port-chooser (sinks) handler))

(defn source-chooser [handler]
  (port-chooser (sources) handler))

(def *midi-in* (ref nil))
(def *midi-out* (ref nil))
(def *sequencer* (ref (MidiSystem/getSequencer)))
(.open @*sequencer*)

(def connect-sink
  "Connect the sequencer to the midi-out device."
  []
  (.setReceiver (.getTransmitter @*sequencer*) (receiver *midi-out*)))

(def connect-source
  "Connect the sequencer to the midi-in device."
  []
  ())

(defn- receiver 
  [device]
  (.open device)
  (.getReceiver device))

(defn sequencer 
  "Setup and start a sequencer with a midi device connected."
  []
  (sink-chooser (fn [] 
                    seqr)))

(defn midi-out []
  (sink-chooser (sinks) connect-sink))

(defn midi-out []
  (sink-chooser (sinks) connect-sink))

(defn start [seqr]
  (.start seqr))

(defn stop [seqr]
  (.stop seqr))

(defn note [out note vel dur]
  (let [on-msg (ShortMessage.)
        off-msg (ShortMessage.)]
    (.setMessage on-msg ShortMessage/NOTE_ON 0 note vel)
    (.setMessage off-msg ShortMessage/NOTE_OFF 0 note 0)
    (.send out on-msg -1)
    (Thread/sleep dur)
    (.send out off-msg -1)))

(defn play [out notes velocities durations]
  (loop [notes notes
         velocities velocities
         durations durations]
    (if notes
      (let [n (first notes)
            v (first velocities)
            d (first durations)]
        (note out n v d)
        (recur (next notes) (next velocities) (next durations))))))

; MIDI message constants
(def NOTE-ON 144)
(def NOTE-OFF 128)


; 10 pulses per quarter note
;(def session-seq (new Sequence Sequence/PPQ 10))
;(def track-0 (.createTrack session-seq))
;
;(.setSequence sequencer session-seq)
;

;(defn seqr-note [seqr time note vel dur]
;  (let [on-msg (ShortMessage.)
;        off-msg (ShortMessage.)]
;    (.setMessage on-msg NOTE-ON 0 note vel)
;    (.setMessage off-msg NOTE-OFF 0 note 0)
;    (.add track-0 (MidiEvent. on-msg time))
;    (.add track-0 (MidiEvent. off-msg (+ time dur))))
;  (if (not (.isRunning seqr))
;    (start seqr)))

;(def synth (MidiSystem/getSynthesizer))
;(.open synth)
;
;(def soundbank (.getDefaultSoundbank synth))
;(def instruments (.getInstruments soundbank))
;(.loadInstrument synth (first instruments))
;
;(def midi-channels (.getChannels synth))
;(def chan-data (ChannelData.))

; Enable recording on a track
; (.recordEnable track-1)

; Time check
(defn now [seqr]
  (.getTickPosition seqr))
; (.getMicrosecondPosition))

(defn set-bpm [seqr bpm]
  (.setTempoInBPM seqr bpm))

; public void setTempoInMPQ(float mpq) (microseconds per quarter)
; public void setTempoFactor(float factor)

(defn track-mute [seqr]
  (.setTrackMute seqr 0 true))

(defn track-solo [seqr t on-off]
  (.setTrackSolo seqr t on-off))

;(play-note-seq (now) 45 120 20)
;(play-note-seq (+ 10 (now)) 47 120 20)
;(play-note-seq (+ 100 (now)) 45 120 20)

;(play-note-external 45 100 1000)

;(defn play-note-synth [note vel dur]
;  (let [channels (.getChannels synth)
;        chan (first channels)]
;    (.noteOn chan note vel)
;    (Thread/sleep dur)
;    (.noteOff chan note vel)))
;
;(play-note-synth 45 100 1000)
