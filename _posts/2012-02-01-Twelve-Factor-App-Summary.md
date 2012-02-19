---
layout: post
title: Twelve Factor App - An Overview
category: draft
---

Recently [Adam Wiggins](https://twitter.com/#!/hirodusk) of Heroku fame released a very
insightful document.
It is called [The Twelve Factor App](http://12factor.net) and contains twelve one-pagers about
different areas of application development.

I will try to give a brief overview about every *factor* below.

## Codebase

Store your code in a version control system. Don't share code accross apps. Use libraries
instead.

- - -

## Dependencies

Define all dependencies in your codebase. Provide tools to manage these dependencies.
Use tools like [Bundler](http://gembundler.com) or [Virtualenv](http://www.virtualenv.org/en/latest/index.html#what-it-does).
Don't make any further assumptions about the execution environment than that there is a language runtime
and a dependency management tool.

- - -

## Configuration

Store configuration options in environment variables.

- - -

## Backing Services

Treat backing services as resources

- - -

## Buildcycle

Build releases by combining your codebase with appropriate configuration.
Give unique identifiers to each release. Deploy and run a release. Never modify an existing
release. Build a new one instead.

- - -

## Processes

Make all processes stateless. Store all data in *backing services* instead of within the
apps state. Use data-stores with time-expiration functionality when storing session data.

- - -

## Port Binding

Export your app by binding it to a port. Do this by declaring dependencies and configuring
these.

> The contract with the execution environment is binding to a port to serve requests.
> <cite>12 Factors: [Concurrency](http://www.12factor.net/concurrency)</cite>

- - -

## Concurrency

- - -

## Disposability


- - -

## Development/Production Parity

Keep the differences between execution and development environment as small as possible.
Practise continous deployment. Deploy code you wrote yourself.
Avoid lightweight alternatives to parts of your execution environment (ex. SQLite vs. MySQL).

- - -

## Logs

Collect log messages with a seperate tool. Do not handle any log messages or application
output with the code of your application. Index and analyze logs in dedicated tools like
[Graylog2](http://graylog2.org) or [Splunk](http://splunk.com).

- - -

## Admin Processes

Store all code that is used for administrative tasks within your codebase. Use the same
dependency management you use for the application for administrative tools.
