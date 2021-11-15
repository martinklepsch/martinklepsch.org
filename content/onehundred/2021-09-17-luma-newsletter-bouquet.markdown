---
title: Luma Newsletter No. 1
---

> I'm not really sure why but in the spirit of exploration I started a kind of irregular newsletter. You can read [the first edition here](https://lu.ma/p/MnWMR5rLJHDcWL5/Martin-says-Welcome) and it's also in full below. Since I started this project here of writing 100 things I decided to be generous with myself and I'll also count any newsletters I'm sending.

Hello and welcome dear friends,

you probably subscribed to this list about a month ago when [I mentioned on Twitter](https://twitter.com/martinklepsch/status/1428333611992993795) that I'd like to do a stream during which I try to build and ship a small ClojureScript app including a backend and database. Time has come: I'll stream this on Twitch next Tuesday 11AM Berlin time. You can register for it [here](https://lu.ma/hackers-cljs).

What follows is a random assortment of things™

Subscription layers (and time)
-------------------------------

I've been thinking about subscription layers in frontend apps quite a bit. In an earlier library of mine ([derivatives](https://github.com/martinklepsch/derivatives)) I essentially followed a "watch-based approach", meaning a "derivative" would recompute whenever any of it's inputs change. Now in the projects I work on the changes or often much more granular and suitable for a more reduce-based approach that processes individual change events.

This reduce-based approach is something I'm intending to explore more.

Another interesting challenge I've come across is that sometimes I have an entity with a state like `active?` that really depends on the current time. Most subscription layers don't really expose anything to ensure that the subscription is recomputed the moment `active? `should change. The best idea I have right now is to schedule a "touch" event that forces a rebuild of the subscription but I haven't really explored an implementation of that yet.

Cycle time
-----------

Yesterday I listened to [The REPL with Paulus Esterhazy](https://www.therepl.net/episodes/40/) and it made me once again aware of the importance of cycle time in software development. One of the core metrics Paulus mentions is the time between first commit on a feature branch to production deploy.

A lot of the conversation reminded me of [Theory of Constraints](https://www.notion.so/rostnl/Systems-Thinking-Notes-Resources-2871c456284b47388b7a76d47521038c#2c3b8ac9282848f9b5506134c38202d9) (some notes by Robert Stuttaford) and how to optimize for throughput you have to tackle bottlenecks first.

I'm hoping that over the next months I can explore this some more and put some metrics in place for the product team at [Gatheround](https://gatheround.com/).

Retrospectives
---------------

Through the above podcast I also discovered the podcast by GeePaw Hill and in particular the one about how for retrospectives, [variety is key](https://www.geepawhill.org/2020/08/04/retrospectives-variety-is-key/). I've been running [POPCORN Flow](https://www.youtube.com/watch?v=cqtxMy58kz8)-inspired retros with our engineering team for a while now but I'm occasionally missing a bit of energy in the room (zoom) and so this podcast was a welcome invitation to experiment with more different format and cycle hosts.

> The goal here: decenter ourselves just the right amount for dynamic, creative discourse about who & how we were --- and who & how we wish to be.

Can trees talk?
----------------

Last but not least a video by the amazing Real Science YouTube channel about [how trees communicate using fungal networks](https://www.youtube.com/watch?v=9HiADisBfQ0). It goes into the details behind various experiments that were used to prove that some information flow is happening between trees.

In one of the experiments one of three plants was infected with a pest and the two other started producing enzymes to defend against the pest within 6 hours. But check it out yourself, it's pretty cool!

Ok, that's it. Happy Friday y'all.
