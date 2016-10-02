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

(defn render-post [post]
  (try
    [:article.lh-copy {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
     [:h1
      [:a.db.f6.normal.link {:href (:permalink post) :title (str "Permalink: " (:title post))}
       (date-fmt (:date-published post))]
      (if (:resource post)
        (:title post) ; TODO add linkthing here
        ;; {{ post.title }} <a class="icon" href="{{ post.resource}}" alt="Link to external resource" target="blank">&#10150;</a>
        (:title post))]
     [:section.mkdwn
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
        [:div.mv4.em-before
         [:a.link {:href +twitter-uri+} "@martinklepsch"]
         ", " (date-fmt (:date-published post))]))

(defn posts-list [title entries]
  [:section.lh-copy.mv5
   (when title [:h3 title])
   (into
    [:ol.list.pa0]
    (for [post entries]
      [:li.mb2
       [:a.f4.link.mr2
        {:href (:permalink post)}
        (:title post)]
       [:span.db.dib-ns.f6 (date-fmt (:date-published post))]]))])

(defn archive-page [{:keys [entries]}]
  (base
   {}
   [:div.mt4
    [:a.link {:href "/"} "Hi, I'm Martin. "]
    [:span "This is the archive."]]
   (posts-list "All Posts. Ever." entries)
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

(defn index-page [{:keys [entries]}]
  (base
   {}
   [:div.mt4
    [:a.link {:href "/"} "Hi, I'm Martin. "]
    [:span
     "Say Hi on Twitter: "
     [:a.link {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]]

   [:div.mw6 (render-post (first entries))]

   (posts-list "Older Posts" (->> entries rest (sort-by :date-published) reverse))))

(def me
  [:div.mt4
   [:a.link {:href "/"} "Hi, I'm Martin. "]
   [:span
    "Say Hi on Twitter: "
    [:a.link {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]])

(defn post-page [{:keys [entry entries]}]
  (base
   {:title (:title entry)}
   me
   [:div.mw6 (signed-post entry)]
   (posts-list "Other Posts" (->> entries (remove #{entry}) (sort-by :date-published) reverse))))

(defn simple-page [{:keys [entry]}]
  (base
   {:title (:title entry)}
   me
   [:article
    [:h1 (:title entry)]
    [:section (:content entry)]]))

(defn daily-ui-page [{:keys [entries] :as stuff}]
  (base
   {}
   [:link {:rel "stylesheet" :href "/tachyons.css"}]
   (let [grouped (group-by #(first (re-seq #"\d{3}" (:short-filename %))) entries)]
     (for [[day images] (reverse (sort-by first grouped))]
       [:div
        [:span.db.pa4
         {:id day}
         (str "Daily UI #" day)
         [:a.fr {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]
        [:div.dt
         (map (fn [{f :filename}]
                (let [uri (str "/daily-ui/" f)]
                  [:a.dtc.bn.v-top.pr4 {:href uri}
                   [:img {:src uri}]]))
              images)]]))))