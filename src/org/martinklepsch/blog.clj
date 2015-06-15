(ns org.martinklepsch.blog
  (:require [hiccup.page :as hp]
            [hiccup.core :as hiccup]))

(defn render-post [post]
  [:li.item {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
   [:a.title {:href (str (:filename post)) :itemprop "name"} (:title post)]
   [:div.item-meta
    [:meta {:itemprop "author" :content (str (:author post) " (" (:author_email post) ")" )}]
    [:img.author-avatar {:src (:author_avatar post) :title (:author post)}]
    #_[:p.pub-data (str (dates/reformat-datestr (:date_published post) "YYYY-MM-dd", "MMM dd, YYYY") ", by " (:author post))
     [:span.reading-time (str " " (:ttr post) " mins read")]]
     [:p {:itemprop "description"} (:description post)]]])


(defn index-render [posts]
  (hp/html5 {:lang "en" :itemtype "http://schema.org/Blog"}
    [:head
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, user-scalable=no"}]
      [:meta {:itemprop "author" :name "author" :content "hashobject (team@hashobject.com)"}]
      [:meta {:name "keywords" :itemprop "keywords" :content "hashobject, blog, clojure, development, heroku, amazon route 53, aws"}]
      [:meta {:name "description" :itemprop "description" :content "Hashobject - software engineering, design and application development"}]
      [:title "Hashobject team blog about development and design"]
      [:link {:rel "shortcut icon" :href "/favicon.ico"}]
      [:link {:rel "publisher" :href "https://plus.google.com/118068495795214676039"}]
      [:link {:rel "author" :href "humans.txt"}]
      [:link {:rel "alternate" :type "application/rss+xml" :title "RSS" :href "/feed.rss"}]
      (hp/include-css "/css/app.css")
      (hp/include-css "http://fonts.googleapis.com/css?family=PT+Sans")
      #_(common/ga)
     ]
    [:body
       #_(common/header)
       [:div.row.content
         [:ul.items.columns.small-12
          (for [post posts] (render-post post))]]
       #_(common/footer)]))
