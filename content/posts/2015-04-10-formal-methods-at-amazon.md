---
date-published: 2015-04-10T00:00:00Z
title: Formal Methods at Amazon
uuid: 915cc2e8-d190-4d54-b746-0e1c34dff835
permalink: /posts/formal-methods-at-amazon.html
og-image: /images/selfies/2.jpg
type: post
---

I saw this paper being mentioned again and again in my Twitter
feed. Basically not even knowing what "formal methods" really means I
was intrigued by the claim that it's easy to read. And it has been.

The paper describes how Amazon used a specification language to
describe designs of complex concurrent fault tolerant systems finding
bugs and verifying changes in the process.

The specification language (TLA+) is not focus of the paper, rather
the authors concentrate on describing benefits, problems and the path
of adopting formal specification of system designs in an engineering
organization.

TLA+, stands for *Temporal Logic of Actions* and ["is especially well suited for writing high-level specifications of concurrent and distributed systems."](http://research.microsoft.com/en-us/um/people/lamport/tla/tla-intro.html)

Reading how they use it at Amazon I'm under the impression that it
works very similar to [generative testing](http://blog.8thlight.com/connor-mendenhall/2013/10/31/check-your-work.html)
dumping a ton of basically random (according to some rules) data into a system
and checking if the desired properties are maintained. Often the term
*"model checker"* is used.

Download the [original paper](http://research.microsoft.com/en-us/um/people/lamport/tla/amazon.html) or a copy of it [with some passages highlighted](/images/formal-methods-amazon.pdf) that I found particulary interesting.
