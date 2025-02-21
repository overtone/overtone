(ns overtone.studio.event-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [overtone.music.rhythm :refer [metronome]]
   [overtone.studio.event :as event]
   [overtone.studio.transport :as transport]
   [overtone.sc.node]))

(def player-init-keys [:align :clock :offset :proto :quant])
(def player-run-keys [:beat :playing])

(def dummy-instrument (reify overtone.sc.node/IControllableNode
                        (node-control [_this _params])))
(def dummy-pattern [{:type :ctl
                     :instrument dummy-instrument
                     :dur 4}])

(deftest pplay
  (testing "pplay parameters initialized with defaults"
    (event/pplay ::player1
                 dummy-pattern)
    (is (= {:clock  transport/*clock*
            :offset 0
            :align  :wait
            :proto  nil
            :quant  4}
           (-> @event/pplayers ::player1
               (select-keys player-init-keys)))))
  (testing "pplay parameters accepted as kv params"
    (let [clock (metronome 100)]
      (event/pplay ::player2
                   dummy-pattern
                   :clock  clock
                   :offset 2
                   :align  :quant
                   :proto  {:mode :minor}
                   :quant  8)
      (is (= {:clock  clock
              :offset 2
              :quant  8
              :align  :quant
              :proto  {:mode :minor}}
             (-> @event/pplayers ::player2
                 (select-keys player-init-keys)))))))

(deftest padd
  (testing "padd parameters initialized with defaults"
    (event/padd ::player1
                dummy-pattern)
    (is (= {:clock  transport/*clock*
            :offset 0
            :align  :wait
            :proto  nil
            :quant  4}
           (-> @event/pplayers ::player1
               (select-keys player-init-keys)))))
  (testing "padd parameters accepted as kv params"
    (let [clock (metronome 100)]
      (event/padd ::player2
                  dummy-pattern
                  :clock  clock
                  :offset 2
                  :align  :quant
                  :proto  {:mode :minor}
                  :quant  8)
      (is (= {:clock  clock
              :offset 2
              :align  :quant
              :quant  8
              :proto  {:mode :minor}}
             (-> @event/pplayers ::player2
                 (select-keys player-init-keys)))))))
