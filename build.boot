(set-env!
 :source-paths    #{"src" "stylesheets" "content"}
 :resource-paths  #{"resources"}
 :dependencies '[[pandeiro/boot-http  "0.7.3" :scope "test"]
                 [adzerk/boot-reload  "0.4.12" :scope "test"]
                 [deraen/boot-sass    "0.2.1" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.21" :scope "test"]
                 [confetti/confetti   "0.1.5" :scope "test"]
                 [perun               "0.4.1-SNAPSHOT" :scope "test"]
                 [hiccup              "1.0.5"]])

(require '[pandeiro.boot-http  :refer [serve]]
         '[deraen.boot-sass    :refer [sass]]
         '[confetti.boot-confetti :refer [sync-bucket create-site]]
         '[boot.util           :as    util]
         '[clojure.string      :as    string]
         '[io.perun            :as    p]
         '[io.perun.core       :as    perun]
         '[org.martinklepsch.blog :as blog])

(defn renderer [data] (:title data))

(defn permalink-fn [{:keys [slug path filename] :as post-data}]
  (if (.startsWith path "posts")
    (str "/posts/" slug ".html")
    (str (string/replace filename #"(?i).[a-z]+$" ".html"))))

(deftask build
  "Build blog."
  []
  (let [post?     (fn [{:keys [path]}] (.startsWith path "posts"))
        page?     (fn [{:keys [path]}] (.startsWith path "pages"))]
    (comp (sass)
          (sift :move {#"martinklepschorg-v2.css" "public/stylesheets/martinklepschorg-v2.css"
                       #"martinklepschorg-v3.css" "public/stylesheets/martinklepschorg-v3.css"})
          (p/base)
          (p/global-metadata)
          (p/markdown)
          (p/slug)
          (p/permalink  :permalink-fn permalink-fn)
          (p/canonical-url)
          (p/collection :renderer 'org.martinklepsch.blog/archive-page
                        :page     "archive.html"
                        :filterer post?)
          (p/collection :renderer 'org.martinklepsch.blog/index-page
                        ;; :groupby  #(-> % :page first blog/pagination-path)
                        :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/post-page :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/simple-page :filterer page?)
          (p/atom-feed :filterer post?
                       :site-title "Martin Klepsch"
                       :description "Martin Klepsch's blog"
                       :base-url "https://www.martinklepsch.org/"))))

(deftask dev
  []
  (comp (serve :resource-root "public"
               :port 4000)
        (watch)
        (speak)
        (build)))

(deftask deploy []
  (comp
   (build)
   (sift :include #{#"^public/"})
   (sift :move {#"^public/" ""})
   (sync-bucket :bucket (System/getenv "S3_BUCKET_NAME")
                :access-key (System/getenv "AWS_ACCESS_KEY")
                :secret-key (System/getenv "AWS_SECRET_KEY")
                :cloudfront-id (System/getenv "CLOUDFRONT_ID")
                :prune true)))
