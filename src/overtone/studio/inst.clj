(ns overtone.studio.inst
  (:use
    [overtone.sc server synth gens envelope node bus]
    [overtone.sc.machinery defaults synthdef]
    [overtone.sc.machinery.ugen fn-gen]
    [overtone.sc.util :only (id-mapper)]
    [overtone.studio mixer fx]
    [overtone.util lib]
    [overtone.libs event]))

(defonce __MIXER-SYNTH__
  (defsynth inst-mixer [in-bus 10 out-bus 0
                        volume DEFAULT-VOLUME pan DEFAULT-PAN]
    (let [snd (in in-bus)]
      (out out-bus (pan2 snd pan volume)))))

(defn inst-volume
  "Control the volume for a single instrument."
  [inst vol]
  (ctl inst :volume vol)
  (reset! (:volume inst) vol))

(defn inst-pan
  "Control the pan setting for a single instrument."
  [inst pan]
  (ctl inst :pan pan)
  (reset! (:pan inst) pan))

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
         (overtone.sc.machinery.ugen.fn-gen/with-overloaded-ugens
           (let [form# ~@ugen-form
                 n-chans# (if (seq? form#)
                            (count form#)
                            1)
                 inst-bus# (or (:bus (get @instruments* ~sname)) (audio-bus n-chans#))
                 [ugens# constants#] (gather-ugens-and-constants (out inst-bus# form#))
                 ugens# (topological-sort-ugens ugens#)]
             [~sname
              ~params
              ugens#
              constants#
              n-chans#
              inst-bus#])))))

(defrecord-ifn Inst [name params args ugens sdef
                     group instance-group fx-group
                     mixer bus fx-chain
                     volume pan]
    (fn [this & args] (apply synth-player name params this :tgt instance-group args)))

(defn inst?
  "Returns true if o is an instrument, false otherwise"
  [o]
  (= overtone.studio.inst.Inst (type o)))

(defmacro inst
  [sname & args]
  `(let [[sname# params# ugens# constants# n-chans# inst-bus#] (pre-inst ~sname ~@args)
         new-inst# (get @instruments* sname#)
         container-group# (or (:group new-inst#)
                              (group :tail @inst-group*))
         instance-group#  (or (:instance-group new-inst#)
                              (group :head container-group#))
         fx-group#        (or (:fx-group new-inst#)
                              (group :tail container-group#))
         imixer#    (or (:mixer new-inst#)
                        (inst-mixer :tgt container-group# :pos :tail :in-bus inst-bus#))
         sdef#      (synthdef sname# params# ugens# constants#)
         arg-names# (map :name params#)
         params-with-vals# (map #(assoc % :value (atom (:default %))) params#)
         fx-chain#  []
         volume#    (atom DEFAULT-VOLUME)
         pan#       (atom DEFAULT-PAN)
         inst#      (with-meta
                      (Inst. sname# params-with-vals# arg-names# ugens# sdef#
                             container-group# instance-group# fx-group#
                             imixer# inst-bus# fx-chain#
                             volume# pan#)
                      {:overtone.util.lib/to-string #(str (name (:type %)) ":" (:name %))})]

     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defmacro definst
  "Define an instrument and return a player function. Arguments are a vector
  of name/value parameter pairs, for example:

  (definst inst-name [param0 value0 param1 value1 param2 value2] ...)

  The returned player function takes any number of positional arguments,
  followed by any number of keyword arguments. For example, all of the following
  are equivalent:

  (inst-name 0 1 2)
  (inst-name 0 1 :param2 2)
  (inst-name :param1 1 :param0 0 :param2 2)

  Omitted parameters are given their default value from the instrument's
  parameter list.

  The instrument definition will be loaded immediately. Instruments differ
  from basic synths in that they will automatically add pan2 and out ugens
  when necessary to create a stereo synth. Also, each instrument is assigned
  its own group which all instances will automatically be placed in. This
  allows you to control all of an instrument's running synths with one command:

  (ctl inst-name :param0 val0 :param1 val1)

  You may also kill all of an instrument's running synths:

  (kill inst-name)

  A doc string may also be included between the instrument's name ant
  parameter list:

  (definst lucille
    \"What's that Lucille?\"
    [] ...)
  "
  [i-name & inst-form]
  (let [[i-name params ugen-form] (synth-form i-name inst-form)
        i-name (with-meta i-name (merge (meta i-name) {:type ::instrument}))]
    `(def ~i-name (inst ~i-name ~params ~ugen-form))))

(defmethod print-method ::instrument [ins w]
  (let [info (meta ins)]
    (.write w (format "#<instrument: %s>" (:name info)))))

; TODO: extract instrument features and setup defrecord and protocol implementations
;
;(defmethod overtone.sc.node/kill :overtone.studio.mixer/instrument
;  [& args]
;  (doseq [inst args]
;    (group-clear (:instance-group inst))))
;
;(defmethod overtone.sc.node/ctl :overtone.studio.mixer/instrument
;  [inst & ctls]
;  (apply node-control (:instance-group inst) (id-mapper ctls))
;  (apply modify-synth-params inst ctls))
;
(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    (load-synthdef synth)))


