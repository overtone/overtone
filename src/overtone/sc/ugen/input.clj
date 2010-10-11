(ns overtone.sc.ugen.input)

(def specs
     [
      ;; MouseX : UGen
      ;; {
      ;; 	// warp 0 = linear
      ;; 	// warp 1 = exponential
      ;; 	*kr {
      ;; 		arg minval=0, maxval=1, warp=0, lag=0.2;
      ;; 		if (warp === \linear, { warp = 0 });
      ;; 		if (warp === \exponential, { warp = 1 });
      ;; 		^this.multiNew('control', minval, maxval, warp, lag)
      ;; 	}
      ;; }

      {:name "MouseX",
       :args [{:name "min", :default 0.0}
              {:name "max", :default 1.0}
              {:name "warp",
               :default :linear
               :map {:linear 0 :exponential 1 :lin 0 :exp 1}}
              {:name "lag", :default 0.2}],
       :rates #{:kr}
       :doc "maps the current mouse X coordinate to a value between min and max"}

      {:name "MouseY" :extends "MouseX"
       :doc "maps the current mouse Y coordinate to a value between min and max"}

      {:name "MouseButton",
       :args [{:name "up", :default 0.0}
              {:name "down", :default 1.0}
              {:name "lag", :default 0.2}],
       :rates #{:kr}
       :doc "toggles between two values when the left mouse button is up or down"}

      {:name "KeyState",
       :args [{:name "keycode", :default 0.0}
              {:name "minval", :default 0.0}
              {:name "maxval", :default 1.0}
              {:name "lag", :default 0.2}],
       :rates #{:kr}
       :doc "toggles between two values when a key on the keyboard is up or down"}
      ])
