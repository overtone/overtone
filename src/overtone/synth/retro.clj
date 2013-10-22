(ns
    ^{:doc "Clones of historical synths.  Add your favorites here."}
  overtone.synth.retro
  (:use [overtone.core]))

;; TB-303 clone Supercollider starting point by Dan Stowell
;; http://permalink.gmane.org/gmane.comp.audio.supercollider.user/22591
;; SynthDef("sc303", { arg out=0, freq=440, wave=0, ctf=100, res=0.2,
;; 		sus=0, dec=1.0, env=1000, gate=0, vol=0.2;
;; 	var filEnv, volEnv, waves;
;;
;; 	// can't use adsr with exp curve???
;; 	//volEnv = EnvGen.ar(Env.adsr(1, 0, 1, dec, vol, 'exp'), In.kr(bus));
;; 	volEnv = EnvGen.ar(Env.new([10e-10, 1, 1, 10e-10], [0.01, sus, dec],
;;                                  'exp'), gate);
;; 	filEnv = EnvGen.ar(Env.new([10e-10, 1, 10e-10], [0.01, dec],
;;                                  'exp'), gate);
;;
;; 	waves = [Saw.ar(freq, volEnv), Pulse.ar(freq, 0.5, volEnv)];
;;
;; 	Out.ar(out, RLPF.ar( Select.ar(wave, waves), ctf + (filEnv * env), res).dup * vol);
;; }).send(s);
;; Overtone port by Roger Allen.
(defsynth tb-303
  "A clone of the sound of a Roland TB-303 bass synthesizer."
  [note     30        ; midi note value input
   wave     0         ; 0=saw, 1=square
   cutoff   100       ; bottom rlpf frequency
   env      1000      ; + cutoff is top of rlpf frequency
   res      0.2       ; rlpf resonance
   sus      0         ; sustain level
   dec      1.0       ; decay
   amp      1.0       ; output amplitude
   gate     0         ; on/off control
   action   NO-ACTION ; keep or FREE the synth when done playing
   position 0         ; position in stereo field
   out-bus  0]
  (let [freq-val   (midicps note)
        amp-env    (env-gen (envelope [10e-10, 1, 1, 10e-10]
                                          [0.01, sus, dec]
                                          :exp)
                              :gate gate :action action)
        filter-env (env-gen (envelope [10e-10, 1, 10e-10]
                                          [0.01, dec]
                                          :exp)
                              :gate gate :action action)
        waves      [(* (saw freq-val) amp-env)
                    (* (pulse freq-val 0.5) amp-env)]
        tb303      (rlpf (select wave waves)
                           (+ cutoff (* filter-env env)) res)]
    (out out-bus (* amp (pan2 tb303 position)))))
