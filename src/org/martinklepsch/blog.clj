(ns org.martinklepsch.blog
  (:require [org.martinklepsch.blog.common :as common]
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
     [:p {:itemprop "description"} (:description post)]]])

(defn signed-post [post]
  (conj (render-post post)
        [:span.article__signoff
         [:a {:href +twitter-uri+} "@martinklepsch"]
         ", " (date-fmt (:date-published post))]))

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
              [:p "This blog was born."]]))]])))

(defn index-page [global posts]
  (base
   (concat
    (list [:div#me
           [:a.marked {:href "/"} "Hi, I'm Martin."]
           [:span.me__do-it
            "You should follow me: "
            [:a {:target "_blank" :href +twitter-uri+} "@martinklepsch"]]])
    (for [post posts]
      (list (render-post post)
            [:hr])))))

(defn post-page [global post]
  (base (signed-post post)
        {:title (:title post)}))

;; <body class="tl-adelle">
;;   <div id="container">
;;     {{ content }}
;;   </div>
;;   <script src="/cljs-production/main.js" type="text/javascript"></script>
;;   <script type="text/javascript">

;;     var _gaq = _gaq || [];
;;     _gaq.push(['_setAccount', 'UA-3138561-8']);
;;     _gaq.push(['_setDomainName', 'martinklepsch.org']);
;;     _gaq.push(['_trackPageview']);

;;     setTimeout("_gaq.push(['_trackEvent', '15_seconds', 'read'])", 15000);

;;     (function() {
;;      var ga = document.createElement('script'); ga.type = 'text/javascript';
;;      ga.async = true;
;;      ga.src = ('https:' == document.location.protocol ? 'https://ssl'
;;        : 'http://www') + '.google-analytics.com/ga.js';
;;      var s = document.getElementsByTagName('script')[0];
;;      s.parentNode.insertBefore(ga, s);
;;      })();
;;    </script>
;; </body>
;; </html>
