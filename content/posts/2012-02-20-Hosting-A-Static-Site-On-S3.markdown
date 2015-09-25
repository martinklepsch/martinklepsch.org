---
layout: post
category: howto
date-published: 2012-02-20
title: Hosting A Static Site On Amazon S3
---

## Preface

Hosting a static site is preferred by many developers because it gives you a maximum of
control at a minimum of maintenance.
While there are other tools available [Jekyll](https://github.com/mojombo/jekyll) became something like a reference
implementation, especially under Ruby developers.
Since Jekyll's creator, Tom Preston Werner, also founded Github it is not surprising that
Github is providing a service to autogenerate and host your static site as soon as you push
it.

Now, that sounds like perfect all around. It is. As long as you are not using custom
generators or converters with Jekyll everything is good.
If you do however you'll quickly notice that Github [disabled custom Ruby code](https://github.com/mojombo/jekyll/issues/325) to keep
everything secure.

<aside><p>
There are still ways to host your static site with Github but using these would mean
losing nearly all the benefits from hosting at Github (eg. Autogeneration).</p></aside>

Since the requirements for hosting a static site are nearly non-existent you can easily move
it onto any Server.

I decided for Amazon S3 because it's widely used, reliable and cheap.

## Static Site Generators

While there are
[quite](http://nanoc.stoneship.org/ "Nanoc") [a](http://middlemanapp.com "Middleman")
[lot](https://www.ruby-toolbox.com/categories/static_website_generation "Ruby-Toolbox Listing").
Jekyll is used by most people. When I chose Jekyll it was mostly because of it's active
community and the fact that it is developed and used heavily by Github.
Before I settled on Jekyll I gave nanoc a try. I don't exactly remember why I ditched nanoc
but in the end Jekyll feels lighter and I also prefer Liquid Markup over ERB
Syntax.

## Setting Up Amazon S3

Comparing Github's free hosting with Amazon S3 is somewhat unfair since you got to pay for
S3 storage and bandwith. Doing the math however you'll quickly notice that the price for
hosting your static site on S3 is low.
With a complete page size of 100M and traffic of 10G you would not pay more than 2$.

After creating a new bucket in your S3 instance there are only a few steps you need to do in
order to have a proper static site hosted by S3.

**Important:** When creating your bucket make sure that it has the same name as the domain
name you want to use for your static site (ex. www.martinklepsch.org). This domain has to
have some subdomain.

**Step 1:** Enable S3's website feature by enabling it in the properties pane of your bucket.

<figure>
<img alt='Website settings in the bucket propertie pane' src='/images/website-settings-s3.png'>
</figure>

**Step 2:** Set a bucket policy that basically allows everyone to view the contents of your bucket.

    {
      "Version":"2008-10-17",
      "Statement":[{
        "Sid":"PublicReadForGetBucketObjects",
        "Effect":"Allow",
        "Principal": {
          "AWS": "*"
        },
        "Action":["s3:GetObject"],
        "Resource":["arn:aws:s3:::www.REPLACE-THIS.org/*"]
      }]
    }

<!-- <script src="https://gist.github.com/martinklepsch/4991741.js"></script> -->

**Step 3:** Upload your static website to S3. You can either do that manually by using the
AWS Management Console or you can automate the process by writing some small programm. There
are S3 libraries for many programming languages.
I built a [small rake
task](https://github.com/martinklepsch/martinklepsch.org/blob/master/Rakefile
"Rakefile on Github") that does the job.

If you are experiencing problems with the setup of S3 I recommend the official [AWS
documentation](http://docs.amazonwebservices.com/AmazonS3/latest/dev/WebsiteHosting.html
"AWS Static Website Hosting Documentation").

## Naked Domain Name Fowarding

DNS does not allow to set the whats apparently called "zone apex" (`"example.com"`) to be
a CNAME for another domain like `www.example.com.s3-website-us-east-1.amazonaws.com`.
Therefore you need to redirect all requests going to your domain without `www` to you
domain with `www` (`example.com` to `www.example.com`).
[Read more.](https://forums.aws.amazon.com/thread.jspa?threadID=55995
"A thread in AWS forums with good information about the issue")

You can either do this by using your domain registrars control panel or by using a service
like [wwwizer](http://wwwizer.com/naked-domain-redirect). I did it with
[Gandi](http://gandi.net)'s control panel and it works fine.
