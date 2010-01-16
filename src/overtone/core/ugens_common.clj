(ns overtone.core.ugens-common)

; 0 do nothing when the UGen is finished
; 1 pause the enclosing synth, but do not free it
; 2 free the enclosing synth
; 3 free both this synth and the preceding node
; 4 free both this synth and the following node
; 5 free this synth; if the preceding node is a group then do g_freeAll on it, else free it
; 6 free this synth; if the following node is a group then do g_freeAll on it, else free it
; 7 free this synth and all preceding nodes in this group
; 8 free this synth and all following nodes in this group
; 9 free this synth and pause the preceding node
; 10 free this synth and pause the following node
; 11 free this synth and if the preceding node is a group then do g_deepFree on it, else free it
; 12 free this synth and if the following node is a group then do g_deepFree on it, else free it
; 13 free this synth and all other nodes in this group (before and after)
; 14 free the enclosing group and all nodes within it (including this synth)

(defn done-action [int-or-sym]
  (let [da-map {:none 0	
                :nothing 0
                :pause 1	
                :free 2	
                :free-with-preceding 3	
                :free-with-following 4
                :free-with-preceding-group 5	
                :free-with-following-group 6
                :free-upto-this 7	
                :free-from-this-on 8	
                :free-pause-preceding 9
                :free-pause-following 10
                :free-with-preceding-group-deep 11
                :free-with-following-group-deep 12	
                :free-with-siblings 13	
                :free-group 14}]
      (da-map int-or-sym int-or-sym)))

(defn parse-done-action [rate argspec args]
  )

;; checks

(defn rate-of [obj]
  (:rate obj))

(defn rate-of? [obj rate]
  (= (rate-of obj) rate))

(defn name-of [obj]
  (:name obj))

(defn name-of? [obj name]
  (= (name-of obj) name))

(defn ar? [obj] (= (rate-of obj) :ar))
(defn kr? [obj] (= (rate-of obj) :kr))
(defn ir? [obj] (= (rate-of obj) :ir))
(defn dr? [obj] (= (rate-of obj) :dr))

(defmacro defcheck [name params default-message expr]
  (let [message (gensym "message")
        params-with-message (conj params message)]
    `(defn ~name
       (~params
        (fn ~'[rate num-outs inputs spec]
          (when-not ~expr ~default-message)))
       (~params-with-message
        (fn ~'[rate num-outs inputs spec]
          (when-not ~expr ~message))))))

(defcheck same-rate-as-first-input []
  (str (name-of (first inputs)) "must be same rate as called ugen, i.e. " rate)
  (= (rate-of (first inputs)) rate))

(defcheck first-input-ar []
  "the first input must be audio rate"
  (ar? (first inputs)))

(defcheck all-inputs-ar []
  "all inputs must be audio rate"
  (every? ar? inputs))

(defcheck first-n-inputs-ar [n]
  (str "the first " n " inputs must be audio rate")
  (every? ar? (take n inputs)))

(defcheck after-n-inputs-rest-ar [n]
  (str "all but the first " n " inputs must be audio rate")
  (every? ar? (drop n inputs)))

(defcheck all-but-first-input-ar []
          ""
          true)

(defcheck nth-input-ar [index]
          ""
          true)

(defcheck named-input-ar [name]
          ""
          true)

(defcheck num-outs-greater-than [n]
  (str "must have " (+ n 1) " or more output channels"))

(defn check-all [& check-fns]
  (let [all-checks (juxt check-fns)]
    (fn [rate num-outs inputs spec]
      (let [errors (all-checks rate num-outs inputs spec)]
        (when-not (every? nil? errors)
                (apply str errors))))))

(defn when-ar [& check-fns]
  (fn [rate num-outs inputs spec]
    (if (= rate :ar)
      (apply check-all check-fns))))

