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

(defn head
  [{:keys [title] :as opts}]
  ;; (prn (dissoc opts :content))
  ;; TODO use first couple of lines from :content
  (let [desc (or (:description title) "Personal Website and Blog of Martin Klepsch")]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:meta {:itemprop "author" :name "author" :content "Martin Klepsch (martinklepsch@googlemail.com)"}]
     [:meta {:name "keywords" :itemprop "keywords" :content "blog, clojure, development, clojurescript, heroku, amazon s3, aws"}]
     [:meta {:name "description" :itemprop "description" :content desc}]
     [:title (if title (str title " — Martin Klepsch") "Martin Klepsch")]
     ;; OpenGraph
     [:meta {:property "og:title" :content (:title opts)}]
     [:meta {:property "og:type" :content "article"}]
     [:meta {:property "og:description" :content desc}]
     [:meta {:property "og:url" :content (str "http://martinklepsch.org" (:permalink opts))}]
     [:meta {:property "og:image" :content (str "http://martinklepsch.org" (:og-image opts))}]
     [:meta {:property "og:site_name" :content "martinklepsch.org"}]
     ;; Misc
     [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
     [:link {:rel "alternate" :type "application/atom+xml" :title "Sitewide Atom Feed" :href "/atom.xml"}]
     [:link {:type "text/css" :rel "stylesheet"
             :href "/stylesheets/martinklepschorg-v3.css"}]
     (google-analytics)]))
