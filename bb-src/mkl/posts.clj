(ns mkl.posts
  (:require [mkl.pods]
            [mkl.frontmatter :as fm]
            [pod.retrogradeorbit.bootleg.markdown :as markdown]
            [pod.retrogradeorbit.bootleg.glob :as glob]))

(def post-files
  (sort (glob/glob "content/posts/*.md")))

(def onehundred-files
  (sort (glob/glob "content/onehundred/*.md")))

(def date-format (java.text.SimpleDateFormat. "MMM dd"))
(def date-long-format (java.text.SimpleDateFormat. "MMM dd, YYY"))
(def year-format (java.text.SimpleDateFormat. "YYY"))
(.format date-format (java.util.Date.))

(defn read-post [file]
    (let [frontmatter (fm/get-frontmatter file)]
      {:file file
       :frontmatter frontmatter
       :ui {:year (.format  year-format (:date-published frontmatter))
            :date-short (.format  date-format (:date-published frontmatter))
            :date-long (.format  date-long-format (:date-published frontmatter))
            }
       :source (slurp file)
    ;; :content to be compatible with view code
       :content (-> file fm/file-contents second (markdown/markdown :data))}))

(defn sort-posts [posts]
  (->> posts (sort-by (comp :date-published :frontmatter)) reverse))

(def test-post
  (read-post (first post-files)))

(defn -main []
  (read-post (first post-files)))

;; TODO registry
;; maintain a list of posts in memory
;; when files change reread the post data for the post with the same :file key

(comment
  (read-post (first post-files))
  (def file (first post-files))
  (-> file fm/file-contents second (markdown/markdown :data :html))

  )
