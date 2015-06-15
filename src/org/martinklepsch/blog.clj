(ns org.martinklepsch.blog
  (:require [hiccup.page :as hp]
            [hiccup.core :as hiccup]))

(defn render-post [post]
  [:article {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
   [:h1
    [:span.date
     ;; [:a.title {:href (str (:filename post)) :itemprop "name"} (:title post)]
     [:a {:href (:permalink post) :alt (str (:title post) "permalink page")}
      (or (:date post) "no date specified")]]
    (if (:resource post)
      (:title post) ; TODO add linkthing here
      ;; {{ post.title }} <a class="icon" href="{{ post.resource}}" alt="Link to external resource" target="blank">&#10150;</a>
      (:title post))]
   [:section.post_content
    (:content post)]
   [:hr]
   #_[:div.item-meta
    [:meta {:itemprop "author" :content (str (:author post) " (" (:author_email post) ")" )}]
    [:img.author-avatar {:src (:author_avatar post) :title (:author post)}]
    #_[:p.pub-data (str (dates/reformat-datestr (:date_published post) "YYYY-MM-dd", "MMM dd, YYYY") ", by " (:author post))
     [:span.reading-time (str " " (:ttr post) " mins read")]]
     [:p {:itemprop "description"} (:description post)]]])
;; <article>
;; <h1>
;;   <span class="date"><a href="{{ post.url }}" alt="{{ post.title }} permalink page">{{ post.date | date: "%B %Y" }}</a></span>
;;   {% if post.resource %}
;;   {{ post.title }} <a class="icon" href="{{ post.resource}}" alt="Link to external resource" target="blank">&#10150;</a>
;;   {% else %}
;;     {{ post.title }}
;;   {% endif %}
;; </h1>
;; <section class='post_content'>
;; {{ post.content }}
;; </section>
;; </article>
;; <hr>



(defn index-render [posts]
  (hp/html5 {:lang "en" :itemtype "http://schema.org/Blog"}
    [:head
     ;; {% include opengraph.html %}
     ;; <link rel="alternate" type="application/rss+xml" href="feed.xml">
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
      [:meta {:itemprop "author" :name "author" :content "Martin Klephsch (martinklepsch@googlemail.com)"}]
      [:meta {:name "keywords" :itemprop "keywords" :content "hashobject, blog, clojure, development, heroku, amazon route 53, aws"}]
      [:meta {:name "description" :itemprop "description" :content "Personal Website and Blog of Martin Klepsch"}]
      [:title "Martin Klepsch"]
      [:link {:rel "shortcut icon" :href "images/favicon.ico"}]
      [:link {:rel "author" :href "humans.txt"}]
      [:link {:rel "alternate" :type "application/rss+xml" :title "RSS" :href "/feed.rss"}]
      (hp/include-css "/stylesheets/martinklepschorg-v2.css")
      (hp/include-css "http://fonts.googleapis.com/css?family=Open+Sans:300")
      #_(common/ga)
     ]
    [:body
       #_(common/header)
       [:div#container
        (for [post posts] (render-post post))]
       #_(common/footer)]))

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
