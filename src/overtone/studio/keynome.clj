(ns overtone.studio.keynome
  (:import [java.awt GridLayout Dimension]
           [java.awt.event ActionListener KeyListener KeyEvent]
           [javax.swing JFrame JButton JPanel]))


(defn keynome [ &{ :keys [map title]
                  :or {map {} title "ohai!"} }]
  "keynome does two things:

  1. It creates a small Swing-based window that, when it has focus,
     captures keyboard events and, if there is a function associated
     with that event, calls the function.

  2. Returns a closure: calling this closure with no arguments will
     make the window pop up again, in case you closed it. With
     arguments, the following is supported:

      - (k :painter f) sets f as the function painting the JPanel

      - (k :map :key f ... ) updates the event map with aribtrary
        number of pairs of keys and functions

It takes two keyword arguments :map and :title, where :map is a
map between :keys and functions and :title is a string used as a
title for the keynome window.

Demo
user> (def k (keynome))
#'user/k
user> (def s (switch))
#'user/s
user> (s)
false
user> (k :map :q #(s :swap))
user> (s)
false
user> ;; press the Q key once !!
user> (s)
true

"
  (let [map (atom map) painter (atom (fn [g] nil))
        handle-keypress (fn [evt map]
                          (let [key (str (.getKeyChar evt))
                                act (@map (keyword key))]
                            (do (if act (act) nil))))
        key-listener (proxy [KeyListener] []
                       (keyPressed  [evt] (handle-keypress evt map))
                       (keyReleased [evt] nil)
                       (keyTyped    [evt] nil))
        grid-layout (GridLayout. 4 11)
        panel (JPanel.)
        panel (proxy [JPanel] [] (paint [g] (painter g)))
        frame (JFrame. title)]
    (do (doto panel
          (.setLayout grid-layout)
          (.addKeyListener key-listener)
          (.setFocusable true)
          (.setPreferredSize (Dimension. 150 50)))
        (.add (.getContentPane frame) panel)
        (doto frame (.pack) (.setVisible true)))
    (fn ([] (do (.setVisible frame true)))
      ([& msg]
         (case (first msg)
           :painter (reset! painter (second msg))
           :map (reset! map (conj @map (apply hash-map (rest msg)))))))))


(defn switch []
  "(switch) returns a closure with an internal state of true or
false (false is default). Calling this function with no arguments will
return its current state. Calling this function with certain keyword
arguments produces the following behavior:

  - :swap - change to true if it is currently false and vice versa
  - :on - set state to true
  - :off - set state to false
  - :rand - flip a coin

Demonstration usage:

user> (def s (switch))
#'user/s
user> (s)
false
user> (s :on)
true
user> (if (s) 1 0)
1
user> (s :off)
false
user> (s :swap)
true
user> (s :swap)
false
user> (s :rand)
true
user> (s :rand)
false
user> (if (s) 1 0)
0

"
  (let [on-or-off? (atom false)]
    (fn ([] @on-or-off?)
      ([k] (reset! on-or-off?
                   (cond (= k :swap) (if @on-or-off? false true)
                         (= k :on) true
                         (= k :off) false
                         (= k :rand) (if (< (rand) 0.5) true false)
                         ))))))
