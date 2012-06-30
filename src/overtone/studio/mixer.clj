(ns
  ^{:doc "Higher level instrument and studio abstractions."
     :author "Jeff Rose"}
  overtone.studio.mixer
  (:use [clojure.core.incubator :only [dissoc-in]]
        [overtone.music rhythm pitch]
        [overtone.libs event deps]
        [overtone.helpers lib]
        [overtone.sc defaults server synth ugens envelope node bus]
        [overtone.sc.machinery synthdef]
        [overtone.sc.machinery.ugen fn-gen defaults sc-ugen]
        [overtone.sc.machinery.server comms]
        [overtone.sc.util :only [id-mapper]]
        [overtone.music rhythm time])
  (:require [overtone.studio fx]
            [overtone.config.log :as log]))

; An instrument abstracts the more basic concept of a synthesizer used by
; SuperCollider.  Every instance of an instrument will be placed in the same
; group, so if you later call (kill my-inst) it will be able to stop all the
; instances of that group.  (Likewise for controlling them...)

(defonce instruments*  (ref {}))
(defonce inst-group*   (ref nil))

(def MIXER-BOOT-DEPS [:server-ready :studio-setup-completed])
(def DEFAULT-VOLUME 1.0)
(def DEFAULT-PAN 0.0)
(def DEFAULT-PAN-LEFT -1.0)
(def DEFAULT-PAN-RIGHT 1.0)

(defn mixer-booted? []
  (deps-satisfied? MIXER-BOOT-DEPS))

(defn wait-until-mixer-booted
  "Makes the current thread sleep until the mixer completed its boot
  process."
  []
  (wait-until-deps-satisfied MIXER-BOOT-DEPS))

(defn boot-server-and-mixer
  "Boots the server and waits until the studio mixer has complete set
  up"
  []
  (when-not (mixer-booted?)
    (boot-server)
    (wait-until-mixer-booted)))

(defn setup-studio []
  (log/info (str "Creating studio group at head of: " (root-group)))
  (let [root              (root-group)
        g                 (with-server-sync #(group "Studio" :head root))
        insts-with-groups (map-vals (fn [val]
                                      assoc val
                                      :group
                                      (with-server-sync #(group (str "Recreated Inst Group") :tail g)))
                                    @instruments*)]
    (dosync
     (ref-set inst-group* g)
     (ref-set instruments* insts-with-groups))
    (satisfy-deps :studio-setup-completed)))

(on-deps :server-ready ::setup-studio setup-studio)

;; Clear and re-create the instrument groups after a reset
;; TODO: re-create the instrument groups
(defn reset-inst-groups
  "Frees all synth notes for each of the current instruments"
  [event-info]
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
