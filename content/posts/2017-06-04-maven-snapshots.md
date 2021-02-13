---
layout: post
date-published: 2017-06-04T00:00:00Z
title: Maven Snapshots
uuid: 443e70e4-c49d-4391-ac18-bf478b8e2955
permalink: /posts/maven-snapshots.html
og-image: /images/selfies/3.jpg
---

## Or: How to use Maven snapshots without setting your hair on fire.

Ever depended on a Clojure library with a version that ended in
`-SNAPSHOT`? That's what's called a Maven snapshot.

Maven snapshots are a handy tool to provide pre-release builds to
those who are interested. In contrast to proper releases a SNAPSHOT
release can be "updated". And that's where the trouble comes in.

Let's say you depend on a snapshot because it contains a fix you
recently contributed to your favorite open source project. A week
later another fix is added and released under the same
`0.1.0-SNAPSHOT` version.

Now it turns out that second fix contained a minor bug. No big deal,
it's a pre-release after all. The problem with all this however is
that you (Maven) will automatically use the new SNAPSHOT, no action required.
A dependency you use in your project **changes without you being aware**
of it. Suddenly stuff breaks. You wonder what happened. Did you change
anything? No? Frustration ensues.

Because of this for a long time I thought SNAPSHOTS are evil and
instead of using them library authors should release development
builds with a qualifier like `0.1.0-alpha1`.
I still think this is a good practice and try to adhere to it myself
as much as possible.

## In the meantime there is another way to safely depend on Maven snapshots though.

Whenever you push a SNAPSHOT version to a Maven repository (like
Clojars) it does not actually overwrite the previously uploaded jar
but creates a separate jar with a version like this:
`0.1.0-20170301.173959-4`. Once the upload is complete it merely
changes the SNAPSHOT version to point to that release. All previous
releases are still available (by default Maven repos only keep the latest
SNAPSHOT version but Clojars keeps them all).

This means instead of depending on a **mutable** version you can now
depend on an **immutable** version. Oh do we love immutability.

```
[group-id/project-id "0.1.0-20170301.173959-4"]
```

Finding these version identifiers isn't the easiest thing but basically:

1. you go to the page of a jar on Clojars, e.g. [adzerk/boot-cljs](https://clojars.org/adzerk/boot-cljs/)
2. in the sidebar that lists recent versions, click "Show All Versions"
3. [versions page](https://clojars.org/adzerk/boot-cljs/versions)
  you can find a note at the bottom that leads you to the [Maven repository](https://repo.clojars.org/adzerk/boot-cljs/)
4. if you click on a SNAPSHOT version there you get to a page that [lists all the stable identifiers for that version](https://repo.clojars.org/adzerk/boot-cljs/2.0.0-SNAPSHOT/)

To get to the Maven repo page directly you can also just put a `repo.` subdomain in front of a given Clojars project url:

```
https://clojars.org/adzerk/boot-cljs/
https://repo.clojars.org/adzerk/boot-cljs/
        ^^^^
```

And they depend on SNAPSHOTs happily ever after.


