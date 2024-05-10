# Event Reference

## Testing derived properties with `eget`

The event system is very flexible, for example these are all valid ways to play a middle C.

```clj
{:midinote 60}
{:note :c5}
{:note 0 :root :c5}
{:note 0 :root 0 :octave 5}
{:degree 1 :mode :major :root 0 :octave 5}
```

Ultimately all of these result in a computed `:freq` of 261.63Hz. You can test
this with `eget`.

```clj
(eget {:midinote 60} :freq)
;; => 261.6255653005986
```

This is a very useful tool to validate your expectations.

## `:note` event

Plays a note using the provided `:instrument`, all other keys are optional.

### `:instrument`

Instrument to play, should be a `defsynth` or `definst`. The instrument should
have a `:freq` parameter if you intend to control the instrument's pitch (this
may not be the case for instance with percussion).

If the instrument has an envelope with a "sustain" portion (`asr`, `adsr`, etc.)
then it should also have a `:gate` paremeter which is passed to `env-gen`, so
the note can be stopped in time, rather than ring indefinitely. The envelope
should also free itself (`:action FREE`), or SuperCollider will quickly get
overwhelmed when playing mulitple notes, by allocating too many synths and not
freeing them up.

Any other event key that has a matching instrument parameter is used directly to
control that parameter.

An `:amp` paremeter is recommend by convention, but currently does not have any
specific functionality associated with it beyond being a instrument parameter
you can control.

This means the basic structure for an instrument controllable through events and
patterns looks like this:

```clj
(definst example [freq 440 gate 1 amp 1]
  (* (env-gen (adsr) :gate gate :action FREE)
     amp
     your-sound-source))
```

### `:freq`, `:detune`, and `:detuned-freq`

