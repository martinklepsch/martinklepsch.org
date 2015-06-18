---
layout: post
date-published: 2015-02-05
title: (lisp keymap)
---
A while back I wanted to setup hotkeys for the various apps I use.
Mostly because I was annoyed by the cognitive effort you need to
make to figure out how often you need to press `Alt + Tab` exactly
to get to the app you want.

It seemed like a good idea to use Capslock as a modifier key.
This way I could be sure I wouldn't override any other keybindings.
Figuring out how to do this I stumpled upon an excellent
post by Steve Losh ["A Modern Space Cadet"][space-cadet]. It's
described in detail how to set Capslock to `Hyper` - a fifth modifier
key. I then created bindings like `Hyper + S` which will focus Safari
etc. Exactly what I was looking for.


**Then I found something in his post I wasn't looking for**:
instructions to map my shift keys to parentheses. It sounded crazy at
first but doing mostly LISP-y stuff these days I tried it anyways.

Now I wouldn't want to live without it anymore. It's just so much easier
than `Shift + {9,0}`. Also the Shift keys still work as they do usually
when pressed in combination with other keys.

A few days ago I was typing some stuff at a collegues computer and
it immediately felt cumbersome when having to type a parenthesis.

{::options parse_block_html="true" /}

<aside>
PS. Here are Steve Losh's original
[OS X Instructions][space-cadet-shifting]. (What's KeyRemap4MacBook in
this post is now [Karabiner][karabiner].)
</aside>

[space-cadet]: http://stevelosh.com/blog/2012/10/a-modern-space-cadet/
[space-cadet-shifting]: http://stevelosh.com/blog/2012/10/a-modern-space-cadet/#shift-parentheses
[karabiner]: https://pqrs.org/osx/karabiner/index.html.en

