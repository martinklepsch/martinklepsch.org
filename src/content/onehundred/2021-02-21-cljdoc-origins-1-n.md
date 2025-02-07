---
title: cljdoc origins 1/n
date-published: 2021-02-21T09:57:29.870Z
uuid: 7d40d366-4fb0-44b0-a09e-baedb1ee9165
og-image: /images/selfies/2.jpg
type: onehundred
slug: cljdoc-origins-1-n
---
It was one of these cold and dark December evenings in 2018. Like every month [Ben](https://twitter.com/socksy) and [Paulus](https://twitter.com/pesterhazy) hosted the Clojure meetup on the second Wednesday of the month. This time it was at Thoughtworks, and I believe there was pizza. Some 50 people were about to attend the meetup that ignited a spark, which eventually led to cljdoc. 

First, [Oli](https://www.eidel.io/talks) gave a talk on targeting WebGL from a re-frame app. At work he had been working on this quite complex UI to annotate radiology scans so that it could be tested if the resulting data could be used to aid doctors in their diagnostic work. It was his first talk ever and everyone was pumped. 

A couple of pizza slices later Daniel showed off a new \`unrepl\` based Emacs integration that allowed [multimedia objects](https://twitter.com/volrath/status/941342874372894720) and a bunch of other cool things that at the time made people quite hyped about the broader "REPL renaissance" at the time. 

So it was a good meetup. Plenty of good vibes and people seemed to enjoy what Ben and Paulus had organized. As every so often there was a third slot that was still up for grabs and often someone would step up last minute and share something they felt was worth sharing. On this day Arne took the opportunity. 

Arne presented a little script he wrote, called \`autodoc\`. At the time the primary way library authors published API documentation for their Clojure libraries was \`codox\`, but the way most people used it required manual updating and often the documentation would lag behind the most recent release. Arne's script was aiming at simplifying that process and helping people to keep their docs in sync. But besides all this Arne also showed some cool stuff from the Elixir community, namely [hexdocs.pm](https://hexdocs.pm/). Hexdocs is a hosted platform for Elixir library documentation, much like what cljdoc is now. 

Chatting with a bunch of people after Arne's talk I more and more thought "wow, this would be so amazing to have for Clojure". 

A few days later Oli initiated a [conversation on ClojureVerse](https://clojureverse.org/t/creating-a-central-documentation-repository-website-codox-complications/1287/4) and the ball started rolling. I became aware of related projects like Reid's [grimoire](https://github.com/clojure-grimoire/grimoire) and started to work on some early prototypes. Grimoire ended up becoming the storage layer for early versions of cljdoc. 

A month later, on the second Wednesday of January, the Berlin Clojure community gathered again. This time at Acrolynx and this time it was only lightning talks. Excited about the prospect of a "hexdocs for Clojure" I prepared a [tiny presentation](https://clojureverse.org/uploads/default/original/1X/3ecba3a15c85783f14044da7b8be57020304ecce.pdf) painting in broad strokes what I hoped we could build.