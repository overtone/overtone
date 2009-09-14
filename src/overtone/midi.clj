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
     (java.util.concurrent FutureTask)
  (:use clojure.set
     (overtone music)))

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
  [ports]
  (let [frame   (JFrame. "Midi Port Chooser")
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

(defn- receiver [sink-info]
  (let [dev (:device sink-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (.getReceiver dev)))

(defn- transmitter [source-info]
  (let [dev (:device source-info)]
    (if (not (.isOpen dev))
      (.open dev))
    (.getTransmitter dev)))

; TODO: Make midi-in and midi-out synchronous when called with no arguments...
(defn midi-in 
  "Connect the sequencer to a midi input device."
  ([] (transmitter
        (.get (port-chooser (sources))))

  ([in] 
   (let [source (cond
                  (string? in) (find-device (sources) in)
                  (device? in) in)]
     (if source
       (transmitter source)
       (do 
         (println "Did not find a matching midi input device for: " in-name)
         nil)))))

(defn midi-out 
  "Connect the sequencer to a midi output device."
  ([] (receiver 
        (.get (port-chooser (sinks)))))

  ([out] (let [sink (cond
                      (string? out) (find-device (sinks) out)
                      (device? out) out)]
                (if sink
                  (receiver sink)
                  (do 
                    (println "Did not find a matching midi output device for: " out-name)
                    nil)))))

(defn midi-route 
  "Route midi messages from a source to a sink.  Expects transmitter and receiver objects
  returned from midi-in and midi-out."
  [source sink]
  (.setReceiver source sink))

(defn midi-msg [msg]
  (if (instance? ShortMessage msg)
    (let [cmd (.getCommand msg)
          data {:note (.getData1 msg)
                :velocity (.getData2 msg)}]
          (cond 
            (= 0x80 cmd) (assoc data :cmd :off)
            (= 0x90 cmd) (assoc data :cmd :on)
            true nil))))

(defn midi-handler [fun]
  (proxy [Receiver] []
    (close [] nil)
    (send [msg timestamp] 
          (if-let [parsed (midi-msg msg)]
            (fun parsed)))))

(defn midi-note-on [recvr note-num vel]
  (let [on-msg  (ShortMessage.)]
    (.setMessage on-msg ShortMessage/NOTE_ON 0 note-num vel)
    (.send recvr on-msg -1)))

(defn midi-note-off [recvr note-num vel]
  (let [off-msg (ShortMessage.)]
    (.setMessage off-msg ShortMessage/NOTE_OFF 0 note-num 0)
    (.send recvr off-msg -1)))

(defn midi-note [recvr note-num vel dur]
  (midi-note-on recvr note-num vel)
  (schedule #(midi-note-off recvr note-num 0) dur))

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

