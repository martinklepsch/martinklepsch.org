(ns mkl.view
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [mkl.pods]
            [mkl.posts :as posts]
            [pod.retrogradeorbit.bootleg.utils :as utils]))

(defn with-base-url
  [s]
  (assert (.startsWith s "/") s)
  (str "https://martinklepsch.org" s))

(defn truncate
  [length s]
  (when-not (seq s)
    (throw (ex-info "truncate called with blank string" {})))
  (str (subs s 0 (min length (count s))) "..."))

(defn content->desc
  [content]
  (some->> content
           flatten
           (filter string?)
           (remove string/blank?)
           (take 10)
           (map string/trim)
           (string/join " ")
           (truncate 190)))

(defn head
  [{:keys [frontmatter] :as opts}]
  (let [title (if-let [t (:title frontmatter)]
                (str t " — Martin Klepsch")
                "Martin Klepsch")
        title-social (or (:title frontmatter) "Martin Klepsch")
        img   (some-> frontmatter :og-image with-base-url)
        permalink (some-> frontmatter :permalink with-base-url)
        desc (or (:description frontmatter)
                 (content->desc (:content opts))
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
     [:meta {:property "og:title" :content title-social}]
     [:meta {:property "og:type" :content "article"}]
     [:meta {:property "og:description" :content desc}]
     (when permalink [:meta {:property "og:url" :content permalink}])
     (when img [:meta {:property "og:image" :content img}])
     [:meta {:property "og:site_name" :content "martinklepsch.org"}]
     ;; Twitter
     [:meta {:name "twitter:card" :content "summary"}]
     [:meta {:name "twitter:site" :content "@martinklepsch"}]
     [:meta {:name "twitter:creator" :content "@martinklepsch"}]
     [:meta {:name "twitter:title" :content title-social}]
     [:meta {:name "twitter:description" :content desc}]
     (when img [:meta {:name "twitter:image" :content img}])
     ;; Misc
     [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
     [:link {:rel "alternate" :type "application/atom+xml" :title "Sitewide Atom Feed" :href "/atom.xml"}]
     ;; [:link {:type "text/css" :rel "stylesheet" :href "https://unpkg.com/basscss@8.0.2/css/basscss.min.css"}]
     [:link {:type "text/css"
             :rel "stylesheet"
             :href "/stylesheets/martinklepschorg-v4.css"}]]))

(def +twitter-uri+
  "https://twitter.com/martinklepsch")

(def +bluesky-uri+
  "https://bsky.app/profile/martinklepsch.org")

(def bluesky-icon
  [:svg
   {:data-icon "fa6-brands:bluesky",
    :height "1em",
    :viewbox "0 0 576 512",
    :width "1.13em"}
   [:symbol {:id "ai:fa6-brands:bluesky"}
    [:path
     {:d "M407.8 294.7c-3.3-.4-6.7-.8-10-1.3c3.4.4 6.7.9 10 1.3M288 227.1c-26.1-50.7-97.1-145.2-163.1-191.8C61.6-9.4 37.5-1.7 21.6 5.5C3.3 13.8 0 41.9 0 58.4S9.1 194 15 213.9c19.5 65.7 89.1 87.9 153.2 80.7c3.3-.5 6.6-.9 10-1.4c-3.3.5-6.6 1-10 1.4c-93.9 14-177.3 48.2-67.9 169.9C220.6 589.1 265.1 437.8 288 361.1c22.9 76.7 49.2 222.5 185.6 103.4c102.4-103.4 28.1-156-65.8-169.9c-3.3-.4-6.7-.8-10-1.3c3.4.4 6.7.9 10 1.3c64.1 7.1 133.6-15.1 153.2-80.7C566.9 194 576 75 576 58.4s-3.3-44.7-21.6-52.9c-15.8-7.1-40-14.9-103.2 29.8C385.1 81.9 314.1 176.4 288 227.1",
      :fill "currentColor"}]]
   [:use {:xlink:href "#ai:fa6-brands:bluesky"}]])

(def github-icon
  [:svg
   {:data-icon "fa6-brands:github",
    :height "1em",
    :viewbox "0 0 496 512",
    :width "0.97em"}
   [:symbol {:id "ai:fa6-brands:github"}
    [:path
     {:d
      "M165.9 397.4c0 2-2.3 3.6-5.2 3.6c-3.3.3-5.6-1.3-5.6-3.6c0-2 2.3-3.6 5.2-3.6c3-.3 5.6 1.3 5.6 3.6m-31.1-4.5c-.7 2 1.3 4.3 4.3 4.9c2.6 1 5.6 0 6.2-2s-1.3-4.3-4.3-5.2c-2.6-.7-5.5.3-6.2 2.3m44.2-1.7c-2.9.7-4.9 2.6-4.6 4.9c.3 2 2.9 3.3 5.9 2.6c2.9-.7 4.9-2.6 4.6-4.6c-.3-1.9-3-3.2-5.9-2.9M244.8 8C106.1 8 0 113.3 0 252c0 110.9 69.8 205.8 169.5 239.2c12.8 2.3 17.3-5.6 17.3-12.1c0-6.2-.3-40.4-.3-61.4c0 0-70 15-84.7-29.8c0 0-11.4-29.1-27.8-36.6c0 0-22.9-15.7 1.6-15.4c0 0 24.9 2 38.6 25.8c21.9 38.6 58.6 27.5 72.9 20.9c2.3-16 8.8-27.1 16-33.7c-55.9-6.2-112.3-14.3-112.3-110.5c0-27.5 7.6-41.3 23.6-58.9c-2.6-6.5-11.1-33.3 2.6-67.9c20.9-6.5 69 27 69 27c20-5.6 41.5-8.5 62.8-8.5s42.8 2.9 62.8 8.5c0 0 48.1-33.6 69-27c13.7 34.7 5.2 61.4 2.6 67.9c16 17.7 25.8 31.5 25.8 58.9c0 96.5-58.9 104.2-114.8 110.5c9.2 7.9 17 22.9 17 46.4c0 33.7-.3 75.4-.3 83.6c0 6.5 4.6 14.4 17.3 12.1C428.2 457.8 496 362.9 496 252C496 113.3 383.5 8 244.8 8M97.2 352.9c-1.3 1-1 3.3.7 5.2c1.6 1.6 3.9 2.3 5.2 1c1.3-1 1-3.3-.7-5.2c-1.6-1.6-3.9-2.3-5.2-1m-10.8-8.1c-.7 1.3.3 2.9 2.3 3.9c1.6 1 3.6.7 4.3-.7c.7-1.3-.3-2.9-2.3-3.9c-2-.6-3.6-.3-4.3.7m32.4 35.6c-1.6 1.3-1 4.3 1.3 6.2c2.3 2.3 5.2 2.6 6.5 1c1.3-1.3.7-4.3-1.3-6.2c-2.2-2.3-5.2-2.6-6.5-1m-11.4-14.7c-1.6 1-1.6 3.6 0 5.9s4.3 3.3 5.6 2.3c1.6-1.3 1.6-3.9 0-6.2c-1.4-2.3-4-3.3-5.6-2",
      :fill "currentColor"}]] [:use {:xlink:href "#ai:fa6-brands:github"}]])

(defn footer
  []
  [:footer
   {:class "w-full bg-white flex flex-col items-center justify-end mt-12 sticky border-t border-slate-200"}
   [:ul
    {:class "flex justify-center px-6 py-8 space-x-8 text-slate-400/70"}
    [:li {:class "hover:text-[#0c84fc]"}
     [:a {:href +bluesky-uri+, :target "_blank"}
      bluesky-icon]]
    [:li {:class "hover:text-slate-600"}
     [:a {:href "https://github.com/martinklepsch", :target "_blank"}
      github-icon]]
    [:li {:class "hover:text-[#0077B5]"}
     [:a {:href "https://www.linkedin.com/in/martin-klepsch-b59134268/", :target "_blank"}
      [:svg
       {:data-icon "fa6-brands:linkedin",
        :height "1em",
        :viewbox "0 0 448 512",
        :width "0.88em"}
       [:symbol {:id "ai:fa6-brands:linkedin"}
        [:path
         {:d
          "M416 32H31.9C14.3 32 0 46.5 0 64.3v383.4C0 465.5 14.3 480 31.9 480H416c17.6 0 32-14.5 32-32.3V64.3c0-17.8-14.4-32.3-32-32.3M135.4 416H69V202.2h66.5V416zm-33.2-243c-21.3 0-38.5-17.3-38.5-38.5S80.9 96 102.2 96c21.2 0 38.5 17.3 38.5 38.5c0 21.3-17.2 38.5-38.5 38.5m282.1 243h-66.4V312c0-24.8-.5-56.7-34.5-56.7c-34.6 0-39.9 27-39.9 54.9V416h-66.4V202.2h63.7v29.2h.9c8.9-16.8 30.6-34.5 62.9-34.5c67.2 0 79.7 44.3 79.7 101.9z",
          :fill "currentColor"}]]
       [:use {:xlink:href "#ai:fa6-brands:linkedin"}]]]]]])


(defn date-fmt
  [date]
  ;; lol
  (if date
    (str
     (get ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"] (.getMonth date))
     " "
     (+ 1900 (.getYear date)))
    (println "date missing")))

#_(defn nav
    []
    [:div
     {:class "max-w-[120ch] w-full flex justify-between items-center py-4 px-8"}
     [:div {:class "flex flex-col"}
      [:a
       {:class "nav-link text-lg font-bold text-slate-800",
        :href "/"} "Mostly Harmless"]]
     [:div
      {:class "flex space-x-2 text-sm items-center"}
      [:a {:class "nav-link px-2 py-1", :href "/"}
       "Home"]
    ;; [:span {:class "nav-divider"} "/"]
    ;; [:a
    ;;  {:class "px-2 py-1", :href "/about"}
    ;;  "About"]
      [:span {:class "nav-divider"} "/"]
      [:a
       {:class
        "nav-link px-2 py-1 text-sky-500 ring-1 ring-sky-200 rounded-sm bg-sky-50",
        :href "/subscribe.html"} "Subscribe"]]])

(defn base
  [opts & content]
  [:html {:lang "en" :itemtype "http://schema.org/Blog"}
   ;; (nav)
   (head opts)
   [:body.system-sans-serif.dark-gray
    (into [:div] content)]])

(def prose-classes
  "max-w-none
    
    prose
    prose-base
    prose-slate
    
    -- Text
    prose-h1:font-bold
    prose-h1:text-3xl
    
    -- Images
    prose-img:rounded-xl
    
    -- Italics
    prose-em:bg-orange-100/60
    prose-em:px-0.5
    prose-em:rounded-sm
    
    -- Bold
    prose-strong:bg-primary/10
    prose-strong:px-0.5
    prose-strong:font-medium
    prose-strong:rounded-sm
    
    --- Links
    prose-a:decoration-1
    prose-a:transition-text-decoration
    prose-a:decoration-primary/50
    prose-a:font-medium
    prose-a:underline
    prose-a:underline-offset-4
    prose-a:hover:decoration-2
    prose-a:hover:decoration-primary/50
    
    --- Code
    prose-code:text-sm
    prose-code:font-medium

    --- Inline Code
    prose-code-inline:text-orange-600
    prose-code-inline:bg-orange-50
    prose-code-inline:ring-orange-400/20
    prose-code-inline:rounded-md
    prose-code-inline:text-sm
    prose-code-inline:px-2
    prose-code-inline:py-1
    prose-code-inline:ring-1
    prose-code-inline:ring-inset
    
    --- Blockquote
    prose-blockquote:text-lg
    prose-blockquote:font-light
    prose-blockquote:text-slate-500
    prose-blockquote:border-l-4
    prose-blockquote:border-blue-400/70
    prose-blockquote:pl-4
    prose-blockquote:ml-4
    prose-blockquote:mt-4")

(defn render-post
  [{fm :frontmatter :as post} opts]
  (try
    [:article.mt-8.max-w-3xl.mx-auto.px-6 {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
     [:div.text-center
      [:div.text-slate-600.uppercase.text-xs
       (:date-long (:ui post))]
      [:h1.mt-2.mb-12.text-3xl.sm:text-5xl.font-bold.text-balance
       (:title fm)]]
     [:section.max-w-xl.mx-auto
      [:div {:class prose-classes}
       (:content post)]]]
    (catch Exception e
      (println "Rendering %s failed:\n" (:permalink fm))
      (throw e))))

(defn signature
  [post]
  [:div.my-4.em-before.max-w-lg.mx-auto
   [:a {:href +twitter-uri+} "@martinklepsch"] ", "
   (date-fmt (:date-published (:frontmatter post))) " "])

(defn posts-list
  [entries]
  [:section.space-y-4.px-6
   (for [[year entries] (->> entries
                             (remove (comp :hidden :frontmatter))
                             (group-by #(:year (:ui %)))
                             (sort-by key)
                             (reverse))]
     [:div
      [:h3.font-bold.text-xl.mb-2 year]
      (into
       [:ol.divide-y.divide-slate-200]

       (for [post entries]
         [:li
          [:a {:class "flex items-baseline gap-2 py-3 group text-slate-700 hover:text-primary"
               :href (-> post :frontmatter :permalink)}
           [:div.min-w-16.text-xs.text-slate-600.uppercase.font-medium.transition-all
            (:date-short (:ui post))]
           [:div
            {:class "group-hover:translate-x-0.5 transition-all duration-250 ease-out text-balance pr-6"}
            (-> post :frontmatter :title)]]]))])])

(defn dynogee-callout
  []
  [:div
   {:class "p2 max-width-2 mx-auto border-left border-right border-bottom",
    :style {:box-shadow "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)"
            :border-radius "0 0 4px 4px"
            :border-color "rgb(0 0 0 / 0.3)"}}
   "Check out " [:a {:href "https://dynogee.com?ref=mk"} "Dynogee"] ", a new project I'm working on."])

(defn header
  []
  [:div.flex.justify-between.mt-8.mb-16.items-baseline.max-w-5xl.mx-auto.px-6
   [:a.group {:href "/"
              :style {:view-transition-name "logo"}
              :title "Home"}
    [:h1.text-xl.font-semibold.tracking-tight "Mostly clueless"]
    [:div.font-serif.text-sm.text-slate-500.italic.group-hover:translate-x-1.transition-all.duration-250.ease-out.group-hover:text-primary "but curious to find out"]]
   [:div
    {:style {:view-transition-name "socials"}}
    [:a {:href +bluesky-uri+, :target "_blank" :class "hover:text-[#0c84fc] hover:scale-125 duration-800 transition-all ease-out"}
     bluesky-icon
     ]
    ]


   #_[:a {:href "/about.html"} "About"]])

(defn index-page
  [{:keys [all-posts]}]
  (base
   {:frontmatter {:permalink "/index.html"
                  :og-image "/images/selfies/1.jpg"}}
   [:div.max-w-xl.mx-auto
    [:div
     (header)
     (posts-list all-posts)
     ]]
   (footer)
   ))

(defn post-page
  [post]
  [:html {:lang "en" :itemtype "http://schema.org/Blog"}
   (head post)
   [:body.post
    [:div.mx1
     [:div
      (header)
      (render-post post {:permalink-page? true})
      (if-let [bsky-id (-> post :frontmatter :bluesky-post-id)]
        [:div.max-w-3xl.mx-auto.px-6.my-16
         [:h2.text-3xl.font-bold.text-center "Comments"]
         [:script
          {:data-bluesky-uri (str "at://martinklepsch.org/app.bsky.feed.post/" bsky-id)
           ;;https://bsky.app/profile/martinklepsch.org/post/3lcumsy2lls2f
           :src "/bluesky-comments.js",
           :type "module"}]]

        [:div.px-6.my-16
         [:div
          {:class "mx-auto max-w-[700px]"}
          #_[:script
             {:async "true" ,
              :data-uid "e1d62639cd",
              :src "https://martinklepsch.kit.com/e1d62639cd/index.js"}]
          [:script
           {:async "yes" ,
            :data-uid "f620e7ab36",
            :src "https://martinklepsch.kit.com/f620e7ab36/index.js"}]]])
      (footer)]]]])

(defn onehundred-page
  [idx post]
  (let [fm (:frontmatter post)]
    [:html {:lang "en" :itemtype "http://schema.org/Blog"}
     (head post)
     [:body.onehundred
      [:div.max-width-2.mx-auto.my4.px1
       [:h1.bold.w-80-ns.lh-title.max-width-2.mx-auto
        (:title fm)]
       [:section.mkdwn.leading-relaxed
        (:content post)]
       [:div.my3
        [:a {:href "/100/writing-100-things.html"} (inc idx) " / 100"]]
       ]]]))

;; Rendering API
;; Goals
;; - Make it easy to re-render individual files

(defn spit-hiccup-to-out
  [permalink hiccup]
  (let [out-dir "_site"
        out-file (io/file (str out-dir permalink))]
    (io/make-parents out-file)
    (println "Spitting" permalink)
    (spit out-file (str "<!DOCTYPE html>\n" (utils/as-html hiccup)))))

(defn render-all
  []
  (let [posts (posts/sort-posts (map posts/read-post posts/post-files))
        onehundreds (->> (map posts/read-post posts/onehundred-files)
                         (posts/sort-posts)
                         (reverse)
                         (map-indexed (fn [idx p] [idx p])))
        index {:type :index :all-posts posts :frontmatter {:permalink "/index.html"}}]
    (spit-hiccup-to-out (-> index :frontmatter :permalink) (index-page index))
    (doseq [post posts]
      (spit-hiccup-to-out (-> post :frontmatter :permalink) (post-page post)))
    (doseq [[idx p] onehundreds]
      (spit-hiccup-to-out (-> p :frontmatter :permalink)
                          (onehundred-page idx p)))))

(defn -main
  []
  (render-all))

(comment)
