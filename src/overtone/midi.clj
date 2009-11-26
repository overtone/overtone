(ns overtone.midi
  (:import 
     (java.util.regex Pattern)
     (javax.sound.midi Sequencer Synthesizer
                       MidiSystem MidiDevice Receiver Transmitter MidiEvent 
                       MidiMessage ShortMessage SysexMessage
                       InvalidMidiDataException MidiUnavailableException)
     (javax.swing JFrame JScrollPane JList 
                  DefaultListModel ListSelectionModel)
     (java.awt.event MouseAdapter)
     (java.util.concurrent FutureTask))
  (:use clojure.set
     (overtone time)))

; This is basically a higher-level wrapper on top of the Java MIDI apis.  It makes it
; easier to configure midi input/output devices, route between devices, etc.

;; NOTE:
;; * The builtin "real-time" sequencer doesn't support modifying the sequence on-the-fly, so
;; don't waste any more time messing with javax.sound.midi.Sequencer.

;; TODO
;; * figure out how to implement an arpeggiator that captures midi notes and then fills in
;;   or uses the chord played to generate new stuff.

(defn devices []
  "Get all of the currently available midi devices."
  (for [info (MidiSystem/getMidiDeviceInfo)]
    (let [device (MidiSystem/getMidiDevice info)]
      {:type         :device
       :name         (.getName info)
       :description  (.getDescription info)
       :vendor       (.getVendor info)
       :version      (.getVersion info)
       :sources      (.getMaxTransmitters device)
       :sinks        (.getMaxReceivers device)
       :info         info
       :device       device})))

(defn device? [obj]
  (and (map? obj) (= :device (:type obj))))

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

(defn find-device 
  "Takes a set of devices returned from either (sources) or (sinks), and a
  search string.  Returns the first device where either the name or description
  mathes using the search string as a regexp."
  [devs dev-name]
  (first (filter 
           #(let [pat (Pattern/compile dev-name Pattern/CASE_INSENSITIVE)]
              (or (re-find pat (:name %1)) 
                  (re-find pat (:description %1))))
           devs)))

(defn- list-model [items]
  (let [model (DefaultListModel.)]
    (doseq [item items]
      (.addElement model item))
    model))

(defn port-chooser 
  "Brings up a GUI list of the provided ports and then calls handler with the port
  that was double clicked."
  [title ports]
  (let [frame   (JFrame. title)
        model   (list-model (for [port ports] 
                              (str (:name port) " - " (:description port))))
        options (JList. model)
        pane    (JScrollPane. options)
        future-val (FutureTask. #(nth ports (.getSelectedIndex options)))
        listener (proxy [MouseAdapter] []
                   (mouseClicked 
                     [event] 
                     (if (= (.getClickCount event) 2)
                       (.setVisible frame false)
                       (.run future-val))))]
    (doto options
      (.addMouseListener listener)
      (.setSelectionMode ListSelectionModel/SINGLE_SELECTION))
    (doto frame 
      (.add pane) 
      (.pack) 
      (.setSize 400 600)
      (.setVisible true))
    future-val))

(defn- with-receiver [sink-info]
  (let [dev (:device sink-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc sink-info :receiver (.getReceiver dev))))

(defn- with-transmitter [source-info]
  (let [dev (:device source-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (assoc source-info :transmitter (.getTransmitter dev))))

; TODO: Make midi-in and midi-out synchronous when called with no arguments...
(defn midi-in 
  "Connect the sequencer to a midi input device."
  ([] (with-transmitter
        (.get (port-chooser "Midi Input Selector" (sources)))))

  ([in] 
   (let [source (cond
                  (string? in) (find-device (sources) in)
                  (device? in) in)]
     (if source
       (with-transmitter source)
       (do 
         (println "Did not find a matching midi input device for: " in)
         nil)))))

(defn midi-out 
  "Connect the sequencer to a midi output device."
  ([] (with-receiver 
        (.get (port-chooser "Midi Output Selector" (sinks)))))

  ([out] (let [sink (cond
                      (string? out) (find-device (sinks) out)
                      (device? out) out)]
           (if sink
             (with-receiver sink)
             (do 
               (println "Did not find a matching midi output device for: " out)
               nil)))))

(defn midi-route 
  "Route midi messages from a source to a sink.  Expects transmitter and receiver objects
  returned from midi-in and midi-out."
  [source sink]
  (.setReceiver (:transmitter source) (:receiver sink)))

(defn midi-msg [msg]
  (if (instance? ShortMessage msg)
    (let [cmd (.getCommand msg)
          data {:note (.getData1 msg)
                :velocity (.getData2 msg)}]
      (cond 
        (= 0x80 cmd) (assoc data :cmd :off)
        (= 0x90 cmd) (assoc data :cmd :on)
        true nil))))

;; Implementing a midi receiver object so we can take in notes from another
;; midi source, modify them on the fly, and then output them.  Have to write
;; write a more complete midi parser though...
(defn midi-handler [fun]
  (proxy [Receiver] []
    (close [] nil)
    (send [msg timestamp] 
          (if-let [parsed (midi-msg msg)]
            (fun parsed)))))

;; Unfortunately, it seems that either Pianoteq or the virmidi modules
;; don't actually make use of the timestamp...
(defn midi-note-on [sink note-num vel & [timestamp]] 
  (let [on-msg  (ShortMessage.)]
    (.setMessage on-msg ShortMessage/NOTE_ON 0 note-num vel)
    (.send (:receiver sink) on-msg -1)))

;(defn midi-note-on [sink note-num vel & [timestamp]] 
;  (let [timestamp (or timestamp 0)
;        on-msg  (ShortMessage.)
;        micro-delay (* 1000 (- timestamp (now)))
;        t (+ micro-delay (.getMicrosecondPosition (:device sink)))]
;    (.setMessage on-msg ShortMessage/NOTE_ON 0 note-num vel)
;    (if (neg? micro-delay)
;      (.send (:receiver sink) on-msg -1)
;      (.send (:receiver sink) on-msg t))))

(defn midi-note-off [sink note-num vel]
  (let [off-msg (ShortMessage.)]
    (.setMessage off-msg ShortMessage/NOTE_OFF 0 note-num 0)
    (.send (:receiver sink) off-msg -1)))

(defn midi-note [sink note-num vel dur]
  (midi-note-on sink note-num vel)
  (schedule #(midi-note-off sink note-num 0) dur))

(defn midi-play [out notes velocities durations]
  (loop [notes notes
         velocities velocities
         durations durations
         cur-time  0]
    (if notes
      (let [n (first notes)
            v (first velocities)
            d (first durations)]
        (schedule #(midi-note out n v d) cur-time)
        (recur (next notes) (next velocities) (next durations) (+ cur-time d))))))

