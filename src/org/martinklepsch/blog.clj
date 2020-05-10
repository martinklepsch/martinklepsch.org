(ns org.martinklepsch.blog
  (:require [org.martinklepsch.blog.common :as common]
            [boot.util :as util]
            [hiccup.page :as hp])
  (:import  java.text.SimpleDateFormat))

(defn trace [x]
  (prn x)
  x)

(def +twitter-uri+
  "https://twitter.com/martinklepsch")

(defn date-fmt [date]
  (.format (java.text.SimpleDateFormat. "MMMM yyyy") date))

(defn base
  [opts & content]
  (hp/html5 {:lang "en" :itemtype "http://schema.org/Blog"}
            (common/head :title (:title opts))
            [:body.system-sans-serif.dark-gray
             (into [:div.mh3] content)]))

(defn render-post [post opts]
  (try
    [:article.mt5 {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
     [:div.f6.db.normal.mw6.center
      [:a.link {:href (:permalink post) :title (str "Permalink: " (:title post))}
       (if (:permalink-page? opts)
         (date-fmt (:date-published post))
         "Latest Post")]
      (when (:permalink-page? opts) [:span.ph2 "/"])
      (when (:permalink-page? opts) [:a.link {:href "/" :title "Home"} "Home"])
      [:span.ph2 "/"]
      [:a.link {:href +twitter-uri+ :title "@martinklepsch on Twitter"} "@martinklepsch"]
      ;; [:span.ph2 "/"]
      ;; [:a.link {:href "/archive.html" :title "View all posts"} "Older Posts"]
      ]
     [:h1.f1-ns.f2.fw1.w-80-ns.lh-title.mw6.center
      (if (:resource post)
        (:title post) ; TODO add linkthing here
        ;; {{ post.title }} <a class="icon" href="{{ post.resource}}" alt="Link to external resource" target="blank">&#10150;</a>
        (:title post))]
     [:section.mkdwn.lh-copy
      (:content post)]
     ;; Maybe implrement some of that stuff later
     #_[:div.item-meta
        [:meta {:itemprop "author" :content (str (:author post) " (" (:author_email post) ")" )}]
        [:img.author-avatar {:src (:author_avatar post) :title (:author post)}]
        #_[:p.pub-data (str (dates/reformat-datestr (:date_published post) "YYYY-MM-dd", "MMM dd, YYYY") ", by " (:author post))
           [:span.reading-time (str " " (:ttr post) " mins read")]]
        [:p {:itemprop "description"} (:description post)]]]
    (catch Exception e
      (util/fail "Rendering %s failed:\n" (:slug post))
      (throw e))))

(defn signed-post [post opts]
  (conj (render-post post opts)
        [:div.mv4.em-before.mw6.center
         [:a.link {:href +twitter-uri+} "@martinklepsch"]
         ", " (date-fmt (:date-published post))]))

(defn posts-list [title entries]
  [:section.lh-copy
   (when title [:h3.mb0 title])
   (when title [:div.bb.bw1.b--silver.w2.mv4])
   (into
    [:ol.list.pa0]
    (for [post entries]
      [:li.mb3
       [:a.f4.link.mr2
        {:href (:permalink post)}
        (:title post)]
       [:span.db.dib-ns.f6 (date-fmt (:date-published post))]]))])

(defn posts-blocks [title entries]
  [:section.lh-copy
   (when title [:h3.mb0.ph2 title])
   (when title [:div.bb.bw1.b--silver.w2.mv4])
   (into
    [:ol.list.pa0]
    (for [post entries]
      [:li
       [:a.w-50.fl.pv2.h4.border-box.link
        {:href (:permalink post)}
        [:div.ph2.bl.b--blue.bw2
         [:span.db.f4.mr2 (:title post)]
         [:span.f6.dark-gray (date-fmt (:date-published post))]]]]))])

(defn archive-page [{:keys [entries]}]
  (base
   {}
   [:div.mt4.mw7.center
    [:a.link {:href "/"} "Hi, I'm Martin. "]
    [:span "This is the archive."]
    (posts-list "All Posts. Ever." entries)]
   #_[:section.lh-copy
    [:ol.list
     (concat
      (for [post entries]
        [:li
         [:a.link {:href (:permalink post) :alt (:title post)} (:title post)]
         [:span.ml2 (date-fmt (:date-published post))]])
      (list [:li
             [:span "This blog came into existence."]
             [:span.ml2 "December 2011"]
             ]))]]))

(defn pagination-path [page-no]
  (if (= page-no 0)
    "index.html"
    (str "page" (inc page-no) "/index.html")))

(def me
  [:div.mt4.f6
   [:a.link {:href "/"} "Hi, I'm Martin. "]
   [:span
    "Say Hi on Twitter: "
    [:a.link {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]])

(defn index-page [{:keys [entries]}]
  (base
   {}
   [:div.mw7.center
    (render-post (first entries) {})
    [:div.mv6.mw6.center
     (posts-list "Other Posts" (->> entries rest (sort-by :date-published) reverse))]]))

(defn post-page [{:keys [entry entries]}]
  (base
   {:title (:title entry)}
   [:div.mw7.center.mb6
    (signed-post entry {:permalink-page? true})
    #_[:div.mv6.mw6.center
     (posts-list "Other Posts" (->> entries (remove #{entry}) (sort-by :date-published) reverse))]]))

(defn simple-page [{:keys [entry]}]
  (base
   {:title (:title entry)}
   [:div.mt5.mw6.center me]
   [:article.mb6
    [:h1.f1-ns.f2.fw1.w-80-ns.lh-title.mw6.center (:title entry)]
    [:section.mkdwn.lh-copy (:content entry)]]))
