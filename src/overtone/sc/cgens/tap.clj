(ns overtone.sc.cgens.tap
  (:use [overtone.sc defcgen ugens]
        [overtone.sc.machinery.ugen.fn-gen :only [with-ugen-meta]]))

(defcgen tap
  "Tap the hell out of ugens for great win"
  [label {:doc "String label for this tap. Must be unique to a given synth."}
   freq  {:doc "Frequency of tap value updates"}
   src   {:doc "Ugen to tap"}]
  "Allows you to tap arbitrary ugens within a given synth. The
  containing synth then automatically gets atoms for each ugen you tap
  which will automagically be populated by the latest ugen value updated
  at the specified frequency."
  (:kr (let [tr      (impulse freq)
             src     (if (= :ar (:rate-name src))
                       (a2k src)
                       src)
             rand-id (rand-int 64000) ;;super unlikely to clash within a given synth
             reply   (with-ugen-meta
                       (send-reply tr "/overtone/tap" [src] rand-id)
                       {:instance-fn (fn [synth]
                                       (assoc synth
                                         :taps (assoc (:taps synth) label (atom nil))
                                         :tap-labels (assoc (:tap-labels synth) rand-id label)))})]
         src)))
