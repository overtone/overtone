# Pattern Library

This document assumes you are familiar with [Event System](events.md), in
particular the ins and outs of note events. You might also want to keep the
[Events Reference](events_reference.md) handy.

Overtone's pattern libary consists of functions that help you create
(potentially infinite) sequences of event maps, and to play those events in
order. This allows you to create musical phrases, and to piece those together
into a musical piece.

It is (quite clearly) inspired by SuperCollider's pattern library, but fitted to
match what makes sense in the context of Clojure and Overtone. If you are
familiar with the former then expect things to be different.

## Playing equences with `pplay` Notes

A pattern is a sequence of event maps. You can play a pattern with `pplay`,
which takes a key and the sequence. The key is arbitrary, but it lets you
stop/pause/restart the pattern later on.

We will continue to use the example "gloria" synth from the [Events](events.md)
documentation.

```clj
(pplay ::x
       [{:instrument gloria
         :note       :c4
         :dur        1}
        {:instrument gloria
         :note       :d4
         :dur        1/2}
        {:instrument gloria
         :note       :e4
         :dur        1}])
```

This plays a little C D E melody, with the C and E quarter notes (1 beat each),
and the D being an eight note.

Note that in this case there is no need to use any of the timing parameters like
`:beat`, `:start-time`, or `:end-time`. The length of the note, and the start
time for the following, are all determined by `:dur`.

Now it's just a matter of building up these sequences. Clojure's standard
library is your friend! `concat`, `repeat`, `repeatedly`, `map`, `for`, and
others will all serve you well. But Overtone also offers a number of specialized
functions. The true workhorse, and the one most important to get familiar with,
is `pbind`. Before we look at `pbind` though let's walk you through some more of
the features that `pplay` has.

### Nesting sequences, `:proto`

`pplay` will automatically flatten nested sequences, this makes it a bit more
convenient to stitch phrases together into larger phrases and melodies.

If you have repetitive elements in your events, you can put those in a `:proto`
(prototype) map, so you don't have to repeat them all the time.

```clj
(def phrase1
  [{:note :c :dur 1}
   {:note :d :dur 1/2}
   {:note :e :dur 1}
   {:note :rest :dur 3/4}])

(def phrase2
  [{:note :g :dur 1}
   {:note :c :dur 1/2}
   {:note :d :dur 1}
   {:note :rest :dur 3/4}])

(pplay ::x
       (repeat 3 [phrase1 phrase2])
       {:proto {:instrument gloria
                :octave 4}})
```

### Additional options `:offset`, `:quant`, `:clock`

These let you further influence the timing of a pattern. `:quant` will cause the
pattern to start on a beat number that is a multiple of this number. So if you
have 4-beat (one bar) length patterns that should play in sync, then they should
all get a `{:quant 4}` option.

`:offset` will cause `pplay` to wait this many beats before starting the
pattern. This way you can trigger a number of patterns at the same time, but
have some start a bit later than the others.

`:clock` lets you set the clock (metronome) object being used. Defaults to
`overtone.studio.transport/*clock*`.

## Generating pattern sequences with `pbind`

`pbind` takes a map, where one or more of the values are sequences, it expands
this into a sequence of maps.

```clj
(pbind {:x [1 2 3]})
;; => ({:x 1} {:x 2} {:x 3})
```

Any regular (non-sequence) values are simply kept as-is.

```clj
(pbind {:x "a" :y [1 2 3]})
;; => ({:x "a", :y 1} {:x "a", :y 2} {:x "a", :y 3})
```

If there are multiple sequences, then elements of each are taken pairwise.

```clj
(pbind {:x ["a" "b" "c"] :y [1 2 3]})
;; => ({:x "a", :y 1} {:x "b", :y 2} {:x "c", :y 3})
```

If the length of the sequences doesn't match, then the shorter ones loop until
the longest sequence has been fully used.

```clj
(pbind {:x ["a" "b"] :y [1 2 3]})
;; => ({:x "a", :y 1} {:x "b", :y 2} {:x "a", :y 3})
```

Just like `pplay`, `pbind` will automatically flatten nested sequences.

```clj
(pbind {:x [["a" "b"] "c" ["d"]]})
;; => ({:x "a"} {:x "b"} {:x "c"} {:x "d"})
```

### `pbind` repeat

