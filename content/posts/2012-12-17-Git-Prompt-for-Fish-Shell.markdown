---
layout: post
date-published: 2012-12-17
title: Git Information in Fish Shell&rsquo;s Prompt
uuid: 83080c01-c898-4e8e-9996-97f8d60d3a4a
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

```
set normal (set_color normal)
set magenta (set_color magenta)
set yellow (set_color yellow)
set green (set_color green)
set red (set_color red)
set gray (set_color -o black)

# Fish git prompt
set __fish_git_prompt_showdirtystate 'yes'
set __fish_git_prompt_showstashstate 'yes'
set __fish_git_prompt_showuntrackedfiles 'yes'
set __fish_git_prompt_showupstream 'yes'
set __fish_git_prompt_color_branch yellow
set __fish_git_prompt_color_upstream_ahead green
set __fish_git_prompt_color_upstream_behind red

# Status Chars
set __fish_git_prompt_char_dirtystate '⚡'
set __fish_git_prompt_char_stagedstate '→'
set __fish_git_prompt_char_untrackedfiles '☡'
set __fish_git_prompt_char_stashstate '↩'
set __fish_git_prompt_char_upstream_ahead '+'
set __fish_git_prompt_char_upstream_behind '-'


function fish_prompt
  set last_status $status

  set_color $fish_color_cwd
  printf '%s' (prompt_pwd)
  set_color normal

  printf '%s ' (__fish_git_prompt)

  set_color normal
end
```
