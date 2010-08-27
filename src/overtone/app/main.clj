(ns overtone.app.main
  (:gen-class)
  (:import
    (java.awt Toolkit EventQueue Dimension Point Dimension Color Font
              RenderingHints Point BasicStroke BorderLayout)
    (java.awt.event WindowAdapter)
    (java.awt.geom Ellipse2D$Float RoundRectangle2D$Float)
    (javax.swing JFrame JPanel JSplitPane JLabel JButton BorderFactory
                 JSpinner SpinnerNumberModel UIManager BoxLayout)
    (com.sun.scenario.scenegraph JSGPanel SGText SGShape SGGroup
                                 SGAbstractShape$Mode SGComponent SGTransform)
    (com.sun.scenario.scenegraph.event SGMouseAdapter)
    (com.sun.scenario.scenegraph.fx FXShape)
    (com.javadocking.dockable DefaultDockable DockingMode)
    (com.javadocking.dock TabDock Position SplitDock)
    (com.javadocking.model FloatDockModel)
    (com.javadocking.model.codec DockModelPropertiesEncoder)
    (com.javadocking DockingManager))
  (:use (overtone.app editor tools browser)
        (overtone.core setup util sc ugen synth envelope event time-utils config)
        (overtone.gui swing sg scope curve repl)
        clojure.stacktrace
        (clojure.contrib
          [miglayout :only (miglayout components)]))
  (:require [overtone.core.log :as log]))

(alias 'ug 'overtone.ugens)

(def app* (ref {:name "Overtone"
                :padding 5.0
                :background (Color. 50 50 50)
                :foreground (Color. 255 255 255)
                :header-fg (Color. 255 255 255)
                :header-font (Font. "helvetica" Font/BOLD 16)
                :header-height 20
                :status-update-period 1000
                :edit-font (Font. "Bitstream Vera Sans Mono" Font/PLAIN 12)
                :edit-panel-dim (Dimension. 550 900)
                :scene-panel-dim (Dimension. 615 900)
                :tools-panel-dim (Dimension. 300 900)
                }))

(defn metro-panel []
  (let [bpm-lbl (JLabel. "BPM: ")
        bpm-model (SpinnerNumberModel. 120 1 400 1)
        bpm-spin (JSpinner. bpm-model)
        beat-panel (JPanel.)]
    (.setForeground bpm-lbl (:header-fg @app*))
    (doto beat-panel
      (.setBackground (:background @app*))
      (.add bpm-lbl)
      (.add bpm-spin))))

