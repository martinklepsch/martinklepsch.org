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
  [{:keys [frontmatter] :as opts}]
  (let [title (or (:title frontmatter) "Martin Klepsch")
        img   (some-> frontmatter :og-image with-base-url)
        permalink (some-> frontmatter :permalink with-base-url)
        desc (or (:description frontmatter)
                 (some-> (:content opts)
                         (string/replace #"<.*?>" "")
                         (string/replace #"\n" " ")
                         (string/trim)
                         (truncate 190))
                 "Personal Website and Blog of Martin Klepsch")]
    (assert permalink)
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:meta {:itemprop "author" :name "author" :content "Martin Klepsch (martinklepsch@googlemail.com)"}]
     [:meta {:name "keywords" :itemprop "keywords" :content "blog, clojure, development, clojurescript, heroku, amazon s3, aws"}]
     [:meta {:name "description" :itemprop "description" :content desc}]
     (when permalink [:link {:rel "canonical" :href permalink}])
     [:title title]
     ;; OpenGraph
     [:meta {:property "og:title" :content title}]
     [:meta {:property "og:type" :content "article"}]
     [:meta {:property "og:description" :content desc}]
     (when permalink [:meta {:property "og:url" :content permalink}])
     (when img [:meta {:property "og:image" :content img}])
     [:meta {:property "og:site_name" :content "martinklepsch.org"}]
     ;; Twitter
     [:meta {:name "twitter:card" :content "summary"}]
     [:meta {:name "twitter:site" :content "@martinklepsch"}]
     [:meta {:name "twitter:creator" :content "@martinklepsch"}]
     [:meta {:name "twitter:title" :content title}]
     [:meta {:name "twitter:description" :content desc}]
     (when img [:meta {:name "twitter:image" :content img}])
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

(defn prose-edit-link [p]
  [:a.white
   {:target "_blank"
    :href (str "https://prose.io/#martinklepsch/martinklepsch.org/edit/master/content/" p)}
   "edit on prose.io"])

(defn signed-post [post opts]
  (conj (render-post post opts)
        [:div.mv4.em-before.mw6.center
         [:a.link {:href +twitter-uri+} "@martinklepsch"] ", "
         (date-fmt (:date-published post)) " "
         (prose-edit-link (:original-path post))]))

(defn posts-list [title entries]
  [:section.lh-copy
   (when title [:h3.mb0 title])
   (when title [:div.bb.bw1.b--silver.w2.mv4])
   (into
     [:ol.list.pa0]
     (for [post entries]
       [:li.mb3
        [:a.f4.link.mr2
         {:href (-> post :frontmatter :permalink)}
         (-> post :frontmatter :title)]
        [:span.db.dib-ns.f6 (date-fmt (:date-published (:frontmatter post)))]]))])

(defn index-page [posts]
  (base
    {:frontmatter {:permalink "/index.html"
                   :og-image "/images/selfies/1.jpg"}}
    [:div.mw7.center
     (render-post (last posts) {})
     [:div.mv6.mw6.center
      (posts-list "Other Posts" (->> posts rest (sort-by :date-published) reverse))]]))

(defn post-page [{:keys [post]}]
  (base
    post
    [:div.mw7.center.mb6
     (signed-post post {:permalink-page? true})]))

(comment
  (spit "index.new.html"
        (-> (index-page {:entries [mkl.posts/test-post]})
            (utils/convert-to :html)))

  )

