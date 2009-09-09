(ns overtone.scratchy
  (:use (overtone osc)))

(def SCRATCHY-PORT 4241)

(def *id-counter* (ref 0))
(def *client*     (ref nil))
(def *samples*    (ref {}))

(def SAMPLES 
  {:drums "/home/rosejn/projects/overtone/instruments/samples/kit/BOOM.aif"})

(defn- next-id []
  (dosync (alter *id-counter* inc)))

(defn reset-clock []
  (osc-snd @*client* (osc-msg "/setclock")))

(defn client [c]
  (dosync (ref-set *client* c))
  (reset-clock)
  c)

(defn connect 
  ([] (connect "localhost" SCRATCHY-PORT))
  ([port] (connect "localhost" port))
  ([host port] 
   (client (osc-client host port))))

(defn debug [& [on-off]]
  (let [on-off (if (nil? on-off) true on-off)
        on-off (if on-off 1 0)]
    (osc-snd @*client* (osc-msg "/debug" on-off))))

(defn load-samples 
  "Takes a map of name to filename pairs, and loads each sample which can later
  be referred to using the name."
  [samples]
  (doseq [[name filename] samples]
    (let [id (next-id)]
      (osc-snd @*client* (osc-msg "/addtoqueue" id filename))
      (dosync (alter *samples* assoc name id))))
  (osc-snd @*client* (osc-msg "/loadqueue")))

(defn play
  "Play a note at the frequency of hz."
  [name hz]
  (let [id (get @*samples* name)
        hz (float hz)]
    (osc-snd @*client* (osc-msg "/play" 0 0 id hz 0.0 1.0 0.0 79))))

(defn clear
  "Clear all samples."
  [] 
  (dosync (ref-set *id-counter* 0))
  (osc-snd @*client* (osc-msg "/unmapall"))
  (osc-snd @*client* (osc-msg "/unloadall")))
