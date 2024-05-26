(ns overtone.studio.event
  (:require
   [overtone.libs.event :as event]
   [overtone.music.pitch :as pitch]
   [overtone.music.time :as time]
   [overtone.sc.node :as node]
   [overtone.sc.server :as server]
   [overtone.studio.pattern :as pattern]
   [overtone.studio.transport :as transport])
  (:import
   (clojure.lang IDeref)))

(defonce pplayers (atom {}))
(defonce nodes (atom {}))

(def defaults
  {:note
   {:type             :note
    :mtranspose       0
    :gtranspose       0.0
    :ctranspose       0.0
    :octave           5.0
    :root             0.0
    :degree           1
    :mode             :major
    :steps-per-octave 12.0
    :detune           0.0
    :harmonic         1.0
    :octave-ratio     2.0
    :dur              1
    :swing            0
    :swing-quant      2
    :clock            nil
    :bpm              nil}

   :chord
   {:type             :chord
    :mtranspose       0
    :gtranspose       0.0
    :ctranspose       0.0
    :octave           5.0
    :root             0.0
    :degree           1
    :mode             :major
    :chord            :from-scale
    :inversion        0
    :chord-size       3
    :steps-per-octave 12.0
    :detune           0.0
    :harmonic         1.0
    :octave-ratio     2.0
    :dur              1
    :strum            0}

   :ctl
   {:type             :ctl
    :mtranspose       0
    :gtranspose       0.0
    :ctranspose       0.0
    :octave           5.0
    :root             0.0
    :degree           1
    :mode             :major
    :steps-per-octave 12.0
    :detune           0.0
    :harmonic         1.0
    :octave-ratio     2.0
    :dur              1
    :swing            0
    :swing-quant      2
    :clock            nil
    :bpm              nil
    :gate             1}})

(declare derivations)

(defn eget
  ([e k]
   (let [v (eget e k ::not-found)]
     (if (= v ::not-found)
       (throw (ex-info (str "Missing event key or derivation " k)
                       {:event e}))
       v)))
  ([e k fallback]
   (if (contains? e k)
     (let [v (get e k)]
       (if (instance? IDeref v)
         (deref v)
         v))
     (let [t (:type e :note)
           d (get defaults t)]
       (cond
         (contains? d k)
         (get d k)
         (contains? derivations k)
         ((get derivations k) e)
         :else
         fallback)))))