(defn status-panel []
  (let [ugen-lbl  (JLabel. "UGens: 0")
        synth-lbl (JLabel. "Synths: 0")
        group-lbl (JLabel. "Groups: 0")
        cpu-lbl   (JLabel. "CPU: 0.00")
        border (BorderFactory/createEmptyBorder 2 5 2 5)
        lbl-panel (JPanel.)
        updater (fn [] (let [sts (status)]
                           (in-swing
                             (.setText ugen-lbl  (format "ugens: %4d" (:n-ugens sts)))
                             (.setText synth-lbl (format "synths: %4d" (:n-synths sts)))
                             (.setText group-lbl (format "groups: %4d" (:n-groups sts)))
                             (.setText cpu-lbl   (format "avg-cpu: %4.2f" (:avg-cpu sts))))))]
    (doto lbl-panel
      (.setBackground (:background @app*))
      (.add ugen-lbl )
      (.add synth-lbl)
      (.add group-lbl)
      (.add cpu-lbl))

    (doseq [lbl [ugen-lbl synth-lbl group-lbl cpu-lbl]]
      (.setBorder lbl border)
      (.setForeground lbl (:header-fg @app*)))

    (on :connected #(periodic updater (:status-update-period @app*)))
    lbl-panel))

(def WORKSPACE-CONFIG (str OVERTONE-DIR "/workspace-model"))

(defn save-workspace []
  (let [model (:workspace-model @app*)
        encoder (DockModelPropertiesEncoder.)]
    (if (.canSave encoder model)
      (.save encoder model)
      (.export encoder model WORKSPACE-CONFIG))))

(defn app-quit []
  (save-workspace)
  (try
    (quit)
    (finally (System/exit 0))))

(defn header []
  (let [panel (JPanel.)
        metro (metro-panel)
        status (status-panel)
        boot-btn (JButton. "Boot")
        help-btn (JButton. "Help")
        quit-btn (JButton. "Quit")
        btn-panel (JPanel.)]

    (on-action boot-btn boot)
    (on-action help-btn #(println "help is on the way!"))
    (on-action quit-btn app-quit)

    (doto btn-panel
      (.setBackground (:background @app*))
      (.add boot-btn)
      (.add help-btn)
      (.add quit-btn))

    (doto panel
      (.setLayout (BorderLayout.))
      (.setBackground (:background @app*))
      (.add metro BorderLayout/WEST)
      (.add status BorderLayout/CENTER)
      (.add btn-panel BorderLayout/EAST))

    (dosync (alter app* assoc :header panel))
    panel))

(defn controls-scene []
  (let [root (sg-group)]
    (doto root
      (add! (translate (:padding @app*) 0.0 (curve-editor))))
    (dosync (alter app* assoc :scene-group root))
    root))

(defn window-listener [frame]
  (proxy [WindowAdapter] []
    (windowClosed [win-event] (do (.setVisible frame false) (app-quit)))
    (windowIconified [win-event] (event :iconified))
    (windowDeiconified [win-event] (event :deiconified))
    ))

(defn- dock-panel [app child name]
  (let [dock (DefaultDockable. name child name nil DockingMode/ALL)]
    (doto child
      (.setBackground (:background app))
      (.setForeground (:foreground app)))
    dock))

(defn overtone-frame []
  (let [app-frame (JFrame.  "Overtone")
        app-panel (.getContentPane app-frame)

        ;browse-split (JSplitPane. JSplitPane/HORIZONTAL_SPLIT)
        right-split (JSplitPane. JSplitPane/HORIZONTAL_SPLIT)
        main-split (JSplitPane. JSplitPane/VERTICAL_SPLIT)
        tools-split (JSplitPane. JSplitPane/VERTICAL_SPLIT)

        header-panel (header)
        browse-panel (dock-panel @app* (browser-panel) "Browser")
        edit-panel (dock-panel @app* (editor-panel @app*) "Editor")
        repl-panel (dock-panel @app* (repl-panel) "REPL")
        scene-panel (JSGPanel.)
        scene-dock (dock-panel @app* scene-panel "Envelope")
        scope-panel (dock-panel @app* (scope-panel) "Scope")
        color-panel (dock-panel @app* (color-panel @app*) "Color")
        log-panel (dock-panel @app* (log-view-panel @app*) "Log")
        key-panel (dock-panel @app* (keymap-panel @app*) "Keymap")

        top-tab-dock (TabDock.)
        bottom-tab-dock (TabDock.)
        top-tools-tab-dock (TabDock.)
        bottom-tools-tab-dock (TabDock.)
        left-tab-dock (TabDock.)

        top-split-dock (SplitDock.)
        bottom-split-dock (SplitDock.)
        top-tools-split-dock (SplitDock.)
        bottom-tools-split-dock (SplitDock.)
        left-split-dock (SplitDock.)

        dock-model (if (file-exists? WORKSPACE-CONFIG)
                     (FloatDockModel. WORKSPACE-CONFIG)
                     (FloatDockModel.))]

    (.addDockable top-tab-dock edit-panel (Position. 0))
    (.addDockable bottom-tab-dock repl-panel (Position. 0))
    (.addDockable left-tab-dock browse-panel (Position. 0))
    (.addDockable top-tools-tab-dock scope-panel (Position. 0))
    (.addDockable top-tools-tab-dock scene-dock (Position. 1))
    (.addDockable bottom-tools-tab-dock color-panel (Position. 0))
    (.addDockable bottom-tools-tab-dock log-panel (Position. 1))
    (.addDockable bottom-tools-tab-dock key-panel (Position. 2))

    (.addChildDock top-split-dock top-tab-dock (Position. Position/CENTER))
    (.addChildDock bottom-split-dock bottom-tab-dock (Position. Position/CENTER))
    (.addChildDock left-split-dock left-tab-dock (Position. Position/CENTER))
    (.addChildDock top-tools-split-dock top-tools-tab-dock (Position. Position/CENTER))
    (.addChildDock bottom-tools-split-dock bottom-tools-tab-dock (Position. Position/CENTER))

    (doto dock-model
      (.addOwner "app-frame" app-frame)
      (DockingManager/setDockModel)
      (.addRootDock "top-split" top-split-dock app-frame)
      (.addRootDock "bottom-split" bottom-split-dock app-frame)
      (.addRootDock "left-split" left-split-dock app-frame)
      (.addRootDock "top-tools-split" top-tools-split-dock app-frame)
      (.addRootDock "bottom-tools-split" bottom-tools-split-dock app-frame))

    (comment doto browse-split
      (.setOneTouchExpandable true)
      (.setLeftComponent left-split-dock)
      (.setRightComponent right-split))

    (doto right-split
      (.setOneTouchExpandable true)
      (.setLeftComponent main-split)
      (.setRightComponent tools-split))

    (dosync (alter app* assoc
                   :workspace-model dock-model
                   :right-div right-split))

    (doto main-split
      (.setOneTouchExpandable true)
      (.setTopComponent top-split-dock)
      (.setBottomComponent bottom-split-dock))

    (doto tools-split
      (.setOneTouchExpandable true)
      (.setTopComponent top-tools-split-dock)
      (.setBottomComponent bottom-tools-split-dock))

    (doto scene-panel
      (.setBackground Color/BLACK)
      (.setScene (controls-scene))
      (.setPreferredSize (Dimension. 600 400)))

    (miglayout app-panel
      header-panel "dock north"
      ;browse-split "dock west")
      right-split "dock west")

    (apply-at #(in-swing (do
      ;                                  (.setDividerLocation browse-split 0.1)
                                        (.setDividerLocation right-split 0.7)
                                        (.setDividerLocation main-split 0.8)
                                        (.setDividerLocation tools-split 0.5))) 
              (+ (now) 100))
    (apply-at #(in-swing (do
      ;                                  (.setDividerLocation browse-split 0.1)
                                        (.setDividerLocation right-split 0.7)
                                        (.setDividerLocation main-split 0.8)
                                        (.setDividerLocation tools-split 0.5))) 
              (+ (now) 2000))

    (doto app-frame
      (.addWindowListener (window-listener app-frame))
      (.pack)
      (.setVisible true))))

(def DEFAULT-THEME "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel")

(defn- app-theme []
  (let [theme (:theme @config* DEFAULT-THEME)
        system-lf (UIManager/getSystemLookAndFeelClassName)]
    (if (= :system theme)
      system-lf
      theme)))

(defn -main [& args]
  (JFrame/setDefaultLookAndFeelDecorated true)
  ; Maybe we need Java7 for this API?
  ;(if-let [screen (GraphicsEnvironment/getDefaultScreenDevice)]
  ;  (if (.isFullScreenSupported screen)
  ;    (.setFullScreenWindow screen window))
  ;    (UIManager/setLookAndFeel system-lf)
  (in-swing
    (UIManager/setLookAndFeel (app-theme))
    (overtone-frame)))
