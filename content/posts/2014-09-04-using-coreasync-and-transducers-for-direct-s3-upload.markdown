---
layout: post
date-published: 2014-09-04T00:00:00Z
title: Using core.async and Transducers to upload files from the browser to S3
uuid: a0c71769-4de2-476d-9453-2a00ccd527a5
permalink: /posts/using-coreasync-and-transducers-for-direct-s3-upload.html
---

In a project I'm working on we needed to enable users to upload media
content. In many scenarios it makes sense to upload to S3 directly
from the browser instead of routing it through a server. If you're
hosting on Heroku you need to do this anyways. After digging a bit
into [`core.async`](https://github.com/clojure/core.async) this seemed
like a neat little excuse to give Clojure's new transducers a go.

## The Problem

To upload files directly to S3 without any server in between you need
to do a couple of things:

1. Enable Cross-Origin Resource Sharing (CORS) on your bucket
2. Provide special parameters in the request that authorize the upload

Enabling CORS is fairly straightforward, just follow
[the documentation](http://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html)
provided by AWS. The aforementioned special parameters are based on
your AWS credentials, the key you want to save the file to, it's
content-type and [a few other things](http://aws.amazon.com/articles/1434/).
Because you don't want to store your credentials in client-side code
the parameters need to be computed on a server.

We end up with the following procedure to upload a file to S3:

1. Get a Javascript File object from the user
2. Retrieve special parameters for post request from server
3. Post **directly from the browser** to S3

## Server-side code

I won't go into detail here, but here's
[some rough Clojure code](https://gist.github.com/martinklepsch/0c6b40f45a415046f0fe)
illustrating the construction of the special parameters and how
they're sent to the client.

## Client-side: Transducers and core.async

As we see the process involves multiple asynchronous steps:
![](/images/s3-direct.png)

To wrap all that up into a useful minimal API that hides all the
complex back and forth happening until a file is uploaded core.async
channels and transducers turned out very useful:

    (defn s3-upload [report-chan]
      (let [upload-files (map #(upload-file % report-chan))
            upload-chan  (chan 10 upload-files)
            sign-files   (map #(sign-file % upload-chan))
            signing-chan (chan 10 sign-files)]

        (go (while true
              (let [[v ch] (alts! [signing-chan upload-chan])]
                ; that's not really required but has been useful
                (log v))))
        signing-chan))

This function takes one channel as argument where it will `put!` the
result of the S3 request. You can take a look at the `upload-file` and
`sign-file` functions
[in this gist](https://gist.github.com/martinklepsch/96e548d9595e111d70ce).

**So what's happening here?** We use a channel for each step of the
process: `signing-chan` and `upload-chan`. Both of those channels have
an associated transducer. In this case you can think best of a
transducer as a function that's applied to each item in a channel on
it's way through the channel. I initially trapped upon the fact that
the transducing function is only applied when the element is being
taken from the channel as well. Just putting things into a channel
doesn't trigger the execution of the transducing function.

`signing-chan`'s transducer initiates the request to sign the File
object that has been put into the channel. The second argument to the
`sign-file` function is a channel where the AJAX callback will put
it's result. Similary `upload-chan`'s transducer initiates the upload
to S3 based on information that has been put into the channel. A
callback will then put S3's response into the supplied `report-chan`.

The last line returns the channel that can be used to initiate a new upload.

## Using this

Putting this into a library and opening it up for other people to use
isn't overly complicated, the exposed API is actually very simple.
Imagine an [Om](https://github.com/swannodette/om) component `upload-form`:

    (defn queue-file [e owner {:keys [upload-queue]}]
      (put! upload-queue (first (array-seq (.. e -target -files)))))

    (defcomponent upload-form [text owner]
      (init-state [_]
        (let [rc (chan 10)]
          {:upload-queue (s3-upload rc)
           :report-chan rc}))
      (did-mount [_]
        (let [{:keys [report-chan]} (om/get-state owner)]
          (go (while true (log (<! report-chan))))))
      (render-state [this state]
        (dom/form
         (dom/input {:type "file" :name "file"
                     :on-change #(queue-file % owner state)} nil))))

I really like how simple this is. You put a file into a channel and
whenever it's done you take the result from another
channel. `s3-upload` could take additional options like logging
functions or a custom URL to retrieve the special parameters required
to authorize the request to S3.

This has been the first time I've been doing something useful with
core.async and, probably less surprisingly, the first time I played
with transducers. I assume many things can be done better and I still
need to look into some things like how to properly shut down the `go`
blocks. **Any feedback is welcome!** [Tweet](https://twitter.com/martinklepsch) or
[mail](mailto://martinklepsch@googlemail.com) me!

**Thanks** to Dave Liepmann who let me peek into some code
he wrote that did similar things and to Kevin Downey (*hiredman*)
who helped me understand core.async and transducers by answering
my stupid questions in `#clojure` on Freenode.
