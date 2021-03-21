---
title: 'Clojure Macros: Creating vars from a map'
permalink: /posts/clojure-macro-magic-vars-from-map.html
date-published: 2021-03-21T12:18:42.591Z
uuid: b2865f91-de5b-41f5-9632-6a3055b8e93d
og-image: /images/selfies/2.jpg
type: post
---
The other day I was looking for a way to turn a map into a bunch of vars. I know a macro is what will get the job done but I write macros so rarely that it always takes me a while to figure it out. In the end I ended up with something like this:

```
(defmacro def-all [m]
  (->> (for [[n v] m]
         `(def ~(symbol n) ~v))
       (into [])))
```

Using `macroexpand` you can see that this translates to the a bunch of `def` calls in a vector:

```
user=> (macroexpand '(def-all {:a 1 :b 2}))
[(def a 1) (def b 2)]
```

Like myself, you may wonder why the vector is needed. The issue is that `for` will return a list and that would result in the macro emitting the following Clojure code:

```
((def a 1) (def b 2))
```

After evaluating the two inner `def` forms, this will result in another function call where the return value of the first `def` is used as a function. Depending on what you are defining this may fail or lead to unexpected behavior.

After sharing my solution using `(into [])` in the [Clojurians Slack](https://clojurians.net/) I was made aware that instead if turning the thing into a vector you can also just prepend a do into that list, resulting in code that feels slightly more aligned with my intention:

```
(defmacro def-all [m]
  (->> (for [[n v] m]
         `(def ~(symbol n) ~v))
       (cons 'do)))

(macroexpand '(def-all {:a 1 :b 2}))
; returns
(do (def a 1) (def b 2))
```

I realize this is a super basic macro but I can totally see how that might be useful to people starting to write their own macros. If you're looking for a more full-fledged guide, [Clojure for the Brave and True](https://www.braveclojure.com/writing-macros/) got you covered.

Thanks to Justin Smith for sharing his experience on Slack with me so many times.