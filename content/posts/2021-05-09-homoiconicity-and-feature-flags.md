---
title: Homoiconicity & Feature Flags
date-published: 2021-05-09T22:09:30.032Z
permalink: /posts/homoiconicity-and-feature-flags.html
og-image: /images/selfies/10.jpg
uuid: a9a77281-eb62-472c-a08b-1e9935c8a9c2
published: true
type: post
---
At work we've been using feature flags to roll out various changes of the product. Most recently the rebrand from Icebreaker to [Gatheround](https://gatheround.com). This allowed us to continuously ship small pieces and review and improve these on their own pace without creating two vastly different branches of changes.

With the rebrand work in particular there were lots of places where we needed relatively small, local differentiations between the old and the new appearance. Oftentimes just applying a different set of classes to a DOM element. Less often, up to swapping entire components.

Overall this approach seemed to work really well and we shipped the rebrand without significant delays and at a level of quality that made everyone happy.
What we're left with now is some 250+ conditionals involving our `use-new-brand?` feature flag.

*This tells the story of how we got rid of those.*

## Introducing Homoiconicity

If you're well familiar with homoiconicity this may not be entirely new but for those who aren't: homoiconicity is the fancy word for when you can read your program as data. Among many other lisp/scheme languages Clojure is homoiconic:

```clojure
(doseq [n (range 10)]
  (println n))
```

The program above can be run but it can also be read as multiple nested lists:

```clojure
[doseq     [n [range 10]]    [println n]]
```

Now, if you know what I'm talking about you will see that I skipped over a small detail here, namely that the code above uses two types of parenthesis and that information got lost in this simplified array representation.

When doing it right (by differentiating between the two types of lists) we would end up with exactly the same representation as in the first code sample. And that is homoiconicity.

## Homoiconicity & Feature Flags

With this basic understanding of homoiconicity, lets take a look at what those feature flags looked like in practice:

```clojure
[:div
 {:class (if (config/use-new-brand?)
           "bg-new-brand typo-body"
           "bg-old-brand typo-large")}]
```

```clojure
(when (config/use-new-brand?)
  (icon/Icon {:name "conversation-color"
              :class "prxxs h3"}))
```

And so on. Now we have 250+ of those in our codebase but don't really plan on reversing that change any time soon... so we got to get rid of them. Fortunately Clojure is homoiconic and doing this is possible in a fashion that really tickles my brain in a nice way. 

## Code Rewriting

... isn't new of course, CircleCI famously [rewrote 14.000 lines of test code to use a new testing framework](https://circleci.com/blog/rewriting-your-test-suite-in-clojure-in-24-hours/). I'm sure many others have done similar stuff and this general idea also isn't limited to Clojure. Code rewriting tools exist in many language ecosystems. **But how easily you can do it in Clojure felt very empowering.** 

The next two sections will be about some 30 lines of code that got us there about 90% of the way.

## Babashka + rewrite-clj

[Babashka](https://babashka.org/) is a "fast, native Clojure scripting runtime". With Babashka you can work with the filesystem with shell-like abstractions, make http requests and much more. You can't use every Clojure library from Babashka but many useful ones are included right out of the box. 

One of the libraries that is included is [rewrite-clj](https://github.com/clj-commons/rewrite-clj). And, you guessed it, rewrite-clj helps you 🥁 ... rewrite Clojure/Script code. 

I hadn't used rewrite-clj before much am still a bit unfamiliar with it's API but after asking some questions on Slack [@borkdude](https://twitter.com/borkdude) (who also created Babashka) helped me out with an example of transforming conditionals that I then adapted for my specific situation.

I will not go into the code in detail here but if you're interested, I recorded [a short 4 minute video explaining it at a surface level and demonstrating my workflow](https://www.loom.com/share/70c1d3c45d9f45e9833344b5bd076813).

The rewriting logic showed in the video ignores many edge cases and isn't an attempt at an holistic tool to remove dead code branches but in our case this basic tool removed about 95% of the feature flag usages, leaving a mere 12 cases behind that used things like `cond->` or conjunctions.

Of the more than 230 feature flags that have been removed only about ten needed additional adjustments for indentation. This happened mostly when a feature-flag-using conditional wrapped multiple lines of code. Due to the locality of our changes that (fortunately) was relatively uncommon. If we had set up an automatic formatter for our code this also wouldn't have required any extra work.

## Onward

This has been an extremely satisfying project, if you can even call those 30 lines a "project". I hope you also learned something or found it helpful in other ways!

Thanks to [Michiel "borkdude" Borkent](https://github.com/sponsors/borkdude) for all his work on Babashka. The interactive development workflow shown in [the video](https://www.loom.com/share/70c1d3c45d9f45e9833344b5bd076813) paired with blazing startup times and a rich ecosystem makes it feel like there is a lot of potential still to be uncovered.

I'd also like to thank [Lee Read](https://github.com/lread), who has done such an amazing job making rewrite-clj ready for more platforms like ClojureScript and Babashka as well as making sure it's future-proof by adding more tests and fixing many long standing bugs.

After writing this blog post and detailing the beginnings of this idea I also took a bit more time **[to clean up the code and put it on GitHub](https://github.com/martinklepsch/prune-feature-flags.clj)**.

If you thought this was interesting, consider [following me on Twitter](https://twitter.com/martinklepsch)!