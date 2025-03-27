---
title: Using JS in ClojureScript Projects
slug: embracing-js-files-in-clojurescript
date-published: 2025-03-27
uuid: 7d3e9c12-f458-41a7-9e2f-e5a8c2f9b3d4
type: post
---

The pull toward JavaScript has never been stronger. While ClojureScript remains an extremely expressive language, the JavaScript ecosystem continues to explode with tools like [v0](https://v0.dev/), [Subframe](https://subframe.com/) & [Paper](https://paper.design/) generating entire UI trees and even full websites.

I found the feedback loop of these tools extremely quick and often use v0 to prototype specific components or interactions. 

To benefit from these new tools and development experiences in an existing ClojureScript codebase you have two options:

1. Rewrite all the code to CLJS
2. Somehow use it as JS

In reality what I do is a bit of both. I mostly translate components to UIx but sometimes will use JavaScript utility files as is. This post is about that second part.

(I’ll probably write about the first part soon as well!)

## The shadow-cljs JS import toolchain

`shadow-cljs`, the de facto frontend for the ClojureScript compiler, has built-in support for importing JavaScript `.js` files directly into your ClojureScript codebase.

Recently this was helpful when I wanted to add a custom d3-shape implementation to a codebase. I experimented in v0 until I had the desired result, leaving me with `rounded_step.js`:

```javascript
// rounded_step.js
function RoundedStep(context, t, radius) {
  this._context = context;
  this._t = t;           // transition point (0 to 1)
  this._radius = radius; // corner radius
}

RoundedStep.prototype = {
  // ... implementation details full of mutable state
};

const roundedStep = function (context) {
  return new RoundedStep(context, 0.5, 5);
}

export { roundedStep };
```

Now this code would be kind of annoying (and not very valuable) to rewrite to ClojureScript. I tried briefly but eventually settled on just requiring the JS file directly:

```clojure
(ns app.molecules.charts
  (:require
   [applied-science.js-interop :as j]
   [uix.core :as uix :refer [defui $]]
   ["/app/atoms/charts/rounded_step" :refer [roundedStep]]
   ["recharts" :as rc]))
```

Note the path `/app/atoms/charts/rounded_step` - shadow-cljs understands this refers to a JavaScript file in your source tree and will look for it in on the classpath. 

Assuming you have `:paths “src”` then the file would be at `src/app/atoms/charts/rounded_step.js`.


## When to use JavaScript directly

While I generally will still translate components to UIx (using [these instructions](https://ctxs.ai/weekly/react-clojure-script-conversion-y9d06f)) using plain JS can be nice in a few cases:

1. **Code relying on mutability** - some library APIs may expect it and it’s usually a bit annoying and perhaps even error prone to articulate in CLJS
2. **Hard to translate syntax constructs** - spreading operators, async/await, etc. 
3. **Performance** - If you want to drop down a level to squeeze out higher performance


## Limitations

1. To use JSX [you’ll need to set up a preprocessor](https://shadow-cljs.github.io/docs/UsersGuide.html#_javascript_dialects), something I didn’t want to get into. And for writing components UIx is nicer anyways. 
2. “Leaf nodes” only, meaning you can’t require CLJS from JS and things like that. (Fine for my use cases.)


## Making it work

Generally when dealing with JS libraries, the following has been helpful for my workflow:

1. **Use js-interop libraries** - `applied-science/js-interop` and `cljs-bean` make working with JavaScript objects more ergonomic
2. **Use literal objects** - The `j/lit` macro makes passing complex configuration objects cleaner

## The payoff

The real benefit? You get to use the best of both worlds:

- ClojureScript's expressive syntax, immutable data structures and functional approach where and when you want it
- Plug in JavaScript snippets when it makes sense
- Less friction when adopting new JavaScript tools

Some folks will be arguing for pure ClojureScript solutions to everything. But in today's landscape, embracing JavaScript interop is the pragmatic choice. 

After all, sometimes the best code is the code you don't have to write.