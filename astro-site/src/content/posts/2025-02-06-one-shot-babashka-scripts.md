---
title: One-Shot Babashka CLI Scripts
type: post
permalink: /posts/one-shot-babashka-cli-scripts.html
og-image: /images/selfies/10.jpg
date-published: 2025-02-05T23:18:12.514Z
uuid: 813135fb-d709-4fbd-8fbd-a3ce33091b69
bluesky-post-id: 3lhhrisefmk2t
---
Like everyone I've been exploring AI tools and reading Simon Willisons excellent blog I discovered [how he uses LLMs to generate one-off Python tools](https://simonwillison.net/2024/Dec/19/one-shot-python-tools/).

In this post I'm gonna share a bit more about how I generate Babashka scripts in similar ways.

## It's all in the context

While Claude is pretty good at Clojure already it often generated code that didn't quite work. One particular case that kept occurring was that Claude kept thinking that `babashka.fs/glob` returns files (something that `slurp` could read) when in reality it returns a path. Globs can match directories after all.

So I started to copy together some pieces of documentation into a snippet that I'd always provide as context. Among the documentation I also included some guidelines for how I want things to be done.

```md
# general tips for writing babashka code

1. When using `p/process` and `p/shell` a variable list of strings is expected at the end. When creating the command using a vector or similar, be sure to use `apply` so that the vector is unwrapped
  1. Example: `(apply p/process {} ["echo" "123"])`

2. Some useful flags for file processing scripts
  1. `--dry-run` only print actions, don’t execute
  2. `--verbose` log additional input

3. When creating namespaces and functions using the babashka.cli APIs, it is useful to alias them into your `bb.edn` file so that they can used as a shorter command
  -  e.g. `{:tasks {prune some.ns/prune}}`
```

Maintaining a list of prompts and context windows seems like a useful thing to do! *There's a link to this one and a few others at the end.*

## Generating a script

Let's say I have a directory of markdown files and I want to add some frontmatter to each of them, say `type: post`.

With the provided context I'd write a short prompt

> Please write a babashka CLI script that transforms markdown files by adding `type: post` to their YAML frontmatter. It should be possible to specify individual files as well as a glob pattern.

The result will be a script like this one. It's not the prettiest and if you look closely there's definitely a few interesting idiosyncrasies but overall it's pretty good for something I didn't have 30 seconds ago!

- There's CLI options for verbose and dry-run modes
- Files to process can be specified via files or pattern options
- Emojis are used in progress messages 
- It's readable!

(If you properly want to read this code, [this Gist](https://gist.github.com/martinklepsch/b534f6be88cd48bf9aad4076dc2ccbfa) will display it better.)

```clojure
#!/usr/bin/env bb

(require '[babashka.cli :as cli]
         '[babashka.fs :as fs]
         '[clojure.string :as str]
         '[clj-yaml.core :as yaml])

(def cli-opts
  {:spec {:files {:desc "Individual markdown files to process"
                  :coerce []}
          :pattern {:desc "Regex pattern to match markdown files (e.g. \"posts/*.md\")"
                   :alias :p}
          :dry-run {:desc "Print what would be changed without making changes"
                   :coerce :boolean}
          :verbose {:desc "Print additional information during processing"
                   :coerce :boolean}}})

(defn extract-frontmatter
  "Extracts YAML frontmatter from markdown content.
   Returns [frontmatter remaining-content] or nil if no frontmatter found."
  [content]
  (when (str/starts-with? content "---\n")
    (when-let [end-idx (str/index-of content "\n---\n" 4)]
      (let [frontmatter (subs content 4 end-idx)
            remaining (subs content (+ end-idx 5))]
        [frontmatter remaining]))))

(defn update-frontmatter
  "Updates the frontmatter by adding type: post if not present"
  [markdown-str]
  (if-let [[frontmatter content] (extract-frontmatter markdown-str)]
    (let [data (yaml/parse-string frontmatter)
          updated-data (cond-> data
                        (not (:type data)) (assoc :type "post"))
          new-frontmatter (yaml/generate-string updated-data :dumper-options {:flow-style :block})]
      (str "---\n" new-frontmatter "---\n" content))
    markdown-str))

(defn process-file
  "Process a single markdown file, updating its frontmatter"
  [file {:keys [dry-run verbose]}]
  (let [content (slurp file)
        updated-content (update-frontmatter content)]
    (when verbose
      (println "📝 Processing" (str file)))
    (if (= content updated-content)
      (when verbose
        (println "⏭️ No changes needed for" (str file)))
      (do
        (when verbose
          (println "🔄 Updating frontmatter in" (str file)))
        (when-not dry-run
          (spit file updated-content))))))

(defn process-files
  "Process multiple markdown files based on CLI options"
  [{:keys [files pattern] :as opts}]
  (let [pattern-files (when pattern
                       (->> (fs/glob "." pattern)
                            (map fs/file)
                            (filter #(str/ends-with? (str %) ".md"))))
        all-files (concat (map fs/file files) pattern-files)]
    (if (seq all-files)
      (do
        (when (:verbose opts)
          (println "🔍 Found" (count all-files) "files to process"))
        (doseq [file all-files]
          (process-file file opts))
        (println "✨ Processing complete!"))
      (println "⚠️ No markdown files found to process"))))

(defn -main [& args]
  (let [opts (cli/parse-opts args cli-opts)]
    (if (:help opts)
      (println (cli/format-opts cli-opts))
      (process-files opts))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
```

## Context windows

Using a prepared context window/prompt I can describe something in a generic way and turn it into something concrete in a twist.

Here's two context windows that might be helpful to you:

1. [**The Babashka one**](https://github.com/ctxs-ai/ctxs.ai/blob/main/contexts/martinklepsch/babashka.md) previewed here
1. [**A JS/ClojureScript conversion helper**](https://github.com/ctxs-ai/ctxs.ai/blob/main/contexts/martinklepsch/js-cljs-conv.md) that understands Uix, Reagent & React and lets you translate code between those.

PRs to improve these are welcome!

I'm curious to make a full REPL loop that runs code that was generated this way. Natural language in, Clojure forms out, confirm to run. Could be fun!