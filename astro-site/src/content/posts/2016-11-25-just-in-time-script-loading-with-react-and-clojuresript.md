---
date-published: 2016-11-25T00:00:00Z
title: Just-in-Time Script Loading With React And ClojureScript
uuid: 21eecbc4-1be6-4930-89ae-9c36c69e0a16
permalink: /posts/just-in-time-script-loading-with-react-and-clojuresript.html
og-image: /images/selfies/3.jpg
type: post
---

In the last projects I've been working on I've come accross the situation that I needed to load some external script (Stripe, Google Maps, ...) at a certain point and then do something with the features exposed by this newly imported library. Some times you might be able to circumvent loading a library at runtime by bundling it with your main application but even then you might want to consider splitting it into a separate module and loading it when it's actually needed.

We won't talk about module splitting and loading in this blog post though and instead focus on loading things like Stripe and Google Maps that just can't be bundled with your application.

The easy way to load these would be using a simple script tag:

```html
<script type="text/javascript" src="https://js.stripe.com/v2/"></script>
```

With this approach however you load the script for every user even though they may never, or already went through, your payment flow. A better way would be to load it when the user actually wants to pay you. I've heard fast loading apps make that more likely as well ;) Also you might say that these scripts could be cached, but even if they are: you still pay for the parsing and execution time.

So how can we go about that? What follows is one pattern that I think is fairly simple and elegant and also a nice use of React's lifecycle features and higher-order components:

```clojure
(ns your-app.lib.reagent
  (:require [reagent.core :as reagent]
            [goog.net.jsloader :as jsl]))

(defn filter-loaded [scripts]
  (reduce (fn [acc [loaded? src]]
            (if (loaded?) acc (conj acc src)))
          []
          scripts))

(defn js-loader
  "Load a supplied list of Javascript files and render a component
   during loading and another component as soon as every script is
   loaded.

   Arg map: {:scripts {loaded-test-fn src}
             :loading component
             :loaded component}"
  [{:keys [scripts loading loaded]}]
  (let [loaded? (reagent/atom false)]
    (reagent/create-class
     {:component-did-mount (fn [_]
                             (let [not-loaded (clj->js (filter-loaded scripts))]
                               (.then (jsl/loadMany not-loaded)
                                      #(do (js/console.info "Loaded:" not-loaded)
                                           (reset! loaded? true)))))
      :reagent-render (fn [{:keys [scripts loading loaded]}]
                        (if @loaded? loaded loading))})))
```

And here's how you can use it:

```clojure
;; payment-form can expect `js/Stripe` to be defined
[js-loader {:scripts {#(exists? js/Stripe) "https://js.stripe.com/v2/"}
            :loading [:div "Loading..."]
            :loaded [payment-form]}]
```

So, what can we take away from this besides the specific snippets above?

- Higher order components can be very useful to hide away side effects needed for your views to function
- They also are perfectly reusable
- You can of course also use higher order components to pass things into child components, we don't do that here but if you create some stateful object this may come in handy

Hope this is helpful — let me know if you have any thoughts or suggestions :)