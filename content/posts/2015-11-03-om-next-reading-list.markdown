---
layout: post
date-published: 2015-11-03
title: Om/Next Reading List
uuid: e2aa0cae-c4ce-42c0-9052-f1b001e51c0e
---

A small dump of things I read to learn more about Om/Next. Most of these
I stumbled upon while lurking in #om on the [Clojurians Slack][clj-slack].

### [Thinking in Relay][thinking-in-relay]

This is Facebook's high level overview for Relay. It explains the
reasoning for colocating queries and how data masking allows
developers to write components that are not coupled to their location
in the UI tree.

### [Om/Next Quick Start][quick-start]

This is the official Om/Next quick start tutorial. It guides you
through building a basic application with Om/Next and introduces the
basic API for queries and mutations. After reading this you should
have a rough idea what's being talked about in the next two reads.

### [Om/Next The Reconciler][the-reconciler]

Kovas Boguta who previously gave an Om/Next workshop with David Nolen
wrote this introduction to the Om/Next reconciler. It covers the
architectural role of the reconciler managing application state and
communicating it to components. The reconciler also acts as an indexer
of all components and, using their queries to build a depdency graph,
knows when to update which components.

### [Om/Next Overview][om-next-overview]

Written by Tony Kay this overview covers many practical aspects of
writing queries and mutations. Before it goes into the nitty gritty
details howvever there is another short *Problem → Solution* section
that nicely describes the concepts in Relay and Om/Next in prose.

Now put all those links into Instapaper/Pocket & enjoy reading!

[clj-slack]: http://clojurians.net/
[thinking-in-relay]: https://facebook.github.io/relay/docs/thinking-in-relay.html
[quick-start]: https://github.com/omcljs/om/wiki/Quick-Start-%28om.next%29
[the-reconciler]: https://medium.com/@kovasb/om-next-the-reconciler-af26f02a6fb4
[om-next-overview]: https://github.com/awkay/om/wiki/Om-Next-Overview
