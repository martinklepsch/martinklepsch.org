---
layout: post
title: Chaf, better Chat.
---
&laquo;Chaf&raquo; is a chat application with first-class threading.

## Why bring threading to chat?

Traditionally chat applications follow a very simple model. There are
two crucial things in each of them: a collection of messages (often
called "room" or "channel") people can add to and messages themselves.
It's an obvious analogy for someone in the 80s inventing a chat
system. There are several issues however that should make us
reconsider these fundamental concepts:

- Following up after absence is a tedious task. Users have to mentally
  keep notes on who was talking to each other in the worst case with
  multiple other intertwined and time-offset conversations.
- Tracing a discussion back in the history of a room provides
  many similar challenges.
- Knowledge that is being shared in chat applications is essentially
  ephemeral unless someone takes the time to transfer it somewhere
  else.
- In general a lot of information is shared and decisions are made in
  a place that serves well for instantaneous communication but only
  provides very unstructured data for uses beyond the very moment of
  conversation.

Threading can be overwhelming and complex leading to poor user
experience but I think that should not prevent further exploration of
the general concept.

## General Idea

There is a global thread (the **Room**) where you can either send a
message or create a new thread by explicitly replying to a specific
message. The latter is called *branching*.

### Threads

A thread contains messages. In all threads except the **Room** the first message
can be edited and those threads can also be concluded. Concluding will also
close the thread i.e. new messages can no longer be posted.

**Concluding** of threads is a good way to summarize a discussion or
consolidate knowledge for later reference. This is especially useful
to get a quick overview when only a preview of a thread is
rendered. To make sure the conclusion has enough context the first
message of a thread has to be edited before the thread can be
concluded.

<aside>Scepticism about whether people will actually "conclude" things
is very valid.  This is a feature that needs to be integrated in very
natural ways probably varying depending on size of thread and
more.</aside>

### Branching

In any thread you can branch (by replying) from a specific message and
create a new thread.  This will nest your reply under the original
message and display a [Thread Preview](#thread-previews)). The thread
preview shows recent messages and a conclusion if available.

As messages in a chat room are usually a reaction to another message
this would cause a new thread for almost every message and make
everything very confusing so the UI needs to be intelligent about
when provide affordances for branching and when not.

**Potential rule of thumb for branching**: Should (not can) a
conclusion on its own be made based on that message?  This could be
easily integrated into the reply UI.

### Thread Previews

Thread previews give insight into an ongoing conversation or present
the result of a concluded thread. They serve as a way to get insight
into conversations that just branched from the current thread while
not overwhelming.

<figure>
<img src='/images/threads.png'>
</figure>

If a thread hasn't been concluded the three most recent messages and a
reply field is included in the preview. For concluded threads the last
two messages and the conclusion are shown.

### Columns

As branching a thread is natural in Chaf the interface has to support
taking part in multiple conversations at once. A multi column layout
suits this situation well as chat is usually taller than wide.

<figure>
<img src='/images/multi-column.png'>
</figure>

Clicking on a thread preview will open a column right to the column the preview
is shown in. As activity in an unfocused thread happens it will be marked as
unread. The order of columns is then defined by the time they have been
unread. The global thread (the **Room**) is always in the leftmost column.

## Other interesting things to explore

- Since there is a clear hierarchical structure it is very easy to
  grant access to sub-trees of the main tree (originating from the **Room**)
- Many chat bot scenarios could benefit from more structured information
- *and some more I forgot about*
