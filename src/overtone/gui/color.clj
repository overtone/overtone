(ns overtone.gui.color
  (:use [seesaw core color]))

(def theme* (atom
              {:background-fill (color 230 230 230)
               :background-stroke (color 255 255 255)
               ;:background-fill (color 40 40 40)
               ;:background-stroke (color 30 30 30)

               :highlight (color 100 100 255)

               :fill-1 (color 0 130 226 150)
               :stroke-1 (color 0 140 236)

               :fill-2 (color 170 170 170 150)
               :stroke-2 (color 170 170 170)

               :fill-3 (color  170 30 30 150)
               :stroke-3 (color 170 30 30)

               :bounding-box (color 250 20 20 150)
               }))

(defn theme-color [tag]
  (get @theme* tag))

(defn adjust-brightness
  "Adjust color brightness by adding an amount to each of red, green, and blue."
  ([color amount]
   (let [{:keys [red green blue]} (bean color)]
   (color (max 0 (+ red amount))
           (max 0 (+ green amount))
           (max 0 (+ blue amount))))))

(defn darken-color
  "Darken a color in steps of -10 for each of r, g, and b."
  ([color] (adjust-brightness color 1))
  ([color factor]
   (adjust-brightness color (* factor -10))))

(defn lighten-color
  "Lighten a color in steps of 10 for each of r, g, and b."
  ([color] (lighten-color color 1))
  ([color factor]
   (adjust-brightness color (* factor 10))))

(defn clarify-color
  "Increase the color transparency in steps of 10."
  ([col] (clarify-color col 1))
  ([col factor]
   (let [r (.getRed col)
         g (.getGreen col)
         b (.getBlue col)]
     (color r g b (- (.getTransparency col) (* factor 10))))))
