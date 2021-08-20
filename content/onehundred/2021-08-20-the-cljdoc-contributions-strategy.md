---
title: the cljdoc contributions strategy
---
Since [cljdocs](https://martinklepsch.org/100/cljdoc-origins-1-n.html) [inception](https://martinklepsch.org/100/cljdoc-origins-2-n.html) I've always been very eager to make it easy for folks to contribute. I wrote almost [two dozen Architecture Decision Records](https://github.com/cljdoc/cljdoc/tree/master/doc/adr) (ADR) hoping it would help future contributors navigate the codebase. I encouraged people to contribute and tried to spec out smaller "good first issue" type contributions so folks could start with something small. 

Once people started contributing I of course wanted to encourage them to stick around and help maintain the project. Since there's so many parts to maintaining a project like this even small non-code contributions like participating in a discussion or reviewing a PR can be tremendously helpful. 

To give a sense of ownership I added anyone who contributed as a contributor on GitHub and gave them push access to the repository. The understanding was that we'd still use PRs for most contributions but it did remove me as a primary gatekeeper. Any previous contributor is empowered merge a PR. I think the first time I read about this approach as in Felix Geisendörfers [Pull Request Hack](https://felixge.de/2013/03/11/the-pull-request-hack/) article. 

As the project grew we added continuous deployment. Any commits to the main branch are automatically deployed. It might seem a bit wild to just let anyone deploy to cljdoc.org but it works for this project.

cljdoc isn't a library. It doesn't have an API. We can just let people improve it in ways they see fit. Sure maintainability is still a concern, but that is much less challenging than maintaining backwards compatible APIs or just designing a good API in the first place. Historically I think most contributions have actually improved the codebase.

I'm really happy I'm not maintaining a popular library with lots of contributors. I'm helping maintain a thing where people can play around and explore their ideas. Where it's ok to make a mistake. We're all here to learn. I learned so much from creating cljdoc and I couldn't be happier that others can do the same. 

Shoutout to Lee Read who prompted these reflections. 🙌