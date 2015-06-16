<!--
layout: post
title: S3-Beam — Direct Upload to S3 with Clojure & Clojurescript
-->

In a
[previous post](http://www.martinklepsch.org/posts/using-coreasync-and-transducers-for-direct-s3-upload.html)
I described how to upload files from the browser directly to S3 using
Clojure and Clojurescript. I now packaged this up into a small (tiny,
actually) library:
[s3-beam](https://github.com/martinklepsch/s3-beam).

An interesting note on what changed to the process described in the
earlier post: the code now uses `pipeline-async` instead of
transducers. After some discussion with Timothy Baldridge this seemed
more appropriate even though there are some aspects about the
transducer approach that I liked but didn't get to explore further.

Maybe in an upcoming version it will make sense to reevaluate that
decision. If you have any questions, feedback or suggestions I'm happy
to hear them!
