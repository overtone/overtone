(ns overtone.studio.aux
  "Model the concept of an AUX bus, as found on analog mixing consoles. From each
  instrument you can 'send' some of the signal to the AUX bus, forming a
  separate mix.

  Common example use cases are reverb, or creating a separate headphone or
  monitor mix.

  Currently all AUX busses are mono (single channel).

      (demo 60 (gverb (in (aux-bus :reverb)) :drylevel 0 :roomsize 3))
      (aux-ctl sampled-piano :reverb 0.3)
  "
  (:require
   [overtone.sc.ugens :as u]
   [overtone.sc.synth :as synth]
   [overtone.sc.bus :as bus]
   [overtone.sc.node :as node]
   [overtone.studio.core :refer [studio*]]))

;; Structure in the studio* atom:
;; {:aux {:bus <audio-bus>
;;        :sends {inst-name <synth aux-send>}}}

(synth/defsynth aux-send [bus 0 send-bus 0 amount 0]
  (let [sig (u/in bus)]
    (u/out send-bus (* sig amount))))

(defn- ensure-aux! [aux-name]
  (swap! studio* update :aux
         (fn [aux-map]
           (if (contains? aux-map aux-name)
             aux-map
             (assoc aux-map aux-name {:bus (bus/audio-bus 1 (name aux-name))})))))

(defn aux-bus
  "Get the AUX bus for the given name (keyword), pass this to the `in` ugen to
  consume the signal on the bus."
  [aux-name]
  (get-in (ensure-aux! aux-name) [:aux aux-name :bus]))

(defn aux-ctl
  "Change the level of a given instrument's signal that is sent to the AUX bus.
  Amount is between 0 1."
  [inst aux-name amount]
  (let [inst-name (:full-name inst)
        {:keys [instruments aux]}
        (swap! (ensure-aux! aux-name)
               (fn [{:keys [instruments] :as studio}]
                 (let [{:keys [bus fx-group]} (get instruments inst-name)]
                   (update-in
                    studio [:aux aux-name :sends inst-name]
                    (fn [send]
                      (or send
                          (aux-send [:tail fx-group]
                                    bus
                                    (aux-bus aux-name)
                                    amount)))))))
        {:keys [bus fx-group]} (get instruments inst-name)]
    (node/ctl (get-in aux [aux-name :sends inst-name]) :amount amount)))
