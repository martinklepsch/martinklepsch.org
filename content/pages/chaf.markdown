---
layout: post
title: Chaf, better Chat.
---
&laquo;Chaf&raquo; is a chat application with simple threading.

## Why bring threading to chat?

## General Idea

There is a global thread (the **Room**) where you can either send a message or create a new thread by
explicitly replying to a specific message. The latter is called *branching*.

## Threads

A thread contains messages. In all threads except the **Room** the first message
can be edited and those threads can also be concluded. Concluding will also
close the thread i.e. new messages can no longer be posted.

**Concluding** of threads is a good way to summarize a discussion or
consolidate knowledge for later reference. This is especially useful
to get a quick overview when only a preview of a thread is
rendered. To make sure the conclusion has enough context the first
message of a thread has to be edited before the thread can be
concluded.

## Branching

In any thread you can branch (by replying) from a specific message and create
a new thread.  This will nest your reply under the original message and display
a [Thread Preview](#thread-previews)). The thread preview shows recent messages
and a conclusion if available.

As messages in a chat room are usually a reaction to another message
this would cause a new thread for almost every message and make everything
very confusing so there needs to be some simple rule for when to branch and when
not.

**Rule of thumb for branching**: Should (not can) a conclusion on its own be
made based on that message?  This could be easily integrated into the reply UI.

## Thread Previews

Thread previews give insight into an ongoing conversation or present the result
of a concluded thread.

<figure>
<img src='/images/threads.png'>
</figure>

If a thread hasn't been concluded the three most recent messages and a reply
field is included in the preview. For concluded threads the last two messages
and the conclusion are shown.

## Columns

As branching a thread is natural in Chaf the interface has to support
taking part in multiple conversations at once. A multi column layout suits this
situation well as chat is tradionally taller than wide.

<figure>
<img src='/images/multi-column.png'>
</figure>

Clicking on a thread preview will open a column right to the column the preview
is shown in. As activity in an unfocused thread happens it will be marked as
unread. The order of columns is then defined by the time they have been
unread. The global thread (the **Room**) is always in the leftmost column.
