## Event System

Overtone uses a simple event system where an event can be published like this:

{% highlight clojure %}
  (event :my-event :foo "asdf" :bar 200.0)
{% endhighlight %}

where an event has a label and any number of key/value pairs.  Events can be
handled by registering a handler function like this:

{% highlight clojure %}
  (on :my-event #(do-stuff %))
{% endhighlight %}

If the handler accepts an argument it is passed an event map, otherwise it is
called with no argument, which allows for simple handlers that don't care about
the contents of an event:

{% highlight clojure %}
  (on :my-event #(log/debug "my-event fired here..."))
{% endhighlight %}

The event map contains the key/value pairs and an :event-type:

{% highlight clojure %}
 {:event-type :my-event
  :foo "asdf"
  :bar 200.0}
{% endhighlight %}

Event handlers can de-register themselves by returning the keyword :done after
executing.  This can be used to create one-off handlers and more.

{% highlight clojure %}
  ; Register a one shot handler for a :special-moment event
  (on :special-moment #(do (println "special moment") :done))

  ; Fire the :special-moment event in 5 and 6 seconds
  (schedule #(event :special-moment) 5000)
  (schedule #(event :special-moment) 6000)

  ; You will only see the event fire once
{% endhighlight %}

### OSC Message Events

OSC messages are automatically re-published as events, so when the application
receives the OSC message "/boom" it will publish an event of type "/boom" that
has two properties, :path and :args, corresponding to the osc path and
arguments.  If boom had a single frequency value argument, you could handle it
like this:

{% highlight clojure %}
  (on "/boom" #(play-boom (first (:args %))))
{% endhighlight %}

### Built-in Events:

Some of the built-in events that might be of general interest are:

:booted    => the audio server process has been booted
:connected => we have successfully connected with an audio server


