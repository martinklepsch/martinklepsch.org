---
layout: post
FBimage:
title: Owning Your Analytics Data
---
For quite a while now there is this idea of *being data-driven* and analyzing
everything that is happening on your platform in order to identify potential
changes that could improve your core metrics such as user engagement.

The most recent YC batches (Mixpanel, Chart.io, Segment.io, ...) show that
there is some significant interest in solutions that make it easy to track more
data and get advanced reports on it. They all allow you to send events triggered
by users to their servers and provide you with some beautiful interfaces to
actually make some sense of this data.

While this is all great I see one major problem with this style of analytics:
the data is not really yours. In a time where user engagement is getting more
and more important I find it troubling to store all the data you have on
a platform that only provides access to your data through a limited API (vs.
full featured database access.)

Limited access to this data slows you down building internal tools
like dashboards and custom metrics/reports. There is also a vendor lock-in
to a certain degree. While you can switch vendors quite simple using
Segment.io's [analytics.js]() you can't take your data with you easily.

While I'm usually a fan of outsourcing certain complex things (i.e. hosting)
I feel like this is one of the rare occasions where people should think twice.
Unfortunately I'm not aware of good alternatives.
If you are please [tweet me](http://twitter.com/mklappstuhl).

<!--
—
Remember when the worst thing you could possible do was to delete 'user'
column in your MySQL database? Maybe now thats true for your analytics
data.
-->
