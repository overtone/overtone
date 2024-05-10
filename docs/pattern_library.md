# Pattern Library

This document assumes you are familiar with [Event System](events.md), in
particular the ins and outs of note events. You might also want to keep the
[Events Reference](events_reference.md) handy.

Overtone's pattern libary consists of functions that help you create
(potentially infinite) sequences of event maps, and to play those events in
order. This allows you to create musical phrases, and to piece those together
into a musical piece.

It is (quite clearly) inspired by SuperCollider's pattern library, but fitted to
match what makes sense in the context of Clojure and Overtone.

## Sequences of Notes

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
and the D being an eigth note.
