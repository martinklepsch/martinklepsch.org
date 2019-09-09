---
layout: post
date-published: 2019-09-09
title: Working with Firebase Documents in ClojureScript
uuid: 3e616db0-a417-4bc4-93d0-b2a24256ab86
---

In a project [I’m currently working on](https://icebreaker.video) we’re making use of Google's [Firebase](https://firebase.google.com) to store domain data and run cloud functions.

In Firestore, which is Firebase’s database offering, every document is essentially a Javascript object. While interop in ClojureScript is pretty good we ended up converting the raw data of these documents to ClojureScript data structures using `js->clj`. This also meant we’d need to convert them back to JS objects before writing them to Firestore.

Because IDs are technically not part of the document the project adopted a pattern of representing documents as tuples:

```clj
[id (js->clj firestore-data)]
```

This works but isn’t particularly extensible. What if we also wanted to retain the “Firestore Reference” specifying a documents location inside the database? (Firestore stores data in a tree-like structure.)

It also leads to some funky gymnastics when working with collections of documents:

```clj
(sort-by (comp :join_dt second) list-of-document-tuples)
```

Could be worse... but also could be better.

This blogpost will compare various approaches approach to address the problems above using [cljs-bean](https://github.com/mfikes/cljs-bean), basic ClojureScript data structures, custom protocols and `:extend-via-metadata`.

## cljs-bean
With the recent release of [cljs-bean](https://github.com/mfikes/cljs-bean) we have an interesting alternative to `js->clj`. Instead of eagerly walking the structure and converting all values to their ClojureScript counterparts (i.e. persistent data structures) the original object is wrapped in a thin layer that allows us to use it as if it were a ClojureScript-native data structure:

```clj
(require '[cljs-bean.core :as cljs-bean])

(-> (cljs-bean/bean #js {"some_data" 1, :b 2})
    (get :some_data)) ; => 1
```

Given a Firestore [QueryDocumentSnapshot](https://firebase.google.com/docs/reference/js/firebase.firestore.QueryDocumentSnapshot) we can make the JS object representing the data easily accessible from ClojureScript:

```clj
(-> (cljs-bean/->clj (.data query-document-snapshot))
    (get :some_field))

;; (cljs-bean/->clj data) is roughly the same as
;; (cljs-bean/bean data :recursive true)
```

The bean is immutable and can be used in client side app-state as if it is one of ClojureScript’s persistent data structures.

**Caveat:** Updating a bean using `assoc` or similar will create a copy of the object (Copy-on-Write). This is less performant and more GC intensive than with persistent data structures. Given that the data is usually quite small and that the document representations in our app state mostly aren’t written to directly this is probably ok ([cljs-bean #72](https://github.com/mfikes/cljs-bean/issues/72)).

Whenever we want to use the raw object to update data in Firestore we can simply call `->js` on the bean. Conveniently this will fall back to `clj->js` when called on ClojureScript data structures.

```clj
(.set some-ref (cljs-bean/->js our-bean))
```

Arguably the differences to using plain `clj->js` aren’t monumental but working with a database representing data as JS objects it is nice to retain those original objects.

## Integrating Firestore Metadata

Now we got beans. But they still don’t contain the document ID or reference. In most places we don’t care about a documents ID or reference. So how could we enable the code below while retaining ID and reference?

```clj
(sort-by :join_dt participants)
```

Let’s compare the various options we have.


### Tuples and Nesting
I already described the tuple-based approach above. Another, similar, approach achieves the same by nesting the data in another map. Both fall short on the requirement to make document fields directly accessible.

```clj
;; structure
{:id "some-id", :ref "/events/some-id", :data document-data}
;; usage (including gymnastics)
(sort-by (comp :join_dt :data) participants)
```

I’m not too fond of either approach since they both expose a specific implementation detail, that the actual document data is nested, at the call site. In a way my critique of this approach is similar to why [Eric Normand advocated for getters in his IN/Clojure ’19 talk](https://youtu.be/Sjb6y19YIWg) — as far as I understand anyways.

### Addition of a Special Key

Another approach could be to add metadata directly to the document data.

```clj
(defn doc [query-doc-snapshot]
  (-> (cljs-bean/->clj (.data query-doc-snapshot))
      (assoc ::meta {:id (.-id query-doc-snapshot
                     :ref (.-ref query-doc-snapshot})))
```

This is reasonable and makes document fields directly accessible. However it also requires us to separate document fields and metadata before passing the data to any function writing too Firestore.

```clj
;; before writing we need to remove ::meta
(.set some-ref (cljs-bean/->js (dissoc document-data ::meta))
```

I think this is a reasonable solution that improves upon some of the issues with the tuple and nesting approach. I realize that this isn’t a huge change but this inversion of how things are nested does give us that direct field access that the nesting approach did not.

### Protocols and `:extend-via-metadata`

An approach I’ve found particularly interesting to play with makes use of a protocol that can be implemented via metadata, as enabled by the new `:extend-via-metadata` option. This capability was added in [Clojure 1.10](https://clojure.org/reference/protocols#_extend_via_metadata) and subsequently added to ClojureScript with the [1.10.516 release](https://clojurescript.org/news/2019-01-31-release):

```clj
(defprotocol IFirestoreDocument
  :extend-via-metadata true
  (id [_] "Return the ID (string) of this document")
  (ref [_] "Return the Firestore Reference object"))

(defn doc [query-doc-snapshot]
  (with-meta
    (cljs-bean/->clj (.data query-doc-snapshot))
    {`id (fn [_] (.-id query-doc-snapshot))
     `ref (fn [_] (.-ref query-doc-snapshot))}))
```

Using `with-meta` we extend a specific instance of a bean to implement the `IFirestoreDocument` protocol. This allows direct access to document properties while retaining important metadata:

```clj
(:name participant) ; => "Martin"
(firebase/id participant) ; => "some-firebase-id"
```

At call sites we use a well-defined API (defined by the protocol) instead of reaching into nested maps whose structure may need to change as our program evolves. This arguably could also be achieved with plain functions.

**Sidenote:** A previous iteration of this used `specify!`. Specify modifies the bean instance however, meaning that whenever we’d update a bean the protocol implementation got lost. In contrast metadata is carried over across updates.

## Summary
Using [cljs-bean](https://github.com/mfikes/cljs-bean) we’ve enabled idiomatic property access for JS data structures without walking the entire document and converting it to a persistent data structure. We also retain the original Javascript object making it easy to use for Firestore API calls.

We’ve compared different ways of attaching additional metadata to those documents using compound structures as well as  the new and shiny `:extend-via-metadata`. Using it we’ve extended instances of beans to support a custom protocol allowing open ended extension without hindering the ergonomics of direct property access.

While I really enjoyed figuring out how to extend beans using `:extend-via-metadata` it turned out that any approach storing data in “unusual places” (i.e. metadata) causes notable complexity when also wanting to serialize the data.

Serializing metadata is something that [has been added to Transit quite some time ago](https://gist.github.com/mfikes/3a160a1504debd31e5771736256ca022) but compared to the plug and play serialization we get when working with plain maps it did not seem worth it. Even if set up properly the protocol implementations, which are functions, are impossible to serialize.

Ultimately we ended up with plain beans and storing metadata under a well known key that is removed before writing the data to Firestore again:

```clj
(defn doc [query-doc-snapshot]
  (-> (cljs-bean/->clj (.data query-doc-snapshot))
      (assoc ::meta {:id (.-id query-doc-snapshot)
                     :ref (.-ref query-doc-snapshot)})))

(defn id [doc]
  (-> doc ::meta :id))

(defn ref [doc]
  (-> doc ::meta :ref))

(defn data [doc]
  (cljs-bean/->js (dissoc doc ::meta)))
```

If you're using Firebase or comparable systems, I'd be curious to [hear how you address these issues](https://clojureverse.org/t/working-with-firebase-documents-in-clojurescript/4813).
