(ns overtone.examples.gui.keynome
  (:use overtone.live)
  (:require [seesaw [core :as sscore]
                    [keymap :as sskeymap]]))

(def keyboard
  [:1 :2 :3 :4 :5 :6 :7
   :q :w :e :r :t :y :u
   :a :s :d :f :g :h :j
   :z :x :c :v :b :n :m ])

(defn new-keynome []
  (let [action-map (ref {})
        click-handler (fn [e]
                        (let [key (keyword (sscore/config e :text))
                              f (@action-map key)]
                          (if f
                            (f)
                            (println "Key" key "pressed, no action"))))
        buttons (for [kwd keyboard]
                  (let [ b (sscore/button :text (name kwd))]
                    (sscore/listen b :action click-handler)
                    b))
        panel (sscore/grid-panel :rows 4 :columns 7 :hgap 5 :vgap 5 :items buttons)
        frame (sscore/frame :title "SeeSaw Keynome"
                            :content panel)]
    (doseq [btn buttons] (sskeymap/map-key frame (.toUpperCase (sscore/config btn :text)) btn :scope :global))
    (-> frame sscore/pack! sscore/show!)
    {:frame frame
     :action-map action-map
     :buttons (apply hash-map (interleave keyboard buttons))}))

(defn set-actions [ & stuff]
  (let [am ((first stuff) :action-map)
        kwd-action-pairs (partition 2 (rest stuff))]
    (doall (for [[kwd action] kwd-action-pairs]
             (dosync (ref-set am (assoc @am kwd action))))) nil))


;; make a short keyboard with Q key as Eb
(def marimba (new-keynome))
(definst marimba-note [freq 300]
  (let [src (sin-osc freq)
        env (env-gen (perc 0.01 1.0) :action FREE)]
    (* 0.9 src env)))

(def note-diff (Math/pow 2.0 (/ 1.0 12.0)))
(defn change-note [note steps]
  (* note (Math/pow note-diff steps)))

;; keyboard like, though we aren't restricted to that
(def melody-kbd
  (let [base-note (change-note 440 -6)
        keys [:q :a :s :e :d :r :f :t :g :h :u :j :i :k :l :p
              (keyword (str ";")) (keyword (str "["))]
        notes (for [i (range (count keys))] (change-note base-note i))]
    (doall (for [[key note] (partition 2 (interleave keys notes))]
             (set-actions marimba key #(marimba-note note))))
    {:keys keys :notes notes}))

;; lazy harmony, press z for bass only, x  for highs and bass
(def hit-count (ref 0))
(def base-note (change-note 440 -16))

(def bass-sequence [[0] [3 7] [3 7]
                    [2] [5 9] []
                    [3] [7 10] [7 10]
                    [2] [5 9] []])

(def high-sequence [[0 36 43] [3 7 36 43] [3 7 36 43]
                    [2 38 45] [5 9 38 45] [ 38 45]
                    [3 39 46] [7 10 39 46] [7 10 39 46]
                    [2 38 45] [5 9 38 45] [ 38 45]])

(defn play-bass []
  (let [cc (mod @hit-count 12)
        notes (nth bass-sequence cc)]
    (do (dosync (ref-set hit-count (inc @hit-count)))
        (doall (map #(marimba-note (change-note base-note %)) notes)))))

(defn play-high []
  (let [cc (mod @hit-count 12)
        notes (nth high-sequence cc)]
    (do (dosync (ref-set hit-count (inc @hit-count)))
        (doall (map #(marimba-note (change-note base-note %)) notes)))))

(set-actions marimba
             :x play-high
             :z play-bass
             :1 #(dosync (ref-set hit-count 0)) ;; reset sequence
             )

;; which is enough to play a Zelda theme on marimba
;; I saw somewhere on youtube..
