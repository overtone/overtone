(ns overtone.studio.inst
  (:refer-clojure :exclude [Inst])
  (:use [overtone.sc.defaults]
        [overtone.sc.bindings]
        [overtone.sc.server]
        [overtone.sc.synth]
        [overtone.sc.ugens]
        [overtone.sc.envelope]
        [overtone.sc.node]
        [overtone.sc.bus]
        [overtone.sc.dyn-vars]
        [overtone.sc.machinery.synthdef]
        [overtone.studio.core]
        [overtone.studio.mixer]
        [overtone.studio.fx]
        [overtone.helpers.lib]
        [overtone.libs.event]
        [potemkin :refer [def-map-type]])
  (:require [clojure.pprint]
            [lentes.core :as l]
            [overtone.sc.protocols :as protocols]
            [overtone.sc.util]
            [overtone.helpers.lib :as ov.lib]
            [overtone.sc.machinery.server.comms :refer [with-server-sync]]
            [overtone.libs.deps :as ov.deps]))

(ov.lib/defrecord-ifn-2 Inst [name full-name params args sdef
                              ^:deref group ^:deref instance-group
                              ^:deref fx-group
                              ^:deref mixer mixer-params volume pan bus
                              n-chans]
  (fn [this & args]
    (apply synth-player sdef params this [:tail @instance-group] args))

  to-sc-id*
  (to-sc-id [_] (to-sc-id @instance-group)))

(derive Inst :overtone.sc.node/node)

(def DEFAULT-VOLUME 1.0)
(def DEFAULT-PAN    0.0)

(defonce __MIXER-SYNTHS__
  (do
    (defsynth mono-inst-mixer
      [in-bus  10
       out-bus 0
       volume  DEFAULT-VOLUME
       pan     DEFAULT-PAN]
      (let [snd (in in-bus)
            snd (select (check-bad-values snd 0 0)
                        [snd (dc 0) (dc 0) snd])
            snd (limiter snd 0.99 0.001)]
        (out out-bus (pan2 snd pan volume))))

    (defsynth stereo-inst-mixer
      [in-bus  10
       out-bus 0
       volume  DEFAULT-VOLUME
       pan     DEFAULT-PAN]
      (let [zero (dc 0)
            sndl (in in-bus 1)
            sndl (select (check-bad-values sndl 0 0)
                         [sndl zero zero sndl])
            sndl (limiter sndl 0.99 0.001)
            sndr (in (+ in-bus 1) 1)
            sndr (select (check-bad-values sndr 0 0)
                         [sndr zero zero sndr])
            sndr (limiter sndr 0.99 0.001)]
        (out out-bus (balance2 sndl sndr pan volume))))))

(defn default-get-inst-mixer
  "Instantiate a mono or stereo inst-mixer synth."
  [{:keys [n-chans] :as _inst}]
  (if (> n-chans 1)
    stereo-inst-mixer
    mono-inst-mixer))

(defn inst-mixer
  "Instantiate an instrument mixer."
  [inst & args]
  (let [get-mixer (get @studio* ::get-inst-mixer default-get-inst-mixer)
        mixer-synth (get-mixer inst)]
    (apply mixer-synth args)))

(defn replace-inst-mixer!
  "Replace the mixer synth in an instrument."
  [{:keys [bus mixer-params] :as inst} new-get-inst-mixer & {:as params}]
  (ensure-node-active! inst)
  (let [{:keys [mixer]} (ov.lib/ov-raw-map inst)
        mixer-synth (new-get-inst-mixer inst)
        synth-params (->> (merge @mixer-params params)
                          ;; Flatten to :key1 val1 :key2 val2 ...
                          (mapcat identity))
        new-mixer (apply mixer-synth
                         [:replace @mixer]
                         :in-bus bus
                         synth-params)]
    (reset! mixer new-mixer)))

(defn replace-all-inst-mixer!
  "Replace the mixer synth in all current and future instruments."
  [new-get-inst-mixer & params]
  (swap! studio* assoc ::get-inst-mixer new-get-inst-mixer)
  (doseq [[_name inst] (:instruments @studio*)]
    (apply replace-inst-mixer! inst new-get-inst-mixer params)))

(defn inst-channels
  "Internal fn used for multimethod dispatch on Insts."
  [inst & _args]
  (let [n-chans (:n-chans inst)]
    (if (> n-chans 1) :stereo :mono)))

(defn inst-volume!
  "Control the volume of a single instrument."
  [inst vol]
  (ensure-node-active! inst)
  (ctl (:mixer inst) :volume vol)
  (reset! (:volume inst) vol))

(defn inst-pan!
  "Control the pan setting of a single instrument."
  [inst pan]
  (ensure-node-active! inst)
  (ctl (:mixer inst) :pan pan)
  (reset! (:pan inst) pan))

(defn inst-mixer-ctl!
  "Control a named parameters of the output mixer of a single instrument."
  [inst & {:as args}]
  (ensure-node-active! inst)
  (apply ctl (:mixer inst) (-> args seq flatten))
  (swap! (:mixer-params inst) merge args))

