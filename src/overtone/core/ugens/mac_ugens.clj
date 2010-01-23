
(ns overtone.core.ugens.mac-ugens)

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
       :args [{:name "minval", :default 0.0}
              {:name "maxval", :default 1.0}
              {:name "warp",
               :default :linear 
               :map {:linear 0 :exponential 1 :lin 0 :exp 1}}
              {:name "lag", :default 0.2}],
       :rates #{:kr}}

      ;; MouseY : MouseX {}

      {:name "MouseY" :extends "MouseY"}

      ;; MouseButton : UGen {
      ;; 	*kr {
      ;; 		arg minval=0, maxval=1, lag=0.2;
      ;; 		^this.multiNew('control', minval, maxval, lag)
      ;; 	}
      ;; }

      {:name "MouseButton",
       :args [{:name "minval", :default 0.0}
              {:name "maxval", :default 1.0}
              {:name "lag", :default 0.2}],
       :rates #{:kr}}

      ;; KeyState : UGen {
      ;; 	*kr {
      ;; 		arg keycode=0, minval=0, maxval=1, lag=0.2;
      ;; 		^this.multiNew('control', keycode, minval, maxval, lag)
      ;; 	}
      ;; }

      {:name "KeyState",
       :args [{:name "keycode", :default 0.0}
              {:name "minval", :default 0.0}
              {:name "maxval", :default 1.0}
              {:name "lag", :default 0.2}],
       :rates #{:kr}}])