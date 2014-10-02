---
title: Patalyze &mdash; An Experiment Exploring Publicly Available Patent Data
layout: post
---
For a few months now I've been working on and off on a little
"data-project" analyzing patents published by the US Patent &
Trademark Office. Looking at the time I spent on this until now I
think I should start talking about it instead of just hacking away
evening after evening.

It started with a simple observation: there are companies like
Apple that sometimes collaborate with smaller companies building a
small part of Apple's next device. A contract like this usually gives
the stock of the small company a significant boost. What if you could
foresee those relationships by finding patents that employees from
Apple and from the small company filed?

## An API for patent data?

Obviously this isn't going to change the world for the better but just
the possibility that such predictions or at least indications are
possible kept me curious to look out for APIs offering patent data. I
did not find much. So thinking about something small that could be
"delivered" I thought a patent API would be great. To build the
dataset I'd parse the archives provided on Google's
[USPTO Bulk downloads](http://www.google.com/googlebooks/uspto-patents.html)
page.

I later found out about [Enigma](http://enigma.io) and some offerings
by [Thomson Reuters](http://ip.thomsonreuters.com). The prices are
high and the sort of analysis we wanted to do would have been hard
with inflexible query APIs.

For what we wanted to do we only required a small subset of the data a
patent contains. We needed the organization, it's authors, the title
and description, filing- and publication dates and some identifiers.
With such a reduced amount of data that's almost only useful in
combination with the complete data set I discarded the plan to build
an API. Maybe it will make sense to publish reduced and more easily
parseable versions of the archives Google provides at some point.
Let me know if you would be interested in that.

## What's next

So far I've built up a system to parse, store and query some 4 million patents
that have been filed at the USPTO since beginning of 2001. While it
sure would be great to make some money off of the work I've done so
far I'm not sure what product could be built from the technology I created
so far. Maybe I could sell the dataset but the number of potential
customers is probably small and in general I'd much more prefer to
make it public. I'll continue to explore the possibilities with regards
to that.

For now I want to explore the data and share the results of this
exploration. I setup a small site that I'd like to use as home for any
further work on this. By now it only has a newsletter signup form
(just like any other landing page) but I hope to share some
interesting analysis with the subscribers to the list every now and
then in the near future. Check it out at
**[patalyze.co](http://www.patalyze.co)**.  There even is a small
chart showing some data.