(defmulti inst-fx!
  "Append an effect to an instrument channel. Returns a SynthNode or a
  vector of SynthNodes representing the effect instance."
  inst-channels)

(defmethod inst-fx! :mono
  [inst fx & args]
  (ensure-node-active! inst)
  (let [fx-group (:fx-group inst)
        bus      (:bus inst)
        fx-id    (apply fx [:tail fx-group] :bus bus args)]
    fx-id))

(defmethod inst-fx! :stereo
  [inst fx & args]
  (ensure-node-active! inst)
  (let [fx-group (:fx-group inst)
        bus-l    (to-sc-id (:bus inst))
        bus-r    (inc bus-l)
        fx-ids   [(apply fx [:tail fx-group] :bus bus-l args)
                  (apply fx [:tail fx-group] :bus bus-r args)]]
    fx-ids))

(defn clear-fx
  [inst]
  (ensure-node-active! inst)
  (group-clear (:fx-group inst))
  :clear)

(defmacro pre-inst
  [& args]
  (let [[sname params param-proxies ugen-form] (normalize-synth-args args)]
    `(let [~@param-proxies]
       (binding [*ugens*     []
                 *constants* #{}]
         (with-overloaded-ugens
           (let [full-name# '~(symbol (str *ns*) (str sname))
                 form#               ~@ugen-form
                 ;; form# can be a map, or a sequence of maps. We use
                 ;; `sequence?` because `coll?` applies to maps (which
                 ;; are not sequential) and `seq?` does not apply to
                 ;; vectors (which are sequential).
                 n-chans#            (if (sequential? form#) (count form#) 1)
                 inst-bus#           (or (:bus (get (:instruments @studio*) full-name#))
                                         (audio-bus n-chans#))
                 [ugens# constants#] (gather-ugens-and-constants (out inst-bus# form#))
                 ugens#              (topological-sort-ugens ugens#)
                 main-tree#          (set ugens#)
                 side-tree#          (filter #(not (main-tree# %)) *ugens*)
                 ugens#              (concat ugens# side-tree#)
                 constants#          (into [] (set (concat constants# *constants*)))]
             [~sname
              full-name#
              ~params
              ugens#
              constants#
              n-chans#
              inst-bus#]))))))

(defn instrument?
  "Returns true if o is an instrument, false otherwise"
  [o]
  (= overtone.studio.inst.Inst (type o)))

(defmacro with-server-sync-atom
  [action-fn error-msg]
  `(atom
    (when (server-connected?)
      (with-server-sync ~action-fn ~error-msg))))

(defmacro inst
  [sname & args]
  `(let [[sname# full-name# params# ugens# constants# n-chans# inst-bus#] (pre-inst ~sname ~@args)
         new-inst# (ov.lib/ov-raw-map (get (:instruments @studio*) full-name#))

         container-group# (or (:group new-inst#)
                              (with-server-sync-atom
                                #(group (str "Inst " sname# " Container")
                                        :tail (:instrument-group @studio*))
                                "whilst creating an inst container group"))

         instance-group# (or (:instance-group new-inst#)
                             (with-server-sync-atom
                               #(group (str "Inst " sname#)
                                       :head @container-group#)
                               "whilst creating an inst instance group"))

         fx-group#        (or (:fx-group new-inst#)
                              (with-server-sync-atom
                                #(group (str "Inst " sname# " FX")
                                        :tail @container-group#)
                                "whilst creating an inst fx group"))

         imixer#    (or (:mixer new-inst#)
                        (atom ::uninitialized-mixer))
         sdef#      (synthdef sname# params# ugens# constants#)
         arg-names# (map :name params#)
         params-with-vals# (map #(assoc % :value (control-proxy-value-atom full-name# %)) params#)
         mixer-params# (atom {:volume DEFAULT-VOLUME
                              :pan    DEFAULT-PAN})
         volume#    (l/derive (l/key :volume) mixer-params#)
         pan#       (l/derive (l/key :pan) mixer-params#)
         inst#      (with-meta
                      (->Inst sname# full-name# params-with-vals# arg-names# sdef#
                              container-group# instance-group# fx-group#
                              imixer# mixer-params# volume# pan# inst-bus#
                              n-chans#)
                      {:overtone.helpers.lib/to-string #(str (name (:type %)) ":" (:name %))})]
     (when (= ::uninitialized-mixer @imixer#)
       ;; Briefly delayed mixer creation to support passing the realized inst# to
       ;; the mixer selection function.
       (reset! imixer#
               (when (server-connected?)
                 (with-server-sync
                   #(inst-mixer inst#
                                [:tail @container-group#]
                                :in-bus inst-bus#)
                   "whilst creating an inst imixer"))))
     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defn- load-all-insts
  []
  ;; TODO Get atoms using something else.
  (doseq [-inst (:instruments @studio*)]
    (let [sname (first -inst)
          {:keys [instance-group fx-group mixer bus] container-group :group}
          (ov.lib/ov-raw-map (second -inst))]
      (reset! container-group (group (str "Inst " sname " Container")
                                     :tail (:instrument-group @studio*)))

      (reset! instance-group (group (str "Inst " sname)
                                    :head @container-group))

      (reset! fx-group (group (str "Inst " sname " FX")
                              :tail @container-group))

      (reset! mixer (inst-mixer (second -inst)
                                [:tail @container-group]
                                :in-bus bus))))

  (ov.deps/satisfy-deps :insts-loaded))

(ov.deps/on-deps :studio-setup-completed ::load-all-insts load-all-insts)

(defmacro definst
  "Define an instrument and return a player function. The instrument
  definition will be loaded immediately, and a :new-inst event will be
  emitted. Expects a name, an optional doc-string, a vector of
  instrument params, and a ugen-form as its arguments.

  Instrument parameters are a vector of name/value pairs, for example:

  (definst inst-name [param0 value0 param1 value1 param2 value2] ...)

  The returned player function takes any number of positional
  arguments, followed by any number of keyword arguments. For example,
  all of the following are equivalent:

  (inst-name 0 1 2)
  (inst-name 0 1 :param2 2)
  (inst-name :param1 1 :param0 0 :param2 2)

  Omitted parameters are given their default value from the
  instrument's parameter list.

  A doc string may also be included between the instrument's name and
  parameter list:

  (definst lucille
    \"What's that Lucille?\"
    [] ...)

  Instruments are similar to basic synths but still differ in a number
  of notable ways:

  * Instruments will automatically wrap the body of code given in an
    out ugen. You do not need to include an out ugen yourself. For
    example:

    (definst foo [freq 440]
      (sin-osc freq))

    is similar to:

    (defsynth foo [freq 440]
      (out 0 (sin-osc freq)))

  * Instruments are limited to 1 or 2 channels. Instruments with more
    than 2 channels are allowed, but additional channels will not be
    audible. Use the mix and pan2 ugens to combine multiple channels
    within your inst if needed. For example:

    (definst bar
      [f1 100 f2 200 f3 300 f4 400]
      (mix (pan2 (sin-osc [f1 f2 f3 f4]) [-1 1 -1 1])))

  * Each instrument is assigned its own group which all instances will
    automatically be placed in. This allows you to control all of an
    instrument's running synths with one command:

    (ctl inst-name :param0 val0 :param1 val1)

    You may also kill all of an instrument's running synths:

    (kill inst-name)

  * A bus and bus-mixer are created for each instrument. This allows
    you to control the volume or pan of the instrument group with one
    command:

    (inst-pan! bar -1)     ;pan hard left.
    (inst-volume! bar 0.5) ;half the volume.

    For a stereo inst, you can control left and right pan or volume
    separately by passing an additional arg:

    (inst-pan! bar 1 -1)   ;ch1 right, ch2 left.
    (inst-volume! bar 0 1) ;mute ch1.

  * Each instrument has an fx-group to which you can add any number of
    'fx synths' using the inst-fx! function.
  "
  {:arglists '([name doc-string? params ugen-form])}
  [i-name & inst-form]
  (let [[i-name params ugen-form] (synth-form i-name inst-form)
        i-name                    (with-meta i-name (merge (meta i-name) {:type ::instrument}))]
    `(def ~i-name (inst ~i-name ~params ~ugen-form))))

