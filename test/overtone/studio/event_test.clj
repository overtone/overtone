(ns overtone.studio.event-test
  (:require
   [clojure.test :refer [is]]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :refer [for-all]]
   [overtone.music.rhythm :refer [metronome metro-start]]
   [overtone.studio.event :as event]
   [overtone.studio.pattern :as pattern]
   [overtone.sc.node]))

(def dummy-instrument (reify overtone.sc.node/IControllableNode
                        (node-control [_this _params])))

(defspec quantization 1000
  (let [align      (gen/elements [:wait :quant :none])
        quant      (gen/choose 1 10)
        offset     (gen/choose 0 10)
        duration   (gen/such-that #(> % 0) gen/ratio 100)
        pattern    (gen/bind
                    (gen/choose 1 20)
                    (fn [n]
                      (gen/vector
                       (gen/hash-map
                        :type (gen/return :ctl)
                        :instrument (gen/return dummy-instrument)
                        :dur duration)
                       1 n)))
        clock      (gen/return (metronome 1))

        params (gen/hash-map
                :align  align
                :quant  quant
                :offset offset
                :clock  clock)]
    (for-all [start-beat    (gen/choose 1 100)
              start-after   (gen/choose 0 10)
              params        params
              pseq-a        pattern
              pseq-b        pattern
              key-b         (gen/elements [::a ::b])]
             (with-redefs [event/schedule-next-job (fn [_clock _beat _k])]
               ;; Clear players
               (event/pclear)

               ;; metro-start rounds up to next beat, so we decrement to
               ;; ensure (clock) yields start-beat
               (metro-start (:clock params) (dec start-beat))
               (event/pplay ::a pseq-a params)

               (when-not (zero? start-after)
                 (metro-start (:clock params)
                              (dec (+ start-beat start-after))))
               (event/pplay key-b pseq-b params)

               (let [{:keys [align quant offset]} params
                     {next-beat-a :beat} (::a @event/pplayers)
                     {next-beat-b :beat
                      next-seq-b :pseq}  (key-b @event/pplayers)
                     b-start (+ start-beat start-after)]
                 (when (= key-b ::b)
                   (is (= start-beat next-beat-a)))
                 (case align
                   :wait  (if (= key-b ::a)
                            (let [q-mod (mod (- quant (mod start-after quant)) quant)
                                  quantized-offset (+ q-mod offset)
                                  pseq (concat
                                        (event/take-pseq quantized-offset pseq-a)
                                        pseq-b)]
                              (is (= b-start next-beat-b))
                              (is (= pseq next-seq-b)))
                            (let [q-mod (mod (- quant (mod start-after quant)) quant)
                                  quantized-b-start (+ b-start q-mod offset)]
                              (is (= quantized-b-start next-beat-b))
                              (is (= pseq-b next-seq-b))))
                   :quant (let [beats-to-remove (- (mod start-after quant) offset)
                                ;; The following repeats the implementation of align-pseq
                                ;; How could this be tested without doing that?
                                [diff pseq] (loop [to-remove beats-to-remove
                                                   pseq pseq-b]
                                              (cond
                                                (<= to-remove 0) [to-remove pseq]
                                                (empty? pseq) [0 pseq]
                                                :else
                                                (let [dur (event/eget (pattern/pfirst pseq) :dur)]
                                                  (recur (- to-remove dur)
                                                         (pattern/pnext pseq)))))
                                quantized-b-start (- b-start diff)]
                            (is (= quantized-b-start next-beat-b))
                            (is (= pseq next-seq-b)))
                   :none  (do
                            (is (= b-start next-beat-b))
                            (is (= pseq-b next-seq-b)))))))))
