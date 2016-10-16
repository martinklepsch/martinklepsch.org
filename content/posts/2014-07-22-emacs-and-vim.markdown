---
layout: post
date-published: 2014-07-22
title: Emacs & Vim
uuid: 6d204755-b8a0-4c43-98b2-b592e22970b7
---

After using Vim for more than four years my recent contacts with Lisp
encouraged me to take another look at Emacs. I used to make jokes
about Emacs just as Emacs users about Vim but actually it seems to be
a pretty decent piece of software.

Being a Vim user in the Clojure community sometimes feels weird. You
are happy with Vim. Running Clojure code with right from the editor
works well these days. Still you wonder why all those people you
consider smart seem to be so committed to Emacs. So I decided to try
it once again.

## Keybindings

Let's start with a slight rant: I completely do not understand how
anyone can use Emacs' default keybindings.  Being a Vim user I
obviously have a thing for mode-based editing but Emacs' keybindings
are beyond my understanding. Some simple movement commands to
illustrate this:

<table>
  <thead>
    <tr>
      <th>Command</th>
      <th>Emacs</th>
      <th>Vim</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Move cursor down one line</td>
      <td><code>Ctrl-n</code></td>
      <td><code>j</code></td>
    </tr>
    <tr>
      <td>Move cursor up one line</td>
      <td><code>Ctrl-p</code></td>
      <td><code>k</code></td>
    </tr>
    <tr>
      <td>Move cursor left one character</td>
      <td><code>Ctrl-b</code></td>
      <td><code>h</code></td>
    </tr>
    <tr>
      <td>Move cursor right one character</td>
      <td><code>Ctrl-f</code></td>
      <td><code>l</code></td>
    </tr>
  </tbody>
</table>

These are the commands recommended in the Emacs tutorial (which you
open with `Ctrl-h t`). They are mnemonic, what makes them easy to
learn--but is that really the most important factor to consider for
commands you will use hundreds of times a day? I don't think so. I
tried to convince myself that it might be worth to get used to Emacs'
default keybindings but after some time I gave up and installed
`evil-mode`.

## Mode-based Editing with Evil Mode

In my memory `evil-mode` sucked. I was delightfully surprised that it
doesn't (anymore?). Evil brings well-done mode based editing to
Emacs. As you continue to evolve your Emacs configuration you will
most likely install additional packages that bring weird Emacs-style
keybindings with them. Since you now have a mode-based editor you can
use shorter, easier to remember keybindings to call functions provided
by these packages. A useful helper that fits a sweet spot in my
Vim-brain is `evil-leader` which allows you to setup `<leader>` based
keybindings, just like you can do it in Vim:

    (evil-leader/set-leader ",")
    (evil-leader/set-key
      "," 'projectile-find-file)

With this I can open a small panel that finds files in my project in a
fuzzy way (think Ctrl-p for Vim) hitting `,` two times instead of
`Ctrl-c p f`.

## Batteries Included

What I really enjoyed with Emacs was the fact that a package manager
comes right with it. After adding a community maintained package
repository to your configuration you have access to some 2000 packages
covering Git integration, syntax and spell checking, interactive
execution of Clojure code and more. This has been added in a the last
major update (`v24`) after being a community project for some years.

## Conclusion

Vim's lack of support for async execution of code has always bugged me
and although there are some projects to change this I can't see it
being properly fixed at least until NeoVim becomes the go-to Vim
implementation. Emacs allows me to kick off commands and do other
things until they return. In addition to that it nicely embeds Vim's
most notable idea, mode-based editing, very well, allowing me to
productively edit text while having a solid base to extend and to
interactively write programs.

If you are interested in seeing how all that comes together in my
Emacs configuration you can find it
[on Github](https://github.com/martinklepsch/dotfiles/blob/master/emacs.d/init.el).
