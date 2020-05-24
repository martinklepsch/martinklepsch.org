---
title: Promises in a ClojureScript REPL
date-published: 2020-05-24T11:09:30.032Z
uuid: 7e85ecb2-3033-493b-81f5-8b27cef7574c
og-image: /images/selfies/3.jpg
permalink: /posts/working-with-promises-in-clojurescript-repls.html
published: true
---

[Roman](https://twitter.com/roman01la) wrote a nice post on working inside ClojureScript REPLs, also touching on [how to deal with promises](https://gist.github.com/roman01la/b939e4f2341fc2f931e34a941aba4e15#repl--asynchrony). If you're unfamiliar, the problem is that in Javascript many operations return promises and unlike in Clojure you cannot block until the promise is resolved. Instead you _have to_ handle the resulting value asynchronously. So if you for instance use `fetch` that could look something like this:

    (.then (js/fetch "https://jsonip.com/") prn)
    
This will use `prn` to print the value of the resolved promise. Sometimes you don't just want to print things though, the real power of a REPL lies in reusing values and successively building up just the shape of data you need.

One nice trick I learned from Sean Grove years ago is that you can just use `def`. This isn't something you'd do in production code but it's zero-ceremony and very handy to capture values.

    (.then (js/fetch "https://jsonip.com/") #(def -r %))
    
After this you can evaluate the `-r` symbol in your REPL and it will give you the value of the `fetch` promise. Alternatively to `def` we could also use an `atom` to store the return value.
    
    (def s (atom nil))
    (.then (js/fetch "https://jsonip.com/") #(reset! s %))

### Ergonomics

Now that we know how we can access the resulting value of a promise, let's make it convenient. For convenience I basically want two things:

- Make it easy to wrap any promise-returning form to capture it's return value
- Make it easy to access the return values of multiple promises

What I came up with is a function I just named `t` which can be used like this:

    
    (let [s (atom {})]
      (defn t
        ([kw] (get @s kw))
        ([p kw] (.then p (fn [r] (swap! s assoc kw r) r)))))
        
    (-> (js/fetch "https://jsonip.com/")
        (t :jsonip))

When `t` receives two arguments it will consider the first argument a promise storing the resulting value in an atom under the key provided as the second argument, `:jsonip` in this case. 

This API is particularly nice when you consider that most editor integrations provide the ability to evaluate the form around your cursor. If I place my cursor within `(t :jsonip)` and evaluate this form I can look at the value the promise returned without changing any of the code. I can also just continue chaining with `then` since `t` returns the original promise. 

Another nice feature is that I can reuse the values for future REPL evaluations by referring to them using forms like `(t :jsonip)`.

Obviously **this is just one way** but I liked how that simple 4 line function made working with promises in a REPL a lot more enjoyable.
