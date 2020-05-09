(set-env!
 :source-paths    #{"src" "stylesheets" "content"}
 :resource-paths  #{"resources"}
 :dependencies '[[org.clojure/clojure "1.9.0"]
                 [pandeiro/boot-http  "0.7.3" :scope "test"]
                 [adzerk/boot-reload  "0.4.12" :scope "test"]
                 [deraen/boot-sass    "0.2.1" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.21" :scope "test"]
                 [confetti/confetti   "0.1.5" :scope "test"]
                 [perun               "0.4.2-20170704.122243-5" :scope "test"]
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

;; TODO use original-path more where path is currently used
(defn permalink-fn [_ {:keys [slug path filename] :as post-data}]
  (if (.startsWith path "public/posts")
    (str "/posts/" slug ".html")
    (str "/" slug ".html")))

(defn slug-fn [_ {:keys [path filename] :as m}]
  (cond
    (.startsWith path "public/posts")
    (->> (string/split filename #"[-\.]")
         (drop 3)
         drop-last
         (string/join "-")
         string/lower-case)

    (.startsWith path "public/pages")
    (->> (string/split filename #"[-\.]")
         drop-last
         (string/join "-")
         string/lower-case)

    :else (throw (ex-info "Could not build slug" {:file m})) ))

#_(defn slug-dbg [_ m]
  (let [slug (slug-fn nil m)]
    (prn 'f (:filename m) 'slug slug)
    slug))

(deftask build
  "Build blog."
  []
  (let [post?     (fn [{:keys [original-path] :as m}] (some-> original-path (.startsWith "posts/")))
        page?     (fn [{:keys [original-path] :as m}] (some-> original-path (.startsWith "pages/")))]
    (comp (sass)
          (sift :move {#"martinklepschorg-v2.css" "public/stylesheets/martinklepschorg-v2.css"
                       #"martinklepschorg-v3.css" "public/stylesheets/martinklepschorg-v3.css"})
          (p/global-metadata)
          (p/markdown)
          (p/slug       :slug-fn slug-fn)
          (p/permalink  :permalink-fn permalink-fn)
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

(deftask build-to-site-dir
  []
  (comp
   (build)
   (sift :include #{#"^public/"})
   (sift :move {#"^public/" ""})
   (target :dir #{"_site"})))

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
