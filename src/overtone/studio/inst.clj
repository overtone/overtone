(ns overtone.studio.inst
  (:use [overtone.sc defaults bindings server synth ugens envelope node bus]
        [overtone.sc.machinery synthdef]
        [overtone.sc.util :only (id-mapper)]
        [overtone.studio mixer fx]
        [overtone.helpers lib]
        [overtone.libs event]))

(defonce __MIXER-SYNTHS__
  (do
    (defsynth mono-inst-mixer
      [in-bus  10
       out-bus 0
       volume  DEFAULT-VOLUME
       pan     DEFAULT-PAN]
      (let [snd (in in-bus)]
        (out out-bus (pan2 snd pan volume))))

    (defsynth stereo-inst-mixer
      [in-bus  10
       out-bus 0
       volumel DEFAULT-VOLUME
       volumer DEFAULT-VOLUME
       panl    DEFAULT-PAN-LEFT
       panr    DEFAULT-PAN-RIGHT]
      (let [snd (in in-bus 2)]
        (out out-bus
             (+ (pan2 (select 0 snd) panl volumel)
                (pan2 (select 1 snd) panr volumer)))))))

(defn inst-mixer
  "Instantiate a mono or stereo inst-mixer synth."
  [n-chans & args]
  (if (> n-chans 1)
    (apply stereo-inst-mixer args)
    (apply mono-inst-mixer args)))

(defn- inst-channels
  "Internal fn used for multimethod dispatch on Insts."
  [inst & args]
  (let [n-chans (:n-chans inst)]
    (if (> n-chans 1) :stereo :mono)))

(defmulti inst-volume
  "Control the volume of a single instrument."
  inst-channels)

(defmethod inst-volume :mono
  [inst vol]
  (ctl (:mixer inst) :volume vol)
  (reset! (first (:volume inst)) vol))

(defmethod inst-volume :stereo
  ([inst vol]
     (inst-volume inst vol vol))
  ([inst voll volr]
     (ctl (:mixer inst) :volumel voll :volumer volr)
     (let [volume (:volume inst)]
       (reset! (nth volume 0) voll)
       (reset! (nth volume 1) volr))))

(defmulti inst-pan
  "Control the pan setting of a single instrument."
  inst-channels)

(defmethod inst-pan :mono
  [inst pan]
  (ctl (:mixer inst) :pan pan)
  (reset! (first (:pan inst)) pan))

(defmethod inst-pan :stereo
  [inst panl panr]
  (ctl (:mixer inst) :panl panl :panr panr)
  (let [pan (:pan inst)]
    (reset! (nth pan 0) panl)
    (reset! (nth pan 1) panr)))

(defn inst-fx
  "Append an effect to an instrument channel."
  [inst fx]
  (let [fx-group (:fx-group inst)
        bus (:bus inst)
        fx-id (fx :tgt fx-group :pos :tail :bus bus)]
    fx-id))

(defn clear-fx
  [inst]
  (group-clear (:fx-group inst))
  :clear)

