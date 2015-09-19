(set-env!
 :source-paths    #{"src" "_posts" "stylesheets"}
 :resource-paths  #{"resources"}
 :dependencies '[[jeluard/boot-notify "0.1.1"          :scope "test"]
                 [pandeiro/boot-http  "0.6.3-SNAPSHOT" :scope "test"]
                 [mathias/boot-sassc  "0.1.5"          :scope "test"]
                 [hashobject/boot-s3  "0.1.0-SNAPSHOT" :scope "test"]
                 [perun               "0.1.3-SNAPSHOT" :scope "test"]
                 [hiccup              "1.0.5"]])

(require '[jeluard.boot-notify :refer [notify]]
         '[pandeiro.boot-http  :refer [serve]]
         '[mathias.boot-sassc  :refer [sass]]
         '[hashobject.boot-s3  :refer [s3-sync]]
         '[clojure.string      :as    string]
         '[io.perun            :as    p]
         '[org.martinklepsch.blog :as blog])

(defn renderer [data] (:title data))

(defn permalink-fn [post-data]
  (str  "/posts/" (:slug post-data) ".html"))

(deftask build
  "Build blog."
  []
  (comp (sass :output-dir "public/stylesheets/")
        (p/markdown)
        (p/slug)
        (p/permalink :permalink-fn permalink-fn)
        ;; (draft)
        ;; (ttr)
        (p/render     :renderer 'org.martinklepsch.blog/post-page)
        (p/collection :renderer 'org.martinklepsch.blog/archive-page :page "archive.html")
        (p/collection :renderer 'org.martinklepsch.blog/index-page :page "index.html")))

(deftask dev
  []
  (comp (serve :resource-root "public")
        (watch)
        (build)))

#_(deftask deploy-wip []
  (s3-sync :source "public"
           :bucket "www.martinklepsch.org/new"
           :access-key (System/getenv "S3_ACCESS_KEY")
           :secret-key (System/getenv "S3_SECRET_KEY")))
