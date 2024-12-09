---
date-published: 2014-03-12T00:00:00Z
title: Heroku-like Deployment With Dokku And DigitalOcean
uuid: 2818d220-0d76-4797-b967-b6fa9e5018e0
permalink: /posts/setting-up-your-own-little-heroku-with-dokku-and-digitalocean.html
hidden: true
og-image: /images/selfies/2.jpg
type: post
---
Over the weekend I sat down to play around with Docker/Dokku and was able to
set up a small machine on DigitalOcean that basically offers Heroku-like
deployment. It was surprisingly simple so here is some rough outline that should
get you going.

<aside>This guide is slightly opinionated and things can be done differently.
I decided to settle for the way thats closest to Heroku to keep things short.</aside>

## Create a Droplet at Digitalocean

Go to [DigitalOcean](https://www.digitalocean.com/?refcode=fb6eb9b8b286) and
create a droplet (note the comments below the screenshots):

<figure>
<img class='bordered' src='/images/hostname.png'>
<figcaption>Make sure the hostname is a fully qualified domain name, as it will
be the git remote you'll push to to deploy.</figcaption>
</figure>

<figure>
<img class='bordered' src='/images/image.png'>
<figcaption>When selecting the image, go to Applications and select the Dokku
one.</figcaption>
</figure>

There are some unresolved problems with Dokku on Ubuntu 13+ so if you are not
just playing around you might want to setup Dokku manually.  After that you're
ready to hit the create button and a droplet will be created within the next
minute. The last bit of server-setup that’s required is Dokku’s.

## Setting up Dokku

To get to Dokku’s setup screen just steer your browser to the IP shown in the
droplet’s detail view:

<figure>
<img class='bordered' src='/images/droplet-ip.png'>
</figure>

What you’ll see next is Dokku’s setup screen:

<figure>
<img class='bordered' src='/images/dokku-setup.png'>
<figcaption>Add an SSH key, tick the virtualhost checkbox, and make
sure the hostname is correct.</figcaption>
</figure>

## DNS Setup

To get the hostname you chose earlier actually point to your machine running
Dokku you need to add two A records to the zonefile of your domain.
This strongly varies between domain/DNS providers so I can’t exactly say how
it’d be done for yours. Whats important is that your provider supports wildcard
entries. Also the value of an A record should be only the subdomain part of the
hostname you set earlier, not the complete domain.

    A       <subdomain-of-hostname>      <droplet-ip>
    A       *.<subdomain-of-hostname>    <droplet-ip>

    # in a zonefile it’d look like this:
    *.apps 10800 IN A 107.170.35.171
    apps 10800 IN A 107.170.35.171

## Deploying

After you’ve waited three hours for DNS servers to propagate the changes you
should be able to do something like the following:

    git clone git@github.com:heroku/node-js-sample.git
    cd node-js-sample
    git remote add digital-ocean dokku@apps.example.com:nodeapp
    git push digital-ocean master

Now going to `nodeapp.<dokku-hostname>` should bring up “Hello World” from the app
we just cloned and pushed.

If you want to add have a custom domain point to your app you'll need to either
push to a remote like `dokku@apps.example.com:example.com` or edit the
nginx.conf that comes with Dokku’s nginx plugin.

Thanks to Dokku’s [Buildstep](https://github.com/progrium/buildstep) that
utilizes Heroku’s opensource buildpacks you can now deploy almost every application
you can deploy to Heroku to Dokku as well.

**Have fun!**
