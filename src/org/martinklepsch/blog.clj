(ns org.martinklepsch.blog
  (:require [org.martinklepsch.blog.common :as common]
            [boot.util :as util]
            [hiccup.page :as hp]
            [hiccup.core :as hiccup])
  (:import  java.text.SimpleDateFormat))

(defn trace [x]
  (prn x)
  x)

(def +twitter-uri+
  "https://twitter.com/martinklepsch")

(defn date-fmt [date]
  (.format (java.text.SimpleDateFormat. "MMMM yyyy") date))

(defn base
  ([content]
   (base content {}))
  ([content opts]
   (hp/html5 {:lang "en" :itemtype "http://schema.org/Blog"}
             (common/head :title (:title opts))
             [:body
              [:div#container content]])))

(defn render-post [post]
  (try
    [:article {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
     [:h1
      [:span.date
       ;; [:a.title {:href (str (:filename post)) :itemprop "name"} (:title post)]
       [:a {:href (:permalink post) :alt (str (:title post) " permalink page")}
        (date-fmt (:date-published post))]]
      (if (:resource post)
        (:title post) ; TODO add linkthing here
        ;; {{ post.title }} <a class="icon" href="{{ post.resource}}" alt="Link to external resource" target="blank">&#10150;</a>
        (:title post))]
     [:section.post_content
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

(defn signed-post [post]
  (conj (render-post post)
        [:span.article__signoff
         [:a {:href +twitter-uri+} "@martinklepsch"]
         ", " (date-fmt (:date-published post))]))

(defn error-page [_ _]
  (base
   [:section {:style "max-width: 733px; margin: 100px auto"}
    [:h1 "Oops. Looks like this page went missing."]
    [:p "Most likely you can still find it in the "
     [:a {:href "/archive.html"} "archive"] "."]]))

(defn archive-page [global posts]
  (base
   (list 
    [:div#me
     [:a.marked {:href "/"} "Hi, I'm Martin."]
     [:span.me__do-it "This is the archive."]]
    [:section
     [:ol#archive
      (concat
       (for [post posts]
         [:li
          [:span.date (date-fmt (:date-published post))]
          [:a {:href (:permalink post) :alt (:title post)} (:title post)]])
       (list [:li
              [:span.date "December 2011"]
              [:p "This blog came into existence."]]))]])))

(defn pagination-path [page-no]
  (if (= page-no 0)
    "index.html"
    (str "page" (inc page-no) "/index.html")))

(defn index-page [global posts]
  (let [[curr-page no-of-pages] (:page (first posts))]
    (base
     (concat
      (list [:div#me
             [:a.marked {:href "/"} "Hi, I'm Martin."]
             [:span.me__do-it
              "You should follow me: "
              [:a {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]])
      (for [post posts]
        (list (render-post post)
              (if-not (= (last posts) post) [:hr])))
      (if (> no-of-pages 1)
        (list [:section {:id "pagination"}
               (for [i (range no-of-pages)]
                 (if (= i curr-page)
                   [:strong (inc i)]
                   [:a {:href (str "/" (pagination-path i))} (inc i)]))]))))))

(defn post-page [global post]
  (base
   (list 
    [:div#me
     [:a.marked {:href "/"} "Hi, I'm Martin."]
     [:span.me__do-it
      "You should follow me: "
      [:a {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]]
    (signed-post post))
   {:title (:title post)}))

(defn simple-page [global page]
  (base
   (list
    [:div#me
     [:a.marked {:href "/"} "Hi, I'm Martin."]
     [:span.me__do-it
      "Say Hi on Twitter: "
      [:a {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]]
    [:article [:section (:content page)]])
   {:title (:title page)}))
