(ns overtone.examples.buses.ambisonics
  (:use overtone.live))

;; Ambisonics is a full-sphere surround sound format.
;; See https://en.wikipedia.org/wiki/Ambisonics

;; SuperCollider has some support for full First Order Ambisonics (3D), using 4
;; channels (W, X, Y, and Z). However, it has only a 2D rendering function,
;; `decode-b2` that takes just 3 channels as input (W, X, and Y). 2D audio means
;; that all audio sources are assumed to be placed in the plane of the listener's
;; ears, horizontal to the ground, with no varying elevation.

;; This example file is a demonstration of how to create a complete
;; 2D Ambisonic audio pipeline in Overtone.

;; Overtone's default definst assumes a stereo output pipeline.
;; Instrument output is controlled by one of two mixers created when the
;; instrument is defined, either mono-inst-mixer or stereo-inst-mixer.
;; These mixers take volume and pan parameters.

;; To support Ambisonic instruments, we replace the default instrument
;; mixer with Ambisonic-aware mixers that place the instrument in a plane.
;; The basic 2D mixer represents audio positions in polar coordinates, using
;; azimuth and gain. One could build on the polar coordinate mixer to support
;; Cartesian coordinates, and of course, the location of an instrument in
;; the plane may be as dynamic as anything else in Overtone.

;; The output of the polar mixers is 3 channels of Ambisonic audio: W, X, and Y.
;; These Ambisonic audio channels from all instruments are mixed together onto
;; the foa-output-bus. The contents of this three channel bus is then rendered
;; into the output speaker configuration. `decode-b2` handles 2D rendering, with
;; any number of speakers evenly spaced in a circle around the listener.
;; `start-foa` configures the studio with a given number of speakers,
;; starts the foa-output-synth, and replaces the default instrument mixer
;; with Ambisonic mixers.

(def n-foa-channels 3)  ;; 3 channels for 2D First Order Ambisonics (FOA)

(defonce foa-output-bus (audio-bus n-foa-channels "ambisonic-output"))

;; Instrument mixers - mono, stereo, and direct 2D FOA

(defsynth mix-polar-mono
  "Places the source mono bus at a particular azimuth and gain"
  [in-bus 0 az 0 gain 1]
  (let [source (in in-bus)]
    (->> (pan-b2 :in source :azimuth az :gain gain)
         (out foa-output-bus))))

(defsynth mix-polar-stereo
  "Places the source stereo signal with the given spread centered at a
   particular azimuth and gain.

  `az` and `gain` are defined as in pan-b2.
  `spread` has the same units as azimuth (`radians/pi`), 0 has the left and
  right channels coming from the same source, 0.5 places them 90 degrees
  from each other, and 1.0 places them diametrically opposite."
  [in-bus 0 az 0 gain 1 spread 0.5]
  (let [[left right] (in :bus in-bus :num-channels 2)
        half-spread (* spread 0.5)
        left-amb (pan-b2 :in left :azimuth (wrap (- az half-spread) -1 1) :gain gain)
        right-amb (pan-b2 :in right :azimuth (wrap (+ az half-spread) -1 1) :gain gain)]
    (->> (map vector left-amb right-amb)
         (map sum)
         (out foa-output-bus))))

(defsynth mix-direct-foa
  "An Ambisonic instrument produces WXY channels directly.
  This mixer mixes them directly onto the foa output bus."
  [in-bus 0]
  (->> (in :bus in-bus :num-channels n-foa-channels)
       (out foa-output-bus)))

(defn create-ambisonic-mixer [n-chans & params]
  (cond
    (= n-chans 1)
    mix-polar-mono

    (= n-chans 2)
    mix-polar-stereo

    ;; Demonstration only - there may be multichannel instruments that
    ;; match here, but have multiple channels for other reasons.
    ;; Whereas the default output mixers of Overtone instruments ignore
    ;; channels after the first two, those existing
    ;; instruments would not work correctly with this mixer.
    (= n-chans n-foa-channels)
    mix-direct-foa))

(defn- def-foa-output-synth [n-speakers orientation]
  ;; Wrapped defsynth because n-speakers and orientation are constant numbers.
  ;; Trying to make them synth parameters yielded:
  ;; >> Argument for ugen DecodeB2 must be a number, yet found: #overtone.sc.machinery.ugen.sc_ugen.ControlProxy
  (defsynth foa-output
    "Constantly running synth that decodes from the FOA bus to the output bus."
    []
    (let [[w x y] (in foa-output-bus n-foa-channels)]
      (out 0 (decode-b2 n-speakers w x y orientation)))))

(defn stop-foa
  "Restore the default Overtone output pipeline."
  []
  (when-let [output-mixer (get @studio* ::output-mixer)]
    (kill output-mixer)
    (swap! studio* dissoc ::output-mixer))
  (replace-all-inst-mixer! default-get-inst-mixer))

(defn start-foa
  "Configure and start the Ambisonic output pipeline."
  [n-speakers & {:keys [orientation]
                 :or {orientation 0.5}}]
  (def-foa-output-synth n-speakers orientation)
  (when-let [output-mixer (get @studio* ::output-mixer)]
    (kill output-mixer))
  ;; Start FOA output synth before the real output group but after
  ;; instrument nodes and safe from stopping with `stop`.
  (let [output-synth (foa-output [:head (foundation-safe-post-default-group)])]
    (swap! studio* assoc ::output-mixer output-synth))
  (replace-all-inst-mixer! create-ambisonic-mixer))

(comment
  ;; More fun with more speakers, but you can try it out with 2 without
  ;; adding hardware.
  (start-foa 2)

  ;; Move an instrument around
  (require '[overtone.inst.drum :as drum])
  (drum/bing)
  (inst-mixer-ctl! drum/bing :az -0.5 :gain 0.5)  ;; To the left
  (inst-mixer-ctl! drum/bing :az 0.5 :gain 0.75)  ;; To the right, "closer"

  ;; Sine instrument with rotation, taking advantage of `mix-direct-foa`
  ;; defined above.
  (definst sin-rotator [freq 440 rpm 30 gain 0.5]
    (let [azimuth (var-saw:kr :width 1.0 :freq (/ rpm 60))]
      (pan-b2 :in (sin-osc freq) :azimuth azimuth :gain gain)))
  (sin-rotator :rpm 60)
  (stop)

  ;; Restore original instrument output mixers
  (stop-foa)
  )