(defn rest? [o]
  (#{:_ :rest} o))

(defn octave-note [e octave note]
  (* (+ octave
        (/ (+ note (eget e :gtranspose))
           (eget e :steps-per-octave)))
     12
     (/ (Math/log (eget e :octave-ratio))
        (Math/log 2))))

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
     (let [note (eget e :note)
           root (eget e :root)]
       (cond
         (rest? note)
         note

         (or (keyword? note) (string? note))
         (let [{:keys [interval octave]
                :or   {octave (eget e :octave)}} (pitch/note-info note)]
           (octave-note e octave interval))

         (or (keyword? root) (string? root))
         (let [{:keys [interval octave]
                :or   {octave (eget e :octave)}} (pitch/note-info root)]
           (octave-note e octave (+ note interval)))

         :else
         (octave-note e (eget e :octave) (+ note root)))))

   :scale-intervals
   (fn [e]
     (get pitch/SCALE (eget e :mode)))

   :scale-notes
   (fn [e]
     (butlast (reductions + 0 (eget e :scale-intervals))))

   :note
   (fn [e]
     (let [degree (eget e :degree)]
       (if (#{:_ :rest} degree)
         degree
         (let [degree (pitch/degree->int degree)
               scale  (eget e :scale-notes)
               size   (count scale)
               degree (+ (dec degree)
                         (eget e :mtranspose))]
           ;; Not too sure about this... would be good to compare results with SC
           (+ (nth scale (mod degree size))
              (* (eget e :steps-per-octave)
                 (cond-> (quot degree size)
                   (< degree 0)
                   dec)))))))

   :beat
   (fn [e]
     (when-let [clock (eget e :clock)]
       (clock)))

   :start-time
   (fn [e]
     (let [clock       (eget e :clock)
           beat        (eget e :beat)
           swing       (eget e :swing)
           swing-quant (eget e :swing-quant)]
       (when (and clock beat)
         (clock (cond-> beat
                  ;; beats count from 1
                  (and swing swing-quant
                       (= 1 (mod beat swing-quant)))
                  (+ swing))))))

   :end-time
   (fn [e]
     (let [clock (eget e :clock)
           beat  (eget e :beat)
           dur   (eget e :dur)
           start (eget e :start-time)]
       (when (and beat dur clock)
         (if start
           (clock (+ beat dur))
           (+ (time/now)
              (* dur
                 60000
                 (/ 1 (or (eget e :bpm)
                          (:bpm (or (eget e :clock)
                                    transport/*clock*))))))))))

   :params
   (fn [e]
     (let [i (eget e :instrument)]
       (or (:params (meta i))
           (map (comp keyword :name)
                (:pnames (:sdef i))))))})

(def pname-mapping
  "If a synth has a :freq parameter, we actually use the computed :detuned-freq
  value."
  {:freq :detuned-freq
   :note :midinote})

(defn params-vec [e]
  (reduce (fn [acc kn]
            (let [lk (get pname-mapping kn kn)]
              (if (or (contains? e lk) (contains? derivations lk))
                (conj acc kn (eget e lk))
                acc)))
          []
          (eget e :params)))

(defn node-key [e]
  (if-let [k (::key e)]
    [k (eget e :instrument)]
    (eget e :instrument)))

(defn handle-end [e args]
  (let [params    (eget e :params)
        has-gate? (some #{:gate} params)
        end       (eget e :end-time)]
    (when (and end has-gate? (not (eget e :keep-node? false)))
      (doseq [h (get (first (swap-vals! nodes dissoc (node-key e)))
                     (node-key e))]
        (server/at end
          (node/ctl h :gate 0))
        (server/at (+ end (eget e :release 0) 1000)
          (node/kill h))))))

(defmacro at-start
  {:style/indent [1]}
  [e & body]
  `(let [start# (eget ~e :start-time)]
     (if start#
       (server/at start#
         ~@body)
       (do
         ~@body))))

(defn handle-note [e]
  (when-not (rest? (eget e :freq))
    (let [i         (eget e :instrument)
          params    (eget e :params)
          args      (params-vec e)
          has-gate? (some #{:gate} params)
          end       (eget e :end-time)]
      (at-start e
        (swap! nodes update (node-key e) conj (apply i args))
        (handle-end e args)) )))

(defn handle-chord [e]
  (let [chord     (eget e :chord)
        inversion (or (eget e :inversion) 0)
        midinote  (eget e :midinote)
        chord-notes
        (pitch/invert-chord
         (if (= :from-scale chord)
           (for [n (range (eget e :chord-size))]
             (eget (assoc e :degree
                          (+ (* 2 n)
                             (pitch/degree->int (eget e :degree))))
                   :midinote))
           (map (partial + midinote) (pitch/resolve-chord chord)))
         inversion)]
    (when-not (keyword? midinote)
      (doseq [[n idx] (map vector chord-notes (range))]
        (event/event :note (assoc (update e :beat + (* idx (eget e :strum)))
                                  :type :note
                                  :midinote n))))))

(defn handle-ctl [e]
  (let [i     (eget e :instrument)
        args  (params-vec e)
        start (eget e :start-time)]
    (at-start e
      (apply node/ctl i args)
      (handle-end e args))))

(event/on-event :note #'handle-note ::note)
(event/on-event :chord #'handle-chord ::chord)
(event/on-event :ctl #'handle-ctl ::ctl)

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
        e        (merge {:clock transport/*clock*}
                        (pattern/pfirst pseq)
                        proto)
        dur      (eget e :dur)
        type     (eget e :type)
        next-seq (pattern/pnext pseq)
        next-e   (pattern/pfirst next-seq)]
    (if (and next-seq (not paused?))
      (let [job (time/apply-by (clock (+ beat dur -0.5)) schedule-next [k])]
        (swap! pplayers update k assoc
               :job job
               :pseq next-seq
               :beat (+ beat dur)))
      (swap! pplayers dissoc k))

    (when (seq pseq)
      (event/event
       (eget e :type)
       (-> e
           (assoc :beat beat :clock clock ::key k)
           ;; don't end the note if there's a :ctl that follows
           (cond-> (= :ctl (:type next-e))
             (assoc :dur nil :keep-node? true)))))))

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
             :beat next-beat)))
  nil)

(defn ppause [k]
  (when-let [job (get-in @pplayers [k :job])]
    (time/kill-player job))
  (swap! pplayers update k
         assoc :paused? true :job nil)
  nil)

(defn pplay
  "Register a pattern and immediately start playing it. A pattern is
  a (potentially nested) sequence of event maps, which will get triggered in
  turn, based on their `:dur` (duration in beats).

  An optional map can be passed as a second argument, which understands the
  following keys:

  - `:proto` event prototype, a map with common attributes that gets merged into
    the event maps
  - `:offset` wait this many beats before starting the pattern
  - `:quant` start the pattern at a beat number that is a multiple of `:quant`
  "
  [k pattern & args]
  (apply padd k pattern args)
  (presume k)
  nil)

(defn premove [k]
  (when-let [job (get-in @pplayers [k :job])]
    (time/kill-player job))
  (swap! pplayers dissoc k)
  nil)

(defn pclear []
  (doseq [job (keep :job (vals @pplayers))]
    (time/kill-player job))
  (reset! pplayers {})
  nil)
