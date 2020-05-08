# This is my blog

This blog is built using Clojure, [Boot][boot-clj] & [perun][perun]. To get an
overview you can take a look at the `build.boot` file. Templates are
defined as functions returning hiccup in `src/org/martinklepsch/blog.clj`.

Testing prose.io.

### Common workflows

Building the blog:
```
boot build
```

Continously build blog and serve on localhost:3000:
```
boot dev
```

Build and deploy site:
```
boot build deploy
```
**Note:** This requires the two env vars `S3_ACCESS_KEY` & `S3_SECRET_KEY` to be set.

[boot-clj]: http://boot-clj.com/
[perun]: https://github.com/hashobject/perun
