(ns overtone.studio.event
  (:require
   [overtone.at-at :as at-at]
   [overtone.libs.event :as event]
   [overtone.music.pitch :as pitch]
   [overtone.music.rhythm :as rhythm]
   [overtone.music.time :as time]
   [overtone.sc.node :as node]
   [overtone.sc.sample :as sample]
   [overtone.sc.server :as server]
   [overtone.studio.pattern :as pattern]
   [overtone.studio.transport :as transport]))

(set! *warn-on-reflection* true)

(defonce
  ^{:doc "Thread pool for `at-at`, separate from the main pool overtone
  uses so we can control the behavior when `stop` is called (:reset event)"}
  ^:private
  player-pool (at-at/mk-pool))

(defonce pplayers (atom {}))

(def ^:private event-defaults
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
   {:type   :ctl
    :dur    1
    :root   0.0
    :octave 5.0
    :gtranspose       0.0
    :steps-per-octave 12.0
    :octave-ratio     2.0
    :harmonic         1.0
    :ctranspose       0.0
    :detune           0.0
    :swing-quant      2
    :swing            0
    }})

(declare event-derivations)

(defn eget
  ([e k]
   (if (contains? e k)
     (get e k)
     (let [t (:type e :note)
           d (get event-defaults t)]
       (cond
         (contains? d k)
         (get d k)
         (contains? event-derivations k)
         ((get event-derivations k) e)
         :else
         (throw (ex-info (str "Missing event key or derivation " k)
                         {:event e}))))))
  ([e k fallback]
   (if (contains? e k)
     (get e k)
     (let [t (:type e :note)
           d (get event-defaults t)]
       (cond
         (contains? d k)
         (get d k)
         (contains? event-derivations k)
         ((get event-derivations k) e)
         :else
         fallback)))))

