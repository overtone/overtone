(ns overtone.core.ugens.envgen)

(def specs
     [

      ;; Done : UGen {  
      ;;  *kr { arg src;
      ;;    ^this.multiNew('control', src)
      ;;  }
      ;; }

      {:name "Done",
       :args [{:name "src"}],
       :rates #{:kr}}
      
      ;; FreeSelf : UGen {  
      ;;  *kr { arg in;
      ;;    this.multiNew('control', in);
      ;;    ^in
      ;;  }
      ;; }

      {:name "FreeSelf",
       :args [{:name "in"}],
       :rates #{:kr}}

      ;; PauseSelf : UGen { 
      ;;  *kr { arg in;
      ;;    this.multiNew('control', in);
      ;;    ^in
      ;;  }
      ;; }

      {:name "PauseSelf",
       :args [{:name "in"}],
       :rates #{:kr}}
      
      ;; FreeSelfWhenDone : UGen {  
      ;;  *kr { arg src;
      ;;    ^this.multiNew('control', src)
      ;;  }
      ;; }

      {:name "FreeSelfWhenDone",
       :args [{:name "src"}],
       :rates #{:kr}}
      
      ;; PauseSelfWhenDone : UGen { 
      ;;  *kr { arg src;
      ;;    ^this.multiNew('control', src)
      ;;  }
      ;; }

      {:name "PauseSelfWhenDone",
       :args [{:name "src"}],
       :rates #{:kr}}

      ;; Pause : UGen { 
      ;;  *kr { arg gate, id;
      ;;    ^this.multiNew('control', gate, id)
      ;;  }
      ;; }

      {:name "Pause",
       :args [{:name "gate"}
              {:name "id"}],
       :rates #{:kr}}
      
      ;; Free : UGen {  
      ;;  *kr { arg trig, id;
      ;;    ^this.multiNew('control', trig, id)
      ;;  }
      ;; }

      {:name "Free",
       :args [{:name "trig"}
              {:name "id"}],
       :rates #{:kr}}

      ;; EnvGen : UGen { // envelope generator  
      ;;  *ar { arg envelope, gate = 1.0, levelScale = 1.0, levelBias = 0.0, timeScale = 1.0, doneAction = 0;
      ;;    ^this.multiNewList(['audio', gate, levelScale, levelBias, timeScale, doneAction, `envelope])
      ;;  }
      ;;  *kr { arg envelope, gate = 1.0, levelScale = 1.0, levelBias = 0.0, timeScale = 1.0, doneAction = 0;
      ;;    ^this.multiNewList(['control', gate, levelScale, levelBias, timeScale, doneAction, `envelope])
      ;;  }
      ;;  *new1 { arg rate, gate, levelScale, levelBias, timeScale, doneAction, envelope;
      ;;    ^super.new.rate_(rate).addToSynth.init([gate, levelScale, levelBias, timeScale, doneAction] 
      ;;      ++ envelope.dereference.asArray); 
      ;;  }
      ;;    init { arg theInputs;
      ;;      // store the inputs as an array
      ;;      inputs = theInputs;
      ;;    }
      ;;  argNamesInputsOffset { ^2 }
      ;; }

      ;; in SCLang a ref is used to avoid expansion. the convenience
      ;; of having expansion on a vector of envelopes is great though
      ;; so either use an env object, a nested vector [[...]], or <!> 
      ;; for envelope parameter o avoid expansion
      
      {:name "EnvGen",
       :args [{:name "envelope"} 
              {:name "gate", :default 1.0}
              {:name "levelScale", :default 1.0}
              {:name "levelBias", :default 0.0}
              {:name "timeScale", :default 1.0}
              {:name "doneAction", :default :none}]
       :init (fn [rate [env & args] spec]
               (concat args env))}
               ;(let [envec (TODO turn env object into vector)]
      
      ;; Linen : UGen {
      ;;  *kr { arg gate = 1.0, attackTime = 0.01, susLevel = 1.0, releaseTime = 1.0, doneAction = 0;
      ;;    ^this.multiNew('control', gate, attackTime, susLevel, releaseTime, doneAction)
      ;;  }
      ;; }

      {:name "Linen",
       :args [{:name "gate", :default 1.0}
              {:name "attackTime", :default 0.01}
              {:name "susLevel", :default 1.0}
              {:name "releaseTime", :default 1.0}
              {:name "doneAction", :default :none}],
       :rates #{:kr}}

      ;; from IEnvGen.sc
      ;; IEnvGen : UGen { // envelope index generator  
      ;;  *ar { arg ienvelope, index, mul = 1, add = 0;
      ;;    var offset;
      ;;    ienvelope = ienvelope.isKindOf(Env).if({
      ;;      InterplEnv.new(ienvelope.levels, ienvelope.times, ienvelope.curves);
      ;;      }, {
      ;;      ienvelope;
      ;;      });
      ;;    ^this.multiNewList(['audio', index, `ienvelope]).madd(mul, add);
      ;;  } 
      ;;  *kr { arg ienvelope, index, mul = 1, add = 0;
      ;;    var offset;
      ;;    ienvelope = ienvelope.isKindOf(Env).if({
      ;;      InterplEnv.new(ienvelope.levels, ienvelope.times, ienvelope.curves)
      ;;      }, {
      ;;      ienvelope
      ;;      });
      ;;    ^this.multiNewList(['control', index, `ienvelope]).madd(mul, add);
      ;;  }
      ;;  *new1 { arg rate, index, ienvelope, mul = 1, add = 0;
      ;;    ^super.new.rate_(rate).addToSynth.init([index] 
      ;;      ++ ienvelope.dereference.asArray).madd(mul, add); 
      ;;  }
      ;;    init { arg theInputs;
      ;;      // store the inputs as an array
      ;;      inputs = theInputs;
      ;;    }
      ;;  argNamesInputsOffset { ^2 }
      ;; }

      ;; TODO figure out what an IEnvGen is and write init
      {:name "IEnvGen"
       :args [{:name "ienvelope"}
              {:name "index"}]
       :muladd true
       :init (fn [rate [env & args] spec]
               )}])
