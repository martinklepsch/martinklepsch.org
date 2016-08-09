---
layout: post
date-published: 2016-05-19
title: Props, Children & Component Lifecycle in Reagent
uuid: bdabdf1d-742c-468f-89bb-032986a9d99f
---

Every now and then I come across the situation that I need to compare
previous and next props passed to a Reagent component. Every time
again I fail to find some docs and figure it out by trial and error.

## Props vs. Children

In React **everything** passed to a component is called `props`. Children passed to components are passed as `props.children`. In Reagent things are a bit different and Reagent’s hiccup syntax doesn’t explicitly separate the two:

```clojure
;; configuration and one child
[popup {:style :alert} [delete-confirmation]]
;; two children
[popup [alert-icon] [delete-confirmation]]
```

```xml
<Popup style="alert"><DeleteConfirmation></Popup>
```

In React it is well-defined where you can access the `style` parameter (`props.style`) and how you can access the passed children (`props.children`). 

In Reagent things are a bit different: you have a function definition which takes a number of arguments which you can just refer to in the same way you can refer to any other function parameter. This makes thinking in functions a lot easier but also overshadows some of the underlying React behaviour. 

In a lifecycle handler like `:component-did-update` accessing component arguments via the symbol they’ve been given in the functions argument vector doesn’t work:

The moment you define components that are not simple render functions (remember those [Form-2 and Form-3](https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components) components?) all updates will pass their arguments to the components render function. 

The moment you render a component that has been created via `reagent.core/create-class` all updates will pass their arguments to the `:reagent-render` function, potentially triggering a re-render. The function that returned the result of `create-class` is only ever called once at the time of mounting the component — your top-level `defn` returns a component instead of being a render function itself. This is also [why you need to repeat the arguments in the `:reagent-render` arguments](https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components#form-2--a-function-returning-a-function).

## Props in Lifecycle Handlers

Now how do we get access to these props in a lifecycle handler? The quick answer is, we use `reagent.core/props` — obvious, huh?

One peculiarity about the `props` function is that it expects the `props` data to be the first argument to your function. Also it **has to be a map** (if it’s not `props` returns `nil`).

If the first argument to your component is not a map all arguments are interpreted as children and can be retrieved via `reagent.core/children`.

So now we have the props for the current render, how do we access the previous ones? All previously passed arguments are passed to the lifecycle handler. Not as you might think though.

If you have a component that has a signature like this:

```clojure
(defn my-comp [my-props more] …)
```

You can access it’s previously passed arguments like this:

```clojure
:component-did-update (fn [comp [_ prev-props prev-more]] …))
```

`comp` is a reference to the current component. The second argument which is being destructured here contains what we’re looking for. As far as I understood the first item is the components constructor. The rest are the previously rendered inputs (again in React they’re all `props`, in Reagent they’re `props` and `children`).

As you can see you can inspect all previous arguments to a component. The way you access them differs from the default React lifecycle method signatures so hopefully this post helps to clear up some confusion about this stuff. :)

<aside>Thanks to Jonas Enlund for reading a draft of this post and to Mike Thompson for his excellent Re-frame/Reagent docs.</aside>