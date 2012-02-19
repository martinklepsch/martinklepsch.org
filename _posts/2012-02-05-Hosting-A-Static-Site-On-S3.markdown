---
layout: post
title: Hosting A Static Site On Amazon S3
category: draft
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
There are still ways to host your static site with Github but without the autogeneration
magic.</p></aside>

Since the requirements for hosting a static site are nearly non-existent you can easily move
it onto any Server.

I decided for Amazon S3 because it's widely used, reliable and cheap.

## Static Site Generators

While there are
[quite](http://nanoc.stoneship.org) [a](http://middlemanapp.com)
[lot](https://www.ruby-toolbox.com/categories/static_website_generation) Jekyll is used by
most people. When I chose Jekyll it was mostly because of it's active community and the fact
that it is developed and used heavily by Github.
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
<img src="/images/website-settings-s3.png" alt="AWS Console Website Settings">
<figcaption>Website settings in the bucket propertie pane</figcaption>
</figure>

**Step 2:** Set a bucket policy that basically allows everyone to view the contents of your bucket.

  <pre><code>
    {
      "Version":"2008-10-17",
      "Statement":[{
      "Sid":"PublicReadForGetBucketObjects",
            "Effect":"Allow",
        "Principal": {
                "AWS": "*"
             },
          "Action":["s3:GetObject"],
          "Resource":["arn:aws:s3:::www.martinklepsch.org/*"]
        }
      ]
    }
  </code></pre>

> Prices, Error handling, Privacy Policy, Website Settings

If you are experiencing problems with the setup of S3 I recommend the official [AWS
documentation](http://docs.amazonwebservices.com/AmazonS3/latest/dev/WebsiteHosting.html).

## Naked Domain Name Fowarding

wwwizer gani etc
