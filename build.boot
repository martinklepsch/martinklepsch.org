(set-env!
 :source-paths    #{"src" "stylesheets" "content"}
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
         '[io.perun.core       :as    perun]
         '[org.martinklepsch.blog :as blog])

(defn renderer [data] (:title data))

(defn permalink-fn [post-data]
  (str  "/posts/" (:slug post-data) ".html"))

(def ^:private +paginate-defaults+
  {:per-page 5
   :sortby (fn [[path file]] (:date-published file))
   :comparator (fn [i1 i2] (compare i2 i1))})

(defn add-page-key [pages]
  (let [inj (fn [i posts] (map #(assoc-in % [1 :page] [i (count pages)]) posts))]
    (reduce (fn [x o] (merge x (into {} o))) {} (map-indexed inj pages))))

(deftask paginate
  "Add :page key to perun metadata"
  [p per-page NUMBER int "Number of items to put on one page"
   s sortby   SORTBY code "Sort by function"
   c comparator COMPARATOR code "Sort by comparator function"]
  (with-pre-wrap fileset
    (let [options (merge +paginate-defaults+ *opts*)
          files   (perun/get-meta fileset)
          sorted  (sort-by (:sortby options) (:comparator options) files)
          pages   (partition (:per-page options) sorted)]
      (perun/set-meta fileset (add-page-key pages)))))

(deftask build
  "Build blog."
  []
  (comp (sass :output-dir "public/stylesheets/")
        (p/markdown)
        (p/slug)
        (p/permalink :permalink-fn permalink-fn)
        (paginate)
        ;; (draft)
        ;; (ttr)
        (p/render     :renderer 'org.martinklepsch.blog/post-page)
        (p/collection :renderer 'org.martinklepsch.blog/error-page :page "error.html")
        (p/collection :renderer 'org.martinklepsch.blog/archive-page :page "archive.html")
        (p/collection :renderer 'org.martinklepsch.blog/index-page :groupby #(-> % :page first blog/pagination-path))))

(deftask dev
  []
  (comp (serve :resource-root "public")
        (watch)
        (build)))

(deftask deploy []
  (s3-sync :source "target/public"
           :bucket "www.martinklepsch.org"
           :access-key (System/getenv "S3_ACCESS_KEY")
           :secret-key (System/getenv "S3_SECRET_KEY")))
