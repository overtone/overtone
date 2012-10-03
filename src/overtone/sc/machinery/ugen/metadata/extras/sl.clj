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
    

    All inputs can have .kr rate UGens plugged in. F is itself an arbitrary audio rate UGen input forcing term"}])
