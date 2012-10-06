(ns overtone.sc.machinery.ugen.metadata.extras.membrane
  (:use [overtone.sc.machinery.ugen common check]))

(def specs
  [
   {:name "MembraneCircle"
    :summary "Waveguide mesh physical models of circular drum membrane."
    :args [{:name "excitation"
            :doc "sound in"}

           {:name "tension"
            :default 0.05
            :doc "tension of the membrane"}

           {:name "loss"
            :default 0.99999
            :doc "loss of the membrane"}]

    :rates #{:ar}
    :doc "Triangular waveguide meshes of a drum-like membrane. You
    input some excitation, such as a pulse of noise, and can adjust
    the tension and loss while it plays.

    Also see MembraneHexagon. The ugens are named after the shape made out
    of triangular meshes. Obviously you can't make a circle out of
    triangles, but it tries. At the moment MembraneCircle is a bit
    bigger than MembraneHexagon, using more waveguides and therefore
    more CPU.

    These UGens are by Alex McLean (c) 2008."}

   {:name "MembraneHexagon"
    :summary "Waveguide mesh physical models of hexagonal drum membrane."
    :args [{:name "excitation"
            :doc "sound in"}

           {:name "tension"
            :default 0.05
            :doc "tension of the membrane"}

           {:name "loss"
            :default 0.99999
            :doc "loss of the membrane"}]

    :rates #{:ar}
    :doc "Triangular waveguide meshes of a drum-like membrane. You
    input some excitation, such as a pulse of noise, and can adjust
    the tension and loss while it plays.

    Also see MembraneCircle.  The ugens are named after the shape made
    out of triangular meshes.

    These UGens are by Alex McLean (c) 2008."}])
