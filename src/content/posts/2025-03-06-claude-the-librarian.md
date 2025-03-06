---
title: Claude, the librarian
slug: claude-the-librarian
date-published: 2025-03-06
uuid: a6e5be1b-e974-471b-8ea7-45c920acdd6c
tweet: |
  Cursor makes you write code, but maybe you don't have to?

  Recently I was trying to organize a personal knowledge base of sorts, dozens of markdown files, many of which I regularly use as context with LLMs.

  Now I wanted to 
---

Recently I was trying to create a knowledge base of sorts, dozens of markdown files with various contents. My little treasure trove of context windows that I feed to Cursor, Claude and friends. 

To get a better overview of whats in my library I wanted to add some tags and descriptions to each entry. 

Cursor was quick to spit out a few hundred lines to detect missing tags and update them as needed but in the end it was an equally curious experience to tackle this with **Claude Code**.

## The prompt

…wasn't very complex:

> Please identify markdown files in the current directory that ARE MISSING a tags frontmatter property. Identify those files by grepping for the string "tags:" in the file.
> For those files, add a tags frontmatter property selecting from the following list of tags:
> 
> \<tags> {list_of_tags} \</tags>
> 
> You can also suggest the addition of new tags, follow the naming pattern of the existing tags in that case.

Some instructions how to identify files that need updating and a list of options for the tags property.

## Why is this interesting?

_It's an agent!_ (you may cross this of your bingo cards now)

More interestingly though I found that Claude code has an understanding of simple task concepts like a list of tasks (the files to update) and progressing through those in some form of a loop.

It feels somewhat obvious in retrospect but in all the hype-talk of agents I didn't see this discussed much.

_It's interactive, or human-in-the-loop_ (bingo!)

Which has been really useful in cases where I wanted to add a tag that was not in the predefined list or prevent it from adding a tag that's not quite right.

_It's local_ (bingo?)

Having access to a local system is a bit of a superpower, be it for more programmatic "context building" or just not having to drop files on ChatGPT like its still 2024. People [are already using](https://x.com/dwarkesh_sp/status/1894181621215236257) Claude Code to work with their Obsidian notes. [Goose](https://block.github.io/goose/) is similar in many ways.

---

Claude Code is still new but it is intriguing to think about what this could do in a place like contiuous integration (e.g. for reviewing code) and a bit of YOLO mode.

For now its slow, kind of expensive and a bit clunky. That said no crystal ball is needed to see this being fast and polished, Goose already is showing the way.

Alright, back to topping up my Anthropic credits.

Oh, and MCP! (bingo!)