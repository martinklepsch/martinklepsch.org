---
date-published: 2013-04-28T00:00:00Z
title: Analytics Data
uuid: 4682a859-015c-401e-a3e4-f47938c9312b
hidden: true
og-image: /images/selfies/3.jpg
type: post
slug: analytics-data
---
For quite a while now there is this idea of *being data-driven* and analyzing
everything that is happening on your platform in order to identify potential
changes that could improve your core metrics such as user engagement.

There are [quite](https://keen.io/) [a](https://mixpanel.com/)
[few](https://www.kissmetrics.com/) services allowing you to send events
triggered by users to their servers that provide you great interfaces to
actually make some sense of the data you gathered.

While this is all great I see one major problem with this style of analytics:
the data is not really yours. In a time where user engagement is getting more
and more important I find it troubling to store all the data you collected
on a service only accessible through a limited API (vs. full featured database access.)

Limited access to this data can slow you down building internal tools
like dashboards and custom metrics/reports. There is also a vendor lock-in
to a certain degree. While you can switch vendors easily using
Segment.io's [analytics.js](https://segment.io/libraries/analytics.js/)
taking your data with you is not as easy as that.

While I'm usually a fan of outsourcing certain complex things (i.e. hosting)
I feel like this is one of the rare occasions where people should think twice.
Unfortunately I'm not aware of good alternatives.
If you are please [tweet me](http://twitter.com/martinklepsch).
