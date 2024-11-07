# Unreleased

## Added

- [567](https://github.com/overtone/overtone/pull/567)
  - Add `overtone.sc.sclang` namespace to interact with the `sclang` commands
    and to use `sclang` generated synthdefs
    - Even if the final user doesn't have  `sclang` available on their machine,
      as long as the resource was generated previously, it should work transparently
  - Add `:sclang-path` config to set `sclang` executable location
- Add `at-offset` as an addition to the `at-at` based scheduling API
- Add support for URL and byte arrays in `synth-load`

## Fixed

- [567](https://github.com/overtone/overtone/pull/567)
  - `resources` directory is included in Overtone's jar and deps.edn path (adds `overtone-logo.png` back to class path)

- [#556](https://github.com/overtone/overtone/issues/556)
  - multichannel expanding logic for ugens now correctly handles keyword arguments
  - don't `flatten` single map arguments passed to ugens, use `apply concat` instead

- [#557](https://github.com/overtone/overtone/issues/557): envelope description array generators have line numbers for jump-to-definition purposes

- fix double-eval for `overtone.sc.server/at{-offset}` macros
- setup studio groups only after foundational groups
- ensure later events in dep-state* go after earlier events in `:history`
- Fix `defsynth-load` macro, handle arguments that aren't a string literal
- `overtone.music.pitch/scale` : correctly handle scales that contain more than 8 notes 
- Support setting `:beat` or `:start-time` in note events

## Changed
- `overtone.sc.ugen-collide/binary-div-op` ugen has been renamed `overtone.sc.ugen-collide//`
  - no change under `with-overloaded-ugens`
- `overtone.studio.aux` is now named `aux-bus` instead, can't have files named "AUX" on windows
- Pattern library: make the default behavior to wait for the next sync point (`:align :wait`)

# 0.15.3295 (2024-10-24 / d354b4f)

This release features a significant update of the Pattern Library introduced in
0.14. These changes make it more suitable for live programming, improving the
behavior when patterns which are currently playing are being redefined. The
pattern libary is still considered alpha and is liable to change, feedback is
welcome.

There are a bunch of under the hood changes and quality of life improvements.
There is now a small GUI window that will pop up when doing the OAuth
authentication flow with Freesound, and since we now correctly refresh tokens
hopefully you won't see that very often. A bunch of old bugs and issues have
been addressed, as well as reflection and boxed math warnings, which should help
with performance.

There are two **breaking changes**. The bitwise ugens have been renamed to
`bit-{and,or,xor}`, the old ones never worked correctly, so this is unlikely to
impact users.

The synths used to play samples have been changed to a more basic version, and
currently do not support looping like the old ones did. On the flip side they
can play a sample without an audible click at the end, which has been a decade
old issue. The looping behavior may come back in a future version if we can
reconcile the two.

## Breaking Changes

- `{and,or,xor}` ugens have been renamed `bit-{and,or,xor}`
  - `with-overloaded-ugens` and macros that use it (like `def{inst,synth}`)
    will no longer shadow/bind `{and,or,xor}` but will now shadow `bit-{and,or,xor}`
  - renamed ugens will overload to `clojure.core/bit-{and,or,xor}` for numeric
    arguments and are foldable
- Changed the implementation of `mono-partial-player` and
  `stereo-partial-player` (the default synths used to play samples) to a more
  basic version based on `play-buf` instead of `buf-rd` and `phasor`. The old
  version has a long standing issue that it causes clicks at the end of the sample.

## Added

- [freesound] add a Swing-based dialog box for Freesound auth, fall back to
  reading from stdin (#337)
- [freesound] refresh_token handling, so you don't need to re-authenticate every
  single time
- [doc] Explain how to get `mda-piano` from sc3-plugins
- [doc] Add a tip about scsynth and Homebrew
- [implementation] Warn when imported functions into `overtone.live` conflict
- [studio] The instrument mixers now contains safety precautions: limiter+check-bad-values
- [studio] Allow passing additional arguments to `inst-fx!`
- [studio] Add "aux send" API (`aux-bus`, `aux-ctl`)

## Fixed

- [examples] Fix the getting_started examples: `foo`, `foo-pause`
- [examples] Fix broken freesound link for dirty-kick
- [pattern library] make patterns more easily redefinable while maintaining the
  same relative position, for smoother live updates
- [pattern library] Accept `:-` as a rest note, in addition to `:_` and `:rest`
- [pattern library] Recognize samples in the pattern player, so things like `:amp` work
- Metronome: also accept `:start` and `:bar-start`, instead of just `:bpm`
- Allow the default `at-at` threadpool to be overridden by a dynvar
  (`*current-pool*`)
- `:sc-path` in `~/.overtone/config.clj` can now be a vector instead of a
  string, for passing additional arguments
- [instruments] `grunge-bass` : make the amp parameter do something
- [doc] Change incorrect `pluck-strings` to `pick-string` in docs. (#521)
- [doc] fix `chord` docstring
- [ugen] Allow `free-self` UGen to take audio-rate signals (#515)
- [ugen] Remove ignored mul/add arguments (just use + and *)
- [ugen] fix stk-bee-three arg names
- [AOT] Don't boot overtone while compiling
- [AOT] Add example of how to run from an uberjar
- [implementation] fix toString overrides for overtone.helpers.lib/callable-map
- [implementation] fix reflection and boxed math warnings
- `overtone.sc.ugen-collide` can now be safely required to access colliding
  ugens explicitly.

## Changed

- [at-at] Bumped at-at to 1.4.65, which fixes reflection warnings.
- [insts] sampled-piano now takes an `:amp` arg, as per synth/inst conventions
- [studio] `clear-instruments` now also frees the instruments on the server

# 0.14.3199 (2024-05-19 / 5d1c1ed)

## Added

- First release of the Pattern Libary, see `docs/event.md`,
  `docs/event_reference.md`, and `docs/pattern_libary.md`

# 0.13.3177 (2024-01-05 / ccedb1d)

## Added

- New `loop-buf` UGen, for looping samples (part of sc3-plugins "extras")
- Watch for MIDI device plug/unplug, so that adding a device doesn't require a
  restart. These will also emit events: `:midi-device-connected` /
  `:midi-device-disconnected` / `:midi-receiver-connected` /
  `:midi-receiver-disconnected`
- Add `buffer-alloc-read-channel`, like `buffer-alloc-read` (instruct the SC
  server to load a sound file), but only reads a single channel. Corresponds
  with the `/b_allocReadChannel` OSC message.

## Fixed

- Make sure we print the correct version when booting
- Fix the license information in pom.xml (MIT)
- Handle a 401 response from Freesound by asking for a new token, instead of retrying
- Allow writing buffers that are bigger than MAX-OSC-SAMPLES
- Reuse param `:value` atoms when re-evaluating a `defsynth`/`definst`, so that
  your synth settings aren't lost after a change

## Changed

- Use JNA Jack (via casa.squid.jack) to connect SuperCollider's audio output,
  instead of relying on `jack_lsp` which may not be available, especially on
  PipeWire-based systems
- Reduce HTTP retries when downloading samples from 100 to 20
- print `BufferInfo` as a reader conditional + map, to make it clear it's a data object

# 0.12.3152 (2023-12-26 / 7bad685)

This is the first version without the internal SuperCollider server
(libscsynth). See [this mailing list post](https://groups.google.com/g/overtone/c/qndjDV5FS9Y/m/lPo4QFYpAAAJ)
for the reasoning behind that change. This also means we could drop the bulk of our dependencies,
making Overtone much lighter.

Our work continues to keep Overtone relevant for years to come. We've fixed a
bunch of other long standing issues large and small, modernized the release
tooling, and improved and added many docstrings.

Since Linux users in particular face a rather confusing audio landscape, we've
added a [Linux Audio Primer](https://github.com/overtone/overtone/wiki/Linux-Audio-Primer) to the
wiki, to help you get situated.

## Changed

- Remove embedded (internal) SuperCollider server
- Provide clearer output about what it's doing when starting an external `scsynth`
- Remove `project.clj`, switch to full Clojure CLI based tooling (see `bin/proj`)
- Use `at-at` from Clojars, rather than inlining it here
- Detect PipeWire only systems, and prefix `scynth` with `pw-jack`, if it's available

## Added

- Add Karl Thorssens sampled trumpet instrument (`overtone.inst.sampled-trumpet`)
- Added `set-fret` and `slide-string` to `overtone.synth.stringed` (#287)
- Added `freesound-sample-pack`, for downloading a whole pack at once
- Add an example file for the stringed synths (#287)
- Add an alias `lin-env` for `lin`, for backwards compatibility
- On the generated docstring for ugens that collide with Clojure built-ins, mention that you can add a final `:force-ugen` argument as a hint to treat it as a ugen (#505)
- Store Freesound token in between sessions (#506)

## Fixed

- Fix an issue where Clojure fails to resolve the right `Thread/sleep` implementation on newer JVMs (#502)
- Fix calling synths/instruments with 21 arguments or more (#504)
- Fix the namespace `overtone.inst.synth` on Clojure 1.11 (#505)
- Mark `abs` as a Clojure numerical function, to make sure it is treated as a UGen when its arguments are not numerical (#505)
- Make `synth`/`defsynth` and `inst`/`definst` take the same form of params (fixes regression, and makes `synth`/`inst` more useful)
- Ignore errors in `jack_lsp`. Wayland based systems often don't have this command, in which case people can connect SuperCollider to their audio device manually, we should not fail for that.
- Handle a 429 "too many requests" from Freesound more gracefully

# 0.11.0 (2023-11-02 / 2907605ba)

The first release in a number of years, and the first step in reviving Overtone,
and keeping it relevant for years to come.

* Fix `overtone.music.pitch/dec-last` (#437)
* Return notes in ascending order in `overtone.music.pitch/chord`
* Fix printing of huge map when calling instruments with Cider (#432)
* Fix size checks to multichannel buffer writes (#338)
* Add clj-kondo support (#493)
* Fix broken version comparison in args/SC-ARG-INFO (#449)
* OSC: use #getHostScript to fallback on hostname string (#450)
* Upgrade dependencies (#456)
* Add support for the `grain-buf` ugen (#470)
* use canonical URL for freesound API (#479)
* Fix window paths to allow downloading samples (#487)
* Removed obsolete JVM option CMSConcurrentMTEnabled (#488)
* Read synthdef files correctly (#489)
* Fix buffer reading (#490)
* Add clj-kondo support (see `overtone.linter`) (#493)
* Qualify the overtone ns in lein example (#495)

With thanks to contributors: Andréas Kündig, dvc, Hlöðver Sigurðsson, Lee Araneta, Markku Rontu, Matt Kelly, Nada Amin, Paulo Rafael Feodrippe, Perry Fraser, Phillip Mates, Wesley Merkel

# Version 0.10.6 (19th May 2019)
* major bug fix: make sure that deps.edn is loaded from the classpath if it's not found locally (version 0.10.5 will crash when used with leiningen)

# Version 0.10.5 (18th May 2019)
* overtone can now be used with tools.deps
* fix classException for note and chord function #428
* failures in the test runner fixed
* alert linux users in case jack server wasn't started prior to external-server connection runner

# Version 0.10.4 (8th May 2019)

## ugens
* `index` now available on :ir rate, but keeps defaulting to :kr.

## scsynth
* now compiled against supercollider 3.9.3
* scsynth-extras includes new plugins that can to be spec'd in metadata/extras
* jna paths are explicitly set for every os
* 64-bit architecture support for windows machines
* native resources will be copied from target to project's root-dir, if present in target and missing in root-dir.

## Breaking Changes
* 32-bit support for internal-synths on windows is discontinued (use 64-bits instead)
* Linux: Jack client name now defaults to `Overtone` instead of `SuperCollider` #409
* `load-samples` now accepts many directory and/or file paths, this breaks the functionality of the previous varag sequence.
* freesound.org samples are now saved with filename and extension, preserving safe-filenames for tmp storage (this causes all freesound.org samples to be redownloaded if they are cached from older overtone version)

## Improvements
* `add` parameter added to `var-saw`
* `overtone.music.pitch/rand-chord` now vararg with `inversions` parameter
* `sampled-flute` and `sampled-piano` now load faster from cache

## New Fns
* `overtone.sc.sample/load-samples-async` works like `load-samples` a faster but unsafer alternative to `load-samples`

# Version 0.10.3 (11th October 2017)

## Bug Fix
* `overtone.sc.vbap` any? now called `some-element?` and doesn't rely on Clojure 1.9

# Version 0.10.2 (30th August 2017)

## Breaking Changes

* `inst?` has been renamed to `instrument?`

## New Fns
* `overtone.algo.euclidean-rhythm` Generates euclidean rhythms using bjorklund's algorithm.

## New example
* `overtone.examples.midi.keyboard` `sustain-ding` Creates an midi instrument with a sustain parameter.

## Improvements
* `overtone` is now running on clojure-1.9-alpha
* `freesound` gives better error message when file/sample is not found.
* `overtone.sc.vbap` uses clojure 1.9's `clojure.core.any?`, removes replace symbol warnings.

## Bug Fixes
* Prevent double trigger of freesound samples by setting #318
* Replace `use` with `:use` for Clojure 1.9 compatability.
* Fix error when Supercollider version is in development
* Fix warning for deprecated CMSIncrementalMode
* `connect-to-external-server` logs correctly configured port number

# Version 0.10.1 (1st April 2016)

## Breaking Changes

* `control-bus-set-range!` arguments have been updated (to match OSC
  API).  start and len args have been removed and offest params have
  been added.
* Freesound API has been updated to v2 and now requires a key :-(

## New Synths

* `mono-play-buffer-partial`
* `stereo-play-buffer-partial`
* Sample flute with vibrato

## New ugens/cgens

* `dyn-klang`
* `dyn-klank`
* `dfm1`
* `distortion2`
* `varlag`
* `del-tap-wr`
* `del-tap-rd`
* `vbap`
* `grain-fm`


## New Fns

* `sputter` - probabilistic repetition of a list's elements
* `buffer-mix-to-mono` - create a new mono buffer by mixing a stereo buffer
* `server-radians-per-sample`
* `server-control-dur`
* `server-control-rate`
* `server-sample-dur`
* `control-bus-get-channel` - get the value of an individual channel of a control bus.
* `control-bus-fill!` - fill a sequence of control bus values with a specific value.
* `env-adsr-ng` non-gated ADSR envelope


## New clock
Add new internal server clock with control-rate resolution. Introduces the folloing functions:

* `server-clock-n-ticks`
* `server-clock-uptime`
* `server-clock-time`
* `server-clock-drift`

And also a new group: `foundation-timing-group` which is at the head of
all groups. There's also a new two-channel global clock-bus:
`server-clock-b`.

## Improvements

* Make metronome safe to use across multiple threads
* `*add-current-namespace-to-synth-name*`- new dynamic var for switching off auto namespacing of a synthdef name
* `control-bus-get` may now return a sequence of vals when passed a multi-channel control bus.
* `.aif` is now a synonym for `.aiff` in list of supported audio files
* Reduce chance of unexpected control bus clashes by auto-reserving bus
  0 (and therefore ensuring that a default bus of 0 has no bad side
  effects.)
* Added new default value: `SC-MAX-FLOAT-VAL` - representing the maximum
  whole number represented with scsynth's floats due to precision
  constraints (2**24)
* Only auto-allocate the required number of audio busses based on sound card properties.
* Add support for par-groups (for supernova)

## Bug Fixes

* Graphviz - draw `:ir` rate control ugens with dashes
* `node-tree-seq` now works correctly with no args
* `defunk` now handles `nil` correctly in args


# Version 0.9.1 (25th November 2013)

Version bump forced by Clojars missing a commit. Nothing new here.

# Version 0.9.0 (25th November 2013)

## New Committers

* Mike Anderson
* Karsten Schmidt
* Joseph Wilk
* Rich Hickey
* Kevin Irrwitzer
* James Petry

## Major Additions & Changes

### apply-*

`apply-at` has been renamed to `apply-by` which more
 correctly represents its semantics as it applies the function *before*
 the specified time. `apply-at` still exists, except it now applies the
 fn *at* the specified time. To update, simply grep for all occurences of
 `apply-at` and replace with `apply-by`.

### Synth Positioning

When triggering synths it was possible to specify a position for the
synth node to be executed in the node tree. This is important for
ensuring that synth chains are correctly ordered such that any post-fx
synths are executed after the source synth they are modifying. Prior to
0.9.0 this was possibly by prefixing the synth args with 'special'
values:

     (def my-g (group))
     (my-synth :tgt my-g :freq 440)

Overtone figured out that the `:tgt my-g` key-val pair were special, and
used them to target the synth node, and not pass them as params to the
synth along with the `:freq` param. This was slightly magical and also
potentially clashed with a synth designer's ability to use the special
keywords as valid synth param names.

This syntax has now been deprecated and replaced with a more explicit
vector-based syntax. If you wish to target your synth, you need to pass
a vector as the first parameter. The vector should be a pair of:

    [:target-specifier target]

Valid target specifiers are:

* `:head` - places new synth node at the head of the target (group)
* `:tail` - places new synth node at the tail of the target (group)
* `:before` - places new synth node immediately before target (group/synth)
* `:after` - places new synth node immediatedly after target (group/synth)
* `:replace` - replaces target with new synth node

Therefore, to place a new instance of `my-synth` at the head of `my-g`
you can issue the following:

    (my-synth [:head my-g] :freq 440)

Currently, you'll get an exception if you use the old style syntax. This
means that the old keywords are still unavailable to synth designs. This
will be relaxed in a future version.

### MIDI

The MIDI API has been substantially revamped. This is in the Apple
tradition of actually reducing functionality with the aim of making the
surviving functionality easier to use. Essentially the underlying MIDI
library provided by the dependency `overtone/midi-clj` is no longer
available in the global API which is pulled in automatically to the
`overtone.live` and `overtone.core` namespaces. Of course, you're still
free to pull in the `overtone.midi` namespace, which is still on the
classpath should you need access to the old functions. However, if you
find yourself doing this - please let me know. The aim is for this not
to be necessary.

MIDI devices are now automatically detected on boot and auto-hooked up
to the event system. You have access to the list of detected devices
(and receivers) via the functions: `midi-connected-devices` and
`midi-connected-receivers`. Take a look at the example file
`examples/midi/basic.clj` for more a quick tour of the MIDI API.


### Graphviz

If you're working on a sophisticated synth design, or just simply want
to have another perspective of a given synth's design, it's often useful
to be able to look at a visual representation. This is now possible with
the new Graphviz support. You can generate dot notation for an arbitrary
synth design with the function `graphviz`.

For example, given the synth:

    (defsynth foo []
      (out 0 (sin-osc 440)))

You can produce corresponding dot notation with:

    (graphviz foo)

Which will return the following string:

    digraph synthdef {
    1 [label = "{{ <bus> bus 0.0|{{<signals___sin____osc___0>}|signals}} |<__UG_NAME__>out }" style="filled, bold, rounded"  shape=record rankdir=LR];
    0 [label = "{{ <freq> freq 440.0|<phase> phase 0.0} |<__UG_NAME__>sin-osc }" style="filled, bold, rounded"  shape=record rankdir=LR];

    0:__UG_NAME__ -> 1:signals___sin____osc___0 ;

    }

You can then easily `spit` this out to a file and feed it into graphviz
to render an image/pdf etc manually. However, we also provide the
function `show-graphviz-synth` which will automatically call `dot` to
generate a pdf and then display it for you:

    (show-graphviz-synth foo) ;;=> PDF pops up!

This has been exhaustively tested on OS X, so any pull requests for
minor niggles on Linux/Windows are happily
considered. `show-graphviz-synth` is currently pretty much guaranteed
not to work on Windows, but it would be awesome if it did.

### Bus monitoring

One aspect of Overtone which is seeing active development is means with
which to monitor the internal values within running synths. Overtone
0.9.0 now ships with a bus monitoring system which works with both audio
and control busses.

Calling `bus-monitor` with a bus will return an atom containing the
current value of the bus. Note that this isn't the peak amplitude,
rather the direct value of the control bus. For multi-channel buses, an
offset may be specified. Current amplitude is updated within the
returned atom every 50 ms.

### Persistent store

Overtone now supports a simple persistent key value store which is
essentially a Clojure map serialised as EDN in file with the path
`~/.overtone/user-store.clj`. Adding to the store is a matter of
`store-set!` and getting values from it is simply `store-get`. The store
is meant merely as a simple convenience mechanism for sharing data
between Overtone projects.

### Stopping and Clearing Default Group

Overtone has long provided `stop` which kills all synths in the default
group. However, it doesn't clear out all the subgroups which is
sometimes wanted. This is now available with `clear`. Overtone also
provides an initial group structure with 'safe' groups both before and
after the default group. These are intended for longer-running synths
either feeding control signals to ephemeral synths or adding FX to
them. These 'safe' groups can now be stopped with `stop-all` and also
all the subgroups can be cleared out with `clear-all`. For more
information on the default group structure see the `foundation-*` fns.

### Node events

It's now possible to register oneshot handler function for when specific
nodes are created, destroyed, paused or started with the new `on-node-*`
family of functions. For example to have a function execute every time a
node is started:

    (defsynth foo [] (out 0 (sin-osc)))

    (def f (foo))

    (on-node-started f (fn [m] (println "Node"  (-> m :node :id) "started!")))
    (node-pause f)
    (node-start f) ;;=> "Node 31 started!"
    (node-pause f)
    (node-start f) ;;=> "Node 31 started!"

### Synth Triggers

It is possible to send information out of a specific synth and into
Overtone as an event via the `send-trig` ugen. This is now a little bit
easier with the new trigger handler functions. Firstly, there's
`trig-id` which will return you a unique id for use as a trigger id. You
can then feed that to your synth and also use it to register handler
functions to execute when data from that specific synth is received:

    ;; create new id
    (def uid (trig-id))

    ;; define a synth which uses send-trig
    (defsynth foo [t-id 0] (send-trig (impulse 10) t-id (sin-osc)))

    ;; register a handler fn
    (on-trigger uid
                (fn [val] (println "trig val:" val))
                ::debug)

    ;; create a new instance of synth foo with trigger id as a param
    (foo uid)

### Envelopes

Using envelopes effectively has long been a dark Overtone art. They have
a huge potential for powerful manipulation of synth internals to finely
control both pitch and timbre though time. The simplest approach to
using envelopes is using the `env-gen` helper fns such as `perc` and
`adsr`. These functions have supported keyword argument semantics
similar to the ugen functions given that they're used in the same
context. However, until this release, they haven't reported their param
list in the function argument list metadata. This is now fixed in this
release. In addition, to help ease discovery of these helper fns, they
are also now prefixed with `env-` so those using editors with
autocomplete can find them more easily. The helper functions provided in
0.9.0 are: `env-triangle`, `env-sine`, `env-perc`, `env-lin`,
`env-cutoff`, `env-dadsr`, `env-adsr` and `env-asr`.

The `envelope` function (which all the helper functions are written in
terms of) has also been improved. It is now possible to pass a
heterogeneous list of keywords and floats for the `curves`
parameter. This means that it's now possible to request different
keywords for different envelope segments. Take a look at the `envelope`
docstring for extensive information.

### Resonate Workshop

Karsten 'Toxi' Schmidt has kindly donated his resonate workshop
files to the examples folder. These can be found within
`examples/workshops/resonate2013`. Karsten is renowned for giving
awesome workshops, so it's wonderful to be able to ship with this
material for everyone to play.

### Docstrings

Although it can be fairly argued that Overtone is still missing end-user
documentation (something we're currently working hard at fixing) we have
always had excellent internal documentation and this release continues
with this tradition. All of our end-user functions have full docstrings
and many of them have been improved and tweaked to make them more
readable and understandable.

## New fns

* `midi-find-connected-devices` - list all auto-connected MIDI devices
* `midi-find-connected-device` - list all auto-connected MIDI devices
* `midi-find-connected-receivers` - list all auto-connected MIDI receivers
* `midi-find-connected-receiver` - list all auto-connected MIDI receivers
* `midi-device-num` - get the unique device num (for a specific MIDI make/model)
* `midi-full-device-key` - get the full device key used for the event system
* `cycle-fn` - create a composite fn which cycles through a list of fns on application
* `rotate` - treat a list as a circular buffer and offset into it
* `fill` -  fill a list with the values of another (cycling if necessary)
* `trig-id` - return a unique id for use with trigger ugen
* `on-trigger` - add handler for when a specific synth trigger event is received
* `on-latest-trigger` - similar to on-latest-event but for triggers
* `on-sync-trigger` - similar to on-sync-event but for triggers
* `clear` - stop all running synths in default group and remove all subgroups
* `stop-all` - stop all running synths including within safe groups. Does not remove subgroups.
* `clear-all` - stop all rurnning synths including within safe groups and remove all subgroups.
* `node-destroyed-event-key` - returns the key used for node destroyed events
* `node-created-event-key` - returns the key used for node created events
* `node-paused-event-key` - returns the key used for node paused events
* `node-started-event-key` - returns the key used for node-started events
* `on-node-destroyed` - create a oneshot handler triggered when node is destroyed
* `on-node-created` - create a oneshot handler trigered when node is created
* `on-node-paused` - create a oneshot handler triggered when node is paused
* `on-node-started` - create a oneshot handler triggered when node is started
* `graphviz` - create a valid dot file representation of a synth design
* `show-graphviz-synth`- show a PDF of the visual representation of a synth design
* `freesound` - create a playable sample from a freesound id
* `audio-bus-monitor` - returns an atom which is auto updated with the current bus value
* `control-bus-monitor`- returns an atom which is auto updated with the current bus value
* `bus-monitor` - returns an atom which is auto updated with the current bus value
* `synth-args` - returns a seq of the synth's args as keywords
* `synth-arg-index` - returns an integer index of synth's arg
* `store-get` - get value with key from persistent user store
* `store-set!` - set value with key within user's persistent store
* `store` - get full persistent store map
* `env-triangle` - duplicate of `triangle`
* `env-sine` - duplicate of `sine`
* `env-perc` - duplicate of `perc`
* `env-lin` - duplicate of `lin`
* `env-cutoff` - duplicate of `cutoff`
* `env-dadsr` - duplicate of `dadsr`
* `env-adsr` - duplicate of `adsr`
* `env-asr` - duplicate of `asr`

## Renamed fns

* `node-get-control` -> `node-get-controls`
* `bus-set!` -> `control-bus-set!`
* `bus-get` -> `control-bus-get`
* `bus-set-range` -> `control-bus-set-range`
* `bus-get-range` -> `control-bus-get-range`
* `remove-handler`  -> `remove-event-handler`
* `remove-all-handlers` -> `remove-all-event-handlers`
* `lin-env` -> `lin`
* `connected-midi-devices` -> `midi-connected-devices`
* `connected-midi-receivers` -> `midi-connected-receivers`
* `apply-at` -> `apply-by`

## Deprecated fns

* `midi-devices`
* `midi-device?`
* `midi-ports`
* `midi-sources`
* `midi-sinks`
* `midi-find-device`
* `midi-in`
* `midi-out`
* `midi-route`
* `midi-shortmessage-status`
* `midi-sysexmessage-status`
* `midi-shortmessage-command`
* `midi-shortmessage-keys`
* `midi-msg`
* `midi-handle-events`
* `midi-send-msg`
* `hex-char-values`
* `midi-mk-byte-array`
* `midi-play`

## New synths

* `overtone.synth.sts/prophet`
* `overtone.synth.retro/tb-303`

## Renamed synths

* `bitcrusher` -> `fx-bitcrusher`

## User visible improvements

* Report `:num-control-busses` in `server-info`
* Rename `apply-at` to `apply-by` and implement `apply-at` to apply the fn at the specified time, not before it.
* Remove limit and ordering restrictions on `scale-range`
* Teach `fm` synth about `out-bus` param
* Teach `sampled-piano` about `amp` and `rate` params
* Modify `AudioBus` and `ControlBus` print formatter to print a default name and to label the attributes more clearly.
* `RecurringJob` and `ScheduledJobs` are now killable
* Add `pan` param to the sample players
* *Breaking change* - `node-get-control` now only accepts one arg and returns a value. Use new `node-get-controls` for old behaviour.
* Teach `sync-event` to take a single map as an argument - similar to event
* Add new store-fns `store-get` and `store-set!` for storing user values separate from the config within `~/.overtone/user-store.clj`
* Warn users when JVM argument `tieredStopAtLevel` is set to 1
* Remove support for using mmj MIDI library on OS X
* Make more things killable - Integers, Floats, Synths, regexs
* idify synth args

## Internal improvemnts

* automatically create `MidiOutReceiver` objects for all detected midi out receivers to enable comms.
* Reduce `MAX-OSC-SAMPLES` to work within the constraints of UDP packets
* handle exceptions generated in `on-latest-event` hander-fns
* Add `reset-returning-prev!`
* Don't ensure-connected in kill fn
* Extend `Group` to support `ISynthNode` (given that groups are actually nodes).
* Emit events when nodes are destroyed, created, paused and started.
* Unify ugen name in debug namespace to Overtone style name - and also replace Binary/UnaryOpUGens with appropriate names: `*`, `+`, `/` etc.
* Add some explicit type checks for synthdef manipulation fns.
* Remove reliance on presence of `:spec` key in sdefs for unification process. This allows sdefs read from binary scsynthdef files to also be correctly unified.
* Update `at-at` dependency to 1.2.0
* Teach `fs` to take multiple txt strings (which will subsequently be separated with a space)
* Teach `find-note-name` to handle nil arg - let nil flow through
* Add Coyote onset detector ugen to exceptions which can take ar ugens
* Catch `UnsatisfiedLinkError` when attempting to load native libs and print out error.

## Bugfixes

* Calling either `stop-player` or `kill-player` on the return obj from one of the scheduling fns such as `periodic` or `after-delay` now has correct behaviour.
* Fix `group-free` to actually delete a group
* Fix `pitch-shift` arg name from window-cize to window-size
* Fix control-bus asynchronous message multiplexing issue.
* Fix `indented-str-block` to correctly count length of lines
* Switch to much simpler (also non-explosive) implementation of `topological-sort-ugens`
* Reset `*print-length*` dynamic var to ensure all data is printed out
* Ensure `bur-rd` and `buf-wr` phase arg is at audio rate when ugen is also at audio rate.
* Add ugen checks for `balance2`
* Fixed `vintage-bass` inst to be audible

# Version 0.8.1 (28th January 2013)

## Bugfixes

* Fix bug in free-bus which was still assuming audio and control busses were differentiated by keywords rather than records. Added new protocol IBus to handle the polymorphism for this fn.

# Version 0.8.0 (26th January 2013)

## New Committers

(Some of these committers may have made contributions to previous versions, but this is the first time they're mentioned in this change log).

* Nada Amin
* George Jahad
* Colleen Twitty
* Mikko Harju
* Paul Sanwald
* Roger Allen
* Mikkel Gravgaard
* J. Graeme Lingard
* Chris Ford
* Mat Schaffer
* Joel Jorgensen

## Major Features

* New, all Clojure, in-memory scsynth interface using clj-native
* New (optionally disabled) machinery to stop the control and modification of non-live nodes (controlling loading nodes blocks the current thread and controlling destroyed nodes throws an exception).
* New event handler `on-latest-event`  which serially handles incoming events with the lowest latency by dropping events it hasn't had time to handle, yet always handling the last event seen.
* Complete overhaul of the default group structure. See `foundation-*` fns below.
* Many new synths
* Many, many new ugens: major progress has been made porting the metadata for the extra ugens not included by default in SuperCollider. See `overtone/sc/machinery/ugen/metadata/extras/README.md` for progress.
* Clojure 1.5 compatibility


## New fns
* `on-latest-event` - Handles events with minimum latency - drops events it can't handle in time
* `event-monitor-on` - prints out all events to stdout (can be very noisy!)
* `event-monitor-off` - turns off event monitoring
* `event-monitor-timer` - records incoming events for a specified period of time
* `event-monitor` - returns map of most recently recorded events
* `event-monitor-keys` - returns seq of all keys of recently seen events
* `midi-capture-next-controller-key` Returns the event key for the next modified controller
* `buffer-write-relay` - similar to buffer-write! but doesn't require native synth. Can be very slow for large amounts of data.
* `chord-degree` - Returns the notes constructed by picking thirds in the given note of a given scale
* `foundation-overtone-group` - returns the group for the whole of the Overtone foundational infrastructure
* `foundation-output-group` - returns the group for output synths
* `foundation-monitor-group` - returns the group for the monitor synths (executed last)
* `foundation-input-group` - returns the group for the input synths (executed first)
* `foundation-root-group` - returns the main group for synth activity
* `foundation-user-group` - returns the container group for user activity
* `foundation-default-group` - returns the default user group - use this group for your synths
* `foundation-safe-group` - returns the safe group - synths in here will not stop when #'stop is called
* `foundation-safe-post-default-group` - returns the safe group positioned before the default group - synths in here will not stop when #'stop is called
* `foundation-safe-pre-default-group` - returns the safe group positioned after the default group - synths in here will not stop when #'stop is called
* `node?` - returns true if obj is a synth node or group
* `node-live?` - returns true if node is live
* `node-loading?` - returns true if node is loading (i.e. the server hasn't responded to say that it's loaded)
* `node-active?` - returns true if node is either loading or live
* `inactive-node-modification-error` - Returns a keyword representing the current node-modification errors trategy
* `inactive-buffer-modification-error` - Returns a keyword representing the current buffer-modification error strategy
* `block-node-until-ready?` - Returns true if the current message strategy is to block the current thread until the node you're attempting to communicate with is ready (i.e. live).
* `pp-node-tree` - pretty-print the node-tree to *out*
* `interspaced` - calls a fn repeatedly with an interspacing of ms-period. i.e. the next call of the fn will happen ms-period ms after the completion of the previous call.

## New macros

* `with-no-ugen-checks` - Disables ugen checks in containing form instead printing warning messages instead of raising exceptions. This is useful for the cases when the ugen checks are over zealous.
* `with-ugen-debugging` - Prints debugging information for the ugens within the containing form.
* `without-node-blocking` - Disables the blocking behaviour when attempting to communicate with a node that's not yet live.
* `with-inactive-node-modification-error` - Sets the error strategy for inactive node modification. Options are :exception, :warning and :silent
* `with-inactive-buffer-modification-error` - Sets the error strategy for inactive buffer modification. Options are :exception, :warning and :silent
* `with-inactive-modification-error` - Sets the error strategy for both inactive node and buffer modification. Options are :exception, :warning and :silent

## Removed fns
* `on-trigger` - prefer event system
* `remove-trigger` - prefer event system
* `remove-all-handlers` - calling this removed Overtone's default handlers rendering the system useless.

## Renamed fns

* `stop-midi-player` -> `midi-player-stop` - It can now handle keys

## New Insts

* `supersaw`
* `dance-kick`
* `quick-kick`
* `haziti-clap`
* `daf-bass`
* `cs80lead`
* `simple-flute`

## New Synths

New timing synths
* `trigger`
* `counter`
* `divider`

Started work porting synths from Ixi Lang (`overtone/synth/ixi`):

* `impulser`
* `kick`
* `kick2`
* `kick3`
* `snare`

* `sampled-piano`- now we also have a synth version of the sampled piano with support for `:out-bus` arg.

## cgens

* `sum` - Adds all inputs together
* `mix` - Now divides the inputs signals by the number of number of inputs
* `add-cents` - Returns a frequency which is the result of adding n-cents to the src frequency.
* `mul-add` now auto-determins the correct rate.
* `range-lin` - maps ugens with default range of -1 to 1 to specified range
* `poll` - now implemented via `send-reply` to print out via Overtone stdout and remove flushing latency.

## User Visible Improvements

* Further work on SuperCollider book translation (`/docs/sc-book`)
* `out-bus` argument now added to a number of synths. This should be considered standard practice.
* Overtone icon displayed on OS X systems
* `tb303` inst now accepts `:amp` param
* Synth control proxies (args) can now accept a [default rate] vector i.e. [0 :kr]
* Allow following ugens to be foldable (following Clojure's semantics): `#{"+" "-" "*" "/" "<" ">" "<=" ">=" "min" "max" "and" "or"}`
* Teach certain binary ugens to behave appropriately if passed just one argument (unity).
* Default scsynth memory size is now 256 (up from 8mb!)
* `env-gen` now defaults to control rate.
* Event stream now also gets a generic MIDI event with the value as a payload rather than as part of the key
* Metronome now stores current bar
* Add chord `:m7+9`
* Buffer modifying fns now typically return the modified buffer
* MIDI data map now includes keys `:data2-f` and `:velocity2-f` storing floats between 0 and 1
* Add more scales
* Ugen arity and keys are now checked with sensible error messages.
* Allow samples to be forcefully reloaded - avoiding cache with via supplying the `:force` arg to `load-sample`
* Midi poly player now sends both velocity (0-127) and amp (0-1)
* Midi poly player can now be associated with specific MIDI devices
* Midi poly player can now be created with a specific key allowing it to be removed independently
* Teach group fn to ensure that the target node is active.
* teach Overtone OSC peer to throw exceptions when nested OSC bundles are attempted to be sent to SuperCollider (despite being an explicit part of the OSC spec, SuperCollider doesn't support nested OSC bundles).
* Rename buffer record slot allocated-on-server to status containing either :live or :destroyed similar to node records.
* Add print-method writers for buffers and samples
* Update scope to support control-rate buses and to free resources created on closing the scope
* Teach buffers to store descriptive names
* Ensure buffer is active before allowing modifications - may be disabled
* Add deref! description messages to improve the error reporting by providing some context when a deref! takes too long.
* Improved MIDI sysex support: can now receive sysex messages, which are placed onto the event stream
* Ability to handle multiple identical connected MIDI devices
* Support for the mmj lib (on OS X) via the config key `:use-mmj`
* Add ability to handle bus, buffer (and other) arguments in synth creation and control messages without requiring explicit :id extraction
* New `~/.overtone/config.clj` example documenting all config options. Found in `docs/config.clj`

## Internal Improvements

* SCUGen now stores the ugen spec
* SynthNodes now store the original synth design and arguments
* SynthNodes and SynthGroups now print themselves succintly
* Mixers are now part of studio
* Many additional ugen checks
* `group-node-tree` now knows how to handle a group as a param
* `extract-target-pos-args` extraced for `:tgt` and `:pos` munging.
* Allow for different JVM args per OS
* Extend Integers to handle `ISynthNode` and `IControllableNode` protocols
* Fixed race condition in node creation where `/s_new` could occasionally trigger the handler fn before the ide id is added to `active-synth-nodes*`
* Synth names are now namespaced - allowing multiple synths to be defined with the same name but in different namespaces. Abbreviate safely to a 31 character limit.
* Teach `synthdef-write` to resolve tilde paths
* Internal event `:osc-msg-received` is now `[:overtone :osc-msg-received]`
* Freeing node ids is delayed by 1 second to reduce the chance of a race condition occurring in the case where the id has been recycled and used before the node status has been set to :destroyed.
* Add dynamic var `overtone.sc.node/*inactive-node-modification-error*` which may be bound to `:silent`, `:warning` or `:exception` to control the behaviour of the error created when an inactive node is either controlled or killed.  (Default is `:exception`).
* Teach insts how about node-status and node-block-until-ready. For insts, this is really composite behaviour depending on internal groups and synths.
* Move to using non-id-recycling keywords for node and buffer ids. (Thanks to Kevin Neaton for discovering that this was possible). IDs *will* run out, but only after a solid 24 hours of hardcore jamming.
* add new `os-name` and `os-description` helper fns
* update `osc-clj` dependency (which now supports nested OSC bundles in the macro `in-osc-bundle`) and move to using the new non-nested osc bundle macro `in-unested-osc-bundle` to explicitly not create OSC bundles for SC comms. However, the nested bundle functionality may be useful for communicating with other OSC servers which support this behaviour (which is in the OSC spec).

## New Examples

* Examples are now located in `overtone/examples`
* Get on the bus - introduction to busses
* Internal sequencer
* Getting started video transcript
* Jazz experiment
* Internal metro
* Clapping Music
* Bass and drum funk
* Add fun new schroeder-reverb-mic example

## Bugfixes

* Many, many many! See git history for full list.

# Version 0.7.1 (27th June 2012)

## Improvements

* Improve booting of external server on Windows.
* Working dir is now set on Windows machines for `scsynth`
* `scsynth` path is now discovered on Windows rather than hardcoded
* Users may set :sc-path in their config to point to their scsynth
  executable if it's not to be found in the default locations.

# Version 0.7 (26th June 2012)

## New Committers
* Damion Junk
* Jacob Lee
* Fabian Steeg
* Michael Bernstein
* Ian Davies

## New fns
* `overtone.sc.buffer/buffer-alloc-read` - read a audio file from path into a buffer
* `overtone.sc.mixer/recording?` - returns true if Overtone is currently recording audio
* `overtone.sc.buffer/buffer-info?` - determins whether the arg is buffer information
* `overtone.sc.sample/free-sample` - free buffer associated with a loaded sample
* `overtone.sc.sample/free-all-loaded-samples` - frees buffers associated with all loaded samples
* `overtone.sc.buffer/file-buffer?` - returns true if arg is a file buffer
* `overtone.music.pitch/find-scale-name` - discover the name of a scale
* `overtone.music.pitch/find-note-name` - discover the name of a note
* `overtone.music.pitch/find-chord` - discover the name of a chord
* `overtone.music.pitch/note-info` - return an info map representing a note
* `overtone.music.pitch/mk-midi-string` - returns a validated midi note string
* `overtone.lib.at-at/show-schedule` - displays a list of currently scheduled jobs
* `overtone.sc.node/node-get-control` - get a set of named synth control values
* `overtone.sc.node/node-get-control-range` - getn synth control values starting at a given control name or index
* `overtone.libs.freesound/freesound-search` search freesound.org
* `overtone.libs.freesound/freesound-searchm` search freesound.org and expand results at macro expansion time
* `overtone.libs.freesound/freesound-search-paths` search and download from freesound.org
* `overtone.sc.node/node-tree-zipper` - return a zipper over the node-tree
* `overtone.sc.node/node-tree-seq` - return a seq of node-tree nodes
* `overtone.sc.node/node-tree-matching-synth-ids` - return a seq of node-tree nodes matching a string or regexp
* `overtone.studio.inst/inst-volume` - control the volume of a specific inst
* `overtone.studio.inst/inst-pan` - control the pan of a specific inst

## Renamed fns
* `buffer-cue-close` -> `buffer-stream-close`
* `find-note-name` -> `find-pitch-class-name`

## New cgens
* `tap`- Listen in to values flowing through scsynth and have them periodically update atoms associated with a synth instance for easy reading from Clojure.
* `scaled-play-buf` - similar to `play-buf` but auto-scales rate
* `scaled-v-disk` - similar to `v-disk-in` but auto-scales rate
* `hold` - hold input source for set period of time, then stop safely
* `local-buf` - now supports SCLang's argument ordering

## Improvements
* defsynths now no longer need only one root - therefore they now support side-effecting ugen trees.
* Varied welcome messages
* definsts and friends now accept a single arg map
* Instruments are now stereo and panned with controls for each
* You can supply a map of options for the scsynth binary as a `:sc-args` key in the config
* Store and read default log level from config as `:log-level`
* `config-get` now supports a default value
* `ctl` can now handle a sequence of nodes to control as its first argument.
* Log event debug messages on a separate thread to ensure logging doesn't interfere with event handling latency
* Using `overtone.live` now consults the config to determine whether t* boot an internal or external synth
* All midi events are now broadcasted on the event stream
* Connected midi devices are automatically detected on boot
* Add support for local buffers in synths through local-buf
* Log output is now Clojure data
* Update logging to log to `~/.overtone.log` with two separate rolling files of max 5mb each.
* Immigrate fns as vars, not vals - allows modifications to core fns to propogate immediately. Helpful for hacking on Overtone :-)
* Add received OSC messages to debug output
* Soften release of `demo` - no more clicks or pops!
* Allow cgens to perform multichannel-expansion - similar to ugens
* Increase amount of info stored in buffer-info and sample-info records
* Improve interaction with external servers
* `buffer-cue` now auto-allocates a buffer
* Callable-maps now preserve metadata on `assoc` and `cons`
* `defsynth` now supports cgen-like argument maps
* New helper ns for internal helper fns
* Safety system now uses a standard limiter
* Groups may now be created with no params - defaulting to the tail of the root-group
* Store names with groups and make them visible in node-tree
* Add buffer size checking fns with sensible error messages.
* Improve ugen error checking
* Add additional SuperCollider paths for 3.5.1 version of SC

## New protocols
* `IMetronome`
* `ISynthNode`
* `ISynthGroup`
* `ISynthBuffer`
* `ISaveable`
* `IControllableNode`
* `IKillable`

## New Examples
* `examples/piano_phase.clj`
* `examples/row_row_row_your_boat.clj`

## Bugfixes
* Sampled-piano link now points to a hopefully more persistent freesound version of samples
* Allow creation of a buffer within the body of an at macro
* Fix race condition by updating active-synth-nodes* before sending OSC message.
* Fix divide-by-zero error in `buffer-info`
* Only allow a-zA-Z0-9 chars in asset filenames. Fixes issue with bad filenames on Windows.
* Fix startup race condition by thread-syncing retreival of in and out bus counts
* Fix arg positioning in `buffer-cue`
* Simplify safety system and fx using replace-out ugen correctly
* Allocator now allows for the correct allocation of multi-channel busses.
* `buffer-write` now handles start-idx argument correctly
* Fix `weighted-choose`
* Remove metronome glitches when changing bpm
* Fix blues example
* Fix issue caused by node not returning the synthdef
* Fix OutputProxy printing errors by adding name field to record

# Version 0.6 (19th Dec 2011)

## New Committers
* Matthew Gilliard
* Dave Ray
* Harold Hausman
* Jennifer Smith

## New

* Improve scsynth executable lookup strategy for linux
* Warn users if the number of samples they're attempting to write exceeds the capacity of UDP packets
* Add #'write-wav for writing data (either a buffer or a seq) to an external file.
* Add SC compatable wavetable format converter #'data->wavetable
* Add more file helper fns #'mkdir #'file-exists? #'mv! #'file? #'mk-tmp-dir! #'rm-rf! #'mkdir-p! #'absolute-path? #'dir-exists? #'subdir? #'cannonical-path #'ensure-trailing-file-separator #'remote-file-size #'dir-empty?
* Add retry functionality to #'download-fie
* Add string helper fns #'split-on-char
* Add system helper fns #'window-os? #'linux-os? #'mac-os?
* Add zip helper fns #'zip-file #'zip-entry #'zip-ls #'zip-cat #'unzip
* Make #'defcgen globally accessible
* Improve file-store fns. Add #'config-get #'config-set!
* Change ~/.overtone structure - put logs in separate dir and also create assets dir.
* Print user name on boot
* Add #'create-buffer-data for generating buffer data
* Add master mixer volume and input gain controls #'volume #input-gain
* Set default port for external server to 57110
* Add basic support for chord inversion #'inc-first #'dec-last #'invert-chord
* Add beginnings of an asset management system for dealing with direct asset urls and zipped bundles.
* Allow #'load-samples to take a list of strings of path partials as a param which it can stitch together using the correct path
* Add #'server-sample-rate cached server info val.
* Expand docstrign for definst
* Add simple 2 channel recorder functionality available through #'start-recording and #'stop-recording
* Add support for mapping control busses to a synth params
* Automatically determine if a sample is stereo or mono and create the appropriate player
* Add #'buffer-cue for streaming audio from disk. Also add supporting fn #'buffer-cue-pos
* Add ability to query a metronome for its current bpm value by passing it a single arg :bpm
* Make find-ugen more flexible - allow searches for SC name, lowecase and overtone names. Also rename *-ug ugen helper fns to *-ugen
* Allow samples returned by #'sample to be used as buffers
* Remember versions seen by the config by storing them in a set.
* Illustrate that the mixer is receiving too high volumes by outputting pink noise in addition to printing warning messages (this bevaviour is likely to change in a future version).


## New Instruments
* Add sampled-piano (piano samples are linked to as an asset)

## Examples

* Add an example of mapping a control bus onto synth params
* Add Schroeder-reverb example
* Add feedback example
* Festive Troika melody complete with fully synthesised bells

## Bugfixes

* Fix cgens to deal with the case where arg names were overriden by the explicit bindings created with #'with-overloaded-ugens
* Fix #'bus-set!
* Fix #'nth-interval for the case where no scale is specified (defaults to diatonic)
* Only run #'shutdown-server in JVM shutdown hook when the server is already connected.
* Evaluate default vals passed to synths - allows the use of fars and fns to set default parms.
* Fix #'buffer-save - choose defaults that actually work.
* Refer to correct namespaced keyword for instrument
* Fix sample re-loading on server boot
* Add #'status alias for #'server-status
* Fix :shutdown on-sync-event callback fn ::reset-cached-server-info
* Fix hardcoded wav path with freesound asset.

# Version 0.5 (17th Oct 2011)

## New Committers
* Nick Orton
* Kevin Neaton
* Jowl Gluth
* Chris Ford
* Philip Potter

## New
* Add new anti-ear-bleeding (TM) safety harness
* Add noise TOO LOUD!!! warnings when output is above a safe threshold
* Add repl.shell fns to core and live
* Rename await-promise! to deref!
* Pull all Overtone synthdefs into a defonce statement so they don't get reloaded multiple times
* Add glob capability to file manip fns
* Teach file manip fns to resolve ~
* Add #'load-samples to allow the loading of multiple samples via a globbing string
* Allow uppercase B to be usable as a flat notation for notes.
* Add volumes to mono and stereo sample players
* Add canonical-pitch-class-name to return the canonical version of the specified pitch class
* Free allocated id and throw exception if a sample isn't loaded correctly
* Add docstrings for ugen done-action constants and default vars
* sc-ugens can now take any map (such as buffer and bus) args and automagically map them to their :id vals.
* ctl can now take buffer and bus args
* Rename :n-frames to :size for buffer-info map to make the keys consistent with buffer maps
* Massage Doubles to Floats in outgoing OSC messages
* Ensure multiple shutdowns/boots can't happen simultaneously.
* Teach scopes to only work with an internal server
* Only allow #'buffer-data to work with an internal server
* Clean up REPL output when starting/stopping servers
* Add handy file downloading fns (for downloading samples)
* Create gens namespace containing all ugens and cgens
* Move more internals into overtone.sc.machinery
* Rename #'server-info to connection-info
* Add #'server-info which polls information directly from the server
* Add individually cached server info fns (server-num-output-buses, server-num-audio-buses etc.)
* Add #'deps-satisfied? and #'satisfied-deps query fns to examine the state of satisfied deps
* Add wait-until-deps-satisfied fn which blocks the current thread till all specified deps have been satisfied
* Allow longs as buffer ids
* Make sc-osc-debug-on public (some users were reporting issues with /dumpOSC)
* Add #'ensure-connected!
* Make samples play at normal rate even when sample-rates differ
* Add #'run - like #'demo but for testing non-audio synths
* Ensure sc-ugens and numbers are allowed as ugen args
* Ensure inst and synth player fns have similar calling semantics
* Rename #'status ->  #'server-status
* Rename #'connected? and #'disconnected? -> #'server-connected? and #'server-disconnected? respectively

## Examples
* Add Pepijn's vocoder example

## Deprecated
* Remove await-promise as deref in Clojure 1.3 now accepts a timeout val.
* out cgen doesn't support auto-rating so need to explicitly specify when we're outputting to a control bus

## Bugfixes
* Fix minor niggling bugs in shell fns
* Ensure osc lib fns are in scope withing server (fixes #'at macro)
* (use :reload-all 'overtone.live) now works again
* Ensure ugen arg docstrings don't lose their order
* Fix THX example and make it sound more beefy
* Fix drum loading
* Fix ks1, ks1-demo, tb303 and whoahaha synths
* Ensure #apply-at uses the playeor thread pool (fixes use of #'stop with temporal recursion)
* Make sure a synth's param list consists of symbols not strings
* Preserve an instrument's metadata - instrument docstrings are now honoured
* Fix closing buffer scopes
* Make scope draw with the correct orientation
* Don't consistently redraw scope buffers (short-term fix for the scsynth-jna memory leak)
* Various example fixes
* Fix ls-file-names to only return the file names, not the full path
* Only satisfy :synthdefs-loaded dep when *all* synthdefs have been loaded
* Fix FAILURE /n_free Node not found warning after stopping a currently running demo
* Fix ugen arg checking fns

# Version 0.4 (25th Sept 2011)

## New

* Support for Clojure 1.3
* Provide more separation between 'public' and 'private' APIs by moving non-public aspects of overtone.sc into overtone.sc.machinery
* Similarly pull out 'private' machinery from overtone.sc.server into overtone.sc.machinery.server - overtone.sc.server now contains only public fns.
* Create new helpers namespace for useful 'public' fns. overtone.util is now meant for internal/private util fns.
* Define and use default Overtone at-at pool
* Remove server dependence on studio.rig. Use #'boot-rig to boot and wait for rig to complete its initialisation
* Improve doc for sin-osc-fb ugen
* Improve doc for node-free
* Clean up implementation of cgen macro
* Rename boot to boot-server
* Rename quit to kill-server
* Make osc-validator check for actual types not the 'fuzzy' types Clojure uses
* Add information about resolving collider ugen fns to collider docstrings
* Teach resolve-degree about sharps and flats
* Stop users from attempting to receive osc messages from the server when it's not connected
* Various ugen metadata fixes
* Various docstring improvements

## Examples
* Add piano piece - Gnossienne No. 1 by Erik Satie

## Helpers
* Add some useful string manipulation fns
* Add some file helper fns
* Move splay-pan into helpers ns
* Move sc-lang converter here

## REPL
* Add odoc - Overtone version of doc which gives information about ugen colliders
* Add super rudimentary (but still pretty fun) shell fns ls and grep
* Make find-ug and find-ug-doc macros so you can pass unquoted symbols as args
* Allow ugen searches to also match the ugen name in addition to its full doc string
* Teach find-ug to print the full docstring of the match if only one is returned

## Deprecated
* Support for vijual representation of node tree

## Bugfixes

* Fix clear-ids in allocator
* Don't explicitly free node id when freeing node as this is already handled by a callback
* Fix resolve-gen-name
* Fix cgen bug in arglist generation
* Fix snare drum inst


# Version 0.3 (12th Sept 2011)

## New

* Print ascii art on boot (for both internal and external servers)
* Add :params key to ugen map
* Make ugens store their Overtone name
* Add Examples mechanism - defexamples - to store executable example documentation for ugens
* Make all trig fns :kr by default
* Update osc-clj to 0.6.2
* Teach callable maps to identify themselves when printing
* Make ugens print themselves nicely
* Rename ugen type to sc-ugen to help differentiate between the low-level maps representing supercollider ugens and the ugen fns which generate those maps
* Automatically capitilise ugen arg docstrings
* Make pulse and impulse :kr by default
* Create overtone.sc.defaults to store default vals such as the synth limits
* Let both alloc-id and free-id take an optional action-fn which will be executed within the allocation/deallocation transaction. This allows fns with side-effects to have their execution tightly bound to the allocation mechanism such that they cannot be incorrectly interleaved within a concurrent context.
* Add additional server sync fns - server-sync and with-server-self-sync and also allow a matcher-fn in recv
* Update buffer fns with explicit consideration of concurrent contexts
* Fix send-reply - it's now possible to send arbitrary information from the server via osc messages
* Move handler keys to end of event param list to match osc-clj
* Update and format event docstrings
* Add summaries to ugens, cgens and examples
* Re-organise namespaces
* rename clear-handlers to remove-all-handlers to match osc-clj
* Pull out spec generation code from ugen.clj to specs.clj
* Add checking functionality to ugens. Now the :check fns in the metadata are honoured
* Allow :check fns to be vecs of fns (will evaluate them all and aggregate any errors)
* Throw friendly exception when user doesn't specify both the val and rate in synth params when using a vec
* Add checks for nil args in control-proxy, sc-ugen and output-proxy
* Add check fns for a few ugens (such as compander)
* Add nil-arg-checker for ugen fns - throws an exception when a ugen fn is called with nil args
* Print out :none in docstring if a ugen's arg has no default
* Unify binary/unary-ugen and standard ugens. Now they are both callable maps and have the same calling semantics
* Add docstrings to binary/unary-ugens
* Add Overtone version number
* Add at-at as an explicit dependency
* Implement scope updating usign at-at
* Implement music.time with at-at
* Add lovely algorithmic piano example translated from Extempore example
* Add other drives (D and E) to windows scsynth paths

## New algo/music/repl fns

* choose-n
* cosr
* sinr
* tanr
* rand-chord
* find-ug
* find-ug-doc
* ug-doc

## New cgens

* sound-in
* mix
* splay

## New Examples

* dbrown
* diwhite
* dwhite
* impulse
* send-reply
* compander

## Bugfixes

* Fix piano inst - deal with 20 arg restriction by using apply
* Fix missed references to :free (keywords are no longer allowed as param vals)
* Fix ugen rate checker
* Fix blues example
* Pause thread booting Overtone until the boot has fully completed

# Version 0.2.1 (4th August 2011)

## Bugfixes

* Fix missing parens in drum and synth

# Version 0.2 (3rd August 2011)

## New

* Added example implementation of MAD (Music as Data) notation
* Add ugen arg rate checking - ensures ugen rates are <= parent rates for most cases
* Add docstrings to pretty much all ugens now
* Add :auto-rate option for ugen metadata to allow the default ugen automatically determine its rate
* Make all :ir rated ugens :auto rated
* Add list of arg names to synth map with the key :args
* Add hybrid args/keyword-args calling strategy for ugens. Matches SCLang's behaviour
* Unify synth and ugen hybrid args/keyword-args calling strategies
* Separate option :target and :position keys to separate map when calling node. This is to stop them clashing with similarly named args
* Add mda-piano ugen metadata and corresponding synth
* Add stk ugen metadata
* New instruments: tb303 mooger and others
* New fx: rlpf, rhpf and others
* Further work on pitch.clj functions
* Make demo behave similarly to synth
* Added translations for the majority of Chapter 1 of the new SuperCollider book
* Clean up docstring generator. Two \n\n in a row are treated as a carriage return, single \n are treated as spaces
* Add docstrings to defunks
* Add information from scserver to buffer and sample maps in the key :info
* Autoconvert any bus arguments to their :id val when passed as ugen args
* Use namespaced keywords to represent bus types
* Autoconvert any bus arguments to the :id val when passed as synths args
* Add map-ctl to map a node's control param to the values in a given control bus
* Replace java bitset allocator with Clojure ref-based one which can handle the allocation of sequential ids
* Use new allocator to allocate sequential busses
* Add limit for the number of audio busses
* Create new helpers namespace for end-user helper fns. Create two sub-namespaces chance and scaling containing many useful fns
* Allow synths to take :pos and :tgt in any order
* Trimming of docstrings to be within 80 chars
* Add new buffer fns for reading and writing to scsynth buffers
* Add basic synthdef decompilation
* Ensure :dr rate ugens only have :dr rate inputs
* Add dubstep bass examples
* Allow synth params to be passed as maps
* Add support for bespoke envelope curve lists
* Special case FFT ugen to allow :ar ugens to be passed as args
* Add :param key to synths and insts storing the list of param maps
* Add support for control ugens with rates other than :kr
* Don't require console viz stuff as default
* Print out nice Overtone ascii art and welcome message on boot
* Add comparison binary ugens
* Add OSC message validator system - checks all outgoing messages against a type signature
* Add :append-string mode for ugen arg metadata - allows the use of string args for certain ugens
* Fix dpoll and poll ugens
* Don't send any unknown messages to the server
* Add support for multiple paths to scsynth
* Replace crude /done syncronisation mechanism with a more robust one based on /sync
* Add cgens - composite ugens
* Re-implement pseudo-ugens with cgens
* Re-implement a number of demand ugens with cgens allowing to match the arg ordering of SCLang

## Bugfixes

* Fix print-classpath to refer to the project's classpath
* Freeing control bus previously freed an audio bus of the same name
* Masses of ugen metadata fixes
* Allow :kr ugens to be plugged into :dr ugens
* Fix ugen :init behaviour
* Fix klang and klank init fns to munge specs correctly
* Increase APPLY-AHEAD time to 300ms to reduce perceived jitter when using the metronome
* Synchronise creation of first studio group