(defmethod clojure.pprint/simple-dispatch Inst [ins]
  (println (format "#<instrument: %s>" (:name ins))))

(defmethod print-method Inst [ins ^java.io.Writer w]
  (.write w (format "#<instrument: %s>" (:name ins))))

(defmethod print-method ::instrument [ins ^java.io.Writer w]
  (let [info (meta ins)]
    (.write w (format "#<instrument: %s>" (:name info)))))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    (load-synthdef synth)))

(defn- inst-block-until-ready*
  [inst]
  (when (block-node-until-ready?)
    (doseq [sub-node [(:fx-group inst)
                      (:group inst)
                      (:instance-group inst)
                      (:mixer inst)]]
      (node-block-until-ready sub-node))))

(defn- inst-status*
  [inst]
  (let [sub-nodes [(:fx-group inst)
                   (:group inst)
                   (:instance-group inst)
                   (:mixer inst)]]
    (cond
     (some #(= :loading @(:status %)) sub-nodes) :loading
     (some #(= :destroyed @(:status %)) sub-nodes) :destroyed
     (some #(= :paused @(:status %)) sub-nodes) :paused
     (every? #(= :live @(:status %)) sub-nodes) :live
     :else (throw (Exception. "Unknown instrument sub-node state: "
                              (with-out-str (doseq [n sub-nodes] (pr n))))))))

(extend Inst

  ISynthNode
  {:node-free  node-free*
   :node-pause node-pause*
   :node-start node-start*
   :node-place node-place*}

  IControllableNode
  {:node-control           node-control*
   :node-control-range     node-control-range*
   :node-map-controls      node-map-controls*
   :node-map-n-controls    node-map-n-controls*}

  protocols/IKillable
  {:kill* (fn [this] (group-deep-clear (:instance-group this)))}

  ISynthNodeStatus
  {:node-status            inst-status*
   :node-block-until-ready inst-block-until-ready*})
