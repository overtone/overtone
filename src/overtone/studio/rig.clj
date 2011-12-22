(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose"}
  overtone.studio.rig
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
; SuperCollider.  Every instance of an instrument will be played within the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments*  (ref {}))
(defonce inst-group*   (ref nil))
(defonce mixer-group*  (ref nil))
(defonce mixer-id*     (ref nil))
(defonce fx-group*     (ref nil))
(defonce record-group* (ref nil))
(defonce MIXER-BUS     (audio-bus 2))

(def RIG-BOOT-DEPS [:server-ready :studio-setup-completed])
(def DEFAULT-VOLUME 1.0)
(def DEFAULT-PAN 0.0)

(defn rig-booted? []
  (deps-satisfied? RIG-BOOT-DEPS))

(defn wait-until-rig-booted
  "Makes the current thread sleep until the rig completed its boot process."
  []
  (wait-until-deps-satisfied RIG-BOOT-DEPS))

(defn boot-rig
  "Boots the server and waits until the studio rig has complete set up"
  []
  (when-not (rig-booted?)
    (boot-server)
    (wait-until-rig-booted)))

; A mixer synth for volume, pan, and limiting
; TODO: Add basic EQ
(defonce __MIXER-SYNTH__
  (defsynth inst-mixer [in-bus 10 out-bus 0 mix -1
                        low-freq 80 mid-freq 800 hi-freq 2000
                        band1 -45 band2 -45 band3 -45
                        volume DEFAULT-VOLUME pan DEFAULT-PAN]
    (let [dry (in in-bus)
          wet (b-low-shelf dry low-freq 1 band1)
          wet (b-peak-eq wet mid-freq 1 band2)
          wet (b-hi-shelf wet hi-freq 1 band3)
          mixed (x-fade2 dry wet mix)]
      (out out-bus (pan2 mixed pan volume)))))

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

(defn inst-eq-low
  [ins freq]
  ())

(defn inst-out-bus
  "Set an instruments downstream bus."
  [inst bus]
  (let [ins-name (:name inst)]
    (ctl inst :out-bus bus)
    (dosync
      (alter instruments* assoc-in [ins-name :out-bus] bus))))

(defn inst-fx
  "Append an effect to an instrument channel."
  [inst fx]
  (let [ins-name (:name inst)
        fx-chain (:fx-chain (get @instruments* ins-name))
        bus (audio-bus)
        fx-id (fx :tgt @fx-group* :pos :tail :in-bus bus :out-bus MIXER-BUS)
        src  (if (empty? fx-chain)
               inst
               (:fx-id (last fx-chain)))
        entry {:fx fx
               :fx-id fx-id
               :bus bus
               :src src}
        fx-chain (conj fx-chain entry)]
    (if (= src inst)
      (inst-out-bus inst bus)
      (ctl src :out-bus bus))
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] fx-chain))
    entry))

(comment defn remove-fx
  [inst fx]
  (let [ins-name (:name inst)]
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] fx-chain))))

(defn clear-fx
  [inst]
  (inst-out-bus inst MIXER-BUS)
  (let [ins-name (:name inst)
        fx-chain (:fx-chain (get @instruments* ins-name))]
    (doseq [id (map :fx-id fx-chain)]
      (kill id))
    (dosync
      (alter instruments* assoc-in [ins-name :fx-chain] [])))
  :clear)

