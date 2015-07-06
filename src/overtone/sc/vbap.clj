(ns ^{:doc "vbap reimplementation based on Ville Pukki's code for pd
    and its sc port by Scott Wilson."
      :author "Orm Finnendahl"
      :date "06/06/2015"}
  overtone.sc.vbap)

;; VBAP originally created by Ville Pukki
;; This version is a complete reimplementation
;; of the ver 0.99 PD code by Ville Pukki
;; and Scott Wilson's supercollider adoption according to the
;; paper "Creating Auditory Displays with Multiple Loudspeakers Using
;; VBAP: A Case Study with DIVA Project" by Ville Pukki.
;;
;; The original C-code was written by Ville Pulkki 1999
;; Helsinki University of Technology
;; and
;; University of California at Berkeley

;;; vector utility functions

(defn- sqr [n] (* n n))
(defn- sqrt [n] (Math/sqrt n))
(defn- abs [n] (max n (- n)))
;; (defn- mabs [n] (Math/abs n))
(defn- acos [n] (Math/acos n))
(defn- cos [n] (Math/cos n))
(defn- sin [n] (Math/sin n))
(def v+ (partial mapv +))
(def v- (partial mapv -))
(def v= (comp (partial every? true?) (partial map ==)))
(defn- v-len-sqr [u] (reduce + (map sqr u)))
(def v-len (comp sqrt v-len-sqr))

(defn round2
  "Round a double to the given precision (number of significant digits)"
  ([d] (round2 d 3))
  ([d precision]
    (let [factor (Math/pow 10 precision)]
      (/ (Math/round (* d factor)) factor))))

(defn- v* [u v]
  (cond
    (and (vector? u) (vector? v)) (mapv * u v)
    (vector? u) (mapv (partial * v) u)
    (vector? v) (mapv (partial * u) v)))

(defn- v-neg [u] (v* -1 u))
(def v-dist (comp v-len v-))
(def v-dist-sqr (comp v-len-sqr v-))

(defn- v-setlen [u t]
  (let [dim (count u)]
    (if (every? (partial == 0) u)     ; [0 0 ...]?
      (into [t] (repeat (dec dim) 0)) ; [t 0 ...]
      (v* u (/ t (v-len u))))))

(defn- v-norm [u]
  (v-setlen u 1))

(defn- v-dot [u v]
  (reduce + (map * u v)))

(defn- v-cross-prod [[x1 y1 z1] [x2 y2 z2]]
  (v- (v* [y1 z1 x1] [z2 x2 y2])
      (v* [z1 x1 y1] [y2 z2 x2])))

(defn- v-unq-cross-prod [u v]
  (v-norm (v-cross-prod u v)))

(defn- norm-clip
  "clip n to range [-1..1]. Returns a float."
  [n]
  (* (Math/signum (float n)) (min 1 (abs n))))

(defn- v-angle
  "return angle between u and v (any dimension) in radians."
  [u v]
  (-> (/ (v-dot u v)
         (* (v-len u) (v-len v)))
      norm-clip acos abs))

(defn- mtx-transpose [m]
  (apply mapv vector m))

(defn- v-perm-prod-diff [[a1 a2 a3] [b1 b2 b3]]
  (v- (v* [a2 a3 a1] [b3 b1 b2])
      (v* [b2 b3 b1] [a3 a1 a2])))

(defn- inv-det
  "calc inverse-determinant of 2x2 or 3x3 matrices supplied as seq of
  2-d/3-d vectors."
  ([[x1 x2] [y1 y2]]
   (/ (- (* x1 y2) (* x2 y1))))
  ([x y z]
   (/ (reduce + (v* x (v-perm-prod-diff y z))))))

