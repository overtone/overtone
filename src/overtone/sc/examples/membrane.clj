(ns overtone.sc.examples.membrane
  (:use [overtone.sc.machinery defexample]
        [overtone.sc ugens envelope]))

(defexamples membrane-circle
  (:mouse
   "Use mouse button, X and Y locations to play a drum."
   "The mouse button drives the excitation input of the membrane.  The
    mouse X location drives the tension and the Mouse Y location controls
    the loss parameter.  From the .schelp file.  Click and enjoy."

   rate :kr
   []
   "
  (let [excitation (* (env-gen:kr
                         (perc)
                         (mouse-button:kr 0 1 0)
                         1.0 0.0 0.1 0)
                      (pink-noise))
        tension (mouse-x 0.01 0.1)
        loss (mouse-y 0.999999 0.999 EXP)]
        (membrane-circle excitation tension loss))"
   contributor "Roger Allen"))

(defexamples membrane-hexagon
  (:mouse
   "Use mouse button, X and Y locations to play a drum."
   "The mouse button drives the excitation input of the membrane.  The
    mouse X location drives the tension and the Mouse Y location controls
    the loss parameter.  From the .schelp file.  Click and enjoy."

   rate :kr
   []
   "
  (let [excitation (* (env-gen:kr
                         (perc)
                         (mouse-button:kr 0 1 0)
                         1.0 0.0 0.1 0)
                      (pink-noise))
        tension (mouse-x 0.01 0.1)
        loss (mouse-y 0.999999 0.999 EXP)]
        (membrane-hexagon excitation tension loss))"
   contributor "Roger Allen"))
