(ns org.martinklepsch.blog.common
  (:require [hiccup.page :as hp]
            [hiccup.core :as hiccup]))

(defn google-analytics []
  [:script {:type "text/javascript"}
   "var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-3138561-8']);
    _gaq.push(['_setDomainName', 'martinklepsch.org']);
    _gaq.push(['_trackPageview']);

    setTimeout(\"_gaq.push(['_trackEvent', '15_seconds', 'read'])\", 15000);

    (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl'
    : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
    })();"])


(defn opengraph [opts]
  ;; TODO use this
  (list
   [:meta {:property "og:title" :content (:title opts)}]
   [:meta {:property "og:type" :content "article"}]
   [:meta {:property "og:description" :content (:abstract opts)}]
   [:meta {:property "og:url" :content (str "http://martinklepsch.org" (:permalink opts))}]
   (if-let [fbi (:FBimage opts)]
     [:meta {:property "og:image" :content (str "http://martinklepsch.org" fbi)}])
   [:meta {:property "og:site_name" :content "martinklepsch.org"}]
   [:meta {:property "fb:admins" :content "1539288439"}]))

(defn head
  [& {:keys [title] :as opts}]
  [:head
   ;; {% include opengraph.html %}
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
   [:meta {:itemprop "author" :name "author" :content "Martin Klephsch (martinklepsch@googlemail.com)"}]
   [:meta {:name "keywords" :itemprop "keywords" :content "blog, clojure, development, clojurescript, heroku, amazon s3, aws"}]
   [:meta {:name "description" :itemprop "description" :content "Personal Website and Blog of Martin Klepsch"}]
   [:title (if title (str title " — Martin Klepsch") "Martin Klepsch")]
   [:link {:rel "shortcut icon" :href "images/favicon.ico"}]
   [:link {:rel "author" :href "humans.txt"}]
   ;[:link {:rel "alternate" :type "application/rss+xml" :title "RSS" :href "/feed.rss"}]
   [:link {:type "text/css" :rel "stylesheet"
           :href "/stylesheets/martinklepschorg-v2.css"}]
   [:link {:type "text/css" :rel "stylesheet"
           :href "http://fonts.googleapis.com/css?family=Open+Sans:300|Roboto+Slab:400,700"}]
   (google-analytics)])

