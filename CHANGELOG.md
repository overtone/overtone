# Overtone Change Log

## Version 0.7.1 (27th June 2012)

### Improvements

* Improve booting of external server on Windows.
* Working dir is now set on Windows machines for `scsynth`
* `scsynth` path is now discovered on Windows rather than hardcoded
* Users may set :sc-path in their config to point to their scsynth
  executable if it's not to be found in the default locations.

## Version 0.7 (26th June 2012)

### New Committers
* Damion Junk
* Jacob Lee
* Fabian Steeg
* Michael Bernstein
* Ian Davies

### New fns
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

### Renamed fns
* `buffer-cue-close` -> `buffer-stream-close`
* `find-note-name` -> `find-pitch-class-name`

### New cgens
* `tap`- Listen in to values flowing through scsynth and have them periodically update atoms associated with a synth instance for easy reading from Clojure.
* `scaled-play-buf` - similar to `play-buf` but auto-scales rate
* `scaled-v-disk` - similar to `v-disk-in` but auto-scales rate
* `hold` - hold input source for set period of time, then stop safely
* `local-buf` - now supports SCLang's argument ordering

### Improvements
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

### New protocols
* `IMetronome`
* `ISynthNode`
* `ISynthGroup`
* `ISynthBuffer`
* `ISaveable`
* `IControllableNode`
* `IKillable`

### New Examples
* `examples/piano_phase.clj`
* `examples/row_row_row_your_boat.clj`

### Bugfixes
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

## Version 0.6 (19th Dec 2011)

### New Committers
* Matthew Gilliard
* Dave Ray
* Harold Hausman
* Jennifer Smith

### New

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


### New Instruments
* Add sampled-piano (piano samples are linked to as an asset)

### Examples

* Add an example of mapping a control bus onto synth params
* Add Schroeder-reverb example
* Add feedback example
* Festive Troika melody complete with fully synthesised bells

### Bugfixes

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

## Version 0.5 (17th Oct 2011)

### New Committers
* Nick Orton
* Kevin Neaton
* Jowl Gluth
* Chris Ford
* Philip Potter

### New
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

### Examples
* Add Pepijn's vocoder example

### Deprecated
* Remove await-promise as deref in Clojure 1.3 now accepts a timeout val.
* out cgen doesn't support auto-rating so need to explicitly specify when we're outputting to a control bus

### Bugfixes
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

## Version 0.4 (25th Sept 2011)

### New

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

### Examples
* Add piano piece - Gnossienne No. 1 by Erik Satie

### Helpers
* Add some useful string manipulation fns
* Add some file helper fns
* Move splay-pan into helpers ns
* Move sc-lang converter here

### REPL
* Add odoc - Overtone version of doc which gives information about ugen colliders
* Add super rudimentary (but still pretty fun) shell fns ls and grep
* Make find-ug and find-ug-doc macros so you can pass unquoted symbols as args
* Allow ugen searches to also match the ugen name in addition to its full doc string
* Teach find-ug to print the full docstring of the match if only one is returned

### Deprecated
* Support for vijual representation of node tree

### Bugfixes

* Fix clear-ids in allocator
* Don't explicitly free node id when freeing node as this is already handled by a callback
* Fix resolve-gen-name
* Fix cgen bug in arglist generation
* Fix snare drum inst


## Version 0.3 (12th Sept 2011)

### New

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


### New algo/music/repl fns

* choose-n
* cosr
* sinr
* tanr
* rand-chord
* find-ug
* find-ug-doc
* ug-doc

### New cgens

* sound-in
* mix
* splay

### New Examples

* dbrown
* diwhite
* dwhite
* impulse
* send-reply
* compander

### Bugfixes

* Fix piano inst - deal with 20 arg restriction by using apply
* Fix missed references to :free (keywords are no longer allowed as param vals)
* Fix ugen rate checker
* Fix blues example
* Pause thread booting Overtone until the boot has fully completed

## Version 0.2.1 (4th August 2011)

### Bugfixes

* Fix missing parens in drum and synth

## Version 0.2 (3rd August 2011)

### New

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

### Bugfixes

* Fix print-classpath to refer to the project's classpath
* Freeing control bus previously freed an audio bus of the same name
* Masses of ugen metadata fixes
* Allow :kr ugens to be plugged into :dr ugens
* Fix ugen :init behaviour
* Fix klang and klank init fns to munge specs correctly
* Increase APPLY-AHEAD time to 300ms to reduce perceived jitter when using the metronome
* Synchronise creation of first studio group
