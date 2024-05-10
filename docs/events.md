## Event System

Overtone uses a simple event system where an event can be published like this:

```clojure
(event :my-event :foo "asdf" :bar 200.0)
```

where an event has a label and any number of key/value pairs. Events can be
handled by registering a handler function. You specify the event type to handle, the function to be called, and a key. Calling `on-event` with the same key argument will replace the previous handler, which is useful when working in a REPL. (Similar to Clojure's `add-watch`.) 

```clojure
(on-event :my-event #(do-stuff %) ::k)
```

The handler should take a single argument, the event map. The event map contains the key/value pairs and an

```clojure
{:event-type :my-event
 :foo "asdf"
 :bar 200.0}
```

Event handlers can de-register themselves by returning the keyword
`:overtone/remove-handler` after executing. This can be used to create one-off
handlers and more.

```clojure
;; Register a one shot handler for a :special-moment event
(on-event :special-moment
          (fn [_]
            (println "special moment")
            :overtone/remove-handler)
          ::k)

;; Fire the :special-moment event in 3 and 4 seconds
(after-delay 3000 #(event :special-moment))
(after-delay 4000 #(event :special-moment))

;; You will only see the event fire once
```

### OSC Message Events

OSC messages are automatically re-published as events, so when the application
receives the OSC message "/boom" it will publish an event of type "/boom" that
has two properties, :path and :args, corresponding to the osc path and
arguments.  If boom had a single frequency value argument, you could handle it
like this:

```clj
(on "/boom" #(play-boom (first (:args %))))
```

We also publish a `[:overtone :osc-msg-received]`, with a single `:msg` key,
allowing listening to any OSC event.

### Built-in Events

Some of the built-in events that might be of general interest are:

- `:booted => {}` the audio server process has been booted
- `:connected => {}` we have successfully connected with an audio server
- `:new-synth => {:synth {:name, :player, :sdef, :group}}` a synth has been defined and loaded

### Debugging Events

You can turn event debug logging on/off to see exactly which events are being published.

```clj
(event-debug-on)

(defsynth foo []
  (sin-osc))
  
;; event:  :new-synth (:synth #overtone.sc.synth.Synth{:name "foo", :ugens nil, :sdef {:name "mad-sounds.sessions.euro427/foo", :constants [0.0 1.0 440.0], :params (), :pnames (), :ugens (#<sc-ugen: sin-osc:ar [0]>)}, :args (), :params (), :instance-fn #function[clojure.core/identity]}) 

(event-debug-on)
```

### Midi events

Any incoming MIDI message automatically gets published as an event. In fact,
each message gets published four times, with different keys, allowing you to
listen to events of a certain type, events from a certain device, events from a
certain type of device (in case there is more than one connected), or events of
a certain type and from a given device.

See `overtone.examples.midi.basic` and `overtone.examples.midi.keyboard`.

### Note, rest, ctl, and chord events

Overtone has a few built-in handlers, which allow you to trigger and control
synths through events.

We'll use this example synth to demonstrate the options:

```clj
(definst gloria [freq 265
                 gate 1.0
                 amp 1
                 cutoff 4000]
  (pan2 
   (* (env-gen (adsr :attack 0.009 :decay 0.2 :sustain 0.5) :gate gate :action FREE)
      amp
      (moog-ff
       (var-saw freq)
       cutoff))))
```

This synth has four parameters, `freq`, `gate`, `amp`, and `cutoff`. The first
two of these form an important convention. By definining your synths with a
`freq` parameter which determines the pitch of the sound, and a `gate` which
closes the envelope and frees the synth, the event system is able to give you
multiple ways to specify how to play this synth.

For instance:

```clj
(event :note {:instrument gloria
              :midinote 60
              :cutoff 9000})
```

The synth does not have a `:midinote` parameter, but it does have a `:freq`
parameter. So it computes the `:freq` based on the `:midinote`. If the
`:midinote` is also missing, it will look for a `:note`, `:root`, and `:octave`,
use those to compute the `:midinote`, and use that to compute the `:freq`.

Here's a visual overview of how all these different keys relate:

```
detuned-freq
├── detune
└── freq
    ├── ctranspose
    ├── harmonic
    └── midinote
        ├── gtranspose
        ├── note
        │   ├── degree
        │   ├── mtranspose
        │   ├── scale-notes
        │   │   └── scale-intervals
        │   │       └── mode
        │   └── steps-per-octave
        ├── octave
        ├── octave-ratio
        ├── root
        └── steps-per-octave
```

While these parameters are all potentially used to compute the frequency, the
event system also needs to know the timing of the note. When does it start, when
does it end, and when (when playing a pattern, which we'll get to later), should
the next note play. There's a set of parameters to determine those things as
well.

By default a note event will trigger the instrument immediately, and schedule
for the note to stop (the `gate` to go to `0`) after the length of 1 beat, based
on the current speed (bpm) of the built in metronome
(`overtone.studio.transport/*clock*`), which defaults to 128.

At the low level you can specify an explicit `:start-time` and `:end-time`.
These take unix timestamps in milliseconds, and are used internally with the
`at` macro to determine when to send the relevant commands to SuperCollider.

For instance, this plays a half second note, half an hour from now.

```clj
(event :note {:instrument gloria
              :start-time (+ 500 (now))
              :end-time (+ 1000 (now))})
```

You can also pass a `:clock`, and specify the `:beat` number.

```clj
(let [clock (metronome 120)]
  (dotimes [i 3]
    (event :note {:instrument gloria
                  :clock clock
                  :beat (+ i (clock))
                  :dur 1/2})))
```

When using the pattern player (`pplay`), the `transport/*clock*` is always used,
unless a clock is explicitly specified, and the beat is computed based on the
durations (`:dur`) of previous notes in the pattern.

```
start-time
├── beat
│   └── clock
├── clock
├── swing
└── swing-quant

end-time
├── beat
├── bpm
├── clock
└── dur
```

See the [Event Reference](event_reference.md) for all details.

Apart from `:note` events Overtone also understands `:rest` and `:chord` events.
Rest events don't do much by themselves, but are useful in patterns.

An event of `:type :chord` accepts the `:chord` parameter, as per
`resolve-chord`, this means you can either specify a keyword like `:major`,
`:minor`, or `:7sus4`, or you can provide a set like `#{0 4 7}`, with the
intervals from the root in semitones. This is then combined with the `:midinote`
(explicit or computed), to determine the exact notes of the chord. These are all
played at once, unless `:strum` is specified.

The special keyword `:from-scale` means a number of thirds are taken from the
current scale (as per `:root`, `:mode`), starting from the given `:degree`.

To have more (or fewer) than 3 notes in a chord, specify the `:chord-size`. To
get a different inversion, try setting `:inversion`.
