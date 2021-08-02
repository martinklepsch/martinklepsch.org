---
title: interactive design system docs
---
Having [worked on design systems](https://martinklepsch.org/100/gatheround.html) for a good amount of time I've also been thinking about design system documentation quite a bit. Storybook is great but it's also so intertwined with build tools that it's not really fun to target from ClojureScript. I know some people are doing it but they didn't seem too excited about it. 

We ended up just making yet another component which shows all our components. It's basic but it's good enough for our current needs. 

Now recently there's been quite a lot of interesting things happening around the [small clojure interpreter (sci)](https://github.com/borkdude/sci), which made me wonder if maybe it could also be used as tool to provide interactive design system / component documentation. 

And... it looks like it could be quite nice. `sci` makes it very easy to access any function in your ClojureScript code and after that you don't need much more than a small textarea with an example of using the component and a place to render it.

With a bit more work you could probably colocate the examples with the component via macros or metadata. Another macro could be used to fully expose specific namespaces.

Seems nice? 

```
(rum/defc Button < rum/static
  [attrs label]
  [:button attrs label])

(rum/defc ComponentViewer < rum/static
  []
  (let [[input set-input!] (rum/use-state "(ui/Button {:on-click #(js/alert :hello)} \"hello\")")
        [c set-c!] (rum/use-state nil)]
    (rum/use-effect!
     (fn []
       (set-c!
        (sci/eval-string input
                         {:classes {'js goog/global :allow :all}
                          ;; make the Button component available
                          :namespaces {'ui {'Button Button}}})))
     [input])
    [:div
     [:div c]
     [:textarea {:on-change #(set-input! (.. % -target -value))
                 :default-value input
                 :style {:font-family "monospace"}}]]))
```