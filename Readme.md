# This is my blog

This blog is built using Clojure, [Boot][boot-clj] & [perun][perun]. To get an
overview you can take a look at the `build.boot` file. Templates are
defined as functions returning hiccup in `src/org/martinklepsch/blog.clj`.

### Dependencies

- `filterdiff` from [patchutils](http://cyberelk.net/tim/software/patchutils/)
- `bb` from [babashka](https://github.com/borkdude/babashka)
- `bootleg` from [bootleg](https://github.com/retrogradeorbit/bootleg)
- `boot` from [boot-clj](https://github.com/boot-clj/boot)

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


### Rebuild

- github action to commit back to repo: https://github.com/marketplace/actions/add-commit

- [ ] rebuild rss https://epiccastle.io/blog/generating-xml-with-bootleg/
- [x] deploy from directory instead of boot fileset via basic `clj` invocation
- [x] github action to build site and commit it
- [x] commit generated site to Git repo
- [x] github action to add uuid
- [ ] Randomly Chosen portraits as small opengraph Image
- [ ] Integrate snippet of text into opengraph view
- [ ] Unstyled archive page
- [x] Put permalinks into metadata to make it final
- [x] Don’t do redirects, just maintain URLs of old posts
- [x] GitHub action to add permalink if missing
- [ ] Drafts are published at their UUID location with metadata to not be indexed
- [ ] https://pushover.net for visibility
- [ ] There could be a hidden edit button leading to prose.io
- [ ] A preprocessing step could turn raw links to YouTube or twitter into more useful embeds/views
