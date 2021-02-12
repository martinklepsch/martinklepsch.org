(ns mkl.atom
  (:require [mkl.pods]
            [mkl.posts :as posts]
            [mkl.view :as view]
            [pod.retrogradeorbit.bootleg.utils :as utils]))

(def site-root "https://www.martinklepsch.org")

(def author
  [:author
   [:name "Martin Klepsch"]
   [:email "martinklepsch@googlemail.com"]])

(defn utc-date [date]
  (-> (pr-str date)
      (subs 7 29)
      (str "Z")))

(defn post->atom-entry [post]
  (let [fm (-> post :frontmatter)
        url (str site-root (:permalink fm))]
    [:entry
     [:id (str "urn:uuid:" (:uuid fm))]
     [:title (:title fm)]
     [:published (utc-date (:date-published fm))]
     [:updated (utc-date (:date-published fm))]
     [:link {:href url :type "text/html" :title (:title fm) :rel "alternate"}]
     [:content {:type "html" "xml:base" url}
      (utils/as-html [:h1 (:title fm)])
      (utils/as-html (:content post))
      (utils/as-html [:p [:a {:href view/+twitter-uri+} "reply on twitter"]])]
     author]))


(defn -main []
  (let [posts (posts/sort-posts (map posts/read-post posts/post-files))]
    (spit "_site/atom.xml"
          (-> [:feed {:xmlns "http://www.w3.org/2005/Atom"}
               [:title "Martin Klepsch"]
               [:subtitle "Martin Klepsch's blog"]
               [:generator "a bespoke mix of babashka and bootleg"]
               [:link {:href site-root :type "text/html"}]
               [:updated (utc-date (-> posts first :frontmatter :date-published))]
               [:id site-root]
               author
               (for [post posts]
                 (post->atom-entry (assoc post :type :post)))]

              (utils/convert-to :xml)))))
