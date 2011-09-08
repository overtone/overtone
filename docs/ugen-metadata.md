The ugen metadata is converted to a suite of functions which are called within the context of a synth definition. For example, consider the following synth design:

    (defsynth foo [] (sin-osc 440))

Here, we're explicitly calling one ugen fn - `sin-osc`. This function knows how to correctly handle and manipulate its param list so that the correct synthdef is created as part of the compilation process. What kinds of manipulation is necessary? Well, for starters, each ugen has a number of default params which can be omitted. Also, Overtone allows you to use ordered params, keyword params and even a mix of the two. So you could also create a basic sine wave at 440Hz with the following:

    (defsynth foo [] (sin-osc :freq 440))

Both the above synth designs are also equivalent to:

    (defsynth foo [] (sin-osc :freq 440 :phase 0))

As the default value for phase is 0. The ugen function clearly needs to know how to take these args and munge them about correctly so that a correct synthdef data structure can be created. This work takes place in the namespace overtone.sc.ugen.

So, how are these functions created and what they do?

The story starts with the ugen metadata. A set of namespaces describing the properties of each and every scsynth ugen. Here's the metadata for the sine wave we were using above:

      {:name "SinOsc",
       :args [{:name "freq", :default 440.0 :doc "frequency in Hertz"}
              {:name "phase", :default 0.0 :doc "phase offset or modulator in radians"}],
       :doc "sine table lookup oscillator

    Note: This is the same as Osc except that the table has already been fixed as a sine table of 8192 entries."}

Here we have three basic keys: `:name`, `:args` and `:doc`. The name `SinOsc` is the exact scsynth name in camelCase. This is the name we need to use within the synthdef. However, it's not necessarily very idiomatic clojure to use camelCase, so we convert it to the hyphenated `sin-osc`. The logic for this is in the fn `overtone-ugen-name`.

The ugen spec metadata ens up in the var `UGEN-SPECS` which is polpulated by the fn `load-ugen-specs` which gathers the raw spec metadata found in the namespaces `overtone.sc.ugen.~@UGEN-NAMESPACES` and then passes these specs through the fn `derive-ugen-specs` which resolves the inheritence tree (which allows a given ugen to extend another thus inheriting its parent's information). Finally the derived specs are then individually passed through the fn `decorate-ugen-spec` which does all of the fancy work.

* `with-rates`
* `with-categories`
* `with-expands`
* `with-init-fn`
* `with-default-rate`
* `with-fn-names`
* `doc/with-arg-defaults`
* `doc/with-full-doc`
