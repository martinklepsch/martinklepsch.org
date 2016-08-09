---
layout: post
date-published: 2015-06-03
title: Managing Local and Project-wide Development Parameters in Leiningen
uuid: 47b430b8-3e76-48c5-a68e-a12fe88b2e4e
---

Little tip. Long headline.

In any project there are often settings that are specific to the
context the project is run in (think of an `environment` parameter)
and then there are parameters that are specifc to the
developer/workstation they're run on. This is a guide to separate
these two things nicely in Leiningen-based Clojure projects.

So you have a project setup that uses
[environ](https://github.com/weavejester/environ) to determine the
context the project is run in (`development` vs. `production`).

```clojure
; in project.clj:
(defproject your-app "0.1.0-SNAPSHOT"
  ; ...
  :profiles {:dev {:env {:environment "development"}}})
```

Now you also want to use environment variables (or anything else thats
supported by environ) to store AWS credentials to access Amazon
S3. You don't want to commit these credentials into version control,
therefore you can't add them to `project.clj`. The way to go is to
create a file `profiles.clj` in your project to store workstation
specific information. Naively this could look something like this

```clojure
{:dev {:env {:aws-access-key "abc"
             :aws-secret-key "xyz"
             :s3-bucket "mybucket"}}}
```

If you run your project with this `profiles.clj` you will be able to
access your AWS credentials. You might also notice that `(environ/env
:environment)` is nil. **That wasn't intended.**

The problem here is that Leiningen will override keys in profiles
defined in `project.clj` if **the same profile** has also been defined
in `profiles.clj`.  To recursively merge Leiningen profiles combine them like so:

```clojure
;; in project.clj:
(defproject your-app "0.1.0-SNAPSHOT"
  ;; ...
  :profiles {:dev [:project/dev :local/dev]
             :project/dev {:env {:environment "development"}}})

;; in profiles.clj
{:local/dev {:env {:secret-key "xyz"}}}
```

Now both, `:envrionment` and `:secret-key` should be defined when you
retrieve them using environ.

*This is largely inspired by James Reeves' [duct](https://github.com/weavejester/duct) Leiningen template.*
