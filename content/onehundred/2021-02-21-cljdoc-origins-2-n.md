---
title: cljdoc origins 2/n
date-published: 2021-02-21T12:17:05.266Z
uuid: 183d3761-d354-4361-b903-e4e4d9ee9717
og-image: /images/selfies/1.jpg
type: onehundred
permalink: /100/cljdoc-origins-2-n.html
---
Okay, surfing [is done](https://twitter.com/martinklepsch/status/1363428911422521346?s=20). Back to January 2018. As [I said](https://martinklepsch.org/100/cljdoc-origins-1-n.html) I created a [small presentation](https://clojureverse.org/uploads/default/original/1X/3ecba3a15c85783f14044da7b8be57020304ecce.pdf) and shared it, full of excitement, with the attendees of that January Clojure Berlin meetup. At the time I had a prototype that analyzed a jar, and created a super basic page listing vars in the available namespaces.

![The very first namespace with some vars being listed ](https://martinklepsch.org/images/uploads/screen-shot-2018-01-11-at-22.31.01.png "cljdoc v0")

At that time I had maybe sunk a dozen hours or so into this project. I was so excited. So excited to just share the vision of the project but also quite excited to potentially recruit some people to join me.

I don't remember all the interactions I had after that meetup but I do remember one.

One of the core problems of this project was always to analyze Clojure code without opening up a bunch of security holes. Anything can happen when requiring a Clojure namespace. There's no guarantee that it won't start deleting random files or mining bitcoin.

At that January meetup I didn't yet have a good answer for this problem and basically asked the room if they had ideas or might even be able to help with this directly.

And then there was this one person. I was being informed how very difficult this is and that there's companies whose whole business builds around detecting abuse like this. What I was trying to do was basically a futile effort. Not the greatest vibe when you're sharing an early idea but — surprisingly — it did kind of work out for me in the end.

In the coming days I was all about just finding a way to be able to say: "Look, the impossible just happened!"

A twisted part of me just wanted to prove to that person that they're not going to shoot this down. And you know, probably it wasn't even their intention.

As you might guess I didn't magically solve all the problems. But I found a kind of hack that just avoided the problem all together. I used CircleCI's build infrastructure, queued jobs with different parameters via their API and collected the results as a file that was stored with the build as a build artifact. [40.000+ builds later](https://cljdoc.org/builds) this is still going strong as ever.

After figuring this out it felt like the rest was within the boundaries of what I've done in the past. Even though I've only been hacking on this for a couple of weeks it felt like the rest would be like riding down a hill. It turned out to be a pretty long slope but I think I didn't quite realize that at this point.

Anyways, time to call it a night and get some food. Take care of yourselves ☺️
