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

(defn permalink-fn [{:keys [slug path filename] :as post-data}]
  (if (.startsWith path "posts")
    (str "posts/" slug ".html")
    (str (string/replace filename #"(?i).[a-z]+$" ".html"))))

(def ^:private +paginate-defaults+
  {:per-page 5
   :filterer identity
   :sortby (fn [metad] (:date-published metad))
   :comparator (fn [i1 i2] (compare i2 i1))})

(defn add-page [pages]
  (let [no-of-pages (count pages)
        add-key     (fn [i posts] (map #(assoc % :page [i no-of-pages]) posts))]
    (flatten (map-indexed add-key pages))))

(defn debug [metad & to-remove]
  (apply merge (map (fn [[k v]] {k (apply dissoc v to-remove)}) metad)))

(deftask paginate
  "Add :page key to perun metadata"
  [p per-page NUMBER int "Number of items to put on one page"
   f filterer FILTER code "Filter function"
   s sortby   SORTBY code "Sort by function"
   c comparator COMPARATOR code "Sort by comparator function"]
  (with-pre-wrap fileset
    (let [options  (merge +paginate-defaults+ *opts*)
          metadata (perun/get-meta fileset)
          files    (filter (:filterer options) metadata)
          sorted   (sort-by (:sortby options) (:comparator options) files)
          pages    (partition-all (:per-page options) sorted)]
      (doseq [[idx articles] (map-indexed vector pages)]
        (util/dbug "Articles on page %s:\n" (inc idx))
        (doseq [a articles]
          (util/dbug " - %s\n" (:title a))))
      (perun/merge-meta fileset (add-page pages)))))

(deftask build
  "Build blog."
  []
  (let [post? (fn [{:keys [path]}] (.startsWith path "posts"))
        page? (fn [{:keys [path]}] (.startsWith path "pages"))]
    (comp (sass :output-dir "public/stylesheets/")
          (p/base)
          (p/markdown)
          (p/slug)
          (p/permalink  :permalink-fn permalink-fn)
          (paginate     :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/post-page :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/simple-page :filterer page?)
          (p/collection :renderer 'org.martinklepsch.blog/error-page
                        :page     "error.html"
                        :filterer (constantly false))
          (p/collection :renderer 'org.martinklepsch.blog/archive-page
                        :page     "archive.html"
                        :filterer post?)
          (p/collection :renderer 'org.martinklepsch.blog/index-page
                        :groupby  #(-> % :page first blog/pagination-path)
                        :filterer post?))))

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
