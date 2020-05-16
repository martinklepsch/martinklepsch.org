---
title: Improved Support for Foreign Libs in cljdoc
--- 

Foreign libraries of ClojureScript libraries have always been a bit of an issue in cljdoc. With a namespace like the one below cljdoc would try to require `"react"` and then fail because `"react"` isn't a namespace it could find on the classpath. 

```clojure
(ns foo.bar
  (:require ["react" :as react]))
```

No more! After some recent work by [Fabien Rozar](https://github.com/frozar) the analyzer will now walk all files packaged with a library for `:require` forms like the one above and stub them out so that the ClojureScript analyzer thinks they exist.

In the end the [implementation](https://github.com/cljdoc/cljdoc-analyzer/pull/20/files) was less complex than I thought it would be. Which I guess is a testament to the thoughtfulness of the people contributing to the ClojureScript compiler.

### cljdoc-analyzer

All of this work builds on some long standing work by [Lee Read](https://github.com/lread) to provide a standalone analyzer to extract API information from Clojure & ClojureScript libraries. In many ways this is similar to the fantastic clj-kondo, except that it's focused more on full support of Clojure rather than speed. The analyzer cljdoc uses will actually load all your code so that even programatically created vars (often via macros) are returned properly.

[cljdoc-analyzer](https://github.com/cljdoc/cljdoc-analyzer) itself is a continuation of the work started by [codox](https://github.com/weavejester/codox) with some added bells and whistles, like more consistent output between Clojure and ClojureScript analysis results and automatic classpath construction based on a library's dependencies. The goal is that you can just run it on any library and get some API information (as EDN) in return. 

Thanks to Fabien and Lee for their work that made this all possible. I continue to be amazed by the people that come around to contribute to cljdoc. Fabien is from France, Lee is from Canada and unbeknownst to each other they basically shipped this together. Thank you!



