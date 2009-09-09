(ns overtone.itchy
  (:use (overtone osc)))

(def ITCHY-PORT 4242)

(def SUB  "sub")  ; subtractive moog style synth - 2 oscillators 
(def ADD  "add")  ; additive harmonic synth - 8 sine oscillators 
(def DIRT "dirt") ; distorted synth - 2 oscillators 
(def FM   "fm")   ; fm synth with effects - 2 oscillators 
(def DRUM "drum") ; drum synth

(def *id-counter* (ref 0))
(def *client*     (ref nil))

(defn- next-id []
  (dosync (alter *id-counter* inc)))

(defn reset-clock []
  (osc-snd @*client* (osc-msg "/setclock")))

(defn client [c]
  (dosync (ref-set *client* c))
  (reset-clock)
  c)

(defn connect 
  ([] (connect "localhost" ITCHY-PORT))
  ([port] (connect "localhost" port))
  ([host port] 
   (client (osc-client host port))))

(defn debug [& [on-off]]
  (let [on-off (if (nil? on-off) true on-off)
        on-off (if on-off 1 0)]
    (osc-snd @*client* (osc-msg "/debug" on-off))))

(defn add-instrument [id ins]
  (osc-snd @*client* (osc-msg "/addinstrument" id ins)))

(defn instrument 
  "Returns the ID of a newly allocated instrument of type ins."
  [ins]
  (let [id (next-id)]
    (add-instrument id ins)
    id))

(defn reset 
  "Reset an instrument to default settings."
  [id]
  (osc-snd @*client* (osc-msg "/reset" id)))

(defn- kstr 
  "Convert a keyword into a string without the colon."
  [k]
  (if (keyword? k) 
    (.substring (str k) 1)
    k))

(defn modify
  "Generic modify mechanism to setup instrument parameters."
  [id params]
  (let [params (for [[k v] params] [(kstr k) (float v)])
        params (apply concat params)]
    (println "modify params: " params)
  (osc-snd @*client* (apply osc-msg "/modify" id params))))

(defn voices
  "Set the number of polyphonic voices."
  [id num-voices]
  (modify id {:poly num-voices}))

(defn pan 
  "Set the left-right panning."
  [id p]
  (modify id {:pan p}))

(defn feedback
  "Set instrument feedback strength (1.0 is max) and delay time. (1.0 = 1 sec)"
  [id fb delay-time]
  (modify id {:delayfb fb :delay delay-time}))

(defn distortion
  "Set instrument distortion level"
  [id d]
  (modify id {:distort d}))

(defn note 
  "Play a note at the frequency of hz."
  [id hz]
  (let [hz (float hz)]
    (osc-snd @*client* (osc-msg "/play" 0 0 id hz hz 1.0 0.0 79))))

(defn volume 
  ([vol] ; Set the global volume
   (osc-snd @*client* (osc-msg "/globalvolume" (float vol))))
  ([id vol] ; Set per-instrument volume
   (modify id {:mainvolume vol})))

(defn clear
  "Clear all instruments."
  [] 
  (dosync (ref-set *id-counter* 0))
  (osc-snd @*client* (osc-msg "/clear")))

