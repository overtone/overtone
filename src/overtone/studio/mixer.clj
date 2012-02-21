(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose"}
  overtone.studio.mixer
  (:use [clojure.core.incubator :only [dissoc-in]]
        [overtone.music rhythm pitch]
        [overtone.libs event deps]
        [overtone.util lib]
        [overtone.sc.machinery defaults synthdef]
        [overtone.sc.machinery.ugen fn-gen defaults sc-ugen]
        [overtone.sc.machinery.server comms]
        [overtone.sc server synth gens envelope node bus]
        [overtone.sc.util :only [id-mapper]]
        [overtone.music rhythm time])
  (:require [overtone.studio fx]
            [overtone.util.log :as log]))


; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be placed in the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments*  (ref {}))
(defonce inst-group*   (ref nil))

(def MIXER-BOOT-DEPS [:server-ready :studio-setup-completed])
(def DEFAULT-VOLUME 1.0)
(def DEFAULT-PAN 0.0)

(defn mixer-booted? []
  (deps-satisfied? MIXER-BOOT-DEPS))

(defn wait-until-mixer-booted
  "Makes the current thread sleep until the mixer completed its boot process."
  []
  (wait-until-deps-satisfied MIXER-BOOT-DEPS))

(defn boot-mixer
  "Boots the server and waits until the studio mixer has complete set up"
  []
  (when-not (mixer-booted?)
    (boot-server)
    (wait-until-mixer-booted)))

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

(defn setup-studio []
  (log/info (str "Creating studio group at head of: " (root-group)))
  (let [g (with-server-sync #(group :head (root-group)))
        r (group :tail (root-group))]

    (dosync
      (ref-set inst-group* g)
      (ref-set instruments* (map-vals #(assoc % :group (group :tail g))
                                      @instruments*)))
    (satisfy-deps :studio-setup-completed)))

(on-deps :server-ready ::setup-studio setup-studio)

;; Clear and re-create the instrument groups after a reset
;; TODO: re-create the instrument groups
(defn reset-inst-groups
  "Frees all synth notes for each of the current instruments"
  []
  (doseq [[name inst] @instruments*]
    (group-clear (:instance-group inst))))

(on-sync-event :reset reset-inst-groups ::reset-instruments)

; Add instruments to the session when defined
(defn add-instrument [inst]
  (let [i-name (:name inst)]
    (dosync (alter instruments* assoc i-name inst))
    i-name))

(defn remove-instrument [i-name]
  (dosync (alter instruments* dissoc (name i-name)))
  (event :inst-removed :inst-name i-name))

(defn clear-instruments []
  (dosync (ref-set instruments* {})))

(defmacro pre-inst
  [& args]
  (let [[sname params param-proxies ugen-form] (normalize-synth-args args)]
    `(let [~@param-proxies]
         (with-overloaded-ugens
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
         s-player#  (synth-player sname# params-with-vals#)
         player# (fn [& play-args#]
                   (let [ins# (get @instruments* sname#)]
                     (apply s-player#
                            :tgt (:instance-group ins#)
                            play-args#)))
         inst# (callable-map {:type ::instrument
                              :params      params-with-vals#
                              :name        sname#
                              :ugens       ugens#
                              :sdef        sdef#
                              :group       container-group#
                              :instance-group instance-group#
                              :fx-group    fx-group#
                              :mixer       imixer#
                              :bus         inst-bus#
                              :volume      (atom DEFAULT-VOLUME)
                              :pan         (atom DEFAULT-PAN)
                              :fx-chain    []
                              :player      player#
                              :args        arg-names#}
                             player#
                             {:overtone.util.lib/to-string #(str (name (:type %)) ":" (:name %))})]

     (load-synthdef sdef#)
     (add-instrument inst#)
     (event :new-inst :inst inst#)
     inst#))

(defn inst?
  "Returns true if o is an instrument, false otherwise"
  [o]
  (and (associative? o)
       (= ::instrument (:type o))))

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

(defmethod overtone.sc.node/kill :overtone.studio.mixer/instrument
  [& args]
  (doseq [inst args]
    (group-clear (:instance-group inst))))

(defmethod overtone.sc.node/ctl :overtone.studio.mixer/instrument
  [inst & ctls]
  (apply node-control (:instance-group inst) (id-mapper ctls))
  (apply modify-synth-params inst ctls))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    (load-synthdef synth)))


