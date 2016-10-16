(set-env!
 :source-paths    #{"src" "stylesheets" "content"}
 :resource-paths  #{"resources"}
 :dependencies '[[pandeiro/boot-http  "0.7.3" :scope "test"]
                 [adzerk/boot-reload  "0.4.12" :scope "test"]
                 [deraen/boot-sass    "0.2.1" :scope "test"]
                 [org.slf4j/slf4j-nop "1.7.21" :scope "test"]
                 [confetti/confetti   "0.1.2-SNAPSHOT" :scope "test"]
                 [perun               "0.4.0-SNAPSHOT" :scope "test"]
                 [hiccup              "1.0.5"]])

(require '[pandeiro.boot-http  :refer [serve]]
         ;; '[mathias.boot-sassc  :refer [sass]]
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
  (let [post?     (fn [{:keys [path]}] (.startsWith path "posts"))
        daily-ui? (fn [{:keys [path]}] (.startsWith path "public/daily-ui"))
        page?     (fn [{:keys [path]}] (.startsWith path "pages"))]
    (comp (sass)
          (sift :move {#"martinklepschorg-v2.css" "public/stylesheets/martinklepschorg-v2.css"
                       #"martinklepschorg-v3.css" "public/stylesheets/martinklepschorg-v3.css"})
          (p/base)
          (p/global-metadata)
          (p/markdown)
          (p/slug)
          (p/permalink  :permalink-fn permalink-fn)
          ;; (paginate     :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/post-page :filterer post?)
          (p/render     :renderer 'org.martinklepsch.blog/simple-page :filterer page?)
          (p/collection :renderer 'org.martinklepsch.blog/archive-page
                        :page     "archive.html"
                        :filterer post?)
          (p/collection :renderer 'org.martinklepsch.blog/daily-ui-page
                        :page     "daily-ui/index.html"
                        :filterer daily-ui?)
          (p/collection :renderer 'org.martinklepsch.blog/index-page
                        ;; :groupby  #(-> % :page first blog/pagination-path)
                        :filterer post?)
          (p/canonical-url)
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

(def confetti-edn
  (read-string (slurp "martinklepsch-org.confetti.edn")))

(deftask deploy []
  (comp
   (build)
   (sift :include #{#"^public/"})
   (sift :move {#"^public/" ""})
   (sync-bucket :bucket (:bucket-name confetti-edn)
                :prune true
                :cloudfront-id (:cloudfront-id confetti-edn)
                :access-key (:access-key confetti-edn)
                :secret-key (:secret-key confetti-edn))))