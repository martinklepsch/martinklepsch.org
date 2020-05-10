---
layout: post
date-published: 2018-01-19T00:00:00Z
title: 'Sustainable Open Source: Current Efforts'
uuid: 229c2b5e-1cdd-4904-9387-8c0491dc1382
permalink: /posts/sustainable-open-source-current-efforts.html
og-image: /images/selfies/1.jpg
---

The recent appearance of [Clojurists Together](http://clojuriststogether.org/),
friends working on [OpenBounty](https://openbounty.status.im/), as well as recently
finding a lot of energy to work on a documentation platform for the Clojure
ecosystem stirred some thoughts about sustainable OpenSource.

Let's say one thing right out of the gate: sustainable OpenSource
isn't really a goal on it's own. What the community (users as well as
maintainers) strives for is reliability and well-maintained ecosystem
components. Businesses and individuals alike depend on those
properties — not the fact that work done to achieve them is
sustainable. That said I don't see any ways to achieve those without sustainability.

### Current Efforts

There are various platforms trying to improve sustainability of open source
efforts that have continued momentum. Many of them with their own ideas how
the situation can be improved.

- [**OpenCollective**](https://opencollective.com) collects payments from
individuals and companies and stores funds for organizations. People may then
"invoice" the organization. This can be for stickers and labor time alike.

- [**OpenBounty**](https://openbounty.status.im/) is a bounty platform used
with cryptocurrencies. Contributors may work on specific issues and get paid
a bounty which has been defined in advance.

- [**Clojurists Together**](https://clojuriststogether.org/) collects
money from companies and community members to fund open source
projects benefitting the overall Clojure ecosystem. People may apply
with a project they want to work on and get funding (depending on
overall availability) for a duration of three months.

All platforms take care of collecting money and have mechanisms for
redistributing it. With OpenCollective a community will need to agree on
processes to request and distribute funds. Clojurists Together collects money
in similar ways to OpenCollective but has a predefined process for
how funds are allocated.

Projects like webpack [have embraced](https://opencollective.com/webpack/expenses) 
OpenCollective with people getting reimbursed for expenses but also
regular labor invoices for time worked on the project.

OpenBounty also provides some of these processes by assigning bounties
to specific tasks. OpenBounty is used in
[Status.im](https://status.im/)'s development process and while I
don't believe bounties are the answer to everything I'm excited to
watch this space in the future.

<!-- Interesting to note about OpenBounty: it is
developed by a blockchain startup ([Status.im](https://status.im/)) to ramp
up their development efforts and incentivize more people to contribute to the
project **[TODO relevant?]**. -->

### Sustainable Incentives

Contributing to OpenSource is about incentives. As far as I can judge these often are

- fun & community,
- fixing a problem one encountered,
- recognition & better job opportunities.

Nothing is wrong with this list but they are not sustainable on their
own. Just working for the fuzzy feeling of giving back to a community
doesn't pay your bills.
As soon as you can no longer afford to work for the fun of it the stability
and momentum of projects you contributed to will suffer.

<!-- Meaning, those incentives may encourage the -->
<!-- properties discussed in the introduction (well-maintained etc) for some -->
<!-- duration of time but not in a sustainable (~long-term) way. -->

<!-- There's one notable (and rare?) exception I can think of: people getting
hired with a clause to spend time on an OpenSource project. **[TODO maybe not
as important]** -->

<!-- There are some things — or rather two kinds of participants — that need to be given to guarantee stable and well-maintained open source projects: -->
I believe there are two kinds of participants required to achieve reliability, stability and so on:

- There need to be some people contributing on a regular basis. They provide
overall direction, deal with reported issues and incoming contributions.
Often projects refer to this as "core". In my experience stability of an open
source project suffers with fluctuations in the set of "core" people working on it.
- There need to be occasional/new contributors. Life will happen
(kids, work, etc.) to regular contributors causing a natural
decline. New contributors discovering they enjoy working on the
project can fill up those gaps.

In my opinion the incentives listed above are not sufficient —
especially for long-term regular contributors. If work is unpaid it
will eventually become stressful to juggle with other responsibilities
and people will be forced to step back. New contributors are just as
important and I believe there are improvements to be done there as
well but ultimately nothing works without a "core" set of people.

I have some further thoughts on how such incentives could be
structured which I will explore in a later blog post.

<!-- Now that we have an idea of what's required we can think about ways to fulfill those requirements. -->

<!-- ---

THOUGHT: Too strong incentives to contribute to existing projects could hinder innovation? Hidden part of graph we should be aware of/consider.

---

THOUGHT: Stability and reliability are among the most important aspects of open source projects. Any system should optimize towards ongoing contribution while also maintaining an inflow of new people so other people may retreat to other adventures.

---

Let's start with bounties as they are probably more controversial than other models. I believe bounties can excel at achieving the following goals:

- Getting new people to contribute to a project
- Encouraging outsiders to familiarize themselves with a project (think of security)

Bounties fall short when the aim is to create stability and continuous unguided contributions. -->
