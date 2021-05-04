---
title: Localizing a Ghost Theme
permalink: localizing-a-ghost-theme
---
Lately I've spent some time setting up a blog for a friend. [Ghost](https://ghost.org/) is where it's at these days when it comes to blogging — unless you're a nerd and love to play with static site generators like myself. 

Ghost provides many beautiful themes out of the box but most of them don't seem to support localization, which would be a nice thing to have for my friends blog. So I did some digging and essentially it comes down to:

1. Using the `{{t}}` helper for any strings that should be localized ([docs](https://ghost.org/docs/themes/helpers/translate/))
2. Providing a `locales/de.json` file with mappings to localized strings

The [Dawn theme](https://github.com/TryGhost/Dawn) that we were using was pretty light on strings that needed localization so with a little bit of [vim-sandwich](https://github.com/machakann/vim-sandwich) magic and a custom mapping I was able to update it to use the `{{t}}` helper in maybe half an hour.

```
xmap <Leader>t sai{{t "<CR>"}}<CR>
```

With the visual mapping above all I needed to do is select the text that I want to localize and hit `<space>t`.

![selecting some text and using vim-sandwich to wrap it in {{t}}](/images/uploads/sandwich-magic.gif "vim sandwich wrapping with custom head/tail")

Now the last step was to create the initial `locales/en.json` file. Later on I will use the English one as a template to create a German localization.

Since typing out the *more than a dozen* strings manually would have been boring I instead wrote a [babashka](https://babashka.org/) script to generate the English locales file for me.

```clojure
#!/usr/bin/env bb
(require '[clojure.java.io :as io]
         '[cheshire.core :as json]
         '[babashka.fs :as fs])

(def entries
  (->> (fs/glob "." "**/*.hbs")
       (map (fn [p] (slurp (io/file (str p)))))
       (mapcat (fn [file-contents]
                 (map second (re-seq #"\{\{t \"(.*)\"\}\}" file-contents))))
       (set)
       (map (fn [s] [s s]))
       (into {})))

(println (json/generate-string entries {:pretty true}))
```

This script essentially finds all usages of the `{{t}}` helper and spits out a JSON object where the keys are identical to the values (i.e. if the theme was English, that would be the `locales/en.json` file).

Babashka makes figuring this stuff out such a breeze because I can just incrementally build this out in a connected babashka nREPL session instead of changing the file and running the script as a whole on every change. REPLs for the win! 

In the end I created [this little PR to the theme](https://github.com/TryGhost/Dawn/pull/38).