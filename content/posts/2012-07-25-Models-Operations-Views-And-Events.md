---
categories: linked
date-published: 2012-07-25T00:00:00Z
resource: http://cirw.in/blog/time-to-move-on
title: Models, Operations, Views and Events
uuid: 22ccc641-b55e-4c29-83b1-631fbd4598f6
permalink: /posts/models-operations-views-and-events.html
og-image: /images/selfies/3.jpg
type: post
---
Conrad Irwin:

> **Models** encapsulate everything that your application knows.<br>
> **Operations** encapsulate everything that your application does.<br>
> **Views** mediate between your application and the user.<br>
> **Events** are used to join all these components together safely.<br>

While MVC is still widely used and considered a good pattern for modern application
development there are also a few people thinking about alternatives that suit the actual
application logic better than the current mindset.
One of those people is [Conrad Irwin](http://cirw.in). His approach is very focused on
connecting the different parts of the application by using events which seems at least easier to
grasp than the exact task of *Controllers* in *Model-View-Controller*.
