;; Making Music with Clojure
;; Live Coding is all the Rage at Raves
;; By Sam Aaron
;; Originally published in PragPub, December 2013
;; Copyright 2014 Sam Aaron


;; The laser beams sliced through the wafts of smoke as the subwoofer
;; pumped bass deep into the bodies of the crowd. The atmosphere was
;; ripe with a heady mix of synths and dancing. However something wasn't
;; quite right in this nightclub. Projected in bright colours above the
;; DJ booth was futuristic text, moving, dancing flashing. This wasn't
;; fancy visuals, it was merely a projection of a terminal containing
;; Emacs. The occupants of the DJ booth weren't spinning disks, they
;; were writing, editing and evaluating code. This was a Meta-eX
;; (http://meta-ex.com) gig. The code was their musical interface and
;; they were playing it live.

;; This wasn't a scene from a cheesy sci-fi film. Coding music like this
;; is a growing trend and is often described as Live Coding
;; (http://toplap.org). One of the recent directions this approach to
;; music making has taken is the Algorave (http://algorave.com) - events
;; where artists code music for people to dance to. However, you don't
;; need to be in a nightclub to Live Code - you can do it anywhere you
;; can take your laptop and a pair of headphones. In this article, we'll
;; explore one of the most powerful Live Coding toolkits available:
;; Overtone (http://overtone.github.io). Once you reach the end, you'll
;; be programming your own beats and modifying them live. Where you go
;; afterwards will only be constrained by your imagination.

;; Installation

;; To follow along, you'll need a couple of dependencies
;; installed. Firstly you'll need a JVM (https://java.com/getjava)
;; (v1.6+) and you'll also need a handy tool called Leiningen
;; (http://leiningen.org) (v2.0+). Both links provide installation
;; instructions, but ultimately you'll want to be able to run the `lein`
;; command on a terminal/command window and see a list of options.

;; Once you have a JVM and `lein`, create a new project with:

;; lein new insane-sounds

;; You then need to fire up your trusty text editor, open
;; `insane-sounds/project.clj` and add Overtone as a dependency. The file
;; should look as follows:

(defproject foo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [overtone "0.9.1"]])

;; Save the file, then `cd` into the `insane-sounds` directory and run:

;; lein repl

;; This may take some time depending on the speed of your internet
;; connection as it will download all of Overtone's dependencies
;; (including the SuperCollider synthesis server) and then boot you into
;; a Clojure REPL. You should see the following prompt:

;; user=>

;; Now you can start and boot Overtone by typing and entering:

(use 'overtone.live)

;; Evaluating this will start up an audio synthesis server and connect to
;; it for you. Once this process has finished, you should see the following
;; on your console:

;; --> Loading Overtone...
;; --> Booting internal SuperCollider server...
;;
;;    _____                 __
;;   / __  /_  _____  _____/ /_____  ____  ___
;;  / / / / | / / _ \/ ___/ __/ __ \/ __ \/ _ \
;; / /_/ /| |/ /  __/ /  / /_/ /_/ / / / /  __/
;; \____/ |___/\___/_/   \__/\____/_/ /_/\___/
;;
;;  Collaborative Programmable Music. v0.9.1
;;
;; Hello Sam. Do you feel it? I do. Creativity is rushing through your veins today!

;; Let's make a simple sound:

(demo (pan2 (sin-osc)))

;; Beeep! Testing, testing, 1, 2, 3! We can go crazy and change the
;; frequency:

(demo (pan2 (sin-osc 220)))

;; Play around with changing the frequency to different values. How high
;; can you go and still hear it? How low can you go? Try it with some
;; decent headphones/speakers and hear the deep bass pump out...

;; We can even swap out the oscillator generator. Try replacing
;; `sin-osc` with the following: `saw`, `square`,
;; `lf-tri`. Additionally, try removing the `pan2` and replacing the
;; frequency number with a vector of two values:

(demo (saw [55 55.2]))

;; Try different values for `saw`, `100` and `101`. Don't worry too much
;; about what any of this *means* just experiment and have fun! Come
;; back when you've finished giving all your friends a headache...

;; Hello again. Enough with that new fangled synth nonsense. Let's play
;; something more traditional. Pull in the piano synth:

(use 'overtone.inst.piano)

;; We can now trigger a piano sound by calling the `piano` function:

(piano)

;; We can even call it with a MIDI number as an argument to specify the
;; piano note to play:

(piano 63)

;; Luckily, for those that haven't memorised all the MIDI
;; numbers, Overtone provides a handy function:

(note :C4)

;; This then brings us to our first use of Clojure's datastructures for
;; music - we can represent a chord using a vector:

(def c4-minor [(note :C4) (note :Eb4) (note :G4)])

;; This can easily be rendered as audio:

(doseq [note c4-minor]
  (piano note))

;; Or played as a simple arpegio:

(doseq [note c4-minor]
  (piano note)
  (Thread/sleep 200))

;; With the information provided so far in this section, it is
;; completely plausible to imagine piano compositions that consist of
;; interposed calls to `piano` and `Thread/sleep`:

(do
  (piano 50)
  (Thread/sleep 100)
  (piano 72)
  (Thread/sleep 50)
  (piano 49)
  (Thread/sleep 190)
  (piano 68)
  ;;.
  ;;.
  ;;. etc...)

;; The main drawback to defining our composition in such a procedural
;; manner is not only inelegant but also restricts creative flexibility
;; and freedom. Instead, it's often better to use Clojure's
;; datastructures to represent the composition in a declarative
;; style. This then opens us up to using Clojure's powerful set of
;; higher order functions to directly manipulate our compositions. This
;; is something which is explicitly explored in Chris Ford's [Leipzig]
;; (http://github.com/ctford/leipzig) library which can be used to
;; succinctly represent Bach's canons at a very high level. However,
;; rather than look towards Bach, today we're going to consider more
;; contemporary electronic music - live dance and dubstep.

;; Let's drop our piano and introduce some drums. First up, the mighty
;; kick drum:

(def dirty-kick (freesound 30669))

;; You will notice that evaluating this form for the first time may take
;; a few moments to complete. This is because this kick drum is actually
;; a recording of a kick drum in wav format which is available on the
;; fantastic Freesound website (http://freesound.org) and released under
;; a creative commons license. The `freesound` function takes the unique
;; ID of the sound (in this case 30669) and then downloads it on a
;; separate thread caching the file to your hard drive under
;; `~/.overtone`. Therefore the next time you access this particular
;; sound, it will retrieve it from your local file system rather than
;; from the Freesound server.

;; Once this form has completed evaluation, the var `dirty-kick` now
;; references a function which can be used to trigger the sound:

(dirty-kick)

;; Let's define a few more sounds:

(def ring-hat (freesound 12912))
(def snare (freesound 26903))
(def click (freesound 406))
(def wop (freesound 85291))
(def subby (freesound 25649))

;; Feel free to add some of your own. Just navigate to
;; http://freesound.org search around for a sound, make sure it's either
;; a wav or aiff find the ID of the sound (you can see it in the URL)
;; and then pass it as a parameter to the `freesound` function.

;; Let's make a driving beat:

(defonce cont? (atom true))

(future
  (while @cont?
    (subby)
    (Thread/sleep 300)
    (snare)
    (Thread/sleep 300)))

;; OK, so it's more like something out of a marching band than a driving
;; dubstep beat, but it's a good start. To stop it just reset the atom
;; to false:

(reset! cont? false)

;; There's a significant technical issue with this approach related to
;; timing - `Thread/sleep` can never be relied on for strongly timed
;; programs. The main issue here is that it defines a *minimum* amount
;; of time for the current thread to pause not the *precise* amount of
;; time. So, a call to `(Thread/sleep 200)` actually pauses the current
;; thread for `(+ 200 delay-t)` milliseconds where `delay-` is dependent
;; on the internal behaviour and workload of the scheduler and any GC
;; pauses. It can therefore be assumed that `delay-t` is
;; non-deterministic. This means that basing timing on top of
;; `Thread/sleep` will cause temporal drifting which is less than ideal
;; if you want to generate a steady rhythm or beat.

;; Overtone has an excellent solution for this issue - temporal
;; recursion. This is similar to recursion, except for applying the
;; current function at the tail position, we instead schedule it to
;; execute at some future time:

(defn scheduled-hello-world [curr-t sep-t]
  (println "hello world")
  (let [new-t (+ curr-t sep-t)]
    (apply-at new-t #'scheduled-hello-world [new-t sep-t])))

;; Here we create a standard Clojure function which accepts two
;; arguments, the current time and a separation time. The function
;; greets the world and then schedules itself to be executed in the
;; future where the time for execution is the current time plus the
;; separation time. This new time is then passed on to the new function
;; invocation along with the unmodified separation time. When we run
;; this function, with `(now)` and 200 as parameters we'll immediately
;; see our friendly greeting appear on the console every 200ms:

(scheduled-hello-world (now) 200)

;; Luckily this scheduled function honours the `stop` function, so to
;; cease the persistent and repeated friendliness just issue:

(stop)

;; Of course, we can perform any action inside this function, so instead
;; of printing to the console, let's make some sound. However, before we
;; do this, we need just one more time-related concept - the `at`
;; macro. As we saw above, `apply-at` will apply the specified function
;; at the specified time. However, this approach is completely
;; susceptible to similar timing issues as `Thread/sleep`. Through the
;; explicit handling of time, we won't see any cumulative drift
;; effect. However, each individual execution of the scheduled function
;; itself is arbitrarily delayed due to our non-deterministic friend
;; `delay-t`. Luckily we can combat this issue via a two-pronged
;; attack. Firstly, we can use `apply-by` in place of `apply-at`, this
;; will execute our function slightly ahead of our specified time to
;; give room for any execution delay. We then wrap any sound-making
;; triggers or controls within the `at` macro which instructs the audio
;; server to enact the trigger exactly at the specified time:

;; Play the piano 2 seconds from now
(at (+ (now) 2000) (piano))

;; As the audio server is written in highly optimised C++ it can do a
;; far better job of ensuring the action is performed at precisely the
;; correct time. Therefore, by calling our `at` macro sufficiently ahead
;; of time, we can ensure that we suffer no time delays or drift:

(defn beat [curr-t sep-t]
  (at curr-t (subby))
  (let [new-t (+ curr-t sep-t)]
    (apply-by new-t #'beat [new-t sep-t])))

(beat (now) 600)

;; Again we can stop this by calling the stop fn:

(stop)

;; Given this sound-playing and strongly-timed scheduling functionality,
;; we now have the full power of Clojure to build the sound system of
;; our dreams. Let's start by building a simple 8-step sequencer. We can
;; represent the state of a single sequence with a simple vector:

[1 0 0 0 0 0 0 0] ;; One beat at the start of the bar
[1 1 1 1 1 1 1 1] ;; Eight beats per bar
[1 0 1 1 0 0 0 1] ;; A more interesting rhythm

(defn simple-sequencer [curr-t sep-t pattern]
  (at curr-t (when (= 1 (first pattern))
               (subby)))
  (let [new-t (+ curr-t sep-t)]
    (apply-by new-t #'simple-sequencer [new-t sep-t (rest pattern)])))


(simple-sequencer (now) 200 (cycle [1 1 0 1 0 1 0 0]))
(stop) ;; Stop the beat

;; Our simple sequencer lets us play different patterns represented by
;; Clojure vectors. We also take advantage of laziness by creating an
;; infinite lazy sequence of cycles of the pattern to keep the beat
;; rolling on. However, there are a few limitations to this
;; approach. For example, we can only play one pattern at a time, it's
;; hard-coded to play the subby sound and we can't modify it at
;; run-time. Let's tackle each of these issues.

;; One way of representing multiple patterns with arbitrary sounds is to
;; use a map for our representation. For example, the vals within our
;; map could be the patterns and the correspdonding keys the sound
;; functions themselves:

(def pats {subby [1 1 0 1 0 1 0 0]
           snare [1 0 0 1 0 0 1 0]
           wop   [1 0 0 0 0 0 0 1]})

;; For this, we can modify our simple sequencer to work with arbitrary
;; sounds:

(defn play-pattern [curr-t sep-t pattern sound]
  (at curr-t (when (= 1 (first pattern))
               (sound)))
  (let [new-t (+ curr-t sep-t)]
    (apply-by new-t #'play-pattern [new-t sep-t (rest pattern) sound])))

;; We can then create a multi-patterned sequencer:

(defn sequencer [sep-t sequences]
  (let [t (+ (now) 200)]
    (doseq [[sound pattern] sequences]
      (play-pattern t sep-t (cycle pattern) sound))))

(sequencer 200 pats)
(stop)

;; In order to allow us to live-modify the patterns whilst the sequencer
;; is playing, we need to make some modifications. The key change is to
;; store our patterns in an atom. We also need to move away from
;; representing our patterns as an infinite lazy sequence which is
;; defined when the sequencer is created and closed for
;; modification. Instead we can explicitly store the current beat number
;; and on each temporally recursive call into the scheduling function,
;; we can look up the appropriate patten index (which is a mod of the
;; beat number and the size of the pattern). If the value of the pattern
;; at this index is 1 we can then schedule the matching sound to be
;; played. Each time round the temporal recursion we simply need to
;; update the schedule time and increment the beat count.

(def live-pats (atom pats))

(defn live-sequencer
  ([curr-t sep-t live-patterns] (live-sequencer curr-t sep-t live-patterns 0))
  ([curr-t sep-t live-patterns beat]
     (doseq [[sound pattern] @live-patterns
             :when (= 1 (nth pattern (mod beat (count pattern))))]
       (at curr-t (sound)))
     (let [new-t (+ curr-t sep-t)]
       (apply-by new-t #'live-sequencer [new-t sep-t live-patterns (inc beat)]))))

;; Once we have implemented this new live-sequencer function, we can
;; trigger it and then start modifying our live-pats atom:

(live-sequencer (+ 200 (now)) 200 live-pats)

(swap! live-pats assoc subby [1 1 0 1 0 0 1 1])
(swap! live-pats assoc snare [1 1 0 0 0 1 0 0])
(swap! live-pats assoc wop   [1 0 1 0 0 0 1 1])
(stop)

;; Try changing the pattern vector (swapping 1s for 0s and visa versa)
;; and have fun live jamming! When you've had enough, just issue a
;; `(stop)` command.

;; So, how can we have more fun than this? Typically in Overtone land,
;; the feeling that more fun could be had usually indicates that there's
;; an opportunity for us to add more control. One clear way to give us
;; greater control is to allow our patterns to convey information richer
;; than just 1s and 0s to represent on and off. For example, we might
;; want to specify the amplitude or the rate of each individual
;; beat. Luckily our sound function already provides this via keyword
;; arguments:

(subby :rate 2 :amp 0.5)

;; Given this new knowledge, we can now increase our pattern
;; representation from using just 1s and 0s to also include argument
;; maps through the use of the following simple helper fn:

(defn flatten1
  "Takes a map and returns a seq of all the key val pairs:
      (flatten1 {:a 1 :b 2 :c 3}) ;=> (:b 2 :c 3 :a 1)"
  [m]
  (reduce (fn [r [arg val]] (cons arg (cons val r))) [] m))

;; We can now call our synth fn using `apply` and our flattened arg
;; list:

(apply subby (flatten1 {:rate 2 :amp 0.5}))

;; Armed with this, let's improve our sequencer yet again:

(defn live-sequencer
  ([curr-t sep-t live-patterns] (live-sequencer curr-t sep-t live-patterns 0))
  ([curr-t sep-t live-patterns beat]
     (doseq [[sound pattern] @live-patterns
             :let [v (nth pattern (mod beat (count pattern)))
                   v (cond
                      (= 1 v)
                      []

                      (map? v)
                      (flatten1 v)

                      :else
                      nil)]
             :when v]
       (at curr-t (apply sound v)))
     (let [new-t (+ curr-t sep-t)]
       (apply-by new-t #'live-sequencer [new-t sep-t live-patterns (inc beat)]))))

(def a {:rate 0.5})
(def b {:rate 3})
(def c {:rate 10})

(live-sequencer (+ 200 (now)) 200 live-pats)

(swap! live-pats assoc subby [1 1 0 b 0 1 a c])
(swap! live-pats assoc snare [1 1 c c 1 a b c])
(swap! live-pats assoc wop   [c c 1 0 0 0 a c])

(stop)

;; This creates a much richer and interesting rhythm and is already a lot
;; more fun to play with. Let's control one more dimension: time.

;; So far, each sequencer implementation has maintained a constant timed
;; delay between each beat. What if the time for a whole pattern was
;; constant, and the time between each beat a division of that time over
;; the number of beats in the pattern. For example, we could represent a
;; pattern with three beats per bar with:

(def three-beats-per-bar [1 1 1])

;; and a pattern with 9 beats per bar with:

(def nine-beats-per-bar [1 1 1 1 1 1 1 1 1])

;; Therefore, an alternative yet semantically identical version of
;; `three-beats-per-bar` could be:

(def three-beats-per-bar-alt [1 0 0 1 0 0 1 0 0])

;; Let's modify our live-sequencer function to support this new
;; behaviour. First, we need to move to passing the time the full
;; pattern should take to play rather than the separation time between
;; beats. This separation time can them be calculated by dividing the
;; full pattern time by the number of beats within it. We also no longer
;; need to thread a beat count through the temporal recursion as we'll
;; now schedule a whole pattern at once.

(defn normalise-beat-info
  [beat]
  (cond
   (= 1 beat)         {}
   (map? beat)        beat
   (sequential? beat) beat
   :else              {}))

(defn schedule-pattern
  [curr-t pat-dur sound pattern]
  {:pre [(sequential? pattern)]}
  (let [beat-sep-t (/ pat-dur (count pattern))]
    (doseq [[beat-info idx] (partition 2 (interleave pattern (range)))]
      (let [beat-t    (+ curr-t (* idx beat-sep-t))
            beat-info (normalise-beat-info beat-info)]
        (if (sequential? beat-info)
          (schedule-pattern beat-t beat-sep-t sound beat-info)
          (at beat-t (apply sound (flatten1 beat-info))))))))

(defn live-sequencer
  [curr-t pat-dur live-patterns]
  (doseq [[sound pattern] @live-patterns]
    (schedule-pattern curr-t pat-dur sound pattern))
  (let [new-t (+ curr-t pat-dur)]
    (apply-by new-t #'live-sequencer [new-t pat-dur live-patterns])))


(live-sequencer (now) 2000 live-pats)
(swap! live-pats assoc subby [1 1 0 b 0 1 [1 1 1] [1 1 1 1 1 1 1]])
(swap! live-pats assoc snare [1 1 c c 1 a [1 a c 1] c])
(swap! live-pats assoc wop   [c a 0 0 a c c c])

(stop)

;; Finally we need a wobbly bass sound:

(defsynth wobble-bass [amp 1 note 52 wobble 1 detune 1.01 wob-lo 200 wob-hi 20000 pan 0]
  (let [saws          (mix (saw [note (* note detune)]))
        wob-freq      (lin-exp (lf-saw wobble) -1 1 wob-lo wob-hi)
        wob-freq      (lag wob-freq 0.05)
        filtered-saws (lpf saws wob-freq)
        normalized    (normalizer filtered-saws)
        amplified     (* amp normalized)]
    (out 0 (pan2 amplified pan))))

;; Before you freak out too much, we don't expect you to understand how
;; this works any any detail. Suffice to say that `defsynth` is a macro
;; which represents a synthesiser design. If you're interested in
;; further details, you can genrate a more traditional visual
;; representation of this design:

(show-graphviz-synth wobble-bass)

;; The first detail about the `defsynth` macro that is useful to know
;; here is that it creates a function in the current namespace with the
;; same name as the synth - in this case `wobble-bass`. We can use this
;; function to create new running (and therefore audible) instances of
;; the synth:

(wobble-bass)

;; To stop this (and all other running synths) you can use the `stop`
;; function:

(stop)

;; The second useful detail is the vector of symbol value pairs
;; immediately after the synth name represents the controllable
;; parameters. Our `wobble-bass` function allows us to specify these
;; using a named-argument style:

(wobble-bass :amp 0.5 :note 30 :wob-hi 2000)
(stop)

;; The final handy piece of knowledge is that the return value of the
;; `wobble-bass` function is a record which represents the running synth
;; created via the call. This record can be used to directly maniulate
;; the synth live whilst it is running using the `ctl` function which is
;; short for control:

(def wb (wobble-bass))
(ctl wb :amp 0.5 :note 50 :wobble 2)
(ctl wb :amp 0.5 :note 62 :wobble 1)
(ctl wb :note 40)
(ctl wb :wobble 0.1)
(ctl wb :amp 1)
(ctl wb :wob-hi 5000)
(ctl wb :wob-lo 100)

;; bring back the beats!

(live-sequencer (now) 2000 live-pats)
(swap! live-pats assoc subby [1 1 0 b 0 1 [1 1 1] [1 1 1 0 1 1 1]])
(swap! live-pats assoc snare [1 1 c c 1 a [1 a c 1] c])
(swap! live-pats assoc wop   [c a 0 0 a c c c])

(stop) ;; stop the insanity!

;; So, there you have it - we just coded from scratch a mini
;; live-modifiable drum patten DSL. How fun is that! This was clearly
;; just a small taster of the fantastic power that the heady combination
;; of Clojure and SuperCollider can offer you. Try playing with
;; different rhythms, different samples (any wav file from Freesound is
;; just but a call to `freesound` away) and let your imagination run
;; riot.

;; If you stick at it, perhaps you'll find yourself programming in a
;; nightclub too...