(defmacro pre-inst
  [& args]
  (let [[sname params param-proxies ugen-form] (normalize-synth-args args)]
    `(let [~@param-proxies]
       (binding [*ugens* []
                 *constants* #{}]
         (with-overloaded-ugens
           (let [form# ~@ugen-form
                 n-chans# (if (seq? form#)
                            (count form#)
                            1)
                 inst-bus# (or (:bus (get @instruments* ~sname)) (audio-bus n-chans#))
                 [ugens# constants#] (gather-ugens-and-constants (out inst-bus# form#))
                 ugens# (topological-sort-ugens ugens#)
                 main-tree# (set ugens#)
                 side-tree# (filter #(not (main-tree# %)) *ugens*)
                 ugens# (concat ugens# side-tree#)
                 constants# (into [] (set (concat constants# *constants*)))]
             [~sname
              ~params
              ugens#
              constants#
              n-chans#
              inst-bus#]))))))

(defrecord-ifn Inst [name params args ugens sdef
                     group instance-group fx-group
                     mixer bus fx-chain
                     volume pan
                     n-chans]
  (fn [this & args] (apply synth-player name params this :tgt instance-group args))

  to-synth-id*
  (to-synth-id [_] (to-synth-id instance-group)))

(defn inst?
  "Returns true if o is an instrument, false otherwise"
  [o]
  (= overtone.studio.inst.Inst (type o)))

(defn default-volume [n]
  "Returns a vector of 0, 1, or 2 atoms for holding the volume
  settings of an Inst."
  (cond
    (= n 0) []
    (= n 1) [(atom DEFAULT-VOLUME)]
    (> n 1) (vec (repeat 2 (atom DEFAULT-VOLUME)))))

(defn default-pan [n]
  "Returns a vector of 0, 1, or 2 atoms for holding the pan settings
  an Inst."
  (cond
    (= n 0) []
    (= n 1) [(atom DEFAULT-PAN)]
    (> n 1) [(atom DEFAULT-PAN-LEFT) (atom DEFAULT-PAN-RIGHT)]))

(defmacro inst
  [sname & args]
  (ensure-connected!)
  `(let [[sname# params# ugens# constants# n-chans# inst-bus#] (pre-inst ~sname ~@args)
         new-inst# (get @instruments* sname#)
         container-group# (or (:group new-inst#)
                              (group (str "Inst " sname# " Container") :tail @inst-group*))
         instance-group#  (or (:instance-group new-inst#)
                              (group (str "Inst " sname#) :head container-group#))
         fx-group#        (or (:fx-group new-inst#)
                              (group (str "Inst " sname# " FX"):tail container-group#))
         imixer#    (or (:mixer new-inst#)
                        (inst-mixer n-chans#
                                    :tgt container-group#
                                    :pos :tail
                                    :in-bus inst-bus#))
         sdef#      (synthdef sname# params# ugens# constants#)
         arg-names# (map :name params#)
         params-with-vals# (map #(assoc % :value (atom (:default %))) params#)
         fx-chain#  []
         volume#    (default-volume n-chans#)
         pan#       (default-pan n-chans#)
         inst#      (with-meta
                      (Inst. sname# params-with-vals# arg-names# ugens# sdef#
                             container-group# instance-group# fx-group#
                             imixer# inst-bus# fx-chain#
                             volume# pan#
                             n-chans#)
                      {:overtone.helpers.lib/to-string #(str (name (:type %)) ":" (:name %))})]

     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defmacro definst
  "Define an instrument and return a player function. The instrument
  definition will be loaded immediately, and a :new-inst event will be
  emitted. Expects a name, an optional doc-string, a vector of
  instrument params, and a ugen-form as it's arguments.

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

    (desfynth foo [freq 440]
      (out 0 (sin-osc freq))))

  * Instruments are limited to 1 or 2 channels. Instruments with more
    than 2 channels are allowed, but additional channels will not be
    audible. Use the mix and pan2 ugen's to combine multiple channels
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

    (inst-pan bar -1)     ;pan hard left.
    (inst-volume bar 0.5) ;half the volume.

    For a stereo inst, you can control left and right pan or volume
    separately by passing an additional arg:

    (inst-pan bar 1 -1)   ;ch1 right, ch2 left.
    (inst-volume bar 0 1) ;mute ch1.

  * Each instrument has an fx-chain to which you can add any number of
    'fx synths' using the inst-fx function.
  "
  {:arglists '([name doc-string? params ugen-form])}
  [i-name & inst-form]
  (let [[i-name params ugen-form] (synth-form i-name inst-form)
        i-name (with-meta i-name (merge (meta i-name) {:type ::instrument}))]
    `(def ~i-name (inst ~i-name ~params ~ugen-form))))

(defmethod print-method ::instrument [ins w]
  (let [info (meta ins)]
    (.write w (format "#<instrument: %s>" (:name info)))))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    (load-synthdef synth)))

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

  IKillable
  {:kill* (fn [this] (group-deep-clear (:instance-group this)))})
