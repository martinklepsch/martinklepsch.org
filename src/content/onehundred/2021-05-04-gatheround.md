---
title: Gatheround
date-published: 2021-05-04T18:01:34.293Z
uuid: 6510e162-2a88-47d8-bdea-a60894ab999d
og-image: /images/selfies/10.jpg
type: onehundred
slug: gatheround
---
Until recently I worked for a company called Icebreaker. Now I work for a company called [Gatheround](https://gatheround.com). After months of behind the scenes work we finally changed our name to something a little more open. Something that better reflects the ambitions behind the product. 

Some time last year we started with a list of a dozen or so names. And here we are today.

[![screenshot of the Gatheround homepage](/images/uploads/gatheround-homepage-screenshot.png "Gatheround")](https://gatheround.com)

[Brenna](https://www.brennajmarketello.com/) came up with a wonderful design system which I got to pour in code. Prior to this rebrand we didn't have much of a design system so having one now is already paying huge dividends in terms of shipping more consistent interfaces.

**The migration** was also a fun challenge to tackle. In the end we back-ported some of our new design system to the old Icebreaker brand and used a feature flag whenever that didn't feel appropriate. This approach meant we didn't need to feature-freeze at all and that we would preserve the Git history of our UI code. 

We also allowed enabling the feature flag via a URL param which meant that others on the team could review the new brand in preview environments.

Last but not least I had quite a bit of fun getting into [react-spring](https://react-spring.io/) for some **animations** on the [homepage](https://gatheround.com). It's been a few years that I seriously looked at React animation stuff and it seems to have come a long way since then. The hooks-based react-spring API is pretty straightforward and since everything happens inside your program (as opposed to CSS transitions) you have much more direct control over sequencing and overall behavior.

CSS-in-JS was also something that might have made some things easier (e.g. parameterizing styles, avoiding conflicts) but ultimately it felt a little too risky given that no-one on the team had significant experience using it. 

Shout out to everyone who helped make this happen including the many open source projects that we rely on. (Gatheround is in the process of becoming a [ClojuristsTogether](https://www.clojuriststogether.org/) member. 🎉)