`pbind` takes a second "repeat" argument, this will cause the pattern to repeat
a number of times. Use `##Inf` to create an infinite lazy sequence. Note that
the result of `pbind` will always be an infinite sequence if any of the
sequences in the map are infinite.

```clj
(pbind {:x [1 2]} 3)
;; => ({:x 1} {:x 2} {:x 1} {:x 2} {:x 1} {:x 2})

(take 5 (pbind {:x (repeatedly rand)}))
;; => ({:x 0.05675691736257649}
;;     {:x 0.7919472289743806}
;;     {:x 0.4249618005485135}
;;     {:x 0.30638021751335287}
;;     {:x 0.5366715450956588})
```

### `pbind` example

Let's see what you can do with this in practice. Take the three note pattern we
started with. We can write that much more concisely now.

```clj
(pplay ::x
       (pbind {:instrument gloria
               :octave     4
               :note       [:c :d :e]
               :dur        [1 1/2 1]}))
```

Let's add a rest so we fill up a whole bar, and repeat this a few times.

```clj
(pplay ::x
       (pbind {:instrument gloria
               :octave     4
               :note       [:c :d :e :rest]
               :dur        [1 1/2 1 3/4]}
               4))
```

### Different timelines with `:dur` metadata

The examples so far have been fairly simple, meaning it's been also fairly easy
to keep track how the different `:note` and `:dur` values correlate. But take
for example a case where you have a melody which you want to repeat, but
transposed to different root notes.

```clj
(pplay ::x
       (pbind {:instrument gloria
               :octave     4
               :degree     [1 2 3 :rest]
               :root       [:c :c :c :c :g :g :g :g :d :d :d :d]
               :dur        [1 1/2 1 3/4]}))
```

This gets tedious, we need to keep track of the number of notes there are in a
bar, so that we can repeat the `:root` value that many times. What we really
want to say is that each root should stay the same for an entire bar (4 beats).

You can do this, with `:dur` metadata.

```clj
(pplay ::x
       (pbind {:instrument gloria
               :octave     4
               :degree     [1 2 3 :rest]
               :root       ^{:dur [4 4 4]} [:c :g :d]
               :dur        [1 1/2 1 3/4]}))
```

And the same rules apply, so this can simplify to `^{:dur [4]} [:c :g :d]`, or
even `^{:dur 4} [:c :g :d]`

## Pattern helpers

Currently a handful of additional functions and macros are available to help you
create music patterns. This is just a starting point, there is still a lot of
room for expansion. Contributions that port over SuperCollider Pattern
functions, or that add new, useful functions are very much encouraged.

### `pwhite`

Repeatedly generate random numbers. Takes the `min` and `max` of the sampled
range, and an optional number of `repeats` (defaults to infinite).

Using integers for `min` and `max` will yield integers, using float/double
values will yield doubles.

```clj
(pwhite 0 9 4)
;; => (7 6 2 6)
(pwhite 0.0 9.0 4)
;; => (4.4967438662345405 8.873872953797134 4.636984289426836 4.874344111015306)
(take 5 (pwhite 0 99))
;; => (17 76 82 29 12)
```

### `pdo`

Macro, wraps a block which gets repeatedly called to yield sequence values.

```clj
(def a (atom 0))

(pbind {:degree (pdo @a)})
```

### `pseries`

Generate a number series with a given start, step, and size.

```clj
(pseries 0 2 5)
;; => (0 2 4 6 8)
(take 7 (pseries 0 2))
;; => (0 2 4 6 8 10 12)
```

### `pchoose`

Randomly choose value from a collection with `rand-nth`.

```clj
(pplay ::x
       (pbind {:instrument gloria
               :octave     4
               :degree     (pchoose [:i :v :iv :iii])
               :root       ^{:dur 4} [:c :g :d]
               :dur        [1 1/2 1 3/2]}))
```

### `ppad`

Pad the `pattern` with a rest so the total duration of the pattern is a multiple
  of `beats`.

Useful if you have a phrase that should align with e.g. the length of a bar.

```clj
(ppad (pbind {:degree     [:i :v :iv]
              :dur        [1 1/2 1]})
      4)
;; => ({:degree :i, :dur 1}
;;     {:degree :v, :dur 1/2}
;;     {:degree :iv, :dur 1}
;;     {:type :rest, :dur 3/2})
```
