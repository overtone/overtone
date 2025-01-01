(ns overtone.studio.event-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [overtone.music.rhythm :refer [metronome]]
   [overtone.studio.event :as event]
   [overtone.studio.transport :as transport]))

(deftest pplay
  (testing "pplay parameters initialized with defaults"
    (event/pplay ::player1
                 [{:type :ctl
                   :dur 4}])
    (is (= {:clock  transport/*clock*
            ;; :offset 0
            :proto  nil
            :quant  4}
           (-> @event/pplayers ::player1
               (select-keys [:clock :offset :proto :quant])))))
  (testing "pplay parameters accepted as kv params"
    (let [clock (metronome 100)]
      (event/pplay ::player2
                   [{:type :ctl
                     :dur 4}]
                   :clock clock
                   :offset 2
                   :proto {:mode :minor}
                   :quant 8)
      (is (= {:clock  clock
              ;; :offset 2
              :quant  8
              :proto  {:mode :minor}}
             (-> @event/pplayers ::player2
                 (select-keys [:clock :offset :proto :quant])))))))
