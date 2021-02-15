---
title: Static Blogging, Some Lessons Learned
date-published: 2020-05-10T12:13:00.303Z
uuid: 731b58c0-836e-4448-b225-67123f69d9af
permalink: /posts/static-blogging-lessons-learned.html
og-image: /images/selfies/2.jpg
type: post
---

I've been running this blog for more than eight years now. Over these years it went through multiple rewrites, occasionally satisfying my urge to play with new toys. Now I'm in the middle of the next rewrite and I'm realizing some things that I'd love to have done from the start.

### UUIDs For Every Post

Eventually the time will come where you want a unique identifier for a piece of content. Maybe it is to feed it into another system, maybe it is an ID for RSS feeds. No matter what it is it never hurts to have some identifiers for your content. I am now putting UUIDs into the frontmatter (YAML) section of every Markdown file I add. 

### Static Permalinks

Some static site generators will define the permalink of a post by running code over some of the post's information like the title, slug, date, etc. I have found that a permalink should be permanent and thus there is no point in defining it in code. Just put the entire link into your post's metadata and whatever site generator you end up switching to, you'll know where that piece of content should be available in the end.

This means that maybe sometimes my URL schema isn't perfectly consistent but at the same time it also means I don't have to deal with redirects that would need to be configured in some external system system (e.g. websever/S3/Cloudfront).

Because I don't want to type out a UUID and permalink everytime I create a new post I created a little GitHub action that adds these fields to posts that don't already have it. 

### Commit Everything

Committing generated files is one of these things that intuitively sounds wrong but my blog went through so many design iterations and changes and I would love to be able to just go back through those for a good trip down memory lane. In theory the source code is still there but in reality I'm rarely in the mood to get some code working again that I used to use five years ago.

I'm now commiting all files that you can see here in the `_site` directory of the repository backing this blog. 

### Automation

I didn't intend to touch on this but one thing that I'm leaning into a lot for this rewrite is automating all kinds of things. As I write this post on [prose.io](https://prose.io) the frontmatter only has a title. As soon as I commit it an action will run to add `uuid`, `permalink`, `date-published`. This being easy to setup is a somewhat recent development, I guess last time around this wouldn't have been as easy as it is now. But it is exciting to me because it means I can just focus on writing and don't have to switch to a terminal to run `lumo -e '(random-uuid)'` or a deploy script.

This post is the first one that is being published using this automated setup so wish me luck as I hit the save & commit button. 