(defn- mtx-2d-inverse [[x y]]
  (let [[x1 x2] x, [y1 y2] y]
    (mapv #(v* (inv-det x y) %) [[y2 (- x2)] [(- y1) x1]])))

(defn- mtx-3d-inverse [[x y z]]
  (mapv #(v* (inv-det x y z)
             (v-perm-prod-diff %1 %2)) [y z x] [z x y]))

(def any? (comp boolean some))

(defn- get-coords [speaker-set]
  (into [] (map :coords speaker-set)))

(defn- get-ls-nos [speaker-set]
  (map #(+ 1 (:chan-offset %)) speaker-set))

(defn- get-offsets [speaker-set]
  (map :chan-offset speaker-set))

(defn- idx->coords [idx speakers]
  "return the coords of a speaker with given idx (= chan-offset)."
  (loop [spks speakers]
    (cond (empty? spks) '()
          (= idx (:chan-offset (first spks)))
          (:coords (first spks))
          :else (recur (rest spks)))))

(defn- all-combinations-vector [l n]
  "return a vector with all n combinations of a seq l."
  (cond (= n 1) (into [] (map vector l))
        (empty? l) []
        :else (into
               (into [] (map #(into (vector (first l)) %)
                             (all-combinations-vector (rest l) (dec n))))
               (all-combinations-vector (rest l) n))))

(defn- all-combinations-set [l n]
  "return a set with all n combinations of a seq l."
  (cond (= n 1) (into [] (map #(conj #{} %) l))
        (empty? l) []
        :else (into
               (all-combinations-set (rest l) n)
               (into [] (map #(conj % (first l))
                             (all-combinations-set (rest l) (dec n)))))))

(defn- ang->cart
  "multi-arity (2-d and 3-d) function returning a vector with
  cartesian coordinates of a given azimuth angle (or azimuth
  angle/elevation) pair."
  ([azi]
    (let [atorad (* Math/PI 2/360)]
      [(* (cos (* azi atorad)))
       (* (sin (* azi atorad)))]))
  ([azi ele]
    (let [atorad (* Math/PI 2/360)]
      [(* (cos (* azi atorad)) (cos (* ele atorad)))
       (* (sin (* azi atorad)) (cos (* ele atorad)))
       (sin (* ele atorad))])))

(defn- fit-angle [angle]
  "fit angle into the interval ]-180..180] degrees"
  (let [modangle (mod (+ angle 180) 360)]
    (if (= modangle 0) 180 (- modangle 180))))

(defn- init-speaker
  "given an index (= chan-offset) and a direction (azimuth number for
  2-d and azimuth/elevation vector for 3-d) return a map with all
  necessary information for a single speaker."
  ([idx dir]
   (if (number? dir) ;;; 2-d or 3-d?
       (let [fitted-azi (fit-angle dir)]
         {:azi fitted-azi :coords (ang->cart fitted-azi) :chan-offset idx})
       (let [[azi ele] dir
             fitted-azi (fit-angle azi)]
         {:azi fitted-azi :ele ele :coords (ang->cart fitted-azi ele) :chan-offset idx}))))

(defn- get-speaker-maps [directions]
  (map-indexed init-speaker directions))

;;; 3-d helper functions

(defn- ls-outside? [ls inv-matrix]
  "given the coordinate vector of a speaker and the inverse matrix of
  a speaker triplet, check, if the speaker is outside the area of the
  triplet."
  (any? #(< (v-dot ls %) -0.001) inv-matrix))

(defn- remove-triplet
  "remove all speakers of triplet from speakers sequence."
  [triplet speakers]
  (filter (fn [ls] (every? #(not= % ls) triplet))
          speakers))

(defn- every-ls-outside-triplet?
  "check whether every speaker in the speakers seq apart from the
  triplet speakers themselves is outside the area of the triplet."
  [triplet speakers]
  (let [inv-mtx (mtx-3d-inverse (get-coords triplet))]
    (every? #(ls-outside? (:coords %) inv-mtx)
            (remove-triplet triplet speakers))))


(defn- vol-p-side-lgth [[i j k]]
  "calculate volume of the parallelepiped defined by the loudspeaker
  direction vectors and divide it with the total length of the
  triangle sides. This is used when removing too narrow triangles."
  (let [volper (abs (v-dot (v-unq-cross-prod i j) k))
        lgth (reduce + (map #(apply v-angle %)
                            [[i j] [i k] [j k]]))]
    (if (> lgth 0.00001) (/ volper lgth) 0)))

(defn- lines-intersect?
  "check if lines i j and k l intersect on the unit sphere."
  [[i j] [k l]]
  (let [v3 (v-unq-cross-prod ;;; crossing point of planes ij and kl on unit sphere
            (v-unq-cross-prod i j)
            (v-unq-cross-prod k l))
        nv3 (v-neg v3) ;;; crossing point on opposite side of unit sphere
        d-ij (v-angle i j), d-kl (v-angle k l) ;;; distances between points
        d-iv3 (v-angle i v3), d-jv3 (v-angle j v3)
        d-kv3 (v-angle k v3), d-lv3 (v-angle l v3)
        d-inv3 (v-angle i nv3), d-jnv3 (v-angle j nv3)
        d-knv3 (v-angle k nv3), d-lnv3 (v-angle l nv3)]
    (and
;;; no speaker close to crossing points
     (every? #(> (abs %) 0.01)
             [d-iv3 d-jv3 d-kv3 d-lv3 d-inv3 d-jnv3 d-knv3 d-lnv3])
;;; crossing point is on lines between both speaker pairs
     (or (and (<= (abs (- d-ij (+ d-iv3 d-jv3))) 0.01)
              (<= (abs (- d-kl (+ d-kv3 d-lv3))) 0.01))
         (and (<= (abs (- d-ij (+ d-inv3 d-jnv3))) 0.01)
              (<= (abs (- d-kl (+ d-knv3 d-lnv3))) 0.01))))))

(defn- get-all-connections
  "return all possible speaker connections as sets, sorted by their distance."
  [available-triplets]
  (map :speakers ;;; we only need the speaker pairs
       (sort #(< (:dist %1) (:dist %2)) ;; sort pairs by distance
;;; collect maps of all possible speaker pairs and their distances
             (map #(assoc {} :speakers %, :dist (apply v-angle (map :coords %)))
;;; by reducing sets of all possible pairs in all available triplets
;;; into a set, clojure automagically removes duplicates regardless of
;;; the ordering of the pairs themselves:
                  (reduce into #{} (map #(all-combinations-set % 2) available-triplets))))))

;;; while traversing the connections list (provided as a seq of
;;; speaker pairs sorted by distance, see function above) remove
;;; all subsequent pairs which intersect the current pair. The
;;; returned result will contain no intersecting connections.

(defn- remove-intersecting-pairs [connections]
  (cond (empty? connections) '()
        :else (cons (first connections)
                    (remove-intersecting-pairs
                     (filter #(not (lines-intersect?
                                    (into [] (map :coords (first connections)))
                                    (into [] (map :coords %))))
                             (rest connections))))))

(defn- contained? [conn connections]
  (any? #(= % conn) connections))

(defn- triplet-connectable? [triplet connections]
  "is every speaker pair in the triplet contained in connections?"
  (every? #(contained? % connections)
          (all-combinations-set triplet 2)))

;;; main function for 3d: Return all triplets, which cover the whole
;;; 3-d space of the speaker-arrangement. The speaker triplets are
;;; returned as lists of speaker maps, each map containing the
;;; azimuth, elevation, coordinates and channel-offset of an
;;; individual speaker.

(defn- get-3d-triplets [speakers]
  (let [all-valid-triplets (filter #(> (vol-p-side-lgth (get-coords %)) 0.01)
                                   (all-combinations-vector speakers 3))
        valid-connections (remove-intersecting-pairs
                           (get-all-connections all-valid-triplets))]
    (filter #(and (triplet-connectable? % valid-connections)
                  (every-ls-outside-triplet? % speakers))
            all-valid-triplets)))

;;; collect the speaker numbers, the inverse-matrix and matrix for one
;;; single speaker triplet as a one-dimensional vector.

(defn- collect-3d-vbap-data [triplet]
  (let [coords (get-coords triplet)
        ls-nos (into [] (get-ls-nos triplet))]
    (flatten (conj ls-nos
                   (mtx-3d-inverse coords)
                   (mtx-transpose coords)))))

;;; collect the data of all triplets into a single one-dimensional
;;; vector containing the data of all 3d-triplets suitable for the
;;; vbap ugen.

(defn- get-3d-vbap-buffer-data [speakers]
  (reduce into [3 (count speakers)]
          (map #(map round2 (collect-3d-vbap-data %))
               (get-3d-triplets speakers))))

;;; 2-d case:

(defn- sort-2d-speakers [speakers]
  "speaker azimuths have to be reduced to [-180..180] degrees before
  calling this function."
  (sort #(< (:azi %1) (:azi %2)) speakers))

(defn- speaker-2d-back-angle [sp1 sp2]
  "determine counterclockwise angle between two clockwise-sorted
  speakers (supplied angles in the interval [-180..180]), result in
  the interval [0..360]."
  (let [{azi1 :azi} sp1
        {azi2 :azi} sp2]
    (+ 360 (- azi1 azi2))))

;;; main function for 2-d: Return all pairs, which cover the whole
;;; 2-d speaker-arrangement. The speakers are sorted clockwise,
;;; starting at the center behind the listener. In case the
;;; counterclockwise angle of the first and last speaker is < 170
;;; degree, their panning will be included, resulting in the
;;; possibility to use azimuth values for 360 degrees. Otherwise
;;; reasonable panning is only possible at angles (clockwise) between
;;; the angles of the first and last speaker and strange things can
;;; happen at other angles.

(defn- get-2d-pairs [speakers]
  (let [speakers (into [] (sort-2d-speakers speakers))
        speaker-ring (if (< (speaker-2d-back-angle
                             (first speakers) (last speakers))
                            170)
                       (conj speakers (first speakers))
                       speakers)]
    (map list speaker-ring (rest speaker-ring))))

;;; similar to the 3d functions of the same name:

(defn- collect-2d-vbap-data [pair]
  (let [coords (get-coords pair)
        ls-nos (into [] (get-ls-nos pair))]
    (flatten (conj ls-nos
                   (mtx-transpose (mtx-2d-inverse coords))))))

(defn- get-2d-vbap-buffer-data [speakers]
  (reduce into [2 (count speakers)]
          (map #(map round2 (collect-2d-vbap-data %))
               (get-2d-pairs speakers))))

;;; api function for vbap: Different to the original
;;; function (define_loudspeakers in Max/pd and VBapSpeakerArray in
;;; supercollider), the dimension gets determined automatically based
;;; on the structure of the supplied argument.

(defn vbap-speaker-array
  "calculate the sets-and-matrices vector used by the vbap ugen by
  providing a sequence of angles (2-d) or angle/elevation
  pairs (3-d). Elevation should be in the range [0..90] (no checking
  is done!). The sequences can be supplied either as lists or
  vectors. The calculated vector has to get stored in a buffer on the
  sc-server to be referenced by the vbap ugen.

  Examples:

  2-d: (vbap-speaker-array [-45 0 45 90 135 180 -135 -90])

  3-d: (vbap-speaker-array
       [[-45 0] [0 45] [45 0] [90 45] [135 0] [180 45] [-135 0] [-90 45]])

  For complete usage examples see the documentation for the vbap ugen.
  "
  [speaker-defs]
  (let [speakers (get-speaker-maps speaker-defs)]
    (if (number? (first speaker-defs))
      (get-2d-vbap-buffer-data speakers)
      (get-3d-vbap-buffer-data speakers))))
