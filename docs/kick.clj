(ns doc.kick
  (:use [overtone.live])
  (:use [overtone.sc.machinery.synthdef])
  (:use [clojure.pprint]))

;;This is an annotated version of this synth definition to help in understanding
;; some basics about synths and the synthdef data structure used in Overtone.

;; Here is a basic kick drum.  An envelope generator is multiplied by a low frequency
;; sin wave, which is like turning the volume knob up and down really quick while playing
;; a low tone.
(defsynth kick [amp 0.5 decay 0.6 freq 65]
  (let [env (env-gen (perc 0 decay) 1 1 0 1 FREE)
        snd (sin-osc freq (* Math/PI 0.5))]
    (out 0 (pan2 (* snd env amp) 0))))

;; This can be triggered using the name as a function call:
(kick)

;; A shorter variation using definst which allows you to leave out the out and pan ugens,
;; which get added by default to synths that are rooted by an audio rate ugen that isn't out.
(definst kick [amp 0.5 decay 0.6 freq 65]
  (* (sin-osc freq (* Math/PI 0.5))
     (env-gen (perc 0 decay) 1 1 0 1 FREE)
     amp))

;; Similarly this is also triggered using the name as a function call:
(kick)

;; The defsynth function will create a synthesizer definition structure that mimics the
;; binary format sent to the SuperCollider server.  These could also be created in other
;; ways, for example if you have in idea for a synthesizer DSL...

;; The structure is stored in each synth you create in a key called :sdef. So to retrieve
;; the structure of our kick synth we need to issue:
(:sdef kick)

;; This might be improved by pretty printing the result:
(synthdef-print (:sdef kick))

;; Here is the annotated output of (:sdef kick)
(pprint (:sdef kick))

{
 :name "kick",
 :n-params 3,             ; The number of controllable parameters
 :params [0.5 0.6 65.0],  ; default parameter values

 ;; The name of each parameter with the index of its default value in the :params vector.
 :n-pnames 3,
 :pnames [{:index 0, :name "amp"} {:index 1, :name "decay"} {:index 2, :name "freq"}],

 ;; All constant values used in the synth definition
 :n-constants 7,
 :constants [1.0 0.0 2.0 -99.0 5.0 -4.0 1.5707964]

 ; All the ugens in the order that they should execute in the DSP engine.  For the synth
 ; tree created by typical synthdefs we do a depth first iteration to sort the ugens.
 :n-ugens 7,
 :ugens
 [
  ; One control ugen is created for each rate of input parameters.  In this case all of
  ; the inputs are standard control rate (:kr) so we just have a single control.  A control
  ; is always the first ugen executed, and its named parameters can be modified at
  ; runtime.
  {:outputs [{:rate 1} {:rate 1} {:rate 1}],
   :inputs [],
   :special 0,     ; a 'special' number used to parameterize some UGens
   :n-outputs 3,   ; number of outputs
   :n-inputs 0,    ; number of inputs
   :rate 1,        ; operating rate for this ugen
   :name "Control" ; The SuperCollider UGen name
   }

  ; An envelope generator is used to control the amplitude of audio data or a
  ; ugen parameter.
  {:name "EnvGen",
   :rate 2,
   :special 0,
   :n-inputs 17,
   :n-outputs 1,
   :outputs [{:rate 2}], ; A vector of output rates for this ugen

   ; A vector of input specs used to connect up with the outputs of other ugens or
   ; constant values.  The :src field points to another ugen in this :ugens vector, and
   ; the :index points to a specific output of that ugen.  Using a :src of -1 means that
   ; the index points to a value in the :constants vector.
   :inputs [{:index 0, :src -1}
            {:index 0, :src -1}
            {:index 1, :src -1}
            {:index 0, :src -1}
            {:index 2, :src -1}
            {:index 1, :src -1}
            {:index 2, :src -1}
            {:index 3, :src -1}
            {:index 3, :src -1}
            {:index 0, :src -1}
            {:index 1, :src -1}
            {:index 4, :src -1}
            {:index 5, :src -1}
            {:index 1, :src -1}
            {:index 1, :src 0}
            {:index 4, :src -1}
            {:index 5, :src -1}]
   }

  ; A sin wave oscillator connected to the freq output value of the Control ugen
  ; and a constant.
  {:name "SinOsc",
   :rate 2,
   :special 0,
   :n-outputs 1,
   :n-inputs 2,
   :outputs [{:rate 2}],
   :inputs [{:index 2, :src 0}
            {:index 6, :src -1}]
   }

  ; Multiplying the sin-osc with the amplitude control value with the catch all
  ; binary-op-ugen.  Depending on the value of :special it will compute +, -, *, /,
  ; and around 30 other binary operators.  This is created by overloading these
  ; operators in clojure, so any arithmetic done on a ugen is converted to use
  ; this ugen object.
  {:name "BinaryOpUGen",
   :rate 2,
   :special 2,
   :n-outputs 1,
   :n-inputs 2,
   :outputs [{:rate 2}],
   :inputs [{:index 0,
             :src 2}
            {:index 0,
             :src 0}]
   }

  ; Multiplying the env and snd components
  {:name "BinaryOpUGen",
   :rate 2,
   :special 2,
   :n-outputs 1,
   :n-inputs 2,
   :outputs [{:rate 2}],
   :inputs [{:index 0, :src 3}
            {:index 0, :src 1}]
   }

  ; Takes an input source and outputs it in 2 channels with an adjustable
  ; pan control.
  {:name "Pan2",
   :rate 2,
   :special 0,
   :n-outputs 2,
   :n-inputs 3,
   :outputs [{:rate 2}
             {:rate 2}],
   :inputs [{:index 0, :src 4}
            {:index 1, :src -1}
            {:index 0, :src -1}]
   }

  ; Outputs the audio data from this synth and puts it onto an audio bus, which
  ; by default is bus zero, representing the sound-card.  If a vector of two channels
  ; are passed to the out ugen it will output in stereo, or 4 channels for 4 channel
  ; output on an audio interface, etc...
  {:name "Out",
   :rate 2,
   :special 0,
   :n-outputs 0,
   :n-inputs 3,
   :outputs [],
   :inputs [{:index 1, :src -1}
            {:index 0, :src 5}
            {:index 1, :src 5}]
   }],

 :n-variants 0,
 :variants [],
}
