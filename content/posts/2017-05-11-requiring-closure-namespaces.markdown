---
layout: post
date-published: 2017-05-11
title: Requiring Closure Namespaces
uuid: 461f273b-8587-42ce-be1a-6efe22a78d2e
---

Yet another post on properly using
the [Closure Library](https://developers.google.com/closure/library/)
from within ClojureScript. This time we'll discuss how to require
different namespaces from Closure and the edge-cases that may not
be immediately intuitive.

## Namespaces, Constructors, Constants

When requiring things from Closure you mostly deal with its namespaces.
Most namespaces have functions defined in them, some also contain constructors or constants.
Functions are camelCased. Constructors are Capitalized. Constants are ALL_CAPS.
The line between namespaces and constructors gets a bit blurry sometimes as you'll see shortly.

Let's take `goog.Timer` as an example. As per the previous paragraph you can infer that `Timer`
is a constructor. Just like in Clojure we use `:import` to make constructors available:

```
(ns my.app
  (:import [goog Timer]))
```

Now we may use the `Timer` constructor as follows:

```
(def our-timer (Timer. interval))
```

Great. We have a timer. Now we'll want to do something whenever it
"ticks". The `Timer` instance emits events which we can listen
to. Listening to events can be done with the function
`goog.events.listen`. As you can see, this function is not part of any
class instance - it just exists in the `goog.events` namespace.
To make the `listen` function accessible you need to require the
namespace containing it. This is similar to how we require regular
ClojureScript namespaces:

```
(ns my.app
  (:require [goog.events :as events])
  (:import [goog Timer]))
```

We can refer to the function as `events/listen` now. To listen to
specific kinds of events. we need to pass an event type to this function. Many
Closure namespaces define constants that you can use to refer to
those event types. Internally they're often just strings or numbers but
this level of indirection protects you from some otherwise breaking changes to
a namespace's implementation.

Looking at the [Timer](https://google.github.io/closure-library/api/goog.Timer.html) 
docs you can find a constant `TICK`. Now we required the constructor
and are able to use that but the constructor itself does not allow us
to access other parts of the namespace. So let's require the namespace.

```
(ns my.app
  (:require [goog.events :as events]
            [goog.Timer :as timer]) ; <-- new
  (:import [goog Timer]))

(def our-timer (Timer. interval))

(events/listen our-timer timer/TICK (fn [e] (js/console.log e)))
```

Remember the blurry line mentioned earlier? We just required the `goog.Timer` namespace
both as a constructor and as a namespace. While this works fine, there are two more edge cases
worth pointing out.

## Deeper Property Access

Closure comes with a handy namespace for keyboard shortcuts, aptly named [`KeyboardShortcutHandler`](https://google.github.io/closure-library/api/goog.ui.KeyboardShortcutHandler.html).
As you can guess, `KeyboardShortcutHandler` is a constructor that we can use via `:import`. 
Since it emits events, the class also provides an enum of events that we can use to listen for specific ones.
In contrast to the timer's `TICK`, this enumeration is "wrapped" in `goog.ui.KeyBoardShortcutHandler.EventType`. 

The `EventType` property contains `SHORTCUT_PREFIX` and `SHORTCUT_TRIGGERED`. So far we've only imported the constructor.
At this point you might try this:

```
(:require [goog.ui.KeyBoardShortcutHandler.EventType :as event-types])
```

**But that won't work**. The `EventType` is not a namespace but an enum provided by
the `KeyboardShortcutHandler` namespace. To access the enum you need to access it through the
namespace providing it. In the end this will look like this:

```
(:require [goog.ui.KeyBoardShortcutHandler :as shortcut])

(events/listen a-shortcut-handler shortcut/EventType.SHORTCUT_TRIGGERED ,,,)
```

Note how the slash always comes directly after the namespace alias.

## goog.string.format

Last but not least another weird one. `goog.string.format` is a namespace
that
[seems to](https://google.github.io/closure-library/api/goog.string.format.html) contain
a single function called `format`. If you require the format namespace,
however, it turn out to contain no function of that name:

```
(:require [goog.string.format :as format])

(format/format ,,,) ; TypeError: goog.string.format.format is not a function
```

Now in cases like this it often helps to look at [the source code](https://github.com/google/closure-library/blob/master/closure/goog/string/stringformat.js)
directly. Usually Closure Library code is very readable. The format function is defined as follows:

```
goog.string.format = function(formatString, var_args) {
```

As you can see it's defined as a property of `goog.string`, so we can
access it via `goog.string/format` (or an alias you might have chosen
when requiring `goog.string`).  In that sense `goog.string.format` is
not a real namespace but rather something you require for its side
effects - in this case the definition of another function in `goog.string`.
I have no idea why they chose to split things up in that way.
¯\\_(ツ)_/¯

## For Reference

I scratched my head many times about one or the other aspect of this
and usually ended up looking at old code. Next time I'll look at the handy list below 🙂

- Require Google Closure namespaces just as in ClojureScript
    - `(:require [goog.events :as events])`
- Require constructors using one of the two forms. In eiter case you
  may use `Timer.` to construct new objects.
    - `(:import [goog Timer])`
    - `(:import goog.Timer)`
- Only access non-constructor parts of a namespace through a namespace binding previously established via `:require`
- Always use a slash after a namespace alias; use a dot for deep property access.
- Requiring `goog.string.format` will define a function `format` in the `goog.string` namespace.

## Enjoy

...
