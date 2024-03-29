(ns overtone.patterns
  (:use
   overtone.algo.chance
   overtone.sc.server
   overtone.sc.synth
   overtone.sc.node
   overtone.libs.event
   overtone.music.pitch
   overtone.music.time))

(defn- pbind*
  "Internal helper for pbind, complicated by the fact that we want to cycle all
  seqs until we've fully consumed the longest seq, but we can't count because
  seqs could be infinite. So we track for which seqs we've reached the end at
  least once (done set)."
  [ks specs seqs done]
  (when-not (= (count done) (count ks))
    (let [vs (map (fn [v]
                    (if (sequential? v)
                      (first v)
                      v))
                  seqs)]
      (cons
       (zipmap ks vs)
       (lazy-seq
        (pbind*
         ks
         specs
         (map (fn [v spec]
                (if (sequential? v)
                  (let [n (next v)]
                    (if (nil? n)
                      spec
                      n))
                  v))
              seqs
              specs)
         (into done
               (remove nil?
                       (map (fn [k s]
                              (when (and (sequential? s)
                                         (not (next s)))
                                k))
                            ks
                            seqs)))))))))

(defn pbind
  "Takes a map, with some of the map values seqs. Returns a sequence of maps, with
  each successive map value constructed by taking the next value of each
  sequence. Sequences wrap (cycle) until the longest sequence has been consumed.
  Non-sequential values are retained as-is.

  Similar to SuperCollider's `PBind`, part of the Pattern library. "
  [m]
  (let [ks (keys m)
        specs (map m ks)
        seqs (map (fn [v]
                    (if (sequential? v)
                      (seq v)
                      v))
                  specs)
        done (set (remove nil?
                          (map (fn [k s]
                                 (when (not (sequential? s))
                                   k))
                               ks
                               seqs)))]
    (pbind*
     ks
     specs
     seqs
     done)))

(defn pwhite
  ([min max]
   (repeatedly #(rrand min max)))
  ([min max repeats]
   (repeatedly repeats #(rrand min max))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; events

(def default-event
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
   :dur              1})

(declare derivations)

(defn kval [e k]
  (cond
    (contains? e k)
    (get e k)
    (contains? derivations k)
    ((get derivations k) e)
    :else
    (throw (ex-info (str "No derivation or event key " k)
                    {:event e}))))

(def derivations
  {:detuned-freq
   (fn [e]
     (+ (kval e :freq) (kval e :detune)))

   :freq
   (fn [e]
     (* (kval e :harmonic)
        (midi->hz
         (+ (kval e :midinote) (kval e :ctranspose)))))

   :midinote
   (fn [e]
     (+ 60
        (*
         (+ (kval e :octave) (- 5)
            (/ (+ (kval e :note)
                  (kval e :gtranspose)
                  (kval e :root))
               (kval e :steps-per-octave)))
         12 (/ (Math/log (kval e :octave-ratio))
               (Math/log 2)))))

   :scale-intervals
   (fn [e]
     (get SCALE (:scale e)))

   :scale-notes
   (fn [e]
     (butlast (reductions + 0 (kval e :scale-intervals))))

   :note
   (fn [e]
     (let [scale (kval e :scale-notes)
           size  (count scale)
           degree (+ (kval e :degree)
                     (kval e :mtranspose))]
       ;; Not too sure about this... would be good to compare results with SC
       (+
        (nth scale (mod degree size))
        (* (kval e :steps-per-octave)
           (cond-> (quot degree size)
             (< degree 0)
             dec)))))})

(def pname-mapping
  "If a synth has a :freq parameter, we actually use the computed :detuned-freq
  value."
  {:freq :detuned-freq})

(defn handle-note [e]
  (let [i (:instrument e)
        params (:params i)
        args (reduce (fn [acc {:keys [name]}]
                       (let [kn (keyword name)
                             lk (get pname-mapping kn kn)]
                         (if (or (contains? e lk) (contains? derivations lk))
                           (conj acc kn (kval e lk))
                           acc)))
                     []
                     params)
        has-gate? (some #{"gate"} (map :name params))]
    (if-let [t (:start-time e)]
      (at t
          (let [h (apply i args)]
            (prn h)
            (when (and (:end-time e) has-gate?)
              (at (:end-time e) (ctl h :gate 0))))
          args))))

(defn handle-chord [{:keys [chord inversion] :as e
                     :or {inversion 0}}]
  (doseq [n (-> chord
                resolve-chord
                (invert-chord inversion))]
    (event :note (assoc e
                        :type :note
                        :midinote (+ (kval e :midinote) n)))))

(on-event :note #'handle-note ::note)
(on-event :chord #'handle-chord ::chord)

(def pplayers (volatile! {}))

(defn schedule-next [k]
  (vswap! pplayers
          (fn [pp]
            (let [{:keys [clock paused? pseq beat proto] :as player} (get pp k)
                  {:keys [dur type] :as e} (merge default-event (first pseq) proto)

                  next-seq (next pseq)
                  on-t     (clock beat)
                  off-t    (clock (+ beat dur))]
              (when (and player (not paused?))
                (apply-by on-t event (:type e) [(assoc e
                                                       :start-time on-t
                                                       :end-time off-t)]))
              (if next-seq
                (do
                  (apply-by off-t schedule-next [k])
                  (update pp k assoc
                          :pseq next-seq
                          :beat (+ beat dur)))
                (dissoc pp k))))))

(defn pplay [k clock pattern & {:as opts}]
  (vswap! pplayers assoc k (merge {:key     k
                                   :clock   clock
                                   :pattern pattern
                                   :pseq    pattern
                                   :beat    1
                                   :paused? false}
                                  opts))
  (schedule-next k))
