(ns overtone.ugens)
         
;; SuperCollider ugen data scraped from JCollider. Thanks sciss!!! 
(def UGENS [
  {:name "!=", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "&", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "*", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "+", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "-", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "/", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "<", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "<=", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "==", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name ">", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name ">=", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "A2K", :args '({:name "in", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "APF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "radius", :array? false, :default 0.8}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "AllpassC", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "AllpassL", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "AllpassN", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "AmpComp", :args '({:name "freq", :array? false, :default 261.6256} {:name "root", :array? false, :default 261.6256} {:name "exp", :array? false, :default 0.3333}), :rates #{"scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "AmpCompA", :args '({:name "freq", :array? false, :default 1000.0} {:name "root", :array? false, :default 0.0} {:name "minAmp", :array? false, :default 0.32} {:name "rootAmp", :array? false, :default 1.0}), :rates #{"scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Amplitude", :args '({:name "in", :array? false, :default 0.0} {:name "attackTime", :array? false, :default 0.01} {:name "releaseTime", :array? false, :default 0.01}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BAllPass", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BBandPass", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "bw", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BBandStop", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "bw", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BHiPass", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BHiShelf", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rs", :array? false, :default 1.0} {:name "db", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BLowPass", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BLowShelf", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rs", :array? false, :default 1.0} {:name "db", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BPF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BPZ2", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BPeakEQ", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 1200.0} {:name "rq", :array? false, :default 1.0} {:name "db", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BRF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BRZ2", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Balance2", :args '({:name "left", :array? false, :default Float/NaN} {:name "right", :array? false, :default Float/NaN} {:name "pos", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 2, :out-type 0}
  
  {:name "Ball", :args '({:name "in", :array? false, :default 0.0} {:name "g", :array? false, :default 10.0} {:name "damp", :array? false, :default 0.0} {:name "friction", :array? false, :default 0.01}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BeatTrack", :args '({:name "chain", :array? false, :default Float/NaN} {:name "lock", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BeatTrack2", :args '({:name "busindex", :array? false, :default Float/NaN} {:name "numfeatures", :array? false, :default Float/NaN} {:name "windowsize", :array? false, :default 2.0} {:name "phaseaccuracy", :array? false, :default 0.02} {:name "lock", :array? false, :default 0.0} {:name "weightingscheme", :array? false, :default -2.1}), :rates #{"control"}, :fixed-outs 6, :out-type 0}
  
  {:name "BiPanB2", :args '({:name "inA", :array? false, :default Float/NaN} {:name "inB", :array? false, :default Float/NaN} {:name "azimuth", :array? false, :default Float/NaN} {:name "gain", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 3, :out-type 0}
  
  {:name "BinaryOpUGen", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Blip", :args '({:name "freq", :array? false, :default 440.0} {:name "numharm", :array? false, :default 200.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BrownNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufAllpassC", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufAllpassL", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufAllpassN", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufChannels", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufCombC", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufCombL", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufCombN", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufDelayC", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufDelayL", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufDelayN", :args '({:name "buf", :array? false, :default 0.0} {:name "in", :array? false, :default 0.0} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufDur", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufFrames", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufRateScale", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufRd", :args '({:name "bufnum", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "loop", :array? false, :default 1.0} {:name "interpolation", :array? false, :default 2.0}), :rates #{"audio" "control"}, :fixed-outs -1, :out-type 1}
  
  {:name "BufSampleRate", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufSamples", :args '({:name "bufnum", :array? false, :default Float/NaN}), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "BufWr", :args '({:name "bufnum", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "loop", :array? false, :default 1.0} {:name "inputArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "COsc", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 440.0} {:name "beats", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CheckBadValues", :args '({:name "in", :array? false, :default 0.0} {:name "id", :array? false, :default 0.0} {:name "post", :array? false, :default 2.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ClearBuf", :args '({:name "buf", :array? false, :default Float/NaN}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Clip", :args '({:name "in", :array? false, :default 0.0} {:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ClipNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CoinGate", :args '({:name "prob", :array? false, :default Float/NaN} {:name "in", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CombC", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CombL", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CombN", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Compander", :args '({:name "in", :array? false, :default 0.0} {:name "control", :array? false, :default 0.0} {:name "thresh", :array? false, :default 0.5} {:name "slopeBelow", :array? false, :default 1.0} {:name "slopeAbove", :array? false, :default 1.0} {:name "clampTime", :array? false, :default 0.01} {:name "relaxTime", :array? false, :default 0.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "ControlDur", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "ControlRate", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Convolution", :args '({:name "in", :array? false, :default Float/NaN} {:name "kernel", :array? false, :default Float/NaN} {:name "framesize", :array? false, :default 512.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Convolution2", :args '({:name "in", :array? false, :default Float/NaN} {:name "kernel", :array? false, :default Float/NaN} {:name "trigger", :array? false, :default Float/NaN} {:name "framesize", :array? false, :default 512.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Convolution2L", :args '({:name "in", :array? false, :default Float/NaN} {:name "kernel", :array? false, :default Float/NaN} {:name "trigger", :array? false, :default Float/NaN} {:name "framesize", :array? false, :default 512.0} {:name "crossfade", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Convolution3", :args '({:name "in", :array? false, :default Float/NaN} {:name "kernel", :array? false, :default Float/NaN} {:name "trigger", :array? false, :default 0.0} {:name "framesize", :array? false, :default 512.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Crackle", :args '({:name "chaosParam", :array? false, :default 1.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "CuspL", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default 1.9} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "CuspN", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default 1.9} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "DC", :args '({:name "in", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 2}
  
  {:name "Dbrown", :args '({:name "lo", :array? false, :default Float/NaN} {:name "hi", :array? false, :default Float/NaN} {:name "step", :array? false, :default Float/NaN} {:name "length", :array? false, :default Float/POSITIVE_INFINITY}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dbufrd", :args '({:name "bufnum", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "loop", :array? false, :default 1.0}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dbufwr", :args '({:name "input", :array? false, :default 0.0} {:name "bufnum", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "loop", :array? false, :default 1.0}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Decay", :args '({:name "in", :array? false, :default 0.0} {:name "decayTime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Decay2", :args '({:name "in", :array? false, :default 0.0} {:name "attackTime", :array? false, :default 0.01} {:name "decayTime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DecodeB2", :args '({:name "w", :array? false, :default Float/NaN} {:name "x", :array? false, :default Float/NaN} {:name "y", :array? false, :default Float/NaN} {:name "orientation", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs -1, :out-type 1}
  
  {:name "DegreeToKey", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0} {:name "octave", :array? false, :default 12.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Delay1", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Delay2", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DelayC", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DelayL", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DelayN", :args '({:name "in", :array? false, :default 0.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Demand", :args '({:name "trig", :array? false, :default Float/NaN} {:name "reset", :array? false, :default Float/NaN} {:name "demandUGens", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 2, :out-type 2}
  
  {:name "DemandEnvGen", :args '({:name "level", :array? false, :default Float/NaN} {:name "dur", :array? false, :default Float/NaN} {:name "shape", :array? false, :default 1.0} {:name "curve", :array? false, :default 0.0} {:name "gate", :array? false, :default 1.0} {:name "reset", :array? false, :default 1.0} {:name "levelScale", :array? false, :default 1.0} {:name "levelBias", :array? false, :default 0.0} {:name "timeScale", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DetectIndex", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "DetectSilence", :args '({:name "in", :array? false, :default 0.0} {:name "amp", :array? false, :default 1.0E-4} {:name "time", :array? false, :default 0.1} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "Dgeom", :args '({:name "start", :array? false, :default 1.0} {:name "grow", :array? false, :default 2.0} {:name "length", :array? false, :default 100.0}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dibrown", :args '({:name "lo", :array? false, :default Float/NaN} {:name "hi", :array? false, :default Float/NaN} {:name "step", :array? false, :default Float/NaN} {:name "length", :array? false, :default Float/POSITIVE_INFINITY}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "DiskIn", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "loop", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs -1, :out-type 1}
  
  {:name "DiskOut", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio"}, :fixed-outs 0, :out-type 0}
  
  {:name "Diwhite", :args '({:name "lo", :array? false, :default Float/NaN} {:name "hi", :array? false, :default Float/NaN} {:name "length", :array? false, :default Float/POSITIVE_INFINITY}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Donce", :args '({:name "in", :array? false, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Done", :args '({:name "src", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Drand", :args '({:name "repeats", :array? false, :default 1.0} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dseq", :args '({:name "repeats", :array? false, :default 1.0} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dser", :args '({:name "repeats", :array? false, :default 1.0} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dseries", :args '({:name "start", :array? false, :default 1.0} {:name "step", :array? false, :default 1.0} {:name "length", :array? false, :default 100.0}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dshuf", :args '({:name "repeats", :array? false, :default 1.0} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dstutter", :args '({:name "n", :array? false, :default Float/NaN} {:name "in", :array? false, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dswitch", :args '({:name "index", :array? false, :default Float/NaN} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dswitch1", :args '({:name "index", :array? false, :default Float/NaN} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dust", :args '({:name "density", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dust2", :args '({:name "density", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Duty", :args '({:name "dur", :array? false, :default 1.0} {:name "reset", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dwhite", :args '({:name "lo", :array? false, :default Float/NaN} {:name "hi", :array? false, :default Float/NaN} {:name "length", :array? false, :default Float/POSITIVE_INFINITY}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "Dxrand", :args '({:name "repeats", :array? false, :default 1.0} {:name "list", :array? true, :default Float/NaN}), :rates #{"demand"}, :fixed-outs 1, :out-type 0}
  
  {:name "EnvGen", :args '({:name "gate", :array? false, :default 1.0} {:name "levelScale", :array? false, :default 1.0} {:name "levelBias", :array? false, :default 0.0} {:name "timeScale", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0} {:name "envelope", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ExpRand", :args '({:name "lo", :array? false, :default 0.01} {:name "hi", :array? false, :default 1.0}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "FBSineC", :args '({:name "freq", :array? false, :default 22050.0} {:name "im", :array? false, :default 1.0} {:name "fb", :array? false, :default 0.1} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.1} {:name "yi", :array? false, :default 0.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "FBSineL", :args '({:name "freq", :array? false, :default 22050.0} {:name "im", :array? false, :default 1.0} {:name "fb", :array? false, :default 0.1} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.1} {:name "yi", :array? false, :default 0.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "FBSineN", :args '({:name "freq", :array? false, :default 22050.0} {:name "im", :array? false, :default 1.0} {:name "fb", :array? false, :default 0.1} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.1} {:name "yi", :array? false, :default 0.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "FFT", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FFTTrigger", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "hop", :array? false, :default 0.5} {:name "polar", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FOS", :args '({:name "in", :array? false, :default 0.0} {:name "a0", :array? false, :default 0.0} {:name "a1", :array? false, :default 0.0} {:name "b1", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FSinOsc", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Fold", :args '({:name "in", :array? false, :default 0.0} {:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Formant", :args '({:name "fundfreq", :array? false, :default 440.0} {:name "formfreq", :array? false, :default 1760.0} {:name "bwfreq", :array? false, :default 880.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Formlet", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "attacktime", :array? false, :default 1.0} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Free", :args '({:name "trig", :array? false, :default Float/NaN} {:name "id", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FreeSelf", :args '({:name "in", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FreeSelfWhenDone", :args '({:name "src", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "FreeVerb", :args '({:name "in", :array? false, :default Float/NaN} {:name "mix", :array? false, :default 0.33} {:name "room", :array? false, :default 0.5} {:name "damp", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "FreeVerb2", :args '({:name "in", :array? false, :default Float/NaN} {:name "in2", :array? false, :default Float/NaN} {:name "mix", :array? false, :default 0.33} {:name "room", :array? false, :default 0.5} {:name "damp", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 2, :out-type 0}
  
  {:name "FreqShift", :args '({:name "in", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "GVerb", :args '({:name "in", :array? false, :default Float/NaN} {:name "roomsize", :array? false, :default 10.0} {:name "revtime", :array? false, :default 3.0} {:name "damping", :array? false, :default 0.5} {:name "inputbw", :array? false, :default 0.5} {:name "spread", :array? false, :default 15.0} {:name "drylevel", :array? false, :default 1.0} {:name "earlyreflevel", :array? false, :default 0.7} {:name "taillevel", :array? false, :default 0.5} {:name "maxroomsize", :array? false, :default 300.0}), :rates #{"audio"}, :fixed-outs 2, :out-type 0}
  
  {:name "Gate", :args '({:name "in", :array? false, :default 0.0} {:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "GbmanL", :args '({:name "freq", :array? false, :default 22050.0} {:name "xi", :array? false, :default 1.2} {:name "yi", :array? false, :default 2.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "GbmanN", :args '({:name "freq", :array? false, :default 22050.0} {:name "xi", :array? false, :default 1.2} {:name "yi", :array? false, :default 2.1}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Gendy1", :args '({:name "ampdist", :array? false, :default 1.0} {:name "durdist", :array? false, :default 1.0} {:name "adparam", :array? false, :default 1.0} {:name "ddparam", :array? false, :default 1.0} {:name "minfreq", :array? false, :default 440.0} {:name "maxfreq", :array? false, :default 660.0} {:name "ampscale", :array? false, :default 0.5} {:name "durscale", :array? false, :default 0.5} {:name "initCPs", :array? false, :default 12.0} {:name "knum", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Gendy2", :args '({:name "ampdist", :array? false, :default 1.0} {:name "durdist", :array? false, :default 1.0} {:name "adparam", :array? false, :default 1.0} {:name "ddparam", :array? false, :default 1.0} {:name "minfreq", :array? false, :default 440.0} {:name "maxfreq", :array? false, :default 660.0} {:name "ampscale", :array? false, :default 0.5} {:name "durscale", :array? false, :default 0.5} {:name "initCPs", :array? false, :default 12.0} {:name "knum", :array? false, :default Float/NaN} {:name "a", :array? false, :default 1.17} {:name "c", :array? false, :default 0.31}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Gendy3", :args '({:name "ampdist", :array? false, :default 1.0} {:name "durdist", :array? false, :default 1.0} {:name "adparam", :array? false, :default 1.0} {:name "ddparam", :array? false, :default 1.0} {:name "freq", :array? false, :default 440.0} {:name "ampscale", :array? false, :default 0.5} {:name "durscale", :array? false, :default 0.5} {:name "initCPs", :array? false, :default 12.0} {:name "knum", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "GrayNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "HPF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "HPZ1", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "HPZ2", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Hasher", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "HenonC", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.4} {:name "b", :array? false, :default 0.3} {:name "x0", :array? false, :default 0.0} {:name "x1", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "HenonL", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.4} {:name "b", :array? false, :default 0.3} {:name "x0", :array? false, :default 0.0} {:name "x1", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "HenonN", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.4} {:name "b", :array? false, :default 0.3} {:name "x0", :array? false, :default 0.0} {:name "x1", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Hilbert", :args '({:name "in", :array? false, :default Float/NaN}), :rates #{"audio"}, :fixed-outs 2, :out-type 0}
  
  {:name "IEnvGen", :args '({:name "index", :array? false, :default 0.0} {:name "envelope", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "IFFT", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "wintype", :array? false, :default 0.0} {:name "winsize", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "IRand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 127.0}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Impulse", :args '({:name "freq", :array? false, :default 440.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "In", :args '({:name "bus", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 1}
  
  {:name "InFeedback", :args '({:name "bus", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 1}
  
  {:name "InRange", :args '({:name "in", :array? false, :default 0.0} {:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "InRect", :args '({:name "y", :array? false, :default 0.0} {:name "y", :array? false, :default 0.0} {:name "left", :array? false, :default Float/NaN} {:name "top", :array? false, :default Float/NaN} {:name "right", :array? false, :default Float/NaN} {:name "bottom", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "InTrig", :args '({:name "bus", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 1}
  
  {:name "Index", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "IndexInBetween", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Integrator", :args '({:name "in", :array? false, :default 0.0} {:name "coef", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "K2A", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "KeyState", :args '({:name "keycode", :array? false, :default 0.0} {:name "minval", :array? false, :default 0.0} {:name "maxval", :array? false, :default 1.0} {:name "lag", :array? false, :default 0.2}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "KeyTrack", :args '({:name "chain", :array? false, :default Float/NaN} {:name "keydecay", :array? false, :default 2.0} {:name "chromaleak", :array? false, :default 0.5}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Klang", :args '({:name "freqscale", :array? false, :default 1.0} {:name "freqoffset", :array? false, :default 0.0} {:name "specs", :array? true, :default Float/NaN}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Klank", :args '({:name "input", :array? false, :default Float/NaN} {:name "freqscale", :array? false, :default 1.0} {:name "freqoffset", :array? false, :default 0.0} {:name "decayscale", :array? false, :default 1.0} {:name "specs", :array? true, :default Float/NaN}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFClipNoise", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFCub", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFDClipNoise", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFDNoise0", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFDNoise1", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFDNoise3", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFGauss", :args '({:name "duration", :array? false, :default 1.0} {:name "width", :array? false, :default 0.1} {:name "iphase", :array? false, :default 0.0} {:name "loop", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFNoise0", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFNoise1", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFNoise2", :args '({:name "freq", :array? false, :default 500.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFPar", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFPulse", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0} {:name "width", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFSaw", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LFTri", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LPF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LPZ1", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LPZ2", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Lag", :args '({:name "in", :array? false, :default 0.0} {:name "lagTime", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Lag2", :args '({:name "in", :array? false, :default 0.0} {:name "lagTime", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Lag2UD", :args '({:name "in", :array? false, :default 0.0} {:name "lagTimeU", :array? false, :default 0.1} {:name "lagTimeD", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Lag3", :args '({:name "in", :array? false, :default 0.0} {:name "lagTime", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Lag3UD", :args '({:name "in", :array? false, :default 0.0} {:name "lagTimeU", :array? false, :default 0.1} {:name "lagTimeD", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LagIn", :args '({:name "bus", :array? false, :default 0.0} {:name "lag", :array? false, :default 0.1}), :rates #{"control"}, :fixed-outs 1, :out-type 1}
  
  {:name "LagUD", :args '({:name "in", :array? false, :default 0.0} {:name "lagTimeU", :array? false, :default 0.1} {:name "lagTimeD", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LastValue", :args '({:name "in", :array? false, :default 0.0} {:name "diff", :array? false, :default 0.01}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Latch", :args '({:name "in", :array? false, :default 0.0} {:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Latoocarfian", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN} {:name "c", :array? false, :default Float/NaN} {:name "d", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LatoocarfianC", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default 3.0} {:name "c", :array? false, :default 0.5} {:name "d", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.5} {:name "yi", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LatoocarfianL", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default 3.0} {:name "c", :array? false, :default 0.5} {:name "d", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.5} {:name "yi", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LatoocarfianN", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default 3.0} {:name "c", :array? false, :default 0.5} {:name "d", :array? false, :default 0.5} {:name "xi", :array? false, :default 0.5} {:name "yi", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LeakDC", :args '({:name "in", :array? false, :default 0.0} {:name "coef", :array? false, :default 0.995}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LeastChange", :args '({:name "a", :array? false, :default 0.0} {:name "b", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Limiter", :args '({:name "in", :array? false, :default Float/NaN} {:name "level", :array? false, :default 1.0} {:name "dur", :array? false, :default 0.01}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinCongC", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.13} {:name "m", :array? false, :default 1.0} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinCongL", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.13} {:name "m", :array? false, :default 1.0} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinCongN", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.1} {:name "c", :array? false, :default 0.13} {:name "m", :array? false, :default 1.0} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinExp", :args '({:name "in", :array? false, :default 0.0} {:name "srclo", :array? false, :default 0.0} {:name "srchi", :array? false, :default 1.0} {:name "dstlo", :array? false, :default 1.0} {:name "dsthi", :array? false, :default 2.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinLin", :args '({:name "in", :array? false, :default 0.0} {:name "srclo", :array? false, :default 0.0} {:name "srchi", :array? false, :default 1.0} {:name "dstlo", :array? false, :default 1.0} {:name "dsthi", :array? false, :default 2.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinPan2", :args '({:name "in", :array? false, :default Float/NaN} {:name "pos", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 2, :out-type 0}
  
  {:name "LinRand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0} {:name "minmax", :array? false, :default 0.0}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "LinXFade2", :args '({:name "inA", :array? false, :default Float/NaN} {:name "inB", :array? false, :default 0.0} {:name "pan", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Line", :args '({:name "start", :array? false, :default 0.0} {:name "end", :array? false, :default 1.0} {:name "dur", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Linen", :args '({:name "gate", :array? false, :default 1.0} {:name "attackTime", :array? false, :default 0.01} {:name "susLevel", :array? false, :default 1.0} {:name "releaseTime", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LocalIn", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 1}
  
  {:name "LocalOut", :args '({:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "Logistic", :args '({:name "chaosParam", :array? false, :default 3.0} {:name "freq", :array? false, :default 1000.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "LorenzL", :args '({:name "freq", :array? false, :default 22050.0} {:name "s", :array? false, :default 10.0} {:name "r", :array? false, :default 28.0} {:name "b", :array? false, :default 2.667} {:name "h", :array? false, :default 0.05} {:name "xi", :array? false, :default 0.1} {:name "yi", :array? false, :default 0.0} {:name "zi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Loudness", :args '({:name "chain", :array? false, :default Float/NaN} {:name "smask", :array? false, :default 0.25} {:name "tmask", :array? false, :default 1.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MFCC", :args '({:name "in", :array? false, :default Float/NaN} {:name "numcoeff", :array? false, :default 13.0}), :rates #{"control"}, :fixed-outs 1, :out-type 1}
  
  {:name "MantissaMask", :args '({:name "in", :array? false, :default 0.0} {:name "bits", :array? false, :default 3.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Median", :args '({:name "length", :array? false, :default 3.0} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MidEQ", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "rq", :array? false, :default 1.0} {:name "db", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MoogFF", :args '({:name "freq", :array? false, :default 100.0} {:name "gain", :array? false, :default 2.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MostChange", :args '({:name "a", :array? false, :default 0.0} {:name "b", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MouseButton", :args '({:name "minval", :array? false, :default 0.0} {:name "maxval", :array? false, :default 1.0} {:name "lag", :array? false, :default 0.2}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MouseX", :args '({:name "minval", :array? false, :default 0.0} {:name "maxval", :array? false, :default 1.0} {:name "warp", :array? false, :default 0.0} {:name "lag", :array? false, :default 0.2}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MouseY", :args '({:name "minval", :array? false, :default 0.0} {:name "maxval", :array? false, :default 1.0} {:name "warp", :array? false, :default 0.0} {:name "lag", :array? false, :default 0.2}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "MulAdd", :args '({:name "in", :array? false, :default Float/NaN} {:name "mul", :array? false, :default 1.0} {:name "add", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "NRand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0} {:name "n", :array? false, :default 0.0}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NoahNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Normalizer", :args '({:name "in", :array? false, :default Float/NaN} {:name "level", :array? false, :default 1.0} {:name "dur", :array? false, :default 0.01}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumAudioBuses", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumBuffers", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumControlBuses", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumInputBuses", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumOutputBuses", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "NumRunningSynths", :args '(), :rates #{"scalar" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "OffsetOut", :args '({:name "bus", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "OnePole", :args '({:name "in", :array? false, :default 0.0} {:name "coef", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "OneZero", :args '({:name "in", :array? false, :default 0.0} {:name "coef", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Onsets", :args '({:name "chain", :array? false, :default Float/NaN} {:name "threshold", :array? false, :default 0.5} {:name "odftype", :array? false, :default 3.0} {:name "relaxtime", :array? false, :default 1.0} {:name "floor", :array? false, :default 0.1} {:name "mingap", :array? false, :default 10.0} {:name "medianspan", :array? false, :default 11.0} {:name "whtype", :array? false, :default 1.0} {:name "rawodf", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Osc", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 440.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "OscN", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 440.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Out", :args '({:name "bus", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "PSinGrain", :args '({:name "freq", :array? false, :default 440.0} {:name "dur", :array? false, :default 0.2} {:name "amp", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Add", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_BinScramble", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "wipe", :array? false, :default 0.0} {:name "width", :array? false, :default 0.2} {:name "trig", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_BinShift", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "stretch", :array? false, :default 1.0} {:name "shift", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_BinWipe", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN} {:name "wipe", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_BrickWall", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "wipe", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_ConformalMap", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "areal", :array? false, :default 0.0} {:name "aimag", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Conj", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Copy", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_CopyPhase", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Diffuser", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "trig", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Div", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_HainsworthFoote", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "proph", :array? false, :default 0.0} {:name "propf", :array? false, :default 0.0} {:name "threshold", :array? false, :default 1.0} {:name "waittime", :array? false, :default 0.04}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_JensenAndersen", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "propsc", :array? false, :default 0.25} {:name "prophfe", :array? false, :default 0.25} {:name "prophfc", :array? false, :default 0.25} {:name "propsf", :array? false, :default 0.25} {:name "threshold", :array? false, :default 1.0} {:name "waittime", :array? false, :default 0.04}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_LocalMax", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "threshold", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagAbove", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "threshold", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagBelow", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "threshold", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagClip", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "threshold", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagDiv", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN} {:name "zeroed", :array? false, :default 1.0E-4}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagFreeze", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "freeze", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagMul", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagNoise", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagShift", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "stretch", :array? false, :default 1.0} {:name "shift", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagSmear", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "bins", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_MagSquared", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Max", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Min", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_Mul", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_PhaseShift", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "shift", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_PhaseShift270", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_PhaseShift90", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_RandComb", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "wipe", :array? false, :default 0.0} {:name "trig", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_RandWipe", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN} {:name "wipe", :array? false, :default 0.0} {:name "trig", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_RectComb", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "numTeeth", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "width", :array? false, :default 0.5}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PV_RectComb2", :args '({:name "bufferA", :array? false, :default Float/NaN} {:name "bufferB", :array? false, :default Float/NaN} {:name "numTeeth", :array? false, :default 0.0} {:name "phase", :array? false, :default 0.0} {:name "width", :array? false, :default 0.5}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Pan2", :args '({:name "in", :array? false, :default Float/NaN} {:name "pos", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 2, :out-type 0}
  
  {:name "Pan4", :args '({:name "in", :array? false, :default Float/NaN} {:name "xpos", :array? false, :default 0.0} {:name "ypos", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 4, :out-type 0}
  
  {:name "PanAz", :args '({:name "in", :array? false, :default Float/NaN} {:name "pos", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0} {:name "width", :array? false, :default 2.0} {:name "orientation", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs -1, :out-type 1}
  
  {:name "PanB", :args '({:name "in", :array? false, :default Float/NaN} {:name "azimuth", :array? false, :default 0.0} {:name "elevation", :array? false, :default 0.0} {:name "gain", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 4, :out-type 0}
  
  {:name "PanB2", :args '({:name "in", :array? false, :default Float/NaN} {:name "azimuth", :array? false, :default 0.0} {:name "gain", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 3, :out-type 0}
  
  {:name "PartConv", :args '({:name "in", :array? false, :default Float/NaN} {:name "fftsize", :array? false, :default Float/NaN} {:name "irbufnum", :array? false, :default Float/NaN}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Pause", :args '({:name "gate", :array? false, :default Float/NaN} {:name "id", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PauseSelf", :args '({:name "in", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PauseSelfWhenDone", :args '({:name "src", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Peak", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PeakFollower", :args '({:name "in", :array? false, :default 0.0} {:name "decay", :array? false, :default 0.999}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Phasor", :args '({:name "trig", :array? false, :default 0.0} {:name "rate", :array? false, :default 1.0} {:name "start", :array? false, :default 0.0} {:name "end", :array? false, :default 1.0} {:name "resetPos", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PinkNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Pitch", :args '({:name "in", :array? false, :default 0.0} {:name "initFreq", :array? false, :default 440.0} {:name "minFreq", :array? false, :default 60.0} {:name "maxFreq", :array? false, :default 4000.0} {:name "execFreq", :array? false, :default 100.0} {:name "maxBinsPerOctave", :array? false, :default 16.0} {:name "median", :array? false, :default 1.0} {:name "ampThreshold", :array? false, :default 0.01} {:name "peakThreshold", :array? false, :default 0.5} {:name "downSample", :array? false, :default 1.0}), :rates #{"control"}, :fixed-outs 2, :out-type 0}
  
  {:name "PitchShift", :args '({:name "in", :array? false, :default 0.0} {:name "windowSize", :array? false, :default 0.2} {:name "pitchRatio", :array? false, :default 1.0} {:name "pitchDispersion", :array? false, :default 0.0} {:name "timeDispersion", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "PlayBuf", :args '({:name "bufnum", :array? false, :default 0.0} {:name "rate", :array? false, :default 1.0} {:name "trigger", :array? false, :default 1.0} {:name "startPos", :array? false, :default 0.0} {:name "loop", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs -1, :out-type 1}
  
  {:name "Pluck", :args '({:name "in", :array? false, :default 0.0} {:name "trig", :array? false, :default 1.0} {:name "maxdelaytime", :array? false, :default 0.2} {:name "delaytime", :array? false, :default 0.2} {:name "decaytime", :array? false, :default 1.0} {:name "coef", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Pulse", :args '({:name "freq", :array? false, :default 440.0} {:name "width", :array? false, :default 0.5}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "PulseCount", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "PulseDivider", :args '({:name "trig", :array? false, :default 0.0} {:name "div", :array? false, :default 2.0} {:name "start", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "QuadC", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default -1.0} {:name "c", :array? false, :default -0.75} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "QuadL", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default -1.0} {:name "c", :array? false, :default -0.75} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "QuadN", :args '({:name "freq", :array? false, :default 22050.0} {:name "a", :array? false, :default 1.0} {:name "b", :array? false, :default -1.0} {:name "c", :array? false, :default -0.75} {:name "xi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "RHPF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "RLPF", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "rq", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "RadiansPerSample", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Ramp", :args '({:name "in", :array? false, :default 0.0} {:name "lagTime", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Rand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "RandID", :args '({:name "id", :array? false, :default 0.0}), :rates #{"scalar" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "RandSeed", :args '({:name "trig", :array? false, :default 0.0} {:name "seed", :array? false, :default 56789.0}), :rates #{"scalar" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "RecordBuf", :args '({:name "bufnum", :array? false, :default 0.0} {:name "offset", :array? false, :default 0.0} {:name "recLevel", :array? false, :default 1.0} {:name "preLevel", :array? false, :default 0.0} {:name "run", :array? false, :default 1.0} {:name "loop", :array? false, :default 1.0} {:name "trigger", :array? false, :default 1.0} {:name "inputArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ReplaceOut", :args '({:name "bus", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "Resonz", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "bwr", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Ringz", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "decaytime", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Rotate2", :args '({:name "x", :array? false, :default Float/NaN} {:name "y", :array? false, :default Float/NaN} {:name "pos", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 2, :out-type 0}
  
  {:name "RunningMax", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "RunningMin", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "RunningSum", :args '({:name "in", :array? false, :default Float/NaN} {:name "numsamp", :array? false, :default 40.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SOS", :args '({:name "in", :array? false, :default 0.0} {:name "a0", :array? false, :default 0.0} {:name "a1", :array? false, :default 0.0} {:name "a2", :array? false, :default 0.0} {:name "b1", :array? false, :default 0.0} {:name "b2", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SampleDur", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "SampleRate", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Saw", :args '({:name "freq", :array? false, :default 440.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Schmidt", :args '({:name "in", :array? false, :default 0.0} {:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ScopeOut", :args '({:name "bufnum", :array? false, :default 0.0} {:name "inputArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "Select", :args '({:name "which", :array? false, :default Float/NaN} {:name "array", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SendTrig", :args '({:name "in", :array? false, :default 0.0} {:name "id", :array? false, :default 0.0} {:name "value", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "SetResetFF", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Shaper", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SharedIn", :args '({:name "bus", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 1}
  
  {:name "SharedOut", :args '({:name "bus", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"control"}, :fixed-outs 0, :out-type 0}
  
  {:name "Silent", :args '(), :rates #{"audio"}, :fixed-outs 1, :out-type 1}
  
  {:name "SinOsc", :args '({:name "freq", :array? false, :default 440.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SinOscFB", :args '({:name "freq", :array? false, :default 440.0} {:name "feedback", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Slew", :args '({:name "in", :array? false, :default 0.0} {:name "up", :array? false, :default 1.0} {:name "dn", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Slope", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SpecCentroid", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SpecFlatness", :args '({:name "buffer", :array? false, :default Float/NaN}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SpecPcile", :args '({:name "buffer", :array? false, :default Float/NaN} {:name "fraction", :array? false, :default 0.5} {:name "interpolate", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Spring", :args '({:name "in", :array? false, :default 0.0} {:name "spring", :array? false, :default 0.0} {:name "damp", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "StandardL", :args '({:name "freq", :array? false, :default 22050.0} {:name "k", :array? false, :default 1.0} {:name "xi", :array? false, :default 0.5} {:name "yi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "StandardN", :args '({:name "freq", :array? false, :default 22050.0} {:name "k", :array? false, :default 1.0} {:name "xi", :array? false, :default 0.5} {:name "yi", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "Stepper", :args '({:name "trig", :array? false, :default 0.0} {:name "reset", :array? false, :default 0.0} {:name "min", :array? false, :default 0.0} {:name "max", :array? false, :default 7.0} {:name "step", :array? false, :default 1.0} {:name "resetval", :array? false, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "StereoConvolution2L", :args '({:name "in", :array? false, :default Float/NaN} {:name "kernelL", :array? false, :default Float/NaN} {:name "kernelR", :array? false, :default Float/NaN} {:name "trigger", :array? false, :default Float/NaN} {:name "framesize", :array? false, :default 512.0} {:name "crossfade", :array? false, :default 1.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "SubsampleOffset", :args '(), :rates #{"scalar"}, :fixed-outs 1, :out-type 0}
  
  {:name "Sweep", :args '({:name "trig", :array? false, :default 0.0} {:name "rate", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "SyncSaw", :args '({:name "syncFreq", :array? false, :default 440.0} {:name "sawFreq", :array? false, :default 440.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "T2A", :args '({:name "in", :array? false, :default 0.0} {:name "offset", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs 1, :out-type 0}
  
  {:name "T2K", :args '({:name "in", :array? false, :default 0.0}), :rates #{"control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TBall", :args '({:name "in", :array? false, :default 0.0} {:name "g", :array? false, :default 10.0} {:name "damp", :array? false, :default 0.0} {:name "friction", :array? false, :default 0.01}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TDelay", :args '({:name "in", :array? false, :default 0.0} {:name "dur", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TDuty", :args '({:name "dur", :array? false, :default 1.0} {:name "reset", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TExpRand", :args '({:name "lo", :array? false, :default 0.01} {:name "hi", :array? false, :default 1.0} {:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TGrains", :args '({:name "trigger", :array? false, :default 0.0} {:name "bufnum", :array? false, :default 0.0} {:name "rate", :array? false, :default 1.0} {:name "centerPos", :array? false, :default 0.0} {:name "dur", :array? false, :default 0.1} {:name "pan", :array? false, :default 0.0} {:name "amp", :array? false, :default 0.1} {:name "interp", :array? false, :default 4.0}), :rates #{"audio"}, :fixed-outs -1, :out-type 1}
  
  {:name "TIRand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 127.0} {:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TPulse", :args '({:name "trig", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "width", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TRand", :args '({:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0} {:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TWindex", :args '({:name "in", :array? false, :default Float/NaN} {:name "normalize", :array? false, :default 0.0} {:name "array", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Timer", :args '({:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ToggleFF", :args '({:name "trig", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Trapezoid", :args '({:name "in", :array? false, :default 0.0} {:name "a", :array? false, :default 0.2} {:name "b", :array? false, :default 0.4} {:name "c", :array? false, :default 0.6} {:name "d", :array? false, :default 0.8}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Trig", :args '({:name "in", :array? false, :default 0.0} {:name "dur", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Trig1", :args '({:name "in", :array? false, :default 0.0} {:name "dur", :array? false, :default 0.1}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TwoPole", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "radius", :array? false, :default 0.8}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "TwoZero", :args '({:name "in", :array? false, :default 0.0} {:name "freq", :array? false, :default 440.0} {:name "radius", :array? false, :default 0.8}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "UnaryOpUGen", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "VDiskIn", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "rate", :array? false, :default 1.0} {:name "loop", :array? false, :default 0.0} {:name "sendID", :array? false, :default 0.0}), :rates #{"audio"}, :fixed-outs -1, :out-type 1}
  
  {:name "VOsc", :args '({:name "bufpos", :array? false, :default Float/NaN} {:name "freq", :array? false, :default 440.0} {:name "phase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "VOsc3", :args '({:name "bufpos", :array? false, :default Float/NaN} {:name "freq1", :array? false, :default 110.0} {:name "freq2", :array? false, :default 220.0} {:name "freq3", :array? false, :default 440.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "VarSaw", :args '({:name "freq", :array? false, :default 440.0} {:name "iphase", :array? false, :default 0.0} {:name "width", :array? false, :default 0.5}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Vibrato", :args '({:name "freq", :array? false, :default 440.0} {:name "rate", :array? false, :default 6.0} {:name "depth", :array? false, :default 0.02} {:name "delay", :array? false, :default 0.0} {:name "onset", :array? false, :default 0.0} {:name "rateVariation", :array? false, :default 0.04} {:name "depthVariation", :array? false, :default 0.1} {:name "iphase", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "WhiteNoise", :args '(), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "Wrap", :args '({:name "in", :array? false, :default 0.0} {:name "lo", :array? false, :default 0.0} {:name "hi", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "WrapIndex", :args '({:name "bufnum", :array? false, :default Float/NaN} {:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "XFade2", :args '({:name "inA", :array? false, :default Float/NaN} {:name "inB", :array? false, :default 0.0} {:name "pan", :array? false, :default 0.0} {:name "level", :array? false, :default 1.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "XLine", :args '({:name "start", :array? false, :default 1.0} {:name "end", :array? false, :default 2.0} {:name "dur", :array? false, :default 1.0} {:name "doneAction", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "XOut", :args '({:name "bus", :array? false, :default Float/NaN} {:name "xfade", :array? false, :default Float/NaN} {:name "channelsArray", :array? true, :default Float/NaN}), :rates #{"audio" "control"}, :fixed-outs 0, :out-type 0}
  
  {:name "ZeroCrossing", :args '({:name "in", :array? false, :default 0.0}), :rates #{"audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "^", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "abs", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "absdif", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "acos", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "amclip", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ampdb", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "asFloat", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "asInteger", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "asin", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "atan", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "atan2", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "bilinrand", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "bitNot", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ceil", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "clip2", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "coin", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "cos", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "cosh", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "cpsmidi", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "cpsoct", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "cubed", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "dbamp", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "difsqr", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "digitValue", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "distort", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "div", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "excess", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "exp", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "exprand", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "fill", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "firstArg", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "floor", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "fold2", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "frac", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "gcd", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "hanWindow", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "hypot", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "hypotApx", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "isNil", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "lcm", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "leftShift", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "linrand", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "log", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "log10", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "log2", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "max", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "midicps", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "midiratio", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "min", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "mod", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "neg", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "not", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "notNil", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "octcps", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "pow", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ramp", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "rand", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "rand2", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ratiomidi", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "reciprocal", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "rectWindow", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "rightShift", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ring1", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ring2", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ring3", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "ring4", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "round", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "roundUp", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "rrand", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "scaleneg", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "scurve", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sign", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "silence", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sin", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sinh", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "softclip", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sqrdif", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sqrsum", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sqrt", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "squared", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sum3rand", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "sumsqr", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "tan", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "tanh", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "thresh", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "thru", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "triWindow", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "trunc", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "unsignedRightShift", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "welWindow", :args '({:name "a", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "wrap2", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  
  {:name "|", :args '({:name "a", :array? false, :default Float/NaN} {:name "b", :array? false, :default Float/NaN}), :rates #{"demand" "scalar" "audio" "control"}, :fixed-outs 1, :out-type 0}
  ])
