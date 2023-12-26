# Arne's overtone wishlist

Some random notes in no particular order about stuff I'd like to address.

## wayland/pipewire support

The linux desktop landscape has changed a lot since overtone was created,
Wayland is replacing X11, and Pipewire is replacing PulseAudio and Jack. Both
have been a long time coming, and it seems we're now past the inflection point
where most distros are making the new thing the default.

### PipeWire

Pipewire implements the Jack ABI, meaning existing clients continue to work as
before (at least in theory), but there are still some things to consider.

We use some command line tools like `jack_lsp` to set up jack connections. These
will not be installed on pure pipewire distros (even though if they are they
should work with pipewire just fine.) This is something that could resolve
itself upstream, but might be an opportunity to start using jack-jna directly. I
have clojure wrapper code for jack-jna in a separate project, which I might
release as a standalone library, or propose to add it to overtone directly, or
release it as a library under the overtone umbrella.

### Wayland

SuperCollider currently has no first class support for Wayland, meaning things
like `mouse-x` and `mouse-y` don't work, and will generate an error. There's a
lot of these in the overtone bundled instruments and examples. Hopefully this
will resolve itself on the supercollider side, but it might be a while, since
Wayland is way more security conscious, so it's harder for a random client to
peek at input devices even while not in focus. There are APIs in the works to
provide a standard way to do this, but AFAICT it's still somewhat early days.

In the meanwhile it would be nice if at least all of overtone's namespaces would
load... maybe providing some fallback where we replace these ugens with
something else? maybe just a constant? or a slow sin-osc... just an idea.

## Docs and stuff

An area where we can always do more and better. I noticed there's actually a WIP
overtone ebook in the repo. Might be cool to coordinate efforts to get that to
cover more of what's in overtone. There's so much here that people can easily
miss.

Also just more better FAQ and troubleshooting tips.

Adding more example invocations to docstrings would be good, I really like how a
lot of overtone functions already do this.

## Rename main branch

The main branch is called `master`, which feels really dated to me. People
rarely still call the main branch that on new repos, with `main` being the most
common alternative. I see Sam Aaron also uses `main` for new projects, Soni-Pi
uses `stable` and `dev`.

It's a small thing but IMO an important signal.
