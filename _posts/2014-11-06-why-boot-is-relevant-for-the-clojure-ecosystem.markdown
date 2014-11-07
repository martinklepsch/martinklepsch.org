---
layout: post
title: Why Boot is Relevant For The Clojure Ecosystem
---
Boot is a build system for Clojure projects. It roughly competes
in the same area as Leiningen but Boot's new version brings some
interesting features to the table that make it an alternative
worth assessing.

<aside>
If you don't know what Boot is I recommend reading this post by one of
Boot's authors first: <a
href="http://adzerk.com/blog/2014/11/clojurescript-builds-rebooted/">Clojurescript
Builds, Rebooted</a>.
</aside>

## Compose Build Steps

If you've used Leiningen for more than packaging jars and uberjars
you likely came across plugins like `lein-cljsbuild` or
`lein-garden`, both compile your stuff into a target format (i.e. JS, CSS).
Now if you want to run both of these tasks at the same time — which
you probably want during development — you have two options: either
you open two terminals and start them separately or you fall back to
something like below that you run in a `dev` profile (this is how it's
done in [Chestnut](https://github.com/plexus/chestnut)):

{% highlight clojure %}
(defn start-garden []
  (future
    (print "Starting Garden.\n")
    (lein/-main ["garden" "auto"])))
{% endhighlight %}

Now there are issues with both of these options in my opinion. Opening
two terminals to initiate your development environment is just not
very user friendly and putting code related to building the project
into your codebase is boilerplate that unnecessarily can cause trouble
by getting outdated.

What Boot allows developers to do is to write small composable tasks.
These work somewhat similar to stateful transducers and ring middleware
in that you can just combine them with regular function composition.

### A Quick Example

Playing around with Boot, I tried to write a task. To test this task
in an actual project I needed to install it into my local repository
(in Leiningen: `lein install`).  Knowing that I'd need to reinstall
the task constantly as I change it I was looking for something like
Leiningen's Checkouts so I don't have to re-install after every
change.

Turns out Boot can solve this problem in a very different way
that illustrates the composing mechanism nicely. Boot defines a
bunch of
[built-in tasks](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj)
that help with packaging and installing a jar: `pom`, `add-src`, `jar`
& `install`.

We could call all of these these on the command line as follows:

```
boot pom add-src jar install
```

Because we're lazy we'll define it as a task in our project's
`build.boot` file. (Command-line task and their arguments are
symmetric to their Clojure counterparts.)

{% highlight clojure %}
(require '[boot.core          :refer [deftask]]
         '[boot.task.built-in :refer [pom add-src jar install]])

(deftask build-jar
  "Build jar and install to local repo."
  []
  (comp (pom) (add-src) (jar) (install)))
{% endhighlight %}

Now `boot build-jar` is roughly equivalent to `lein install`. To have
any changes directly reflected on our classpath we can just compose
our newly written `build-jar` task with another task from the
repertoire of built-in tasks: `watch`. The `watch`-task observes the
file system for changes and initiates a new build cycle when they
occur:

```
boot watch build-jar
```

With that command we just composed our already composed task with
another task. **Look at that cohesion!**

<aside>I'm not familiar enough with Leiningen Checkouts to say with
confidence if this is identical behavior but for the majority of cases it'll
probably work.</aside>

## There Are Side-Effects Everwhere!

Is one concern that has been raised about Boot. Leiningen is
beautifully declarative. It's one immutable map that describes your
whole project. Boot on the other hand looks a bit different.  A usual
boot file might contain a bunch of side-effectful functions and in
general it's much more a program than it is data.

I understand that this might seem like a step back at first sight, in
fact I looked at it with confusion as well. There are some problems
with Leiningen though that are probably hard to work out in
Leiningen's declarative manner (think back to
[running multiple `lein X auto` commands](https://github.com/technomancy/leiningen/issues/1752).

Looking at Boot's code it becomes apparent that the authors spent a
great deal of time on isolating the side effects that might occur in
various build steps. I recommend reading the
[comments on this Hacker News thread](https://news.ycombinator.com/item?id=8553189)
for more information on that.

## When To Use Boot, When To Use Leiningen

Boot is a build tool. That said it's task composition features only
get to shine when multiple build steps are involved. If you're
developing a library I'm really not going to try to convince you to
switch to Boot.  Leiningen works great for that and is, I'd assume,
more stable than Boot.

If you however develop an application that requires various build
steps (like Clojurescript, Garden, live reloading, browser-repl) you
should totally check out Boot. There are tasks for all of the above
mentioned: [Clojurescript](https://github.com/adzerk/boot-cljs),
[Clojurescript REPL](https://github.com/adzerk/boot-cljs-repl),
[Garden](https://github.com/martinklepsch/boot-garden),
[live reloading](https://github.com/adzerk/boot-reload). I wrote the
Garden task and writing tasks is not hard once you have a basic
understanding of Boot.

If you need help or have questions join the
[#hoplon channel on freenode IRC](http://webchat.freenode.net/?channels=hoplon).
I'll try to help and if I can't Alan or Micha, the authors of Boot,
probably can.
