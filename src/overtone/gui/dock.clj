(ns overtone.gui.dock
  (:use
    [seesaw core make-widget])
  (:import
    [bibliothek.gui DockController Dockable]
    [bibliothek.gui.dock.title DockTitleFactory]
    [bibliothek.gui.dock
     DefaultDockable ScreenDockStation
     FlapDockStation SplitDockStation]))

(defonce dock-controller (DockController.))

(extend-type bibliothek.gui.dock.dockable.AbstractDockable
  MakeWidget
  (make-widget* [d] (.getComponent d)))

(defn empty-dock-title-factory
  []
  (proxy [DockTitleFactory] []
    (install [_] )
    (request [req] (.answer req nil))
    (uninstall [_] )))

(defonce _
  (do
    (let [title-manager (.getDockTitleManager dock-controller)]
      (.registerTheme title-manager "overtone" (empty-dock-title-factory)))))

(defn set-dock-root!
  [w]
  (.setRootWindow dock-controller (to-root w)))

(comment defn screen-dock
  []
  (let [station (ScreenDockStation.)]
    (.add dock-controller station)
    station))

(defn flap-dock
  []
  (let [station (FlapDockStation.)]
    (.add dock-controller station)
    station))

(defn split-dock
  []
  (let [station (SplitDockStation.)]
    (.add dock-controller station)
    station))

(defn dockable
  [component]
  (let [dock (DefaultDockable.)]
    (.add dock component)
    dock))