The frequency passed to the synth/inst is the computed `:detuned-freq` property,
unless specified explicitly (which you generally don't want to do), it is
computed by adding the `:freq` and `:detune` values together. Both are specified
in Herz (cycles per second). `:detune` defaults to 0.0.

`:freq` when not explicitly provided in the event map is computed based on the
`:midinote`, and related properties.

`:freq` can also be the keyword `:rest` or `:_`, both of which will effectively
turn the note event into a rest event, preventing the instrument from being
played. (Mostly useful in patterns, see `pbind`).

### `:midinote`, `:ctranspose`, `:harmonic`

`:midinote` is a number specfying a value in semitones. A middle C (octave 5) is
midinote 60. A C# is midinote 61, a B is midinote 59. While in actual MIDI the
note value is an integer between 0 and 128, in the case of `:midinote` values
outside of this range are also accepted, including fractions and floating point
numbers. The conversion to Herz is done with `midi->hz`.

The `:midinote` value (explicitly provided or computed) can be modified with
`:ctranspose`, which lets you transpose the `:midinote` value by a number of
semitones (default: 0). The result is multiplied by `:harmonic` (default 1), to
get to the actual `:freq` that will be played.

If `:midinote` is not provided, then it is computed based on the `:note`, and
related properties.

`:midinote` can also be the keyword `:rest` or `:_`, both of which will effectively
turn the note event into a rest event, preventing the instrument from being
played. (Mostly useful in patterns, see `pbind`).

### `:note`, `:octave`, `:root`, `:gtranspose`

`:note` has a triple function, it can be used to specify an exact note and
octave using a keyword or string, e.g. `:c#5`. In this case there is enough
information to convert this to a `:midinote`, and no other properties are used.

One can also use a note name, but without the octave, so `"c#"`. In this case
the `:octave` property is used to find which octave the note is in.

If `:note` is a number, then it indicates a number of semitones above the
`:root`, which itself can be specified in the same three ways: note name with
octave (`:d3`), note name + `:octave` (`{:root :d :octave 2}`), number of
semitones (2). The default `:octave` is 5, the default `:root` is 0. In other
words, a middle C.

The final result can be further modulated by a number of semitones (or "steps"
when using a non-traditional defition of an octave) through `:gtranspose`.

The choice between which of the three `:note` notations to use will depend on
what seems most natural and convenient, but also on the kind of control that is
desired. Especially when generating patterns it might be interesting to be able
to keep the notes of a pattern but transpose them to a different octave, in this
case having the `:octave` as a separate integer is beneficial.

If `:note` is not present it is computed based on the `:degree` and related
properties.

Like `:freq` or `:midinote`, `:note` too can take the special keyword `:rest` or
`:_`, to turn the note event into a rest.

### `:octave-ratio`, `:steps-per-octave`

In western music, two pitches are said to be an octave apart when the ratio
between them is 2 (or 1/2, respectively). In a standard equal tempered tuning
this octave interval is further divided into 12 "steps" or semitones, whereby
the frequency ratio between two successive steps is always the same.

Both of these assumptions can be modified with the `:octave-ratio` (default: 2)
and `:steps-per-octave` (default: 12) properties, and this will influence how a
midinote is derived from a `:note`, `:octave`, and `:root`.

### `:degree`, `:mtranspose`

With `:degree`, you can pick notes by specifying degrees of a given scale. For
instance, in a C major scale the third degree is a `E`, whereas in C minor it is
a `Eb`.

As with `:note`, both keywords like `:iii` (Roman numerals) or plain integers
(3) are accepted. Note that similar to common practice in music theory, and in
line with earlier conventions in Overtone, but contrary to the use of "degree"
in SuperCollider, degrees in Overtone start from 1, not from zero.

The degree can be modulated with `:mtranspose` (modal transpose), which is added
onto the degree before resolving it against the current scale.

### `:mode`, `:scale-intervals`, `:scale-notes`

A scale is defined by its mode (major, minor, ionian, ...) and the root note.
The mode determines the intervals between subsequent notes.

The easiest way to specify the `:mode` is with a keyword, which has to be one of
the keys in `overtone.music.pitch/SCALE`.

Alternatively you can specify `:scale-intervals`, for instance to achieve a
major scale you could set `:scale-intervals [2 2 1 2 2 2 1]`.

This is then used to compute `:scale-notes`, which is a sequence of intervals in
semitones starting from the root. So to specify a major scale with
`:scale-notes` would be `[0 2 4 5 7 9 11]`.

### `:clock`, `:beat`, `:dur`, `:start-time`, `:end-time`

These properties together determine when a note starts to play, and when it
ends. When playing patterns you can generally ignore these apart from `:dur`,
which is the duration of a given note in beats. It defaults to 1 beat, or a
quarter note in a typical 4/4 signature. 

In a pattern scenario, each note gets a `:beat` based on where the pattern
player is in the piece, so the `:beat` of a given note is the `:beat`+`:dur` of
the previous note, and based on the `:beat` and the `:clock` the `:start-time`
is determined. If no `:clock` is specified in the event, the global clock
(`overtone.studio.transport/*clock*`) is used.

When triggering events directly the expaction is that notes play when the event
is triggered, so by default no timing calculations happen for the `:start-time`,
the synth is triggered instantly.

We still need to decide upon the length of the note to end it. We can take the
duration `:dur` in account, but this is specified in number of beats, so we need
to know the `:bpm`. The `:bpm` is either provided explicitly, or taken from the
`:clock`, with a falback to the global `overtone.studio.transport/*clock*`.

Note that this does not mean that beats will be synchronized with the clock
(metronome). To do that you can specify a `:clock` and `:beat`. Make sure to
base this relatively off the current value returned by the clock, so you don't
schedule notes in the past.

Finally you can also do your own timing calculations and specify a `:start-time`
and `:end-time` directly, these are unix timestamps with millisecond precisions.

Note that `:dur` and `:end-time` are only relevant when the synth has a `:gate`
parameter, so it can be stopped.

## `:ctl` event

Sends control (`ctl`) messages to SuperCollider to change the parameter
(ControlProxies) of a running synth. The synth/inst is specified with
`:instrument`. Timing is handled as with `:note` (`:start-time`, `:clock`,
`:beat`), but there's no notion of duration or end time, it's simply a one-time
event. Properties in the event map that don't correspond with synth parameters
are ignored.

## `:chord` event

Play multiple notes at once. `:chord` can operate in two ways, based on the
current scale, or based on a given chord type.

### `:chord` property

This should either be one of the keys of `overtone.music.pitch/CHORD` (e.g.
`:major`, `:minor`), a set/sequence of intervals in semitones from the root note
(e.g. `#{0 4 7}`), or the special keyword `:from-scale`.

When the value is `:from-scale`, then the chord is determined by stacking thirds
on top of the note determined by the current `:mode`/`:root`/`:degree`, within
the current mode. In other words, only notes that are part of the current scale
are used, and the type of chord depends on which note of the scale (which
degree) you start from.

Otherwise the current `:midinote` is used (provided or computed), and the chord
is built from there based on the intervals in the given chord type.

### `:inversion` property

Invert the chord.

- `:inversion 1` - root note moves up an octave
- `:inversion 2` - root note and first third move up an octave
- `:inversion -1` - highest note moves down an octave

### `:chord-size` property

Number of thirds to stack, defaults to 3. Change to 4 to get 7th chords, for instance.