(defn setup-studio []
  (log/info (str "Creating studio group at head of: " (root-group)))
  (let [g (with-server-sync #(group :head (root-group)))
        f (with-server-sync #(group :after g))
        m (group :tail (root-group))
        r (group :tail (root-group))]

    (dosync
      (ref-set inst-group* g)
      (ref-set fx-group* f)
      (ref-set mixer-group* m)
      (ref-set record-group* r)
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
    (group-clear (:group inst))))

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
       (binding [*ugens* []
                 *constants* #{}]
         (with-overloaded-ugens
           (let [form# ~@ugen-form
                 n-chans# (count form#)]
             (out MIXER-BUS form#)
             [~sname
              ~params
              *ugens*
              (into [] *constants*)
              n-chans#]))))))

(defmacro inst
  [sname & args]
  `(let [[sname# params# ugens# constants# n-chans#] (pre-inst ~sname ~@args)
         sdef# (synthdef sname# params# ugens# constants#)
         igroup# (or (:group (get @instruments* sname#))
                     (if (server-connected?)
                       (group :tail @inst-group*)
                       nil))
         imixer-bus# (audio-bus n-chans#)
         imixer# (inst-mixer :tgt igroup# :pos :head :in-bus in-bus# :out-bus MIXER-BUS)
         arg-names# (map :name params#)
         params-with-vals# (map #(assoc % :value (atom (:default %))) params#)
         s-player# (synth-player sname# params-with-vals#)
         player# (fn [& play-args#]
                   (let [ins# (get @instruments* sname#)]
                     (apply s-player#
                            :tgt (:group ins#)
                            play-args#)))
         inst# (callable-map {:type ::instrument
                              :params params-with-vals#
                              :name sname#
                              :ugens ugens#
                              :sdef sdef#
                              :group igroup#
                              :mixer imixer#
                              :volume (atom DEFAULT-VOLUME)
                              :pan (atom DEFAULT-PAN)
                              :out-bus MIXER-BUS
                              :fx-chain []
                              :player player#
                              :args arg-names#}
                             player#)]

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

(defmethod overtone.sc.node/kill :overtone.studio.rig/instrument
  [& args]
  (doseq [inst args]
    (group-clear (:group inst))))

(defmethod overtone.sc.node/ctl :overtone.studio.rig/instrument
  [inst & ctls]
  (apply node-control (:group inst) (id-mapper ctls))
  (apply modify-synth-params inst ctls))

(defonce session* (ref
                    {:metro (metronome 120)
                     :tracks {}
                     :playing false}))

(defn track [tname inst]
  (let [t {:type :track
           :name tname
           :inst inst
           :note-fn nil}]
    (dosync (alter session* assoc-in [:tracks tname] t))))

(defn remove-track
  [tname]
  (dosync (alter session* dissoc-in [:tracks tname])))

(defn track-fn [tname f]
  (dosync (alter session* assoc-in [:tracks tname :note-fn] f)))

(defn remove-track-fn [tname]
  (dosync (alter session* dissoc-in [:tracks tname :note-fn])))

(defn session-metro [m]
  (dosync (alter session* assoc :metro m)))

(defn track-start
  [t]
  )

(defn track-stop
  [t]
  )

;(def m (:metro @session*))
;(f m (m) kick)

(defn playing?
  []
  (:playing @session*))

(defn session-play
  "Call the player functions for all tracks with the session metronome,
  and the appropriate track instrument."
  []
  (let [metro (:metro @session*)
        beat (inc (metro))]
    (dosync (alter session* assoc :playing true))
    (doseq [[_ t] (:tracks @session*)]
      ((:note-fn t) metro beat (:inst t)))))

(defn session-stop []
  (dosync (alter session* assoc :playing false)))

(defn load-instruments []
  (doseq [synth (filter #(synthdef? %1)
                        (map #(var-get %1)
                             (vals (ns-publics 'overtone.instrument))))]
    (load-synthdef synth)))

; The goal is to develop a standard "studio configuration" with
; an fx rack and a set of fx busses, an output bus, etc...

; TODO
;
; Audio input
; * access samples from the microphone

; Busses
; 0 & 1 => default stereo output (to jack)
; 2 & 3 => default stereo input

; Start our busses at 1 to makes space for up to 8 on-board I/O channels
(def BUS-MASTER 16) ; 2 channels wide for stereo

; Two mono busses for doing fx sends
(def BUS-A 18)
(def BUS-B 19)

;(synth :master
;  (out.ar 0 (in.ar BUS-MASTER)))

(def session* (ref
  {:tracks []
   :instruments []
   :players []}))

;(def *fx-bus (ref (Bus/audio (server) 2)))

; A track holds an instrument with a set of effects and patches it into the mixer
; * track group contains:
;     synth group => effect group => fader synth

(defn track [track-name & [n-channels]]
  {})

;(defsynth record-bus [bus-num path]
;  )
