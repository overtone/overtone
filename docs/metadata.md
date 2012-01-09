# UGen Metadata

SuperCollider comes with a large collection of UGens, or unit generator
functions.  These are the building blocks of synthesizers, samplers, logical
systems and everything else you can do with SC.  In Overtone rather than having
to define a function for each UGen, we have a large set of meta-data about all
the UGens, and then we generate functions to create them.  This file describes
various aspects of the UGen meta-data system.  If you want to add support for
more UGens then you can probably just copy a similar type of UGen to get a sense
for how the meta-data works, and then look here for details. Let us know if you
run into any problems.

The ugen metadata can be found in the overtone.sc.ugen.metadata.* namespaces.

## Intializing the base specs.

First all specs are loaded, and any spec that derives from another using the
:extends property is merged with it's parent, the child properties overwriting
the parent's.

Then comes mode initialization. All args are assigned a mode.  If the arg does
not have an explicitly statetd mode, then the arg-name-mode-map is searched. If
there is no mode in the name map, then the default mode is :standard. At this
time a boolean :expands? property is also added to each arg entry, depending on
the mode of the arg.

Then an expansion spec is derived from the :expanded properties.  It is just a
vector of booleans and is assigned to the :expansion-spec property. This is
later used during MCE

## function generation

for each rate of each initilized ugen spec, a ugen function is defined.

### Ugen function naming and rates

Ugen functions have multiple versions to support different processing rates.
For example, sin-osc:ar and sin-osc:kr product audio-rate and control-rate
ugens.  Using just sin-osc will also create an audio-rate ugen, except for ugens
that only support :kr or :ir, in which case that is the default rate for the
unqualified version.

## Synthdef time

### multi-channel expansion (MCE)

when a ugen function is called. The args and the expansion-spec are passed to
the expand function.  Which then calls the ugen function potentially multiple
times.

### Check

if a :check function with args [rate num-outs inputs spec] is defined, then it
is called last before the ugen is added to the synth graph. If the check
function returns a string then it is considered an error and an exception is
thrown.

---------------------------------------------------------------

## The anatomy of a spec map

```
:name     mandatory string; containing the sc lang UGen name
:derived  optional string; containing the name of a spec to merge
          with. Note that properties marked mandatory can also be
          specified through derive. Properties in the spec
          containing the derived statement take precedence over
          properties in the "parent"
:args     mandatory vector; containing the argument spec maps each of
          which has the following form:
          :name     mandatorty string; the arg's name
          :default  optional; the default value if none given at synthdef time
                    note that there must not ever be any args with
                    defaults followed by args without them
          :map   a map that the arg will be looked up in
                 if a value exists for key arg then that
                 is used, else the arg is used
          :mode     optional symbol;
                    :not-expanded stops multi channel expansion on
                                  thearg
                    :append-sequence the arg will not be expanded
                            and will be removed from it's position
                            in the inputs and concatenated to their
                            tail end. If there are several args in
                            this mode then they will be appended
                            in argument order.
                    :Append-sequence-set-num-outs
                            same as above, but also sets num-outs
                             via (count arg)
                    :num-outs will not be expanded & will be
                              removed from the input sequence,
                              and will determine the number
                              of output channels. SC arguments with
                              the name "numChannels" typically
                              use this mode
                    :done-action will not be expanded and will be
                              mapped to the integer code for the
                              action. Args named "doneAction" are
                              implicitly in this mode
                    :as-ar  the argument is converted to audio rate
                           using the k2a ugen, if it is not already
                           audio rate. This is only done if the rate
                           of the ugen is :ar
:num-outs optional, the number of fixed outputs, defaults to 1 if
          unspecified. It is unneeded if there is a :mode :num-outs
          or if a :num-outs is defined in a map returned from :init
:init     optional, a function which must take the args [rate args spec]
          it should return a sequence of the new modified args or a
          map with keys :args and :num-outs if they need to be manually
          determined
:check    optional, a function which takes the args
              [rate num-outs inputs spec]
              this is called after init (if specified) and should
              return an error string or nil if no error
```
