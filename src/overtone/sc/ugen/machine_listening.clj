(ns overtone.sc.ugen.machine-listening)

(def specs
     [

      ;; //4 outs
      ;; BeatTrack : MultiOutUGen {
      ;;   *kr { arg chain, lock=0;
      ;;     if(chain.isKindOf(FFT).not){
      ;;       // Automatically drop in an FFT, possible now that we have LocalBuf
      ;;       chain = FFT(LocalBuf(if(SampleRate.ir>48000, 2048, 1024)), chain);
      ;;     };

      ;;     ^this.multiNew('control',chain, lock);
      ;;   }

      ;;   init { arg ... theInputs;
      ;;     inputs = theInputs;
      ;;     ^this.initOutputs(4, rate);
      ;;   }
      ;; }

      {:name "BeatTrack",
       :args [{:name "chain"}
              {:name "lock", :default 0}],
       :rates #{:kr}
       :num-outs 4}

      ;; //loudness output in sones
      ;; Loudness : UGen {
      ;;   *kr { arg chain, smask=0.25, tmask=1;
      ;;     ^this.multiNew('control',chain, smask, tmask);
      ;;   }
      ;; }

      {:name "Loudness",
       :args [{:name "chain"}
              {:name "smask", :default 0.25}
              {:name "tmask", :default 1}],
       :rates #{:kr}}

      ;; Onsets : UGen {
      ;;   *kr { |chain, threshold=0.5, odftype=\rcomplex, relaxtime=1,
      ;;         floor=0.1, mingap=10, medianspan=11, whtype=1, rawodf=0|
      ;;     if(odftype.class == Symbol){
      ;;       odftype = #[\power, \magsum, \complex, \rcomplex, \phase, \wphase,\mkl]
      ;;         .indexOf(odftype)
      ;;     };
      ;;     // mingap of 10 frames, @ 44100 & 512 & 50%, is about 0.058 seconds
      ;;     ^this.multiNew('control', chain, threshold, odftype, relaxtime,
      ;;         floor, mingap, medianspan, whtype, rawodf)
      ;;   }
      ;; }

      {:name "Onsets",
       :args [{:name "chain"}
              {:name "threshold", :default 0.5}
              {:name "odftype",
               :default :rcomplex
               :map {:power 0 :magsum 1 :complex 2
                     :rcomplex 3 :phase 4 :wphase 5 :mkl 6}}
              {:name "relaxtime", :default 1}
              {:name "floor", :default 0.1}
              {:name "mingap", :default 10}
              {:name "medianspan", :default 11.0}
              {:name "whtype", :default 1}
              {:name "rawodf", :default 0}],
       :rates #{:kr}}

      ;; //transient input not currently used but reserved for future use in downweighting frames which have high transient content
      ;; KeyTrack : UGen {
      ;;   *kr { arg chain,keydecay=2.0,chromaleak= 0.5; //transient=0.0;
      ;;     ^this.multiNew('control',chain,keydecay,chromaleak); //transient;
      ;;   }
      ;; }

      {:name "KeyTrack",
       :args [{:name "chain"}
              {:name "keydecay", :default 2.0}
              {:name "chromaleak", :default 0.5}],
       :rates #{:kr}}

      ;; //a bufnum could be added as third argument for passing arbitrary band spacing data
      ;; MFCC : MultiOutUGen {
      ;;   *kr { arg chain, numcoeff=13;
      ;;     ^this.multiNew('control', chain, numcoeff);
      ;;   }

      ;;   init { arg ... theInputs;
      ;;     inputs = theInputs;

      ;;     ^this.initOutputs(theInputs[1], rate);
      ;;   }
      ;; }

      {:name "MFCC",
       :args [{:name "in"}
              {:name "numcoeff", :default 13}],
       :rates #{:kr}
       :num-outs :variable
       :init (fn [rate args spec]
               {:args args
                :num-outs (args 1)})}

      ;; //6 outs
      ;; BeatTrack2 : MultiOutUGen {

      ;;   *kr { arg busindex, numfeatures, windowsize=2.0, phaseaccuracy=0.02, lock=0, weightingscheme;

      ;;     ^this.multiNew('control',busindex, numfeatures,windowsize, phaseaccuracy, lock, weightingscheme ? (-2.1));
      ;;   }

      ;;   init { arg ... theInputs;
      ;;     inputs = theInputs;
      ;;     ^this.initOutputs(6, rate);
      ;;   }
      ;; }

      {:name "BeatTrack2",
       :args [{:name "busindex"}
              {:name "numfeatures"}
              {:name "windowsize", :default 2.0}
              {:name "phaseaccuracy", :default 0.02}
              {:name "lock", :default 0}
              {:name "weightingscheme", :default -2.1}],
       :rates #{:kr},
       :num-outs 6}

      ;; SpecFlatness : UGen
      ;; {
      ;;   *kr { | buffer |
      ;;     ^this.multiNew('control', buffer)
      ;;   }
      ;; }

      {:name "SpecFlatness",
       :args [{:name "buffer"}],
       :rates #{:kr}}

      ;; SpecPcile : UGen
      ;; {
      ;;   *kr { | buffer, fraction = 0.5, interpolate = 0 |
      ;;     ^this.multiNew('control', buffer, fraction, interpolate)
      ;;   }
      ;; }

      {:name "SpecPcile",
       :args [{:name "buffer"}
              {:name "fraction", :default 0.5}
              {:name "interpolate", :default 0}],
       :rates #{:kr}}

      ;; SpecCentroid : UGen
      ;; {
      ;;   *kr { | buffer |
      ;;     ^this.multiNew('control', buffer)
      ;;   }
      ;; }

      {:name "SpecCentroid",
       :args [{:name "buffer"}],
       :rates #{:kr}}])
