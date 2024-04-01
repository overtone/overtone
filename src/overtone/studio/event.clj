(ns overtone.studio.event
  (:require
   [overtone.studio.pattern :as pattern]
   [overtone.libs.event :as event]
   [overtone.music.pitch :as pitch]
   [overtone.music.time :as time]
   [overtone.sc.node :as node]
   [overtone.sc.server :as server]
   [overtone.studio.transport :as transport]))

(defonce pplayers (atom {}))

(def defaults
  {:note
   {:type             :note
    :mtranspose       0
    :gtranspose       0.0
    :ctranspose       0.0
    :octave           5.0
    :root             0.0
    :degree           0
    :scale            :major
    :steps-per-octave 12.0
    :detune           0.0
    :harmonic         1.0
    :octave-ratio     2.0
    :dur              1}

   :chord
   {:type             :chord
    :mtranspose       0
    :gtranspose       0.0
    :ctranspose       0.0
    :octave           5.0
    :root             0.0
    :degree           0
    :scale            :major
    :chord            :major
    :inversion        0
    :steps-per-octave 12.0
    :detune           0.0
    :harmonic         1.0
    :octave-ratio     2.0
    :dur              1
    }})

(declare derivations)

(defn eget [e k]
  (if (contains? e k)
    (get e k)
    (let [t (:type e :note)
          d (get defaults t)]
      (cond
        (contains? d k)
        (get d k)
        (contains? derivations k)
        ((get derivations k) e)
        :else
        (throw (ex-info (str "Missing event key or derivation " k)
                        {:event e}))))))

(def derivations
  {:detuned-freq
   (fn [e]
     (+ (eget e :freq) (eget e :detune)))

   :freq
   (fn [e]
     (let [midinote (eget e :midinote)]
       (if (keyword? midinote)
         midinote
         (* (eget e :harmonic)
            (pitch/midi->hz
             (+ midinote (eget e :ctranspose)))))))

   :midinote
   (fn [e]
     (let [note (eget e :note)]
       (if (keyword? note)
         note
         (+ 60
            (*
             (+ (eget e :octave) (- 5)
                (/ (+ note
                      (eget e :gtranspose)
                      (eget e :root))
                   (eget e :steps-per-octave)))
             12 (/ (Math/log (eget e :octave-ratio))
                   (Math/log 2)))))))

   :scale-intervals
   (fn [e]
     (get pitch/SCALE (eget e :scale)))

   :scale-notes
   (fn [e]
     (butlast (reductions + 0 (eget e :scale-intervals))))

   :note
   (fn [e]
     (let [degree (eget e :degree)]
       (if (keyword? degree)
         degree
         (let [scale (eget e :scale-notes)
               size  (count scale)
               degree (+ degree
                         (eget e :mtranspose))]
           ;; Not too sure about this... would be good to compare results with SC
           (+ (nth scale (mod degree size))
              (* (eget e :steps-per-octave)
                 (cond-> (quot degree size)
                   (< degree 0)
                   dec)))))))

   :clock
   (fn [e]
     transport/*clock*)

   :beat
   (fn [e]
     ((eget e :clock)))

   :start-time
   (fn [e]
     ((eget e :clock) (eget e :beat)))

   :end-time
   (fn [e]
     ((eget e :clock) (+ (eget e :beat)
                         (eget e :dur))))})

(def pname-mapping
  "If a synth has a :freq parameter, we actually use the computed :detuned-freq
  value."
  {:freq :detuned-freq})

(defn handle-note [e]
  (locking *out*
    (prn (map #(eget e %) [:instrument :note :beat])))
  (when-not (keyword? (eget e :freq))
    (let [i (eget e :instrument)
          params (:params i)
          args (reduce (fn [acc {:keys [name]}]
                         (let [kn (keyword name)
                               lk (get pname-mapping kn kn)]
                           (if (or (contains? e lk) (contains? derivations lk))
                             (conj acc kn (eget e lk))
                             acc)))
                       []
                       params)
          has-gate? (some #{"gate"} (map :name params))
          start (eget e :start-time)
          end (eget e :end-time)]
      (if start
        (server/at start
          (let [h (apply i args)]
            (when (and end has-gate?)
              (server/at end (node/ctl h :gate 0))))
          args)))))

(defn handle-chord [{:keys [chord inversion] :as e
                     :or {inversion 0}}]
  (let [midinote (eget e :midinote)]
    (when-not (keyword? midinote)
      (doseq [n (-> chord
                    pitch/resolve-chord
                    (pitch/invert-chord inversion))]
        (event/event :note (assoc e
                                  :type :note
                                  :midinote (+ midinote n)))))))

(event/on-event :note #'handle-note ::note)
(event/on-event :chord #'handle-chord ::chord)

(defn- quantize
  "Quantize a beat to a period, made a bit awkward by the fact that beats counts
  from 1, so e.g. a quant of 4 (align to 4/4 bars), yields 1, 5, 9, etc."
  [beat quant]
  (let [m (mod (dec beat) quant)]
    (if (= 0 m)
      beat
      (+ beat (- quant m)))))

(defn schedule-next [k]
  (let [pp @pplayers
        {:keys [clock paused? pseq beat proto] :as player} (get pp k)
        e        (merge (pattern/pfirst pseq) proto)
        dur      (eget e :dur)
        type     (eget e :type)
        next-seq (pattern/pnext pseq)]
    (if (and next-seq (not paused?))
      (let [job (time/apply-by (clock (+ beat dur -0.5)) schedule-next [k])]
        (swap! pplayers update k assoc
               :job job
               :pseq next-seq
               :beat (+ beat dur)))
      (swap! pplayers dissoc k))

    (when (seq pseq)
      (event/event (eget e :type) (assoc e :beat beat :clock clock)))))

(defn padd [k pattern & {:keys [quant clock offset] :as opts
                         :or   {quant 1
                                offset 0
                                clock transport/*clock*}}]
  (let [pattern (cond-> pattern (map? pattern) pattern/pbind)]
    (swap! pplayers update k
           (fn [p]
             (merge p
                    {:key     k
                     :clock   clock
                     :pattern pattern
                     :pseq    pattern
                     :quant   quant
                     :offset  offset
                     :paused? (if (some? (:paused? p))
                                (:paused? p)
                                true)}
                    opts)))))

(defn presume [k]
  (let [pp @pplayers
        {:keys [clock paused? beat quant offset job]
         :as   player} (get pp k)
        next-beat (+ (quantize (clock) quant) offset)]
    (when job
      (time/kill-player job))
    (let [job (time/apply-by (clock (- next-beat 0.5)) schedule-next [k])]
      (swap! pplayers update k
             assoc
             :job job
             :paused? false
             :beat next-beat))))

(defn ppause [k]
  (when-let [job (get-in @pplayers [k :job])]
    (time/kill-player job))
  (swap! pplayers update k
         assoc :paused? true :job nil))

(defn pplay [k pattern & args]
  (apply padd k pattern args)
  (presume k))

(defn premove [k]
  (when-let [job (get-in @pplayers [k :job])]
    (time/kill-player job))
  (swap! pplayers dissoc k))

(defn pclear []
  (doseq [job (keep :job (vals @pplayers))]
    (time/kill-player job))
  (reset! pplayers {}))
