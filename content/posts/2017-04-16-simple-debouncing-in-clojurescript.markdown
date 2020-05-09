---
layout: post
date-published: 2017-04-16T00:00:00Z
title: Simple Debouncing in ClojureScript
uuid: 271f273e-8587-42ce-be1a-6efe22a78d2e
permalink: /posts/simple-debouncing-in-clojurescript.html
---

This is a short post on a problem that eventually occurs in any
Javascript app: debouncing. While there are various approaches to
this problem I want to focus on one that relies on nothing else than
the [Closure Library](https://developers.google.com/closure/library/).

## Why Debounce

Debouncing is a technique to limit the rate of an action. Usually this
rate is specified as an interval in which the action may be executed
at most once. If execution of the action is requested multiple times
in one interval it is important that the most recently supplied
arguments are used when eventually executing the action.

(If you only care about the rate limiting and using the latest
arguments isn't a requirement that's called throttling.)

Use cases for debouncing are plentiful. Auto-saving something the user
is typing, fetching completions or triggering server side validations
are some examples that come to mind.

## Closure Library Facilities

I've long been a fan of the Closure Library that comes with
ClojureScript.  Many common problems are solved in elegant and
efficient
ways, [the documentation](https://google.github.io/closure-library/)
gives a good overview of what's in the box and the code and tests are
highly readable.

For the problem of debouncing Closure provides a construct [goog.async.Debouncer](https://google.github.io/closure-library/api/goog.async.Debouncer.html)
that allows you to debounce arbitrary functions. A short, very basic example in Javascript:

```js
var debouncer = new goog.async.Debouncer(function(x) {alert(x)}, 500);
debouncer.fire("Hello World!")
```

This will create an alert saying "Hello World!" 500ms after the
`fire()` function has been called. Now let's translate this to
ClojureScript and generalize it slightly. In the end we want to be
able to debounce any function.

```clojure
(ns app.debounce
  (:import [goog.async Debouncer]))

(defn save-input! [input]
  (js/console.log "Saving input" input))

(defn debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    ;; We use apply here to support functions of various arities
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))

;; note how we use def instead of defn
(def save-input-debounced!
  (debounce save-input! 1000))
```

What the `debounce` function does is basically returning a new
function wrapped in a `goog.async.Debouncer`. When and how you create
those debounced functions is up to you. You can create them at
application startup using a simple `def` (as in the example) or you
might also dynamically create them as part of your
component/application lifecycle. (If you create them dynamically you
might want to learn about `goog.Disposable`.)

There's one caveat with our `debounce` implementation above you should
also be aware of: because we use Javascript's `apply` here we don't
get any warnings if we end up calling the function with the wrong
number of arguments. I'm sure this could be improved with a macro but
that's not part of this article.

Also small disclaimer on the code: I mostly tested it
with [Lumo](https://github.com/anmonteiro/lumo) in a REPL but I'm
confident that it will work fine in a browser too.

## Debounce Away

I hope this helps and shows that there's much useful stuff to be found
in Closure Library. To this day it's a treasure trove that has rarely
dissappointed me. Sometimes things are a bit confusing (I still don't
understand `goog.i18n`) but there are many truly simple gems to be
found. *Maybe I should do a post about my favorites some day...*

The [documentation site](https://google.github.io/closure-library) has
a search feature and a tree view of all the namespaces of the library;
use it next time when you're about to add yet another Javascript
dependency to your project.

Also not a big surprise I guess but all of the Closure Library's code
is Closure Compiler compatible just like your ClojureScript code. This
means any functions, constants etc. that are never used will be
removed by the compiler's Dead Code Elimination feature. Yeah!

**Update 2017-05-12** — Multiple people have noted that there also
is a function [`goog.functions.debounce`](https://google.github.io/closure-library/api/goog.functions.html#debounce). For many basic cases this
might result in simpler, more concise code.
