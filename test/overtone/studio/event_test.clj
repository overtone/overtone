(ns overtone.studio.event-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [com.gfredericks.test.chuck.clojure-test :refer [checking for-all] :as prop]
   [overtone.music.rhythm :refer [metronome metro-start]]
   [overtone.studio.event :as event]
   [overtone.studio.transport :as transport]
   [overtone.sc.node]))

(def dummy-instrument (reify overtone.sc.node/IControllableNode
                        (node-control [_this _params])))
(def dummy-pattern [{:type :ctl
                     :instrument dummy-instrument
                     :dur 4}])

(defspec quantization 10
  (let [align      (gen/elements [:wait #_:quant #_:none])
        quant      (gen/choose 1 10)
        offset     (gen/choose 0 10)
        pattern    (gen/return dummy-pattern)
        clock      (gen/return (metronome 1))

        params (gen/hash-map
                :align align
                :quant quant
                :offset offset
                :clock clock)

        pseq-a dummy-pattern]
    (for-all [start-beat (gen/choose 1 100)
              start-after (gen/choose 0 10)
              params     params
              pseq-b     pattern]
             (with-redefs [event/schedule-next-job (fn [_clock _beat _k])]
               ;; Clear players
               (event/pclear)

               ;; metro-start rounds up to next beat, so we decrement to
               ;; ensure (clock) yields start-beat
               (metro-start (:clock params) (dec start-beat))
               (event/pplay ::a pseq-a params)

               (when (< 0 start-after)
                 (metro-start (:clock params) (dec (+ start-beat start-after))))
               (event/pplay ::b pseq-b params)

               (let [{:keys [align quant offset]} params
                     {next-beat-a :beat} (::a @event/pplayers)
                     {next-beat-b :beat
                      next-seq-b :pseq}  (::b @event/pplayers)]
                 (is (= start-beat next-beat-a))
                 (case align
                   :wait  (let [b-start (+ start-beat start-after)
                                q-mod   (mod (- quant (mod start-after quant)) quant)
                                quantized-b-start (+ b-start q-mod #_offset)]
                            #_(future (prn [:start-beat start-beat
                                            :start-after start-after
                                            :b-start b-start
                                            :quant quant
                                            :offset offset
                                            :quantized quantized-b-start
                                            :next-beat-b next-beat-b
                                            :pseq-b-start-fix pseq-b-start-fix
                                            :fixed-quant (+ quantized-b-start
                                                            (if (zero? start-after)
                                                              pseq-b-start-fix
                                                              0))]))
                            (is (= quantized-b-start next-beat-b)))
                   :quant (is (= 1 1))
                   :none  (is (= 1 1))))))))
