---
date-published: 2014-09-11T00:00:00Z
title: Running a Clojure Uberjar inside Docker
uuid: 1f571433-ffa3-4d3b-9c5d-6220cf9ebe54
permalink: /posts/running-a-clojure-uberjar-inside-docker.html
hidden: true
og-image: /images/selfies/2.jpg
type: post
---

For a sideproject I wanted to deploy a Clojure uberjar on a remote server
using Docker. I imagined that to be fairly straight foward but there are some
caveats you need to be aware of.

Naively my first attempt looked somewhat like this:

    FROM dockerfile/java
    ADD https://example.com/app-standalone.jar /
    EXPOSE 8080
    ENTRYPOINT [ "java", "-verbose", "-jar", "/app-standalone.jar" ]

I expected this to work. But it didn't. Instead it just printed the following:

    [Opened /usr/lib/jvm/java-7-oracle/jre/lib/rt.jar]
    # this can vary depending on what JRE you're using


And that has only been printed because I added `-verbose` when starting the jar.
So if you're not running the jar verbosely it'll fail without any output.
Took me quite some time to figure that out.

As it turns out the `dockerfile/java` image contains a `WORKDIR`
command that somehow breaks my `java` invocation, even though it is
using absolute paths everywhere.

## What worked for me

I ended up splitting the procedure into two files in a way that allowed
me to always get the most recent jar when starting the docker container.

The `Dockerfile` basically just adds a small script to the container that
downloads and starts a jar it downloads from somewhere (S3 in my case).

    FROM dockerfile/java
    ADD fetch-and-run.sh /
    EXPOSE 42042
    EXPOSE 3000
    CMD ["/bin/sh", "/fetch-and-run.sh"]

And here is `fetch-and-run.sh`:

    #! /bin/sh
    wget https://s3.amazonaws.com/example/yo-standalone.jar -O /yo-standalone.jar;
    java -verbose -jar /yo-standalone.jar

Now when you build a new image from that Dockerfile it adds the
`fetch-and-run.sh` script to the image's filesystem. Note that the
jar is not part of the image but that it will be downloaded whenever
a new container is being started from the image. That way a simple restart
will always fetch the most recent version of the jar. In some
scenarios it might become confusing to not have precise deployment
tracking but in my case it turned out much more convenient than going
through the process of destroying the container, deleting the image,
creating a new image and starting up a new container.
