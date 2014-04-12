---
layout: post
title: Git Information in Fish Shell&rsquo;s Prompt
---
After toying around with [Zsh](http://zsh.org) for a while I stumbled upon Fish
or more precisely [Ridiculous Fish](http://ridiculousfish.com/shell)
which is a fork of the original [Fish Shell](http://fishshell.com/).

Since Fish does not have a configuration framework like
[oh-my-zsh](https://github.com/robbyrussell/oh-my-zsh)
you have to setup your prompt with additional Git information yourself.
I consider this an advantage though as you can't just take a bunch of configuration files
from somewhere without having an idea what they actually do.

It took me a while to find [this
part](https://github.com/fish-shell/fish-shell/blob/master/share/functions/__fish_git_prompt.fish) of the Fish source code
which documents this functionality quite well. An example which you can put into
`~/.config/fish/config.fish` follows below:

<script src="https://gist.github.com/mklappstuhl/4991069.js"></script>
