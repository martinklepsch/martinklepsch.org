---
date-published: 2015-01-05T00:00:00Z
title: CLJSJS - Use Javascript Libraries in Clojurescript With Ease
uuid: 99353e70-0080-454a-825d-bb85f8398ae4
og-image: /images/selfies/3.jpg
type: post
slug: cljsjs-use-javascript-libraries-in-clojurescript
---
In Clojure, Java interoperability or “interop” is a core feature. In
Clojurescript, interop with Javascript libraries does not work
out-of-the-box across optimization modes. Extern files or “externs”
required for advanced optimizations are often hard to find.

To fix this a few newly found friends and I created **[CLJSJS](http://cljsjs.github.io)**.
CLJSJS is an effort to package Javascript libraries with their respective
extern files and provide tools to integrate them into your project.

My personal hope is that this will make it easier for newcomers to get
started with Clojurescript.

Also existing solutions like `deps.clj` ([more here](https://groups.google.com/forum/#!msg/clojurescript/LtFMDxc5D00/hMR6BcfMMAMJ)) only
address the problem of Javascript dependencies partially. Maybe CLJSJS
can serve as a vehicle to find some "pseudo-standard" for this kind of
stuff.

Thanks to Juho Teperi, Micha Niskin & Alan Dipert for their
contributions and ideas so far. **Now go and check out the [project homepage](http://cljsjs.github.io) or jump straight into the [packages repo](https://github.com/cljsjs/packages) and learn how you can contribute.**

*Announcement post and discussion on the [Clojurescript mailinglist](https://groups.google.com/forum/#!topic/clojurescript/qhFNVEeNCbc)*