(defn- rest? [o]
  (#{:_ :- :rest} o))

(defn- event-octave-note [e octave note]
  (* (+ octave
        (/ (+ note (eget e :gtranspose))
           (eget e :steps-per-octave)))
     12
     (/ (Math/log (eget e :octave-ratio))
        (Math/log 2))))

(def ^:private event-derivations
  {:detuned-freq
   (fn [e]
     (when (and (some #(contains? e %) [:freq :midinote :note :degree])
                (eget e :freq)
                (eget e :detune))
       (+ (eget e :freq) (eget e :detune))))

   :freq
   (fn [e]
     (when (some #(contains? e %) [:midinote :note :degree])
       (let [midinote (eget e :midinote)]
         (if (keyword? midinote)
           midinote
           (* (eget e :harmonic)
              (pitch/midi->hz
               (+ midinote (eget e :ctranspose))))))))

   :midinote
   (fn [e]
     (let [note (eget e :note)
           root (eget e :root)]
       (cond
         (rest? note)
         note

         (or (keyword? note) (string? note))
         (let [{:keys [interval octave]
                :or {octave (eget e :octave)}} (pitch/note-info note)]
           (event-octave-note e octave interval))

         (or (keyword? root) (string? root))
         (let [{:keys [interval octave]
                :or {octave (eget e :octave)}} (pitch/note-info root)]
           (event-octave-note e octave (+ note interval)))

         :else
         (event-octave-note e (eget e :octave) (+ note root)))))

   :scale-intervals
   (fn [e]
     (get pitch/SCALE (eget e :mode)))

   :scale-notes
   (fn [e]
     (butlast (reductions + 0 (eget e :scale-intervals))))

   :note
   (fn [e]
     (let [degree (eget e :degree)]
       (if (rest? degree)
         degree
         (let [degree (pitch/degree->int degree)
               scale (eget e :scale-notes)
               size  (count scale)
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
     (when-let [clock (eget e :clock)]
       (let [beat (eget e :beat)]
         (clock (cond-> beat
                  ;; beats count from 1
                  (= 1 (mod beat (eget e :swing-quant)))
                  (+ (eget e :swing)))))))

   :end-time
   (fn [e]
     (when-let [clock (eget e :clock)]
       (clock (+ (eget e :beat)
                 (eget e :dur)))))})

(def ^:private pname-mapping
  "If a synth has a :freq parameter, we actually use the computed :detuned-freq
  value."
  {:freq :detuned-freq
   :note :midinote})

(defn- eget-instrument [e]
  (let [i (eget e :instrument)]
    (if (sample/sample? i)
      (case (int (:n-channels i))
        1 sample/mono-partial-player
        2 sample/stereo-partial-player)
      i)))

(defn event-params-vec [e]
  (let [i' (eget e :instrument)
        i (eget-instrument e)
        params (or (:params (meta i))
                   (map (comp keyword :name)
                        (:pnames (:sdef i))))]
    (reduce (fn [acc kn]
              (let [lk (get pname-mapping kn kn)]
                (if-let [val (when (or (contains? e lk) (contains? event-derivations lk))
                               (eget e lk))]
                  (conj acc kn val)
                  acc)))
            (if (sample/sample? i')
              [:buf (:id i') ]
              [])
            params)))

(defn- handle-note [e]
  (when-not (keyword? (eget e :freq))
    (let [i         (eget-instrument e)
          params    (:params i)
          args      (event-params-vec e)
          has-gate? (some #{"gate"} (map :name params))
          start     (eget e :start-time)
          end       (if start
                      (eget e :end-time)
                      (+ (time/now)
                         (* (eget e :dur)
                            60000
                            (/ 1 (or (eget e :bpm)
                                     (:bpm (or (eget e :clock)
                                               transport/*clock*)))))))]
      (if start
        (server/at start
          (let [h (apply i args)]
            (when (and end has-gate?)
              (server/at end (node/ctl h :gate 0))))
          args)
        (let [h (apply i args)]
          (when (and end has-gate?)
            (server/at end (node/ctl h :gate 0))))))))

(defn- handle-chord [e]
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

(defn- handle-ctl [e]
  (let [i (eget-instrument e)
        args (event-params-vec e)
        start (eget e :start-time)]
    (when start
      (server/at start (apply node/ctl i args)))))

(event/on-event :note #'handle-note ::note)
(event/on-event :chord #'handle-chord ::chord)
(event/on-event :ctl #'handle-ctl ::ctl)

(defn- quantize-ceil
  "Quantize a beat to a period, rounding up."
  [beat quant-base quant]
  (let [m (mod (- beat quant-base) quant)]
    (if (= 0 m)
      beat
      (+ beat (- quant m)))))

(defn- quantize-floor
  "Quantize a beat to a period, rounding down."
  [beat quant-base quant]
  (let [m (mod (- beat quant-base) quant)]
    (if (= 0 m)
      beat
      (- beat m))))

(declare schedule-next)

(defn schedule-next-job [clock beat k]
  (time/with-pool player-pool
    (time/apply-by (clock (dec beat)) schedule-next [k])))

(defn- player-schedule-next [{:keys [clock playing pseq beat proto] :as player}]
  (if (or (not playing) (not player))
    player
    (let [e         (merge {:clock clock}
                           (pattern/pfirst pseq)
                           proto)
          next-seq  (pattern/pnext pseq)
          dur       (eget e :dur)
          next-e    (pattern/pfirst next-seq)
          next-start-beat (some-> next-e
                                  (eget :start-time)
                                  (- (rhythm/metro-start clock))
                                  (/ (rhythm/metro-tick clock)))
          next-beat (or next-start-beat
                        (eget next-e :beat)
                        (+ beat dur))]
      (if pseq
        (assoc player
               :pseq next-seq
               :beat next-beat
               :last-event (assoc e :beat beat :clock clock))
        nil))))

(defn- schedule-next [k]
  (let [[old new] (map k (swap-vals! pplayers update k player-schedule-next))
        {:keys [clock beat playing] :as player} new
        e (:last-event new)]
    (when playing
      (when (not= (:last-event new) (:last-event old))
        (event/event (eget e :type) e))
      (schedule-next-job clock beat k)))
  nil)

(defn- popts
  "Merge player with default values and options"
  [player opts]
  (merge-with
   ;; merge key fn ensures nil value doesn't override an existing value
   #(if (nil? %2) %1 %2)
   ;; Default values
   {:align  :wait
    :quant  4
    :offset 0
    :clock  transport/*clock*
    :proto  nil}
   player
   opts))

(defn padd
  "Add a pattern to the player pool or merge parameters if the player already
   exists. Does not change whether the player is playing or not."
  [k pattern & {:as opts}]
  (let [pattern (cond-> pattern (map? pattern) pattern/pbind)]
    (swap! pplayers update k
           (fn [player]
             (merge (popts player opts)
                    {:pseq pattern})))))

(defn drop-pseq
  "Drop `len` beats from `pseq`. Insert a rest to guarantee dropping exactly
   `len` beats when the last event duration overshoots."
  [len pseq]
  (cond
    (zero? len) pseq
    (< len 0) (cons {:type :rest :dur (- len)} pseq)
    (empty? pseq) pseq
    :else
    (let [dur (eget (pattern/pfirst pseq) :dur)]
      (recur (- len dur) (pattern/pnext pseq)))))

(defn take-pseq
  "Return a sequence of exactly `len` beats from the beginning of `pseq`,
   padding with a rest or shortening the last event as necessary."
  [len pseq]
  (loop [pseq pseq
         rem len
         res []]
    (let [e (pattern/pfirst pseq)
          dur (eget e :dur)]
      (cond
        (nil? e)
        (conj res {:type :rest :dur rem})
        (<= rem dur)
        (conj res (assoc e :dur rem))
        :else
        (recur (pattern/pnext pseq)
               (- rem dur)
               (conj res e))))))

(defn- player-resume [player pseq opts]
  (let [{:keys [clock beat quant align offset proto]
         old-pseq :pseq
         :as player} (popts player opts)
        ;; Atomically determine both whether any player is playing and the
        ;; earliest quantization base.
        [playing quant-base] (reduce (fn [[playing quant-base] [k p]]
                                       (if (:playing p)
                                         [true (min quant-base (:quant-base p))]
                                         [playing quant-base]))
                                     [false (clock)]
                                     @pplayers)
        beat (if playing
               (max (or beat 1) (clock))
               (clock))
        [beat pseq] (if-not playing
                      ;; nothing is currently playing, so just start
                      ;; immediatedly
                      [beat pseq]

                      ;; Other player(s) are already playing, make sure we are
                      ;; in sync
                      (case align
                        ;; Play as though the sequence started at the prior
                        ;; quant beat. We skip beats between then and now to
                        ;; arrive at what starts playing now.
                        :quant
                        (let [skip-beats (- (mod (- beat quant-base) quant) offset)]
                          [beat (drop-pseq skip-beats pseq)])
                        :wait
                        (let [switch (+ (quantize-ceil beat quant-base quant) offset)]
                          (if-not (:playing player)
                            ;; we aren't playing yet, so start the sequence at the
                            ;; next available sync point (e.g. bar)
                            [switch pseq]
                            ;; We are already playing, let the old pattern play
                            ;; out until we are ready to switch
                            [beat (concat (take-pseq (- switch beat) old-pseq)
                                          pseq)]))
                        ;; Base case, just start at the next beat
                        [beat pseq]))]
    (assoc player
           :playing true
           :beat beat
           :pseq pseq
           :align align
           :quant quant
           :offset offset
           :quant-base quant-base
           :proto proto)))

(defn presume
  ([k]
   (presume k (:pseq (get @pplayers k))))
  ([k new-pseq]
   (presume k new-pseq nil))
  ([k new-pseq opts]
   (let [new-pseq (cond-> new-pseq (map? new-pseq) pattern/pbind)
         [old new] (map k (swap-vals! pplayers update k player-resume new-pseq opts))]
     (when (and (not (:playing old))
                (:playing new))
       (schedule-next-job (:clock new) (:beat new) k)))
   nil))

(defn ppause [k]
  (swap! pplayers update k (fn [p] (-> p
                                       (dissoc :beat)
                                       (assoc :playing false))))
  nil)

(defn pplay
  "Register a pattern and immediately start playing it. A pattern is
  a (potentially nested) sequence of event maps, which will get triggered in
  turn, based on their `:dur` (duration in beats).

  An optional map can be passed as a second argument, which understands the
  following keys:

  - `:proto` event prototype, a map with common attributes that gets merged into
    the event maps
  - `:clock` the clock to use for scheduling events
  - `:offset` wait this many beats before starting the pattern
  - `:quant` start the pattern at a beat number that is a multiple of `:quant`
  "
  [k pattern & {:as opts}]
  (if (= \_ (first (name k)))
    (ppause (keyword (namespace k) (subs (name k) 1)))
    (do
      #_(apply padd k pattern args)
      (presume k pattern opts)))
  nil)


(defn ploop [k pattern & args]
  (apply pplay k (repeat (pattern/pbind pattern)) args))

(defn premove [k]
  (swap! pplayers dissoc k)
  nil)

(defn pclear []
  (at-at/stop-and-reset-pool! player-pool :strategy :kill)
  (reset! pplayers {})
  nil)

(event/on-sync-event
 :reset
 (fn [event-info]
   (swap! pplayers
          (fn [pp]
            (update-vals pp #(assoc (dissoc % :beat) :playing false))))
   (at-at/stop-and-reset-pool! player-pool :strategy :kill))
 ::pplayers-reset)

(comment
  (into {} (map #(dissoc % :pseq :pattern)) @@pplayers)
  )
