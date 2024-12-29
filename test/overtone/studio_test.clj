(ns overtone.studio-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [overtone.test-helper :refer [ensure-server with-sync-reset]]
   [overtone.libs.deps :as ov.deps]
   ;; We call it just to make sure that we are able to load the built-in insts
   ;; before starting the server.
   overtone.inst.synth
   matcher-combinators.test)
  (:use overtone.core))

(definst bar [freq 200]
  (* (env-gen (perc 0.1 0.8) 1 1 0 1 FREE)
     (rlpf (saw freq) (* 1.1 freq) 0.3)
     0.4))

(definst buz [freq 200]
  (* (env-gen (perc 0.1 0.8) 1 1 0 1 FREE)
     (+ (sin-osc (/ freq 2))
        (rlpf (saw freq) (* 1.1 freq) 0.3))
     0.4))

(definst foo [freq 440]
  (* 0.8
     (env-gen (perc 0.1 0.4) :action FREE)
     (rlpf (saw [freq (* 0.98 freq)])
           (mul-add (sin-osc:kr 30) 100 (* 1.8 freq)) 0.2)))

(definst kick [freq 240]
  (* 0.8
     (env-gen (perc 0.01 0.3) :action FREE)
     (sin-osc freq)))

(deftest studio-test
  (when-not (server-connected?)
    (testing "before starting server"
      (is (= {:group nil
              :instance-group nil
              :fx-group nil
              :mixer nil}
             (-> (select-keys kick [:group :instance-group :fx-group :mixer])
                 (update-vals deref))))))

  (testing "after starting server"
    (with-sync-reset
      (fn []
        (ensure-server
         (fn []
           ;; Wait for insts loading after the server startup.
           (ov.deps/wait-until-deps-satisfied [:insts-loaded])

           ;; Test it!
           (is (match?
                ;; We don't test `:group` here as it's `nil` while loading.
                {:instance-group some?
                 :fx-group some?
                 :mixer some?}
                (-> (select-keys kick [:instance-group :fx-group :mixer])
                    (update-vals deref))))))))))
