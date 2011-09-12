# Overtone Change Log

## Version 0.3

### New

* Print ascii art on boot (for both internal and external servers)
* Add :params key to ugen map
* Make ugens store their Overtone name
* Add Examples mechanism - defexamples - to store executable example documentation for ugens
* Make all trig fns :kr by default
* Update osc-clj to 0.6.1
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
* Paulse thread booting Overtone until the boot has full completed

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
