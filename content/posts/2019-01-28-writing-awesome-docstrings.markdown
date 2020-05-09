---
layout: post
date-published: 2019-01-28T00:00:00Z
title: 4 Small Steps Towards Awesome Clojure Docstrings
uuid: b728ab75-373e-46b5-ba68-b01d5918cd70
permalink: /posts/writing-awesome-docstrings.html
---

Through my work on [cljdoc](https://cljdoc.org) I spent a lot of time looking at documentation
and implementing code to render documentation. This made me more aware of the various
facilities in Clojure documentation generators (codox, cljdoc, ...) and I would like to use
this post to share that awareness with the wider Clojure community.

## 1. Backtick-Quote Function Arguments & Special Keywords

Whenever referring to an argument or special keywords, quote them using Markdown style
\`backticks\`. This makes them stand out more when reading the docstring, making it easier to
visually parse and skim. Emacs also nicely highlights this (possibly others too).

```
(defn conj!
  [coll x]
  "Adds `x` to the transient collection, and return `coll`. The 'addition'
   may happen at different 'places' depending on the concrete type."
  ,,,)
```

## 2. Link To Other Functions Using [[Wikilink]] Syntax

Functions call each other and sometimes it can be useful to link to other functions.
In Codox and cljdoc you can do this by wrapping the var name in wikilink-style double brackets:

```
(defn unlisten!
  "Removes registered listener from connection. See also [[listen!]]."
  [conn key]
  (swap! (:listeners (meta conn)) dissoc key))
```

Featured here: [`datascript.core/unlisten!`](https://cljdoc.org/d/datascript/datascript/0.17.1/api/datascript.core#unlisten!).
To link to vars in other namespaces, fully qualify the symbol in the brackets, e.g. `[[datascript.core/listen!]]`.

## 3. Include Small Examples

On cljdoc all docstrings are interpreted as Markdown. With Codox this can be achived with a
small configuration tweak. This means you have access to all the text formatting facilities
that Markdown provides including code blocks. Code blocks can be fantastic when trying to show
how a function is used in a bigger context, as very nicely shown in the [Keechma Toolbox
documentation](https://cljdoc.org/d/keechma/toolbox/0.1.23/api/keechma.toolbox.dataloader.controller#register):

[![keechma register](/images/keechma-register.png)](https://cljdoc.org/d/keechma/toolbox/0.1.23/api/keechma.toolbox.dataloader.controller#register)

See [the source](https://github.com/keechma/keechma-toolbox/blob/176c96a7f8b97a7d67f0d54d1351c23db052d71c/src/cljs/keechma/toolbox/dataloader/controller.cljs#L71-L85) of this majestic docstring.

## 4. Use Tables To Describe Complex Options Maps

cljdoc's Markdown implementation supports tables as well. Those can be very useful when having a function that receives a map of options, like [`reitit.core/router`](https://cljdoc.org/d/metosin/reitit-core/0.2.13/api/reitit.core#router):

[![reitit core router](/images/reitit-router.png)](https://cljdoc.org/d/metosin/reitit-core/0.2.13/api/reitit.core#router)

See [the source](https://github.com/metosin/reitit/blob/0.2.13/modules/reitit-core/src/reitit/core.cljc#L417) of this beautiful docstring.

## Closing

These trivial to implement improvements can make your docstrings 1000x times nicer to read
(scientific studies have shown). Also they will just look plain awesome on [cljdoc](https://cljdoc.org). Check out
some examplary docstring work done by Nikita Prokopov here:

- [Rum](https://cljdoc.org/d/rum/rum/0.11.3/api/rum.core)
- [Datascript](https://cljdoc.org/d/datascript/datascript/0.17.1/api/datascript.core)

And **please tell me** about other projects with exceptional documentation or even more ways to
make docstrings awesome.
