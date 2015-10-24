---
layout: post
date-published: 2015-08-11
title: Parameterizing ClojureScript Builds
---
Just like with most server side software we often want to make minor
changes to the behaviour of the code depending on the environment it's
run in. This post highlights language and compiler features of ClojureScript
making parameterized builds easy peasy.

On servers environment variables are a go-to solution to set things
like a database URI. In ClojureScript we don't have access to those.
You can work around that with macros and emit code based on environment
variables but this requires additional code and separate tools.

With ClojureScript 1.7.48 (<strong>Update:</strong> There was a bug in 1.7.48
`goog-define`. Use 1.7.107 instead.) a new macro `goog-define` has
been added which allows build customization at compile time using
plain compiler options. Let's walk through an example:

    (ns your.app)
    (goog-define api-uri "http://your.api.com")

`goog-define` emits code that looks something like this:

    /** @define {string} */
    goog.define("your.app.api_uri","http://your.api.com");

The `goog.define` function from Closure's standard library plus the JSDoc
`@define` annotation tell the Closure compiler that `your.app.api_uri`
is a constant that can be overridden at compile time.  To do so you
just need to pass the appropriate `:closure-defines` compiler option:

    :closure-defines {'your.app/api-uri "http://your-dev.api.com"}

**Note:** When using Leinigen quoting is implicit so there is no quote
  necessary before the symbol.

<aside>
Prior to 1.7.48 you could annotate things with <code>@define</code> but without
using <code>goog.define</code> overriding those defines is not possible when
using optimizations <code>:none</code> effectively making them much less useful.
</aside>

### Under the hood

When compiling with `:advanced` optimizations the Closure compiler will
automatically replace all occurrences of your defined constants with their
respective values. If this leads to unreachable branches in your code they
will be removed as [dead code](https://developers.google.com/closure/compiler/docs/compilation_levels?hl=en#advanced_optimizations)
by the Closure compiler. Very handy to elide things like logging!

Without any optimizations (`:none`) `goog.define` makes sure the right
value is used. There are two global variables it takes into account
for that: `CLOSURE_UNCOMPILED_DEFINES` and `CLOSURE_DEFINES`. When you
override the default value using `:closure-defines` the ClojureScript
compiler prepends `CLOSURE_UNCOMPILED_DEFINES` with your overridden
define to your build causing `goog.define` to use the value in there
instead of the default value you defined in your source files.

For details see
[the source of goog.define](http://google.github.io/closure-library/api/source/closure/goog/base.js.src.html#l147).
