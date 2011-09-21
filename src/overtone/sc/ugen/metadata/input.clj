(ns overtone.sc.ugen.metadata.input
  (:use [overtone.sc.ugen common constants]))

(def specs
     [

      {:name "MouseX",
       :args [{:name "min", :default 0.0 :doc "minimum value (when mouse is at the left of the screen)"}
              {:name "max", :default 1.0 :doc "maximum value (when mouse is at the right of the screen)"}
              {:name "warp",
               :default LINEAR
               :doc "mapping curve - either :linear or :exponential (:lin and :exp abbreviations are allowed)"}
              {:name "lag", :default 0.2 :doc "lag factor to dezipper cursor movement."}],
       :rates #{:kr}
       :doc "maps the current mouse X coordinate to a value between min and max"}

      {:name "MouseY"
              :args [{:name "min", :default 0.0 :doc "minimum value (when mouse is at the top of the screen)"}
              {:name "max", :default 1.0 :doc "maximum value (when mouse is at the bottom of the screen)"}
              {:name "warp",
               :default LINEAR
               :doc "mapping curve - either :linear or :exponential (:lin and :exp abbreviations are allowed)"}
              {:name "lag", :default 0.2 :doc "lag factor to smooth out cursor movement."}]
       :rates #{:kr}
       :doc "maps the current mouse Y coordinate to a value between min and max"}

      {:name "MouseButton",
       :args [{:name "up", :default 0.0 :doc "value when the key is not pressed"}
              {:name "down", :default 1.0 :doc "value when the key is pressed"}
              {:name "lag", :default 0.2 :doc "lag factor"}],
       :rates #{:kr}
       :doc "toggles between two values when the left mouse button is up or down"}

      {:name "KeyState",
       :args [{:name "keycode", :default 0.0 :doc "The keycode value of the key to check."}
              {:name "minval", :default 0.0 :doc "The value to output when the key is not pressed."}
              {:name "maxval", :default 1.0 :doc "The value to output when the key is pressed."}
              {:name "lag", :default 0.2 :doc "lag factor"
               }],
       :rates #{:kr}
       :doc "Toggles between two values when a key on the keyboard is up or down. Note that this ugen does not prevent normal typing. "}
      ])
