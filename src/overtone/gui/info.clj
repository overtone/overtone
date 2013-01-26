(ns overtone.gui.info
  (:use overtone.libs.event
        [overtone.sc info server]
        [overtone.studio mixer]
        [seesaw core border table chooser color])
  (:require [seesaw.bind :as bind]))

(native!)

(defonce ^{:private true} update-timer (atom nil))

(def ^{:private true} divider-color "#aaaaaa")
(def ^{:private true} normal-font   "ARIAL-12-PLAIN")
(def ^{:private true} title-font    "ARIAL-14-BOLD")

(comment defn make-toolbar
  []
  (border-panel
    :hgap   5
    :border [5 (line-border :bottom 1 :color divider-color)]
    :west   (label :text "City:" :font title-font)
    :center (text
              :id   :city
              :class :refresh
              :text "Ann Arbor,MI")))

(defn- format-status-label
  [id value]
  (str (name id) ": " (if (float? value)
                        (format "%4.2f" value)
                        (format "%4d" value))))

(defn- update-status-panel
  [panel]
  (let [info (server-status)]
    (doseq [[id value] info]
      (config! (select panel [(keyword (str "#" (name id)))]) :text (format-status-label id value)))
    panel))

(defn- make-status-panel
  []
  (let [info (server-status)
        panel (border-panel
                :id :status-panel
                :border 5
                :center
                (table :model
                       [:columns [:l1 :v1 :l2 :v2]
                        :rows [{:l1 "Avg. CPU: "   :v1 (:avg-cpu info)
                                :l2 "Peak CPU: "   :v2 (:peak-cpu info)}
                               {:l1 "UGens: "      :v1 (:n-ugens info)
                                :l2 "Synths: "     :v2 (:n-synths info)}
                               {:l1 "Groups: "     :v1 (:n-groups info)
                                :l2 "Synth Defs: " :v2 (:n-loaded-synths info)}
                               ]]))]
    (when-let [old-timer @update-timer]
      (.stop old-timer))
    ;(reset! update-timer (timer update-status-panel :delay 1000 :initial-value panel))
    panel))

(defn- make-info-panel
  []
  (let [info (server-info)]
    (border-panel
      :id :info-panel
      :border 2
      :center
      (table :model
             [:columns [:l1 :v1 :l2 :v2]
              :rows [{:l1 "Sample Rate: "  :v1 (:sample-rate info)
                      :l2 "Control Rate: " :v2 (:control-rate info)}
                     {:l1 "Buffers: "      :v1 (:num-buffers info)
                      :l2 "Audio Buses: "  :v2 (:num-audio-buses info)}
                     {:l1 "Input Buses: "  :v1 (:num-input-buses info)
                      :l2 "Output Buses: " :v2 (:num-output-buses info)}]]))))

(defn- make-tabs
  []
  (tabbed-panel
    :tabs [{ :title "Server Status" :content (make-status-panel) }
           { :title "Server Info"   :content (make-info-panel) }]))

(defn- make-master-controls
  []
  (let [init-val (int (* (volume) 100.0))
        slide (slider :value init-val :min 0 :max 120 :orientation :vertical
                    :major-tick-spacing 10 :paint-ticks? true)
        spin (spinner :model (spinner-model init-val :from 0 :to 120 :by 1))
        record-btn (button :id :record-button :text "Record" :background (color :lightgreen))
        panel (vertical-panel :id :volume-ctl :items [slide spin record-btn])]
    (bind/bind spin slide)
    (bind/bind slide spin)
    (bind/bind slide (bind/b-do [v] (volume (/ v 100.0))))
    panel))

(defn- toggle-recording
  [btn]
  (println "toggling recording")
  (if (not (recording?))
      (choose-file :type :save
                   :success-fn (fn [fc file]
                                 (recording-start (.getAbsolutePath file))
                                 (invoke-later (config! btn :background (color :red)))))
    (do
      (recording-stop)
      (invoke-later (config! btn :background (color :lightgreen))))))

(defn- add-behaviors
  [frame]
  (listen frame :window-closed (fn [e] (when-let [old-timer @update-timer]
                                         (.stop old-timer)
                                         (reset! update-timer nil))))
  (let [rec-btn (select frame [:#record-button])]
    (listen rec-btn :action (fn [e] (toggle-recording rec-btn))))
  frame)

(defn control-panel
  "Open the Overtone control panel, giving access to server status information
  and additional tools to view and manipulate instruments and sounds."
  []
  (invoke-now
    (-> (frame
          :title "Overtone"
          :size  [500 :by 200]
          :on-close :dispose
          :menubar (menubar :items [(menu :text "View" :items [(menu-item :class :refresh)])])
          :content (border-panel
                     :border 5
                     :hgap 5
                     :vgap 5
                     ;:north  (make-toolbar)
                     :center (make-tabs)
                     :east (make-master-controls)
                     :south (label :id :status :text "Ready")))
      (add-behaviors)
      (show!))))
