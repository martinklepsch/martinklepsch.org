(set-env!
 :source-paths    #{"src" "_posts" "stylesheets"}
 :resource-paths  #{"resources"}
 :dependencies '[[jeluard/boot-notify "0.1.1" :scope "test"]
                 [pandeiro/boot-http  "0.6.3-SNAPSHOT" :scope "test"]
                 [boot-sassc          "0.1.2" :scope "test"]
                 [hiccup "1.0.5"]
                 [perun "0.1.2-SNAPSHOT"]])

(require '[jeluard.boot-notify :refer [notify]]
         '[pandeiro.boot-http  :refer [serve]]
         '[mathias.boot-sassc  :refer [sass]]
         '[clojure.string      :as  string]
         '[io.perun            :as    p]
         '[org.martinklepsch.blog :as blog])

(defn renderer [data] (:title data))

(defn jekyll-slug-fn [filename]
  (->> (string/split filename #"[-\.]")
       drop-last
       (drop 3)
       (string/join "-")
       string/lower-case))


(deftask build
  "Build blog."
  []
  (comp (sass :output-dir "public/stylesheets/")
        (p/markdown)
        (p/slug :slug-fn jekyll-slug-fn)
        (p/permalink)
        ;; (draft)
        ;; (ttr)
        (p/render :renderer blog/post-page)
        (p/collection :renderer blog/archive-page :page "archive.html")
        (p/collection :renderer blog/index-page :page "index.html")))

(deftask dev
  []
  (comp (serve :resource-root "public")
        (watch)
        (build)))
