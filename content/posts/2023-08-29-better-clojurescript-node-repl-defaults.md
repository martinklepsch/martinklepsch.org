---
title: Better ClojureScript Node REPL Defaults
date-published: 2023-08-29T11:34:31.066Z
uuid: a5e97c9e-46a4-4a55-914b-d63af6f698bb
og-image: /images/selfies/2.jpg
type: post
permalink: /posts/better-clojurescript-node-repl-defaults.html
---

Hi there! Welcome back — to you as much as to me. It's been a while that I've
published anything but here we go: **a little quality of life improvement for anyone driving a Node.js REPL from ClojureScript.**

### The Problems

There's two issues I often run into when working with ClojureScript and Node.js REPLs:

1. uncaught errors will cause the Node.js process to exit
2. many values are async, resulting in a `<#Promise>`

The first applies to any kind of ClojureScript REPL while the second is a more Node-specific problem. Losing your REPL state whenever something fails is annoying. This behavior makes sense when you're in production but for a REPL... not ideal.

### A Workaround

Fortunately a bandaid solution is pretty trivial. To solve 1) we can make use of the excellent [portal](https://github.com/djblue/portal) tool. For 2) we can install a handler for `unhandledRejection` events, catching the error and reporting it in whatever way we like.

Below is a namespace that can be added to your [`:preloads`](https://shadow-cljs.github.io/docs/UsersGuide.html#_preloads) or just required when you start a new REPL session.

```clj
(ns acme.node-repl-preloads
  (:require [portal.api :as portal]))

(js/process.on "unhandledRejection"
               (fn [err]
                 (js/console.log "unhandledRejection" err)
                 (tap> {:unhandledRejection err})))

(when (.-ACME_DEV js/process.env)
  (portal/open)
  (add-tap portal/submit))
```

I add this to my `node-repl` helper like this:

```clj
  (shadow/node-repl
    {:config-merge [{:devtools {:preloads '[acme.node-repl-preloads]}}]})
```

Now, with `ACME_DEV` set, we'll get a Portal window whenever we start a Node
REPL, allowing us to chain promises into `tap>` and inspecting their value that
way.

In addition to that any errors will also be logged to the console and to the
Portal window — without crashing the process 🙂 From where I stand this would
be a good default behavior but messing with error handling obviously comes with
it's own tradeoffs.

Adding another handler for `unhandledException` is probably a good idea.

Anyways, nice to be back. I hope this is a slight improvement to someone's setup 🤗
