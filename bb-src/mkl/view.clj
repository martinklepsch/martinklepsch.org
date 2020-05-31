(ns mkl.view
  (:require [mkl.pods]
            [clojure.string :as string]
            [pod.retrogradeorbit.bootleg.utils :as utils]
            [mkl.posts]))

(defn with-base-url [s]
  (assert (.startsWith s "/") s)
  (str "https://martinklepsch.org" s))

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

(defn truncate [s length]
  (str (subs s 0 (min length (count s))) "..."))

(defn head
  [{:keys [title] :as opts}]
  (let [title (or title "Home")
        img   (:og-image opts)
        desc (or (:description opts)
                 (some-> (:content opts)
                         (string/replace #"<.*?>" "")
                         (string/replace #"\n" " ")
                         (string/trim)
                         (truncate 190))
                 "Personal Website and Blog of Martin Klepsch")]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:meta {:itemprop "author" :name "author" :content "Martin Klepsch (martinklepsch@googlemail.com)"}]
     [:meta {:name "keywords" :itemprop "keywords" :content "blog, clojure, development, clojurescript, heroku, amazon s3, aws"}]
     [:meta {:name "description" :itemprop "description" :content desc}]
     (when (:permalink opts)
       [:link {:rel "canonical" :href (with-base-url (:permalink opts))}])
     [:title (if title (str title " — Martin Klepsch") "Martin Klepsch")]
     ;; OpenGraph
     [:meta {:property "og:title" :content title}]
     [:meta {:property "og:type" :content "article"}]
     [:meta {:property "og:description" :content desc}]
     [:meta {:property "og:url" :content (some-> opts :permalink with-base-url)}]
     (when img [:meta {:property "og:image" :content (with-base-url img)}])
     [:meta {:property "og:site_name" :content "martinklepsch.org"}]
     ;; Twitter
     [:meta {:name "twitter:card" :content "summary"}]
     [:meta {:name "twitter:site" :content "@martinklepsch"}]
     [:meta {:name "twitter:creator" :content "@martinklepsch"}]
     [:meta {:name "twitter:title" :content title}]
     [:meta {:name "twitter:description" :content desc}]
     (when img [:meta {:name "twitter:image" :content (with-base-url img)}])
     ;; Misc
     [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
     [:link {:rel "alternate" :type "application/atom+xml" :title "Sitewide Atom Feed" :href "/atom.xml"}]
     [:link {:type "text/css" :rel "stylesheet"
             :href "/stylesheets/martinklepschorg-v3.css"}]
     (google-analytics)]))

(def +twitter-uri+
  "https://twitter.com/martinklepsch")

(defn date-fmt [date]
  "TODO fix date formatting"
  #_(.format (java.text.SimpleDateFormat. "MMMM yyyy") date))

(defn base
  [opts & content]
  [:html {:lang "en" :itemtype "http://schema.org/Blog"}
   (head opts)
   [:body.system-sans-serif.dark-gray
    (into [:div.mh3] content)]])

(defn render-post [{fm :frontmatter :as post} opts]
  (try
    [:article.mt5 {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
     (println (keys fm))
     [:div.f6.db.normal.mw6.center
      [:a.link {:href (:permalink fm) :title (str "Permalink: " (:title fm))}
       (if (:permalink-page? opts)
         (date-fmt (:date-published fm))
         "Latest Post")]
      (when (:permalink-page? opts) [:span.ph2 "/"])
      (when (:permalink-page? opts) [:a.link {:href "/" :title "Home"} "Home"])
      [:span.ph2 "/"]
      [:a.link {:href +twitter-uri+ :title "@martinklepsch on Twitter"} "@martinklepsch"]
      ]
     [:h1.f3.fw5.w-80-ns.lh-title.mw6.center.mv4
      ;; TODO add linkthing here
      (if (:resource fm) (:title fm) (:title fm))]
     [:section.mkdwn.lh-copy
      (:content post)]
     ;; Maybe implrement some of that stuff later
     ; [:div.item-meta
     ;    [:meta {:itemprop "author" :content (str (:author post) " (" (:author_email post) ")" )}]
     ;    [:img.author-avatar {:src (:author_avatar post) :title (:author post)}]
     ;    #_[:p.pub-data (str (dates/reformat-datestr (:date_published post) "YYYY-MM-dd", "MMM dd, YYYY") ", by " (:author post))
     ;       [:span.reading-time (str " " (:ttr post) " mins read")]]
     ;    [:p {:itemprop "description"} (:description post)]]
     ]
    (catch Exception e
      (println "Rendering %s failed:\n" (:slug post))
      (throw e))))

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

(defn index-page [{:keys [entries]}]
  (base
    {:permalink "/index.html"
     :og-image "/images/selfies/1.jpg"}
    [:div.mw7.center
     (render-post (first entries) {})
     [:div.mv6.mw6.center
      (posts-list "Other Posts" (->> entries rest (sort-by :date-published) reverse))]]))

(comment
  (spit "index.new.html"
        (-> (index-page {:entries [mkl.posts/test-post]})
            (utils/convert-to :html)))

  )

