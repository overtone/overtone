(ns overtone.sc.machinery.ugen.metadata.extras.sl
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "Breakcore"
    :summary "Breakcore simulator"
    :args [{:name "bufnum"
            :default 0
            :doc ""}
           
           {:name "capturein"
             :default :none
             :doc ""}
           
           {:name "capturetrigger"
             :default :none
             :doc ""}
           
           {:name "duration"
             :default 0.1
             :doc ""}]
    :rates #{:ar}
    :doc "This is noisy.
    (This UGen (C and SuperCollider code) was written on Feb 7 2005 in one hour in front of a live audience as part of the TOPLAP live coding jam at transmediale, Maria am Ostbahnhof, Berlin. 
    I haven't tried to clear it up after the event, only adding this notice; make what you can of it!)"}

   {:name "Brusselator"
    :summary "Prigogine oscillator"
    :args [{:name "reset"
            :default 0
            :doc "If > 0.0, restart with new initial conditions sampled from initx, inity"}
           
           {:name "rate"
            :default 0.01
            :doc "Update rate for a sample step"}

           {:name "mu"
            :default 1.0
            :doc "Equation constant.  Set mu > (gamma*2 + 1.0) for the more fun limit cycle regions."}

           {:name "gamma"
            :default 1.0
            :doc "Equation constant"}
         
           {:name "initx"
            :default 0.5
            :doc "Reset value for x"}

           {:name "inity"
            :default 0.5
            :doc "Reset value for y"}]
   :rates #{:ar}
   :doc "Euler ODE solver implementation of the Brusselator equations (http://en.wikipedia.org/wiki/Brusselator.)


   x' = x^2*y - (mu+1*x + gamma y')
   

   y' = -x^2*y + mu*x


   All inputs can have .kr rate UGens plugged in.


   Nonlinear oscillators can blow up, treat with caution. This one is relatively stable however, converging to a fixed point, or a limit cycle, in the upper positive quadrant for some reasonable values. Just be careful if mu gets much bigger than gamma (though making it larger is necessary to get some chaotic oscillation behaviour) you can retrigger to get back to normal, and keep the rate lower to avoid Euler integration blow-ups. You may just need to scale and push down around zero to avoid a DC offset. Fixed point is at (x,y = (gamma, mu/gamma))"}

   {:name "DoubleWell"
    :summary "Forced DoubleWell Oscillator"
    :args [{:name "reset"
            :default 0
            :doc "Restart with new initial conditions sampled from initx, inity"}
          
           {:name "ratex"
            :default 0.01
            :doc "Update rate for x"}

           {:name "ratey"
            :default 0.01
            :doc  "Update rate for y"}
            
           {:name "f"
            :default 1
            :doc "Equation constant"}

           {:name "w"
            :default 0.001
            :doc "Equation constant" }

           {:name "delta"
            :default 1
            :doc "Equation constant"}

           {:name "initx"
            :default 0
            :doc "Reset value for x"}
     
           {:name "inity"
            :default 0
            :doc "Reset value for y"}]
            
    :rates #{:ar}
    :doc "Runge-Kutta ODE solver implementation of the chaotic Forced Double Well Oscillator (see Strogatz, Steven H. (1994) Nonlinear Dynamics and Chaos. Addison-Wesley, Reading, MA. pp441-7.)  


    D2x + delta*Dx - x + x^3 = F*cos(w*t)


    All inputs can have .kr rate UGens plugged in."}

   {:name "DoubleWell2"
    :summary "Forced Double Well Oscillator"
    :args [{:name "reset"
            :default 0
            :doc "Restart with new initial conditions sampled from initx, inity"}

           {:name "ratex"
            :default 0.01
            :doc "Update rate for x"}

           {:name "ratey"
            :default 0.01
            :doc "Update rate for y"}

           {:name "f"
            :default 1
            :doc "Equation constant"}

           {:name "w"
            :default 0.001
            :doc "Equation constant"}

           {:name "delta"
            :default 1
            :doc "Equation constant"}

           {:name "initx"
            :default 0
            :doc "Reset value for x"}

           {:name "inity"
            :default 0
            :doc "Reset value for y"}]
   
   :rates #{:ar}
   :doc "Improved Euler ODE solver implementation of the chaotic Forced Double Well Oscillator (see Strogatz, Steven H. (1994) Nonlinear Dynamics and Chaos. Addison-Wesley, Reading, MA. pp441-7)


   D2x + delta*Dx - x + x^3 = F*cos(w*t)
   

   y = Dx
   

   All input can have .kr rate UGens plugged in"}

   {:name "DoubleWell3"
    :summary "Forced Double Well Oscillator"
    :args [{:name "reset"
            :default 0
            :doc "Restart with new initial conditions sampled from initx, inity"}

           {:name "rate"
            :default 0.01
            :doc "Update rate for x and y"}

           {:name "f"
            :default 0
            :doc "Forcing term, an arbitrary audio rate input"}

           {:name "delta"
            :default 0.25
            :doc "Equation constant"}

           {:name "initx"
            :default 0
            :doc "Reset value for x"}

           {:name "inity"
            :default 0
            :doc "Reset value for y"}]

    :rates #{:ar}
    :doc "Runge-Kutta ODE solver implementation of the chaotic Forced Double Well Oscillator (see Strogatz, Steven H. (1994) Nonlinear Dynamics and Chaos. Addison-Wesley, Reading, MA. pp441-7).
    

    D2x + delta*Dx - x + x^3 = F
    

    y = Dx
    

    All inputs can have .kr rate UGens plugged in. F is itself an arbitrary audio rate UGen input forcing term"}

   {:name "GravityGrid"
    :summary "Dynamical System Simulation (Newtonian gravitational force)"
    :args [{:name "reset"
            :default 0
            :doc "Restart the moving mass at a random position within the square (k-rate input)"}

           {:name "rate"
            :default 0.1
            :doc "Amount of position update per sample (k-rate)"}

           {:name "newx"
            :default 0.0
            :doc "kr input to be sampled for new x positions for the moving mass on reset"}

           {:name "newy"
            :default 0.0
            :doc "kr input to be sampled for new y positions for the moving mass on reset"}

           {:name "bufnum"
            :default :none
            :doc "Bufnum for a buffer containing weights for the different outer masses indexed as 0-3 and 5-8 and central moving mass 4. Passing -1 means that the weights are not used (are flat.)"}]

    :rates #{:ar}
    :doc "Eight fixed masses around a boundary apply Newtonian gravitational force dynamics to a central moving mass which cannot escape the [-1, 1] grid in x or y. The position of the moving mass is sonified as an oscillator by its distance from the centre.  This is a relatively expensive oscillator to run.


    Note: This original GravityGrid contains an erroneous folding function, and gravity which is more attractive as the distance increases! Which however, adds interesting distortions to the sound. See GravityGrid2 for a cleaned up version. This one is retained for backwards compatibility."}

   {:name "GravityGrid2"
    :summary "Dynamical System Simulation (Newtonian gravitational force)"
    :args [{:name "reset"
            :default 0
            :doc "Restart the moving mass at a random position within the square (k-rate input)"}

           {:name "rate"
            :default 0.1
            :doc "Amount of position update per sample (k-rate)"}

           {:name "newx"
            :default 0.0
            :doc "kr input to be sampled for new x positions for the moving mass on reset"}

           {:name "newy"
            :default 0.0
            :doc "kr input to be sampled for new y positions for the moving mass on reset"}

           {:name "bufnum"
            :default :none
            :doc "Bufnum for a buffer containing weights and positions for the fixed influencing masses. In the format entry [0] is the number of masses, then 3 components (x, y, mass multiplier for each mass in turn (see below). You can dynamically change this buffer as long as the data contents stay consistent- i.e. if you change suddenly to having twice as many masses, make sure you've provided x,y and weight values for them!"}]

    :rates #{:ar}
    :doc  "Eight fixed masses around a boundary apply Newtonian gravitational force dynamics to a central moving mass which cannot escape the [-1, 1] grid in x or y. The position of the moving mass is sonified as an oscillator by its distance from the centre.  This is a relatively expensive oscillator to run."}])
