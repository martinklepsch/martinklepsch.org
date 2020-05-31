(ns mkl.posts
  (:require [mkl.pods]
            [mkl.view]
            [mkl.frontmatter :as fm]
            [pod.retrogradeorbit.bootleg.utils :as utils]
            [pod.retrogradeorbit.bootleg.html :as html]
            [pod.retrogradeorbit.bootleg.markdown :as markdown]
            [pod.retrogradeorbit.bootleg.glob :as glob]))

(def post-files
  (sort
    (into (glob/glob "content/posts/*.markdown")
          (glob/glob "content/posts/*.md"))))

(defn read-post [file]
  {:file file
   :frontmatter (fm/get-frontmatter file)
   :source (slurp file)

   ;; :content to be compatible with view code
   :content (-> file fm/file-contents second (markdown/markdown :data))})

(def test-post
  (read-post (first post-files)))

(defn -main []
  (read-post (first post-files)))

(comment
  (read-post (first post-files))
  (def file (first post-files))
  (-> file fm/file-contents second (markdown/markdown :data :html))

  (utils/convert-to [:div [:span "xx"]] :html)
  (-> (mkl.view/index-page {:entries [(read-post (first post-files))]})
      (utils/convert-to :html)
      ;; (spit "index.new.html")
      )
  )
