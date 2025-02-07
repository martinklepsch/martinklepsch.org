Project Structure:
├── Makefile
├── Procfile
├── Readme.md
├── _site
│   ├── archive.html
│   ├── atom.xml
│   ├── bluesky-comments.js
│   ├── chaf.html
│   ├── error.html
│   ├── index.html
│   ├── layout_test.html
│   ├── robots.txt
│   ├── tachyons.css
│   ├── test.html
│   ├── work-old.html
│   └── work.html
├── bb-src
├── bb.edn
├── codefetch
│   └── codebase.md
├── content
├── deploy.clj
├── deps.edn
├── package-lock.json
├── package.json
├── pushover-notify.clj
├── resources
│   ├── daily-ui.sketch
│   ├── perun.base.edn
├── stage-if-changed.sh
└── stylesheets
    ├── _pygments.scss
    ├── _reset.scss
    ├── _tachyons.css
    ├── martinklepschorg-v2.sass
    └── martinklepschorg-v4.css


bb-src/mkl/atom.clj
```
1 | (ns mkl.atom
2 |   (:require [mkl.pods]
3 |             [mkl.posts :as posts]
4 |             [mkl.view :as view]
5 |             [pod.retrogradeorbit.bootleg.utils :as utils]))
6 | 
7 | (def site-root "https://www.martinklepsch.org")
8 | 
9 | (def author
10 |   [:author
11 |    [:name "Martin Klepsch"]
12 |    [:email "martinklepsch@googlemail.com"]])
13 | 
14 | (defn utc-date [date]
15 |   (-> (pr-str date)
16 |       (subs 7 29)
17 |       (str "Z")))
18 | 
19 | (defn post->atom-entry [post]
20 |   (let [fm (-> post :frontmatter)
21 |         url (str site-root (:permalink fm))]
22 |     (println "post->atom-entry" (:permalink fm))
23 |     [:entry
24 |      [:id (str "urn:uuid:" (:uuid fm))]
25 |      [:title (:title fm)]
26 |      [:published (utc-date (:date-published fm))]
27 |      [:updated (utc-date (:date-published fm))]
28 |      [:link {:href url :type "text/html" :title (:title fm) :rel "alternate"}]
29 |      [:content {:type "html" "xml:base" url}
30 |       (utils/as-html [:h1 (:title fm)])
31 |       (utils/as-html (:content post))
32 |       (utils/as-html [:p [:a {:href view/+twitter-uri+} "reply on twitter"]])]
33 |      author]))
34 | 
35 | 
36 | (defn -main []
37 |   (let [posts (posts/sort-posts (map posts/read-post posts/post-files))]
38 |     (spit "_site/atom.xml"
39 |           (-> [:feed {:xmlns "http://www.w3.org/2005/Atom"}
40 |                [:title "Martin Klepsch"]
41 |                [:subtitle "Martin Klepsch's blog"]
42 |                [:generator "a bespoke mix of babashka and bootleg"]
43 |                [:link {:href site-root :type "text/html"}]
44 |                [:updated (utc-date (-> posts first :frontmatter :date-published))]
45 |                [:id site-root]
46 |                author
47 |                (for [post posts]
48 |                  (post->atom-entry (assoc post :type :post)))]
49 | 
50 |               (utils/convert-to :xml)))))
```

bb-src/mkl/frontmatter.clj
```
1 | (ns mkl.frontmatter
2 |   (:require [mkl.pods]
3 |             [clojure.string :as str]
4 |             [clojure.java.io :as io]
5 |             [clj-yaml.core :as yaml]
6 |             [pod.retrogradeorbit.bootleg.glob :as glob])
7 |   (:import (java.util UUID)
8 |            (java.time Instant)
9 |            (java.util Date)))
10 | 
11 | (defn slug [file]
12 |   (let [path (.getPath file)
13 |         filename (.getName file)]
14 |     (cond
15 |      (or (.contains path "posts")
16 |          (.contains path "onehundred"))
17 |       (->> (str/split filename #"[-\.]")
18 |            (drop 3)
19 |            drop-last
20 |            (str/join "-")
21 |            str/lower-case)
22 | 
23 |       (.pages path "pages")
24 |       (->> (str/split filename #"[-\.]")
25 |            drop-last
26 |            (str/join "-")
27 |            str/lower-case)
28 | 
29 |       :else (throw (ex-info "Could not build slug" {:file file})))))
30 | 
31 | (defn permalink-fn [file]
32 |   (cond
33 |    (.startsWith (.getPath file) "content/posts")
34 |    (str "/posts/" (slug file) ".html")
35 | 
36 |    (.startsWith (.getPath file) "content/onehundred")
37 |    (str "/100/" (slug file) ".html")
38 | 
39 |    :else
40 |    (str "/" (slug file) ".html")))
41 | 
42 | (defn entry-type [file]
43 |   (cond
44 |    (.startsWith (.getPath file) "content/posts") "post"
45 |    (.startsWith (.getPath file) "content/onehundred") "onehundred"))
46 | 
47 | (defn day-str [d]
48 |   (subs (.format java.time.format.DateTimeFormatter/ISO_INSTANT (.toInstant d)) 0 10))
49 | 
50 | (def yaml-head
51 |   #"---\h*\r?\n")
52 | 
53 | (defn file-contents [f]
54 |   (let [[_ yml content] (str/split (slurp f) yaml-head 3)]
55 |     [yml content]))
56 | 
57 | (def selfies
58 |   (->> (file-seq (io/file "resources/public/images/selfies/"))
59 |        (filter #(.isFile %))
60 |        (remove #(contains? #{"3.jpg"} (.getName %)))
61 |        (map #(str/replace % #"^resources/public/" "/"))
62 |        (sort)))
63 | 
64 | (defn get-frontmatter [f]
65 |   (let [[yml _] (file-contents f)]
66 |     (yaml/parse-string yml)))
67 | 
68 | (defn update-frontmatter! [f]
69 |   (let [[yml content] (file-contents f)
70 |         frontmatter (yaml/parse-string yml)
71 |         updated (cond-> frontmatter
72 |                   (and (nil? (:date-published frontmatter))
73 |                        (not (:draft frontmatter)))
74 |                   (assoc :date-published (Date.))
75 | 
76 |                   (or (nil? (:uuid frontmatter))
77 |                       (str/blank? (:uuid frontmatter)))
78 |                   (assoc :uuid (str (UUID/randomUUID)))
79 | 
80 |                   (nil? (:og-image frontmatter))
81 |                   (assoc :og-image (rand-nth selfies))
82 | 
83 |                   (nil? (:type frontmatter))
84 |                   (assoc :type (entry-type f))
85 | 
86 |                   (nil? (:permalink frontmatter))
87 |                   (assoc :permalink (permalink-fn f)))]
88 |     (when (not= frontmatter updated)
89 |       (println "Frontmatter changed" updated)
90 |       (spit f
91 |             (str "---\n"
92 |                  (yaml/generate-string updated :dumper-options {:flow-style :block})
93 |                  "---\n"
94 |                  content)))))
95 | 
96 | (comment
97 |   (def f (io/file "content/posts/2012-01-28-fix-broken-decoding-of-mail-subjects-in-exim.markdown"))
98 | 
99 |   (str/split (slurp f) #"---\r?\n" 3)
100 |   (update-frontmatter! f)
101 |   )
102 | 
103 | (def post-files
104 |   (shuffle
105 |     (into (glob/glob "content/onehundred/*.md")
106 |           (glob/glob "content/posts/*.md"))))
107 | 
108 | (defn -main []
109 |   (when (seq (into (glob/glob "content/onehundred/*.markdown")
110 |                    (glob/glob "content/posts/*.markdown")))
111 |     (throw (ex-info "Found files with .markdown extension, use .md instead" {})))
112 |   (doseq [f post-files]
113 |     (println f)
114 |     (update-frontmatter! (io/file f))))
```

bb-src/mkl/pods.clj
```
1 | (ns mkl.pods
2 |   "Side effecting namespace that mostly makes sure that we have loaded the
3 |   bootleg pod."
4 |   (:require [babashka.pods :as pods]))
5 | 
6 | (let [loaded? (volatile! false)]
7 |   (when-not @loaded?
8 |     (pods/load-pod "bootleg")
9 |     (vreset! loaded? true)))
```

bb-src/mkl/posts.clj
```
1 | (ns mkl.posts
2 |   (:require [mkl.pods]
3 |             [mkl.frontmatter :as fm]
4 |             [pod.retrogradeorbit.bootleg.markdown :as markdown]
5 |             [pod.retrogradeorbit.bootleg.glob :as glob]))
6 | 
7 | (def post-files
8 |   (sort (glob/glob "content/posts/*.md")))
9 | 
10 | (def onehundred-files
11 |   (sort (glob/glob "content/onehundred/*.md")))
12 | 
13 | (def date-format (java.text.SimpleDateFormat. "MMM dd"))
14 | (def date-long-format (java.text.SimpleDateFormat. "MMM dd, YYY"))
15 | (def year-format (java.text.SimpleDateFormat. "YYY"))
16 | (.format date-format (java.util.Date.))
17 | 
18 | (defn read-post [file]
19 |     (let [frontmatter (fm/get-frontmatter file)]
20 |       {:file file
21 |        :frontmatter frontmatter
22 |        :ui {:year (.format  year-format (:date-published frontmatter))
23 |             :date-short (.format  date-format (:date-published frontmatter))
24 |             :date-long (.format  date-long-format (:date-published frontmatter))
25 |             }
26 |        :source (slurp file)
27 |     ;; :content to be compatible with view code
28 |        :content (-> file fm/file-contents second (markdown/markdown :data))}))
29 | 
30 | (defn sort-posts [posts]
31 |   (->> posts (sort-by (comp :date-published :frontmatter)) reverse))
32 | 
33 | (def test-post
34 |   (read-post (first post-files)))
35 | 
36 | (defn -main []
37 |   (read-post (first post-files)))
38 | 
39 | ;; TODO registry
40 | ;; maintain a list of posts in memory
41 | ;; when files change reread the post data for the post with the same :file key
42 | 
43 | (comment
44 |   (read-post (first post-files))
45 |   (def file (first post-files))
46 |   (-> file fm/file-contents second (markdown/markdown :data :html))
47 | 
48 |   )
```

bb-src/mkl/view.clj
```
1 | (ns mkl.view
2 |   (:require [clojure.java.io :as io]
3 |             [clojure.string :as string]
4 |             [mkl.pods]
5 |             [mkl.posts :as posts]
6 |             [pod.retrogradeorbit.bootleg.utils :as utils])
7 |   (:import [java.net URLEncoder]))
8 | 
9 | (defn with-base-url
10 |   [s]
11 |   (assert (.startsWith s "/") s)
12 |   (str "https://martinklepsch.org" s))
13 | 
14 | (defn truncate
15 |   [length s]
16 |   (when-not (seq s)
17 |     (throw (ex-info "truncate called with blank string" {})))
18 |   (str (subs s 0 (min length (count s))) "..."))
19 | 
20 | (defn content->desc
21 |   [content]
22 |   (some->> content
23 |            flatten
24 |            (filter string?)
25 |            (remove string/blank?)
26 |            (take 10)
27 |            (map string/trim)
28 |            (string/join " ")
29 |            (truncate 190)))
30 | 
31 | (defn- url-encode
32 |   "Returns an UTF-8 URL encoded version of the given string."
33 |   [^String unencoded]
34 |   (URLEncoder/encode unencoded "UTF-8"))
35 | 
36 | (defn head
37 |   [{:keys [frontmatter] :as opts}]
38 |   (let [title (if-let [t (:title frontmatter)]
39 |                 (str t " — Martin Klepsch")
40 |                 "Martin Klepsch")
41 |         title-social (or (:title frontmatter) "Martin Klepsch")
42 |         img   (if (:title frontmatter)
43 |                 (str "https://dynogee.com/gen?id=kw9xren5k1gw1si&title=" (url-encode (:title frontmatter)))
44 |                 (some-> frontmatter :og-image with-base-url))
45 |         permalink (some-> frontmatter :permalink with-base-url)
46 |         desc (or (:description frontmatter)
47 |                  (content->desc (:content opts))
48 |                  "Personal Website and Blog of Martin Klepsch")]
49 |     (assert permalink)
50 |     [:head
51 |      [:meta {:charset "utf-8"}]
52 |      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
53 |      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
54 |      [:meta {:itemprop "author" :name "author" :content "Martin Klepsch (martinklepsch@googlemail.com)"}]
55 |      [:meta {:name "keywords" :itemprop "keywords" :content "blog, clojure, development, clojurescript, heroku, amazon s3, aws"}]
56 |      [:meta {:name "description" :itemprop "description" :content desc}]
57 |      (when permalink [:link {:rel "canonical" :href permalink}])
58 |      [:title title]
59 |      ;; OpenGraph
60 |      [:meta {:property "og:title" :content title-social}]
61 |      [:meta {:property "og:type" :content "article"}]
62 |      [:meta {:property "og:description" :content desc}]
63 |      (when permalink [:meta {:property "og:url" :content permalink}])
64 |      (when img [:meta {:property "og:image" :content img}])
65 |      [:meta {:property "og:site_name" :content "martinklepsch.org"}]
66 |      ;; Twitter
67 |      [:meta {:name "twitter:card" :content "summary"}]
68 |      [:meta {:name "twitter:site" :content "@martinklepsch"}]
69 |      [:meta {:name "twitter:creator" :content "@martinklepsch"}]
70 |      [:meta {:name "twitter:title" :content title-social}]
71 |      [:meta {:name "twitter:description" :content desc}]
72 |      (when img [:meta {:name "twitter:image" :content img}])
73 |      ;; Misc
74 |      [:link {:rel "shortcut icon" :type "image/png" :href "/images/favicon.png"}]
75 |      [:link {:rel "alternate" :type "application/atom+xml" :title "Sitewide Atom Feed" :href "/atom.xml"}]
76 |      [:link {:type "text/css"
77 |              :rel "stylesheet"
78 |              :href "/stylesheets/martinklepschorg-v4.css"}]]))
79 | 
80 | (def +twitter-uri+
81 |   "https://twitter.com/martinklepsch")
82 | 
83 | (def +bluesky-uri+
84 |   "https://bsky.app/profile/martinklepsch.org")
85 | 
86 | (def bluesky-icon
87 |   [:svg
88 |    {:data-icon "fa6-brands:bluesky",
89 |     :height "1em",
90 |     :viewbox "0 0 576 512",
91 |     :width "1.13em"}
92 |    [:symbol {:id "ai:fa6-brands:bluesky"}
93 |     [:path
94 |      {:d "M407.8 294.7c-3.3-.4-6.7-.8-10-1.3c3.4.4 6.7.9 10 1.3M288 227.1c-26.1-50.7-97.1-145.2-163.1-191.8C61.6-9.4 37.5-1.7 21.6 5.5C3.3 13.8 0 41.9 0 58.4S9.1 194 15 213.9c19.5 65.7 89.1 87.9 153.2 80.7c3.3-.5 6.6-.9 10-1.4c-3.3.5-6.6 1-10 1.4c-93.9 14-177.3 48.2-67.9 169.9C220.6 589.1 265.1 437.8 288 361.1c22.9 76.7 49.2 222.5 185.6 103.4c102.4-103.4 28.1-156-65.8-169.9c-3.3-.4-6.7-.8-10-1.3c3.4.4 6.7.9 10 1.3c64.1 7.1 133.6-15.1 153.2-80.7C566.9 194 576 75 576 58.4s-3.3-44.7-21.6-52.9c-15.8-7.1-40-14.9-103.2 29.8C385.1 81.9 314.1 176.4 288 227.1",
95 |       :fill "currentColor"}]]
96 |    [:use {:xlink:href "#ai:fa6-brands:bluesky"}]])
97 | 
98 | (def github-icon
99 |   [:svg
100 |    {:data-icon "fa6-brands:github",
101 |     :height "1em",
102 |     :viewbox "0 0 496 512",
103 |     :width "0.97em"}
104 |    [:symbol {:id "ai:fa6-brands:github"}
105 |     [:path
106 |      {:d
107 |       "M165.9 397.4c0 2-2.3 3.6-5.2 3.6c-3.3.3-5.6-1.3-5.6-3.6c0-2 2.3-3.6 5.2-3.6c3-.3 5.6 1.3 5.6 3.6m-31.1-4.5c-.7 2 1.3 4.3 4.3 4.9c2.6 1 5.6 0 6.2-2s-1.3-4.3-4.3-5.2c-2.6-.7-5.5.3-6.2 2.3m44.2-1.7c-2.9.7-4.9 2.6-4.6 4.9c.3 2 2.9 3.3 5.9 2.6c2.9-.7 4.9-2.6 4.6-4.6c-.3-1.9-3-3.2-5.9-2.9M244.8 8C106.1 8 0 113.3 0 252c0 110.9 69.8 205.8 169.5 239.2c12.8 2.3 17.3-5.6 17.3-12.1c0-6.2-.3-40.4-.3-61.4c0 0-70 15-84.7-29.8c0 0-11.4-29.1-27.8-36.6c0 0-22.9-15.7 1.6-15.4c0 0 24.9 2 38.6 25.8c21.9 38.6 58.6 27.5 72.9 20.9c2.3-16 8.8-27.1 16-33.7c-55.9-6.2-112.3-14.3-112.3-110.5c0-27.5 7.6-41.3 23.6-58.9c-2.6-6.5-11.1-33.3 2.6-67.9c20.9-6.5 69 27 69 27c20-5.6 41.5-8.5 62.8-8.5s42.8 2.9 62.8 8.5c0 0 48.1-33.6 69-27c13.7 34.7 5.2 61.4 2.6 67.9c16 17.7 25.8 31.5 25.8 58.9c0 96.5-58.9 104.2-114.8 110.5c9.2 7.9 17 22.9 17 46.4c0 33.7-.3 75.4-.3 83.6c0 6.5 4.6 14.4 17.3 12.1C428.2 457.8 496 362.9 496 252C496 113.3 383.5 8 244.8 8M97.2 352.9c-1.3 1-1 3.3.7 5.2c1.6 1.6 3.9 2.3 5.2 1c1.3-1 1-3.3-.7-5.2c-1.6-1.6-3.9-2.3-5.2-1m-10.8-8.1c-.7 1.3.3 2.9 2.3 3.9c1.6 1 3.6.7 4.3-.7c.7-1.3-.3-2.9-2.3-3.9c-2-.6-3.6-.3-4.3.7m32.4 35.6c-1.6 1.3-1 4.3 1.3 6.2c2.3 2.3 5.2 2.6 6.5 1c1.3-1.3.7-4.3-1.3-6.2c-2.2-2.3-5.2-2.6-6.5-1m-11.4-14.7c-1.6 1-1.6 3.6 0 5.9s4.3 3.3 5.6 2.3c1.6-1.3 1.6-3.9 0-6.2c-1.4-2.3-4-3.3-5.6-2",
108 |       :fill "currentColor"}]] [:use {:xlink:href "#ai:fa6-brands:github"}]])
109 | 
110 | (defn footer
111 |   []
112 |   [:footer
113 |    {:class "w-full bg-white flex flex-col items-center justify-end mt-12 sticky border-t border-slate-200"}
114 |    [:ul
115 |     {:class "flex justify-center px-6 py-8 space-x-8 text-slate-400/70"}
116 |     [:li {:class "hover:text-[#0c84fc]"}
117 |      [:a {:href +bluesky-uri+, :target "_blank"}
118 |       bluesky-icon]]
119 |     [:li {:class "hover:text-slate-600"}
120 |      [:a {:href "https://github.com/martinklepsch", :target "_blank"}
121 |       github-icon]]
122 |     [:li {:class "hover:text-[#0077B5]"}
123 |      [:a {:href "https://www.linkedin.com/in/martin-klepsch-b59134268/", :target "_blank"}
124 |       [:svg
125 |        {:data-icon "fa6-brands:linkedin",
126 |         :height "1em",
127 |         :viewbox "0 0 448 512",
128 |         :width "0.88em"}
129 |        [:symbol {:id "ai:fa6-brands:linkedin"}
130 |         [:path
131 |          {:d
132 |           "M416 32H31.9C14.3 32 0 46.5 0 64.3v383.4C0 465.5 14.3 480 31.9 480H416c17.6 0 32-14.5 32-32.3V64.3c0-17.8-14.4-32.3-32-32.3M135.4 416H69V202.2h66.5V416zm-33.2-243c-21.3 0-38.5-17.3-38.5-38.5S80.9 96 102.2 96c21.2 0 38.5 17.3 38.5 38.5c0 21.3-17.2 38.5-38.5 38.5m282.1 243h-66.4V312c0-24.8-.5-56.7-34.5-56.7c-34.6 0-39.9 27-39.9 54.9V416h-66.4V202.2h63.7v29.2h.9c8.9-16.8 30.6-34.5 62.9-34.5c67.2 0 79.7 44.3 79.7 101.9z",
133 |           :fill "currentColor"}]]
134 |        [:use {:xlink:href "#ai:fa6-brands:linkedin"}]]]]]])
135 | 
136 | 
137 | (defn date-fmt
138 |   [date]
139 |   ;; lol
140 |   (if date
141 |     (str
142 |      (get ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"] (.getMonth date))
143 |      " "
144 |      (+ 1900 (.getYear date)))
145 |     (println "date missing")))
146 | 
147 | (defn base
148 |   [opts & content]
149 |   [:html {:lang "en" :itemtype "http://schema.org/Blog"}
150 |    ;; (nav)
151 |    (head opts)
152 |    [:body.system-sans-serif.dark-gray
153 |     (into [:div] content)]])
154 | 
155 | (def prose-classes
156 |   "max-w-none
157 |     
158 |     prose
159 |     prose-base
160 |     prose-slate
161 |     
162 |     -- Text
163 |     prose-h1:font-bold
164 |     prose-h1:text-3xl
165 |     
166 |     -- Images
167 |     prose-img:rounded-xl
168 |     
169 |     -- Italics
170 |     prose-em:bg-orange-100/60
171 |     prose-em:px-0.5
172 |     prose-em:rounded-sm
173 |     
174 |     -- Bold
175 |     prose-strong:bg-primary/10
176 |     prose-strong:px-0.5
177 |     prose-strong:font-medium
178 |     prose-strong:rounded-sm
179 |     
180 |     --- Links
181 |     prose-a:decoration-1
182 |     prose-a:transition-text-decoration
183 |     prose-a:decoration-primary/50
184 |     prose-a:font-medium
185 |     prose-a:underline
186 |     prose-a:underline-offset-4
187 |     prose-a:hover:decoration-2
188 |     prose-a:hover:decoration-primary/50
189 |     
190 |     --- Code
191 |     prose-code:text-sm
192 |     prose-code:font-medium
193 | 
194 |     --- Inline Code
195 |     prose-code-inline:text-orange-600
196 |     prose-code-inline:bg-orange-50
197 |     prose-code-inline:ring-orange-400/20
198 |     prose-code-inline:rounded-md
199 |     prose-code-inline:text-sm
200 |     prose-code-inline:px-2
201 |     prose-code-inline:py-1
202 |     prose-code-inline:ring-1
203 |     prose-code-inline:ring-inset
204 |     
205 |     --- Blockquote
206 |     prose-blockquote:text-lg
207 |     prose-blockquote:font-light
208 |     prose-blockquote:text-slate-500
209 |     prose-blockquote:border-l-4
210 |     prose-blockquote:border-blue-400/70
211 |     prose-blockquote:pl-4
212 |     prose-blockquote:ml-4
213 |     prose-blockquote:mt-4")
214 | 
215 | (defn render-post
216 |   [{fm :frontmatter :as post} opts]
217 |   (try
218 |     [:article.mt-8.max-w-3xl.mx-auto.px-6 {:itemprop "blogPost" :itemscope "" :itemtype "http://schema.org/BlogPosting"}
219 |      [:div.text-center
220 |       [:div.text-slate-600.uppercase.text-xs
221 |        (:date-long (:ui post))]
222 |       [:h1.mt-2.mb-12.text-3xl.sm:text-5xl.font-bold.text-balance
223 |        (:title fm)]]
224 |      [:section.max-w-xl.mx-auto
225 |       [:div {:class prose-classes}
226 |        (:content post)]]]
227 |     (catch Exception e
228 |       (println "Rendering %s failed:\n" (:permalink fm))
229 |       (throw e))))
230 | 
231 | (defn signature
232 |   [post]
233 |   [:div.my-4.em-before.max-w-lg.mx-auto
234 |    [:a {:href +twitter-uri+} "@martinklepsch"] ", "
235 |    (date-fmt (:date-published (:frontmatter post))) " "])
236 | 
237 | (defn posts-list
238 |   [entries]
239 |   [:section.space-y-4.px-6
240 |    (for [[year entries] (->> entries
241 |                              (remove (comp :hidden :frontmatter))
242 |                              (group-by #(:year (:ui %)))
243 |                              (sort-by key)
244 |                              (reverse))]
245 |      [:div
246 |       [:h3.font-bold.text-xl.mb-2 year]
247 |       (into
248 |        [:ol.divide-y.divide-slate-200]
249 | 
250 |        (for [post entries]
251 |          [:li
252 |           [:a {:class "flex items-baseline gap-2 py-3 group text-slate-700 hover:text-primary"
253 |                :href (-> post :frontmatter :permalink)}
254 |            [:div.min-w-16.text-xs.text-slate-600.uppercase.font-medium.transition-all
255 |             (:date-short (:ui post))]
256 |            [:div
257 |             {:class "group-hover:translate-x-0.5 transition-all duration-250 ease-out text-balance pr-6"}
258 |             (-> post :frontmatter :title)]]]))])])
259 | 
260 | (defn dynogee-callout
261 |   []
262 |   [:div
263 |    {:class "p2 max-width-2 mx-auto border-left border-right border-bottom",
264 |     :style {:box-shadow "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)"
265 |             :border-radius "0 0 4px 4px"
266 |             :border-color "rgb(0 0 0 / 0.3)"}}
267 |    "Check out " [:a {:href "https://dynogee.com?ref=mk"} "Dynogee"] ", a new project I'm working on."])
268 | 
269 | (defn header
270 |   []
271 |   [:div.flex.justify-between.mt-8.mb-16.items-baseline.max-w-5xl.mx-auto.px-6
272 |    [:a.group {:href "/"
273 |               :style {:view-transition-name "logo"}
274 |               :title "Home"}
275 |     [:h1.text-xl.font-semibold.tracking-tight "Mostly clueless"]
276 |     [:div.font-serif.text-sm.text-slate-500.italic.group-hover:translate-x-1.transition-all.duration-250.ease-out.group-hover:text-primary "but curious to find out"]]
277 |    [:div
278 |     {:style {:view-transition-name "socials"}}
279 |     [:a {:href +bluesky-uri+, :target "_blank" :class "hover:text-[#0c84fc] hover:scale-125 duration-800 transition-all ease-out"}
280 |      bluesky-icon]]
281 | 
282 | 
283 |    #_[:a {:href "/about.html"} "About"]])
284 | 
285 | (defn index-page
286 |   [{:keys [all-posts]}]
287 |   (base
288 |    {:frontmatter {:permalink "/index.html"
289 |                   :og-image "/images/selfies/1.jpg"}}
290 |    [:div.max-w-xl.mx-auto
291 |     [:div
292 |      (header)
293 |      (posts-list all-posts)]]
294 |    (footer)))
295 | 
296 | (defn post-page
297 |   [post]
298 |   [:html {:lang "en" :itemtype "http://schema.org/Blog"}
299 |    (head post)
300 |    [:body.post
301 |     [:div.mx1
302 |      [:div
303 |       (header)
304 |       (render-post post {:permalink-page? true})
305 |       (if-let [bsky-id (-> post :frontmatter :bluesky-post-id)]
306 |         [:div.max-w-3xl.mx-auto.px-6.my-16
307 |          [:h2.text-3xl.font-bold.text-center "Comments"]
308 |          [:script
309 |           {:data-bluesky-uri (str "at://martinklepsch.org/app.bsky.feed.post/" bsky-id)
310 |            ;;https://bsky.app/profile/martinklepsch.org/post/3lcumsy2lls2f
311 |            :src "/bluesky-comments.js",
312 |            :type "module"}]]
313 | 
314 |         [:div.px-6.my-16
315 |          [:div
316 |           {:class "mx-auto max-w-[700px]"}
317 |           #_[:script
318 |              {:async "true" ,
319 |               :data-uid "e1d62639cd",
320 |               :src "https://martinklepsch.kit.com/e1d62639cd/index.js"}]
321 |           [:script
322 |            {:async "yes" ,
323 |             :data-uid "f620e7ab36",
324 |             :src "https://martinklepsch.kit.com/f620e7ab36/index.js"}]]])
325 |       (footer)]]]])
326 | 
327 | (defn onehundred-page
328 |   [idx post]
329 |   (let [fm (:frontmatter post)]
330 |     [:html {:lang "en" :itemtype "http://schema.org/Blog"}
331 |      (head post)
332 |      [:body.onehundred
333 |       [:div.max-width-2.mx-auto.my4.px1
334 |        [:h1.bold.w-80-ns.lh-title.max-width-2.mx-auto
335 |         (:title fm)]
336 |        [:section.mkdwn.leading-relaxed
337 |         (:content post)]
338 |        [:div.my3
339 |         [:a {:href "/100/writing-100-things.html"} (inc idx) " / 100"]]]]]))
340 | 
341 | ;; Rendering API
342 | ;; Goals
343 | ;; - Make it easy to re-render individual files
344 | 
345 | (defn spit-hiccup-to-out
346 |   [permalink hiccup]
347 |   (let [out-dir "_site"
348 |         out-file (io/file (str out-dir permalink))]
349 |     (io/make-parents out-file)
350 |     (println "Spitting" permalink)
351 |     (spit out-file (str "<!DOCTYPE html>\n" (utils/as-html hiccup)))))
352 | 
353 | (defn render-all
354 |   []
355 |   (let [posts (posts/sort-posts (map posts/read-post posts/post-files))
356 |         onehundreds (->> (map posts/read-post posts/onehundred-files)
357 |                          (posts/sort-posts)
358 |                          (reverse)
359 |                          (map-indexed (fn [idx p] [idx p])))
360 |         index {:type :index :all-posts posts :frontmatter {:permalink "/index.html"}}]
361 |     (spit-hiccup-to-out (-> index :frontmatter :permalink) (index-page index))
362 |     (doseq [post posts]
363 |       (spit-hiccup-to-out (-> post :frontmatter :permalink) (post-page post)))
364 |     (doseq [[idx p] onehundreds]
365 |       (spit-hiccup-to-out (-> p :frontmatter :permalink)
366 |                           (onehundred-page idx p)))))
367 | 
368 | (defn -main
369 |   []
370 |   (render-all))
371 | 
372 | (comment)
```

content/pages/chaf.markdown
```
1 | ---
2 | layout: post
3 | title: Chaf, better Chat.
4 | ---
5 | &laquo;Chaf&raquo; is a chat application with first-class threading.
6 | 
7 | ## Why bring threading to chat?
8 | 
9 | Traditionally chat applications follow a very simple model. There are
10 | two crucial things in each of them: a collection of messages (often
11 | called "room" or "channel") people can add to and messages themselves.
12 | It's an obvious analogy for someone in the 80s inventing a chat
13 | system. There are several issues however that should make us
14 | reconsider these fundamental concepts:
15 | 
16 | - Following up after absence is a tedious task. Users have to mentally
17 |   keep notes on who was talking to each other in the worst case with
18 |   multiple other intertwined and time-offset conversations.
19 | - Tracing a discussion back in the history of a room provides
20 |   many similar challenges.
21 | - Knowledge that is being shared in chat applications is essentially
22 |   ephemeral unless someone takes the time to transfer it somewhere
23 |   else.
24 | - In general a lot of information is shared and decisions are made in
25 |   a place that serves well for instantaneous communication but only
26 |   provides very unstructured data for uses beyond the very moment of
27 |   conversation.
28 | 
29 | Threading can be overwhelming and complex leading to poor user
30 | experience but I think that should not prevent further exploration of
31 | the general concept.
32 | 
33 | ## General Idea
34 | 
35 | There is a global thread (the **Room**) where you can either send a
36 | message or create a new thread by explicitly replying to a specific
37 | message. The latter is called *branching*.
38 | 
39 | ### Threads
40 | 
41 | A thread contains messages. In all threads except the **Room** the first message
42 | can be edited and those threads can also be concluded. Concluding will also
43 | close the thread i.e. new messages can no longer be posted.
44 | 
45 | **Concluding** of threads is a good way to summarize a discussion or
46 | consolidate knowledge for later reference. This is especially useful
47 | to get a quick overview when only a preview of a thread is
48 | rendered. To make sure the conclusion has enough context the first
49 | message of a thread has to be edited before the thread can be
50 | concluded.
51 | 
52 | <aside>Scepticism about whether people will actually "conclude" things
53 | is very valid.  This is a feature that needs to be integrated in very
54 | natural ways probably varying depending on size of thread and
55 | more.</aside>
56 | 
57 | ### Branching
58 | 
59 | In any thread you can branch (by replying) from a specific message and
60 | create a new thread.  This will nest your reply under the original
61 | message and display a [Thread Preview](#thread-previews)). The thread
62 | preview shows recent messages and a conclusion if available.
63 | 
64 | As messages in a chat room are usually a reaction to another message
65 | this would cause a new thread for almost every message and make
66 | everything very confusing so the UI needs to be intelligent about
67 | when provide affordances for branching and when not.
68 | 
69 | **Potential rule of thumb for branching**: Should (not can) a
70 | conclusion on its own be made based on that message?  This could be
71 | easily integrated into the reply UI.
72 | 
73 | ### Thread Previews
74 | 
75 | Thread previews give insight into an ongoing conversation or present
76 | the result of a concluded thread. They serve as a way to get insight
77 | into conversations that just branched from the current thread while
78 | not overwhelming.
79 | 
80 | <figure>
81 | <img src='/images/threads.png'>
82 | </figure>
83 | 
84 | If a thread hasn't been concluded the three most recent messages and a
85 | reply field is included in the preview. For concluded threads the last
86 | two messages and the conclusion are shown.
87 | 
88 | ### Columns
89 | 
90 | As branching a thread is natural in Chaf the interface has to support
91 | taking part in multiple conversations at once. A multi column layout
92 | suits this situation well as chat is usually taller than wide.
93 | 
94 | <figure>
95 | <img src='/images/multi-column.png'>
96 | </figure>
97 | 
98 | Clicking on a thread preview will open a column right to the column the preview
99 | is shown in. As activity in an unfocused thread happens it will be marked as
100 | unread. The order of columns is then defined by the time they have been
101 | unread. The global thread (the **Room**) is always in the leftmost column.
102 | 
103 | ## Other interesting things to explore
104 | 
105 | - Since there is a clear hierarchical structure it is very easy to
106 |   grant access to sub-trees of the main tree (originating from the **Room**)
107 | - Many chat bot scenarios could benefit from more structured information
108 | - *and some more I forgot about*
```

content/pages/error.markdown
```
1 | ---
2 | title: Oops. Looks like this page went missing.
3 | ---
4 | 
5 | Most likely you can still find it in the [archive](/archive.html).
```

content/pages/layout_test.markdown
```
1 | ---
2 | layout: post
3 | title: Your bones don’t break, mine do.
4 | ---
5 | 
6 | > Look, just because I don't be givin' no man a foot massage don't make it right for
7 | > Marsellus to throw Antwone into a glass motherfuckin' house, fuckin' up the way the nigger
8 | > talks. Motherfucker do that shit to me, he better paralyze my ass, 'cause I'll kill the
9 | > motherfucker, know what I'm sayin'?
10 | > <cite>Samual L. Jackson, Badass</cite>
11 | 
12 | Then I'm gonna shoot that bitch in the kneecaps, find out where my goddamn money is. She
13 | gonna tell me too. Hey, look at me when I'm talking to you, motherfucker. You listen: we go
14 | in there, and that nigga Winston or anybody else is in there, you the first motherfucker to
15 | get shot. You understand?
16 | 
17 | <aside>
18 | My money's in that office, right? If she start giving me some bullshit about it ain't there,
19 | and we got to go someplace else and get it, I'm gonna shoot you in the head then and there.
20 | </aside>
21 | 
22 | Your bones don't break, mine do.
23 | 
24 | - That's clear.
25 | - Your cells react to bacteria and
26 | - viruses differently than mine.
27 | - You don't get sick, I do.
28 | 
29 | That's also clear. But for some reason, you and I react the exact same way to water. We
30 | swallow it too fast, we choke. We get some in our lungs, we drown. However unreal it may
31 | seem, we are connected, you and I. We're on the same curve, just on opposite ends.
32 | 
33 | <figure>
34 | <img src="/images/flyknitbw.png">
35 | </figure>
36 | 
37 | This gun is advertised as the most popular gun in American crime. Do you believe that shit?
38 | It actually says that in the little book that comes with it: the most popular gun in
39 | American crime. Like they're actually proud of that shit.
40 | 
41 | > Now that there is the Tec-9, a crappy spray gun from South Miami.
42 | 
43 | Do you see any Teletubbies in here? Do you see a slender plastic tag clipped to my shirt
44 | with my name printed on it? Do you see a little Asian child with a blank expression on his
45 | face sitting outside on a mechanical helicopter that shakes when you put quarters in it? No?
46 | Well, that's what you see at a toy store. And you must think you're in a toy store, because
47 | you're here shopping for an infant named Jeb.
48 | 
49 | <div class="responsive-embed"><iframe src="http://player.vimeo.com/video/34017777?byline=0&amp;portrait=0&amp;color=a5e2a1" frameborder="0" webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></div>
50 | 
51 | <p><a href="http://vimeo.com/34017777">Wilson Miner - When We Build</a> from <a href="http://vimeo.com/build">Build</a> on <a href="http://vimeo.com">Vimeo</a>.</p>
```

content/posts/2012-01-28-fix-broken-decoding-of-mail-subjects-in-exim.md
```
1 | ---
2 | date-published: 2012-01-28T00:00:00Z
3 | title: Exim4 Fix Wrongly Decoded Mail Subject
4 | uuid: 6ff5c174-6f89-4449-9a6c-2315bedaed33
5 | permalink: /posts/fix-broken-decoding-of-mail-subjects-in-exim.html
6 | og-image: /images/selfies/3.jpg
7 | hidden: true
8 | type: post
9 | ---
10 | 
11 | If you are using [Exim](http://www.exim.org/) to transfer mails generated by internal software you probably often get
12 | mails with a really weird looking subject that starts with something like this:
13 | 
14 |     =?utf-8?Q?=5BPersonalverwaltung_=2D_Fehler_=23=31=37=38=5D_...
15 | 
16 | 
17 | This problem arises when some system sends an email with **more than 76 characters in the subject line** (usually people don't do that).
18 | 
19 | > While there is no limit to the length of a multiple-line header field,
20 | > each line of a header field that contains one or more
21 | > encoded-word's is limited to 76 characters.
22 | >
23 | > The length restrictions are included both to ease interoperability
24 | > through internetwork mail gateways, and to impose a limit on the
25 | > amount of lookahead a header parser must employ (while looking for a
26 | > final ?= delimiter) before it can decide whether a token is an
27 | > "encoded-word" or something else.
28 | >
29 | > <cite>Excerpt from <a href="http://www.ietf.org/rfc/rfc2047.txt">RFC2047</a></cite>
30 | 
31 | 
32 | ## Fix It
33 | 
34 | You can remove the length checking by adding the following to the main section of your
35 | `/etc/exim4/exim4.conf`. The main section usually ends with the first `begin` in that file.
36 | 
37 |     check_rfc2047_length = false
38 | 
39 | For further reference I recommend the official [Exim documentation](http://www.exim.org/exim-html-current/doc/html/spec_html/ch14.html)
```

content/posts/2012-02-20-Hosting-A-Static-Site-On-S3.md
```
1 | ---
2 | category: howto
3 | date-published: 2012-02-20T00:00:00Z
4 | title: Hosting A Static Site On Amazon S3
5 | uuid: 0af93d8a-9216-4b83-857e-b9bb4f7127d9
6 | permalink: /posts/hosting-a-static-site-on-s3.html
7 | hidden: true
8 | og-image: /images/selfies/3.jpg
9 | type: post
10 | ---
11 | 
12 | ## Preface
13 | 
14 | Hosting a static site is preferred by many developers because it gives you a maximum of
15 | control at a minimum of maintenance.
16 | While there are other tools available [Jekyll](https://github.com/mojombo/jekyll) became something like a reference
17 | implementation, especially under Ruby developers.
18 | Since Jekyll's creator, Tom Preston Werner, also founded Github it is not surprising that
19 | Github is providing a service to autogenerate and host your static site as soon as you push
20 | it.
21 | 
22 | Now, that sounds like perfect all around. It is. As long as you are not using custom
23 | generators or converters with Jekyll everything is good.
24 | If you do however you'll quickly notice that Github [disabled custom Ruby code](https://github.com/mojombo/jekyll/issues/325) to keep
25 | everything secure.
26 | 
27 | <aside><p>
28 | There are still ways to host your static site with Github but using these would mean
29 | losing nearly all the benefits from hosting at Github (eg. Autogeneration).</p></aside>
30 | 
31 | Since the requirements for hosting a static site are nearly non-existent you can easily move
32 | it onto any Server.
33 | 
34 | I decided for Amazon S3 because it's widely used, reliable and cheap.
35 | 
36 | ## Static Site Generators
37 | 
38 | While there are
39 | [quite](http://nanoc.stoneship.org/ "Nanoc") [a](http://middlemanapp.com "Middleman")
40 | [lot](https://www.ruby-toolbox.com/categories/static_website_generation "Ruby-Toolbox Listing").
41 | Jekyll is used by most people. When I chose Jekyll it was mostly because of it's active
42 | community and the fact that it is developed and used heavily by Github.
43 | Before I settled on Jekyll I gave nanoc a try. I don't exactly remember why I ditched nanoc
44 | but in the end Jekyll feels lighter and I also prefer Liquid Markup over ERB
45 | Syntax.
46 | 
47 | ## Setting Up Amazon S3
48 | 
49 | Comparing Github's free hosting with Amazon S3 is somewhat unfair since you got to pay for
50 | S3 storage and bandwith. Doing the math however you'll quickly notice that the price for
51 | hosting your static site on S3 is low.
52 | With a complete page size of 100M and traffic of 10G you would not pay more than 2$.
53 | 
54 | After creating a new bucket in your S3 instance there are only a few steps you need to do in
55 | order to have a proper static site hosted by S3.
56 | 
57 | **Important:** When creating your bucket make sure that it has the same name as the domain
58 | name you want to use for your static site (ex. www.martinklepsch.org). This domain has to
59 | have some subdomain.
60 | 
61 | **Step 1:** Enable S3's website feature by enabling it in the properties pane of your bucket.
62 | 
63 | <figure>
64 | <img alt='Website settings in the bucket propertie pane' src='/images/website-settings-s3.png'>
65 | </figure>
66 | 
67 | **Step 2:** Set a bucket policy that basically allows everyone to view the contents of your bucket.
68 | 
69 |     {
70 |       "Version":"2008-10-17",
71 |       "Statement":[{
72 |         "Sid":"PublicReadForGetBucketObjects",
73 |         "Effect":"Allow",
74 |         "Principal": {
75 |           "AWS": "*"
76 |         },
77 |         "Action":["s3:GetObject"],
78 |         "Resource":["arn:aws:s3:::www.REPLACE-THIS.org/*"]
79 |       }]
80 |     }
81 | 
82 | **Step 3:** Upload your static website to S3. You can either do that manually by using the
83 | AWS Management Console or you can automate the process by writing some small programm. There
84 | are S3 libraries for many programming languages.
85 | I built a [small rake task](https://github.com/martinklepsch/martinklepsch.org/blob/master/Rakefile "Rakefile on Github") that does the job.
86 | 
87 | If you are experiencing problems with the setup of S3 I recommend the official [AWS documentation](http://docs.amazonwebservices.com/AmazonS3/latest/dev/WebsiteHosting.html "AWS Static Website Hosting Documentation").
88 | 
89 | ## Naked Domain Name Fowarding
90 | 
91 | DNS does not allow to set the whats apparently called "zone apex" (`"example.com"`) to be
92 | a CNAME for another domain like `www.example.com.s3-website-us-east-1.amazonaws.com`.
93 | Therefore you need to redirect all requests going to your domain without `www` to you
94 | domain with `www` (`example.com` to `www.example.com`).
95 | [Read more.](https://forums.aws.amazon.com/thread.jspa?threadID=55995 "A thread in AWS forums with good information about the issue")
96 | 
97 | You can either do this by using your domain registrars control panel or by using a service
98 | like [wwwizer](http://wwwizer.com/naked-domain-redirect). I did it with
99 | [Gandi](http://gandi.net)'s control panel and it works fine.
```

content/posts/2012-03-08-Entypo-Icon-Set.md
```
1 | ---
2 | categories: linked
3 | date-published: 2012-03-08T12:00:00Z
4 | resource: http://www.entypo.com/
5 | title: Entypo Icon Set
6 | uuid: 58f62d22-8347-41b0-a368-81ee4b1771f1
7 | permalink: /posts/entypo-icon-set.html
8 | hidden: true
9 | og-image: /images/selfies/3.jpg
10 | type: post
11 | ---
12 | Daniel Bruce:
13 | 
14 | > Entypo is a set of 100+ carefully crafted pictograms available as an OpenType font, vector
15 | > EPS and web font. All released for free under the Creative Commons license CC BY-SA.
16 | 
17 | There is not much more to add. Entypo covers many common icon use cases and the fact that it
18 | ships with an OpenType font makes it even smoother to use.
19 | I use a few icons here and there on this site and mainly prefer this icon set for its
20 | cleanness and simplicity which allows you to easily integrate it into most designs.
21 | 
22 | <strike>Below you can see some of my favorites from the set. The cool thing about having it as
23 | a font is also that you can change colors very easy.</strike> Deprecated the demo.
24 | 
25 | For a complete listing including a character map [check this out](http://bistro.convergencecms.co/entypo).
26 | Be sure to follow [Daniel on Twitter](http://twitter.com/danielbruce_) for more icon
27 | awesomeness.
28 | 
```

content/posts/2012-03-08-Startups-This-Is-How-Design-Works.md
```
1 | ---
2 | category: linked
3 | date-published: 2012-03-08T10:00:00Z
4 | resource: http://www.startupsthisishowdesignworks.com/
5 | title: Startups, This Is How Design Works
6 | uuid: e5058531-a427-4791-8146-ae2f24e6973b
7 | permalink: /posts/startups-this-is-how-design-works.html
8 | og-image: /images/selfies/1.jpg
9 | hidden: true
10 | type: post
11 | ---
12 | 
13 | > Companies like Apple are making design impossible for startups to ignore. Startups like Path,
14 | > Airbnb, Square, and Massive Health have design at the core of their business, and they're
15 | > doing phenomenal work. But what is ‘design’ actually? Is it a logo? A Wordpress theme? An
16 | > innovative UI?
17 | >
18 | > It’s so much more than that. It’s a state of mind. It’s an approach to
19 | > a problem. It’s how you’re going to kick your competitor’s ass. This handy guide will help
20 | > you understand design and provide resources to help you find awesome design talent.
21 | > <cite>Wells Riley: <a href="http://startupsthisishowdesignworks.com">Startups, This Is How Design Works</a></cite>
22 | 
23 | *Startups, This Is How Design Works* is an excellent read for every designer and those
24 | who want to be one. It builds up around a definition of good design by Dieter Rams, an early consumer
25 | product designer at Braun which inspired Apple in many of their fundamental design choices.
26 | 
27 | The web is definitly missing more high quality resources like this.
28 | It just takes people to much time to realize that design is not about photoshop brushes.
```

content/posts/2012-05-03-Kandan-Team-Chat.md
```
1 | ---
2 | date-published: 2012-05-03T00:00:00Z
3 | categories: linked
4 | resource: https://github.com/cloudfuji/kandan
5 | title: Kandan Team Chat
6 | uuid: 31835991-b050-4b17-b56d-c2fcbb06dbb7
7 | permalink: /posts/kandan-team-chat.html
8 | og-image: /images/selfies/2.jpg
9 | hidden: true
10 | type: post
11 | ---
12 | I just discovered [Kandan](http://kandan.me) which seems to be a great opensource alternative to
13 | [Campfire](http://campfirenow.com/) (a web-based team chat application).
14 | The interface is pretty similar and the developers
15 | probably borrowed a few of the interface ideas behind campfire.
16 | 
17 | ![Kandan Screenshot](/images/small-kandan-preview.png)
18 | 
19 | Some time ago there was also [Grove.io](https://grove.io/) which advertises itself as
20 | "hosted IRC". The web interface they provide also provides some of Campfires more pupular
21 | features such as image embedding. Unfortunately they went into a paid-only model which makes
22 | it less attractive to many people.
23 | 
24 | Hopefully Kandan evolves into something bigger and team chat in companies is no longer
25 | limited to just Campfire or worse Skype.
```

content/posts/2012-05-07-A-Friend-Is-Looking-For-A-Summer-Internship.md
```
1 | ---
2 | date-published: 2012-05-07T00:00:00Z
3 | title: A Friend Is Looking For A Summer Internship
4 | uuid: 2c16f352-042b-4c2d-b294-d9d3f34ba0f4
5 | permalink: /posts/a-friend-is-looking-for-a-summer-internship.html
6 | hidden: true
7 | og-image: /images/selfies/3.jpg
8 | type: post
9 | ---
10 | This summer a friend of mine is visiting Berlin for around 6 weeks by end of June. I encouraged him to work somewhere to get an impression of working in technology companies during that time.
11 | 
12 | He is still quite young but eager to learn more than most other people I know.  He's 17 from
13 | Krakow in Poland where he'll attend at [AGH University of Science and Technology](http://www.agh.edu.pl/en) 
14 | studying Computer Science by October next year. He
15 | speaks German on a nearly native level (he got into several final rounds of nationwide
16 | language contests) and also English.
17 | Beside that he also reached the maximum score at
18 | [Mathematical Kangaroo](http://en.wikipedia.org/wiki/Mathematical_Kangaroo) and won several
19 | other math olympiades.
20 | 
21 | From a technical point of view he is mostly comfortable with Python and C++. He did not work
22 | a lot in any special area of software development so he might be unexperienced with the
23 | special limitations or technologies that are used in one area. He has some experience with
24 | web technologies (including Django) though.
25 | 
26 | If you'd like to get in touch and to get to know some great new talent just send me a short
27 | note and I will introduce you gladly.
```

content/posts/2012-05-21-Paris-And-Back.md
```
1 | ---
2 | date-published: 2012-05-21T00:00:00Z
3 | title: Paris And Back
4 | uuid: 9b66924e-12c5-4f70-bfcc-c2b732defb0d
5 | permalink: /posts/paris-and-back.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | After being back home from Paris where I stayed the last two weeks I have a fun story to
10 | share. It's not about something great that happened in Paris but about something that
11 | started the evening before my departure.
12 | 
13 | After meeting with a friend there we got home at 2am and set up the alarm to 4:35am to get
14 | up in time, get some work done and get to the airport sometime around 6.20am.
15 | When I opened my eyes I immediately noticed that there was no alarm ringing. That made me
16 | jump for the alarm clock. It was 7.15am. I started to hate myself. I even asked my friend to
17 | pinch me so that this nightmare ends. 
18 | 
19 | *This was the second flight for me ever and I missed it.*
20 | 
21 | We went to the trainstation then and asked about a train. 125€ and an 8 hours trainride.
22 | Since that was a nighttrain we decided to go to the airport and check out if there are some
23 | cheap flights which I could book — *last minute*.
24 | There was a flight. It was not cheap with a 182€ pricetag though. Given the fact that
25 | I payed 100€ for the original flights I was completely pissed. After a couple of minutes
26 | I decided to take the flight for 182€ and we even made a little fun about what it costs to
27 | sleep in. Nevertheless I still hated myself for not being able to get up in time.
28 | 
29 | Now this is already a story worth telling but it had to come better.
30 | 
31 | When waiting to board the plane things got delayed because the plane was struck by lightning
32 | and the ground crew had to do extra technical checks. After some time they announced that
33 | they found another plane and that boarding will start in 30 minutes.
34 | 
35 | I decided to walk around a little and when I came back the flight assistant was asking some
36 | people sitting on the ground something I could not understand. So I asked one french girl
37 | who was sitting there what she asked. And here it comes. She asked if you would take a later
38 | flight in exchange for 250€. When I heard that I immediately ran to the flight assistant and
39 | told her: *"I'm in!"* and so I got 250€.
40 | 
41 | As a small bonus we even got a voucher for a restaurant at the airport where I another
42 | french girl and one american guy spent our 2 hours extra waiting time eating some good meat
43 | and french fries.
44 | 
45 | *I felt so incredibly lucky.*
46 | 
47 | Before we got to the airport I told my friend:
48 | 
49 | > Oh, and that happens to me. Usually I am so lucky.
50 | 
51 | 
```

content/posts/2012-06-28-Twelve-Factor-App.md
```
1 | ---
2 | categories: linked
3 | date-published: 2012-06-28T00:00:00Z
4 | resource: http://www.12factor.net/
5 | title: The Twelve Factor App
6 | uuid: 36e26c80-51ab-4350-b714-f5083763d86a
7 | permalink: /posts/twelve-factor-app.html
8 | og-image: /images/selfies/1.jpg
9 | type: post
10 | ---
11 | Around December 2011 the awesome crowd around Heroku released a [now open-source document](https://github.com/adamwiggins/12factor) that describes 12 factors that
12 | build up a methodology of developing software-as-a-service products.
13 | 
14 | The main goals of [those 12 rules](http://www.12factor.net/) are:
15 | 
16 | * minimizing effort to board new developers
17 | * maximizing portability
18 | * encouraging continous deployment
19 | * allowing to scale effortless
20 | 
21 | If you are developing a SaaS product this document will show you lots of ways to
22 | improve your development practices. Fortunately it is not directly coupled to any specific
23 | language or development environment (most tool examples are coming from Ruby, Python and Clojure
24 | though) so that everyone will have something to take away.
```

content/posts/2012-07-25-Models-Operations-Views-And-Events.md
```
1 | ---
2 | categories: linked
3 | date-published: 2012-07-25T00:00:00Z
4 | resource: http://cirw.in/blog/time-to-move-on
5 | title: Models, Operations, Views and Events
6 | uuid: 22ccc641-b55e-4c29-83b1-631fbd4598f6
7 | hidden: true
8 | permalink: /posts/models-operations-views-and-events.html
9 | og-image: /images/selfies/3.jpg
10 | type: post
11 | ---
12 | Conrad Irwin:
13 | 
14 | > **Models** encapsulate everything that your application knows.<br>
15 | > **Operations** encapsulate everything that your application does.<br>
16 | > **Views** mediate between your application and the user.<br>
17 | > **Events** are used to join all these components together safely.<br>
18 | 
19 | While MVC is still widely used and considered a good pattern for modern application
20 | development there are also a few people thinking about alternatives that suit the actual
21 | application logic better than the current mindset.
22 | One of those people is [Conrad Irwin](http://cirw.in). His approach is very focused on
23 | connecting the different parts of the application by using events which seems at least easier to
24 | grasp than the exact task of *Controllers* in *Model-View-Controller*.
```

content/posts/2012-08-20-When-We-Build-Stuff.md
```
1 | ---
2 | categories: linked
3 | date-published: 2012-08-20T00:00:00Z
4 | resource: https://vimeo.com/34017777
5 | title: When We Build Stuff
6 | uuid: fa108763-3ed7-4ba6-bc4a-c39ddde35b5a
7 | permalink: /posts/when-we-build-stuff.html
8 | og-image: /images/selfies/1.jpg
9 | type: post
10 | ---
11 | I recently discovered this great presentation by checking out some of my follow-recommendations on
12 | twitter. [Wilson Miner](https://twitter.com/wilsonminer) was one of them.
13 | This talk adresses everyone who builds software and makes some of the broader side-effects
14 | of designing things other people use more visible.
15 | 
16 | The presentation itself is more a piece of acting than a usual talk.
17 | I enjoyed the variety so I hope you will too.
18 | 
19 | <div><iframe src="https://player.vimeo.com/video/34017777?byline=0&amp;portrait=0&amp;color=a5e2a1" frameborder="0" webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></div>
20 | 
21 | <p><a href="http://vimeo.com/34017777">Wilson Miner - When We Build</a> from <a href="http://vimeo.com/build">Build</a> on <a href="http://vimeo.com">Vimeo</a>.</p>
```

content/posts/2012-12-17-Git-Prompt-for-Fish-Shell.md
```
1 | ---
2 | date-published: 2012-12-17T00:00:00Z
3 | title: Git Information in Fish Shell&rsquo;s Prompt
4 | uuid: 83080c01-c898-4e8e-9996-97f8d60d3a4a
5 | permalink: /posts/git-prompt-for-fish-shell.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | After toying around with [Zsh](http://zsh.org) for a while I stumbled upon Fish
10 | or more precisely [Ridiculous Fish](http://ridiculousfish.com/shell)
11 | which is a fork of the original [Fish Shell](http://fishshell.com/).
12 | 
13 | Since Fish does not have a configuration framework like
14 | [oh-my-zsh](https://github.com/robbyrussell/oh-my-zsh)
15 | you have to setup your prompt with additional Git information yourself.
16 | I consider this an advantage though as you can't just take a bunch of configuration files
17 | from somewhere without having an idea what they actually do.
18 | 
19 | It took me a while to find [this part](https://github.com/fish-shell/fish-shell/blob/master/share/functions/__fish_git_prompt.fish) of the Fish source code
20 | which documents this functionality quite well. An example which you can put into
21 | `~/.config/fish/config.fish` follows below:
22 | 
23 | ```
24 | set normal (set_color normal)
25 | set magenta (set_color magenta)
26 | set yellow (set_color yellow)
27 | set green (set_color green)
28 | set red (set_color red)
29 | set gray (set_color -o black)
30 | 
31 | # Fish git prompt
32 | set __fish_git_prompt_showdirtystate 'yes'
33 | set __fish_git_prompt_showstashstate 'yes'
34 | set __fish_git_prompt_showuntrackedfiles 'yes'
35 | set __fish_git_prompt_showupstream 'yes'
36 | set __fish_git_prompt_color_branch yellow
37 | set __fish_git_prompt_color_upstream_ahead green
38 | set __fish_git_prompt_color_upstream_behind red
39 | 
40 | # Status Chars
41 | set __fish_git_prompt_char_dirtystate '⚡'
42 | set __fish_git_prompt_char_stagedstate '→'
43 | set __fish_git_prompt_char_untrackedfiles '☡'
44 | set __fish_git_prompt_char_stashstate '↩'
45 | set __fish_git_prompt_char_upstream_ahead '+'
46 | set __fish_git_prompt_char_upstream_behind '-'
47 | 
48 | 
49 | function fish_prompt
50 |   set last_status $status
51 | 
52 |   set_color $fish_color_cwd
53 |   printf '%s' (prompt_pwd)
54 |   set_color normal
55 | 
56 |   printf '%s ' (__fish_git_prompt)
57 | 
58 |   set_color normal
59 | end
60 | ```
```

content/posts/2013-03-10-From-Zero-To-Marathon-In-Six-Months.md
```
1 | ---
2 | date-published: 2013-03-10T00:00:00Z
3 | FBimage: /images/flyknitbw.png
4 | title: From Zero to Marathon in Six Monthts
5 | uuid: 73abd373-1c0b-44d1-aed4-2e48a5abfc6f
6 | hidden: true
7 | permalink: /posts/from-zero-to-marathon-in-six-months.html
8 | og-image: /images/selfies/1.jpg
9 | type: post
10 | ---
11 | 
12 | After moving to Berlin I kind of failed to continue some regular sports activity.
13 | This is two years ago now. Today I'm going to change that.
14 | 
15 | Today I'm starting to run.
16 | 
17 | ## Why Running
18 | 
19 | The answer is as simple as it gets: because it can be done everywhere at any time and without
20 | any organizational effort such as finding a partner for going climbing, which I did back
21 | then in my old hometown.
22 | 
23 | ## Running a Marathon
24 | 
25 | To stay motivated in the long term I decided it would be nice to set some goal to achieve
26 | and what else could that be beside running a marathon? There is not much coming to my mind.
27 | So starting today I'm training to run a marathon in about six months. I don't know which
28 | marathon yet unfortunately, especially since the [Berlin marathon](http://www.bmw-berlin-marathon.com/)&rsquo;s registration is closed already.
29 | 
30 | My training schedule isn&rsquo;t finished yet but for the first month I&rsquo;m planning to
31 | run at least [3 days a week](http://www.runmap.net/route/437129) at an average distance
32 | of six kilometers.  I hope to run longer distances irregularly as soon as possible as
33 | well.
34 | 
35 | Today is Day One.
```

content/posts/2013-04-25-Asynchronous-Communication.md
```
1 | ---
2 | date-published: 2013-04-25T00:00:00Z
3 | title: Asynchronous Communication
4 | uuid: d3811416-0031-4a02-8578-521a4540f889
5 | permalink: /posts/asynchronous-communication.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | Communication is a central part of working in teams.
10 | I believe having asynchronous communication channels and making heavy use of them can have
11 | great effects on productivity, transparency and therefore happiness and fun within teams.
12 | Here are a few reasons why.
13 | 
14 | ## Interruptions
15 | 
16 | There are [dozens of studies](http://www.nytimes.com/2007/03/25/business/25multi.html)
17 | about the consequences of being interrupted while performing demanding tasks. They usually
18 | suggest that it takes a human around 20 minutes to get back into the state they have been in
19 | prior to the interruption. As if this wouldn't be enough it also increases the chances of
20 | making mistakes.
21 | 
22 | While a chatroom doesn't necessarily removes interruptions all at once it
23 | allows everyone to react to an interruption at will. If you really want no interruption for
24 | a certain period of time you can easily turn off all notifications.
25 | Not reacting to the waving hands of a coworker is quite a bit harder (and not very polite as
26 | well).
27 | 
28 | ## Transparency
29 | 
30 | Transparency is a great thing. I strongly believe an absolutistic approach to transparency
31 | is the only way to go nowadays. Transparency helps to identify bad decisions early on and
32 | increases acceptance of decisions by keeping everyone in the loop. Companies like
33 | [Stripe](https://stripe.com/blog/email-transparency) are doing an excellent job at
34 | advocating this approach. I could go into lengths about how important this is.
35 | 
36 | Achieving this level of transparency with meetings and written reports is very time
37 | consuming if not impossible at all. Channeling most communication through asynchronous
38 | communication channels allows other people to passively follow along and saves a bunch
39 | of time that would otherwise have been spent in inefficient meetings.
40 | 
41 | ## Written Communication
42 | 
43 | Think before you speak. That's what they say. Problem is that this is quite hard in a fast
44 | paced environment like meetings. Just taking 5 minutes to order your thoughts isn't
45 | something people usually do there. Instead they talk. Written communication forces you to
46 | get to the point in a way that everyone can follow and at the same time allows you to refine
47 | your own understanding of the problem.
48 | 
49 | ## Tool Integration
50 | 
51 | Most of the team chat things you'll find provide integrations with your existing toolchain.
52 | It's easy to setup a bot that notifies you about breaking builds, new commits, errors on
53 | your platform or whatever else. Integrating this kind of information into your communication
54 | channels allows better collaboration on it and also increases visibility of what people are
55 | working on (= transparency).
56 | 
57 | ## Logging
58 | 
59 | In my experience things are often discussed in passing and sometimes even decisions are made
60 | in these situations. A week later when you actually find the time to implement the things
61 | decided back then they seem stupid and you are not able to actually retrieve the reasoning
62 | behind them from the back of your head. Having in place the infrastructure to quickly
63 | discuss something without setting up a meeting is a great way to avoid this kind of decision
64 | making and also allows you to trace back past discussions if a decision is being questioned.
65 | 
66 | ## Location-independent Productivity
67 | 
68 | Channeling most conversation through chat rooms makes team members less dependent on being
69 | geographically close to each other. (I'm not expanding on this as I think removing
70 | a dependency is always a good thing.)
71 | 
72 | ## Closing Remarks & Further Reading
73 | 
74 | I'm a strong believer when it comes to asynchronous communication and I hope this list
75 | provides a good overview about the main reasons why it's a good approach to internal
76 | communication.
77 | 
78 | * [Chat Trumps Meetings](http://zachholman.com/posts/chat/) by Zach Holman
79 | * [How Github Works Asynchronous](http://zachholman.com/posts/how-github-works-asynchronous/) by Zach Holman
80 | * [Stripes Culture](http://blog.alexmaccaw.com/stripes-culture) by Alex MacCaw
81 | * [How Stripe Builds Software](http://blog.alexmaccaw.com/stripes-culture) with Greg Brockmann
```

content/posts/2013-04-28-Analytics-Data.md
```
1 | ---
2 | date-published: 2013-04-28T00:00:00Z
3 | title: Analytics Data
4 | uuid: 4682a859-015c-401e-a3e4-f47938c9312b
5 | permalink: /posts/analytics-data.html
6 | hidden: true
7 | og-image: /images/selfies/3.jpg
8 | type: post
9 | ---
10 | For quite a while now there is this idea of *being data-driven* and analyzing
11 | everything that is happening on your platform in order to identify potential
12 | changes that could improve your core metrics such as user engagement.
13 | 
14 | There are [quite](https://keen.io/) [a](https://mixpanel.com/)
15 | [few](https://www.kissmetrics.com/) services allowing you to send events
16 | triggered by users to their servers that provide you great interfaces to
17 | actually make some sense of the data you gathered.
18 | 
19 | While this is all great I see one major problem with this style of analytics:
20 | the data is not really yours. In a time where user engagement is getting more
21 | and more important I find it troubling to store all the data you collected
22 | on a service only accessible through a limited API (vs. full featured database access.)
23 | 
24 | Limited access to this data can slow you down building internal tools
25 | like dashboards and custom metrics/reports. There is also a vendor lock-in
26 | to a certain degree. While you can switch vendors easily using
27 | Segment.io's [analytics.js](https://segment.io/libraries/analytics.js/)
28 | taking your data with you is not as easy as that.
29 | 
30 | While I'm usually a fan of outsourcing certain complex things (i.e. hosting)
31 | I feel like this is one of the rare occasions where people should think twice.
32 | Unfortunately I'm not aware of good alternatives.
33 | If you are please [tweet me](http://twitter.com/martinklepsch).
```

content/posts/2013-09-24-A-Trip-To-The-US.md
```
1 | ---
2 | date-published: 2013-09-24T00:00:00Z
3 | abstract: I'm going on a short trip to the US (NYC, PHL & MIA) and I'd love to meet some fun people!
4 | title: A Trip To The US
5 | uuid: adc62a3d-a5de-43b3-831f-56c3ee377d29
6 | permalink: /posts/a-trip-to-the-us.html
7 | hidden: true
8 | og-image: /images/selfies/2.jpg
9 | type: post
10 | ---
11 | Wow. It has been quite a while since I wrote anything here. Almost 5 months, phew.
12 | 
13 | So what I have to say is that I'm going to visit the US from **26th of October**
14 | to **10th of November**. I plan to be in New York City the first
15 | weekend, spend some time around Philadelphia within the week after that and
16 | then go to Miami for another week.
17 | 
18 | I'd love to meet some interesting people, have some beers, crash some parties.
19 | So if you have some friends I should meet, [let me know](mailto:martinklepsch+us@googlemail.com)!
20 | 
21 | Also me and the friend I'm visiting (who's from Philadelphia) are looking for
22 | a couch to crash while we're staying in New York and Miami.
23 | 
24 | Cheers to that — It's gonna be fun!
```

content/posts/2013-12-01-A-Resurrection-Post.md
```
1 | ---
2 | date-published: 2013-12-01T00:00:00Z
3 | title: A Resurrection Post
4 | uuid: ecc821bc-1816-4571-aab4-da5ef4e8d346
5 | permalink: /posts/a-resurrection-post.html
6 | og-image: /images/selfies/3.jpg
7 | hidden: true
8 | type: post
9 | ---
10 | 
11 | After not being active here for the last couple of months I thought a bit about
12 | what to do with this blog and came up with some changes and ideas.
13 | 
14 | 1. **More regular updates and smaller linked-list style posts.** Writing full
15 |    length posts about more complex topics is nice but not something I can do
16 |    regulary right now. Still there are often things I find and would like to
17 |    share. I also hope that this gets me more used to “regular publishing” and
18 |    thus increases the frequency of more in-depth posts.
19 | 2. **A newsletter.** There will be a (most likely digest-style)
20 |    newsletter in irregular intervals depending on the frequency of updates.<br>
21 |    **Why a newsletter?** This is most likely a topic for a seperate post but
22 |    with the advent of platforms like Medium, Twitter and friends it's common not to “own
23 |    the connection” between a publisher and a writer anymore and I believe that
24 |    this is wrong. There is RSS but it's only a one way channel and I'd actually like to
25 |    get to know the people reading the stuff I'm posting here. Also not everyone
26 |    has an RSS reader set up. There is a subscribe form at the end of each post
27 |    and on [the index page](/).
28 | 3. **Some design changes.** To keep that one short: less wierd “about me”
29 |    bullshit, more content. Also: [rainbow](javascript:;) [colors](javascript:;)
30 |    [everywhere](javascript:;)!
31 | 
32 | I already have a couple of things queued up to be published including one more
33 | lengthy post about how I'd attempt to set up a company if I'd have to do it now (everyone knows better, right?)
34 | If you have thoughts on this [I'd appreciate any input](mailto://martinklepsch@googlemail.com)
35 | on the draft I have laying around.
```

content/posts/2013-12-03-Sculleys-Disease.md
```
1 | ---
2 | tags: quotes
3 | date-published: 2013-12-03T00:00:00Z
4 | resource: http://37signals.com/svn/posts/3497-you-know-one-of-the-things-that-really-hurt
5 | title: Sculley's Disease
6 | uuid: 117af064-eea6-4eff-aaa8-c18c35d97ad3
7 | permalink: /posts/sculleys-disease.html
8 | og-image: /images/selfies/3.jpg
9 | type: post
10 | ---
11 | <p style='display:none'></p>
12 | 
13 | Just found this quote by Steve Jobs on 37signals’ blog. It's taken from
14 | [The Lost Interview](http://www.youtube.com/watch?v=F4L26Jp_AT4&list=TLMnOz1ppz1Sw)
15 | that was conducted shortly after Steve left Apple and founded NeXT. The question
16 | was: “What's important to you in the development of a product?”
17 | 
18 | > You know, one of the things that really hurt Apple was after I left John
19 | > Sculley got a very serious disease. It’s the disease of thinking that
20 | > a really great idea is 90% of the work. And if you just tell all these other
21 | > people “here’s this great idea,” then of course they can go off and make it
22 | > happen.
23 | >
24 | > And the problem with that is that there’s just a tremendous amount of
25 | > craftsmanship in between a great idea and a great product. And as you
26 | > evolve that great idea, it changes and grows. It never comes out like it
27 | > starts because you learn a lot more as you get into the subtleties of
28 | > it. And you also find there are tremendous tradeoffs that you have to
29 | > make. There are just certain things you can’t make electrons do. There
30 | > are certain things you can’t make plastic do. Or glass do. Or factories
31 | > do. Or robots do.
32 | >
33 | > Designing a product is keeping five thousand things in your brain
34 | > and fitting them all together in new and different ways to get what
35 | > you want. And every day you discover something new that is a new
36 | > problem or a new opportunity to fit these things together a little
37 | > differently.
38 | >
39 | > And it’s that process that is the magic.
40 | > <cite>Steve Jobs</cite>
```

content/posts/2013-12-05-What-Do-We-Need-To-Know.md
```
1 | ---
2 | tags: webdevelopment
3 | date-published: 2013-12-05T00:00:00Z
4 | resource: http://alistapart.com/column/never-heard-of-it
5 | title: What do we need to know?
6 | uuid: 2bf0d1e8-11eb-4ee5-970e-cfb3ce6cdf59
7 | permalink: /posts/what-do-we-need-to-know.html
8 | og-image: /images/selfies/3.jpg
9 | type: post
10 | ---
11 | 
12 | > I go through periods of self-doubt about my qualifications as a web developer.
13 | > I have a sense I’m not alone in this. Our field is by nature a generalists’
14 | > field, where expertise involves synthesis of concepts and technologies, not
15 | > complete mastery of a single, static topic. It’s hard to know how to tell if
16 | > you’re good at your job.
17 | 
18 | This post on A List Apart resonated with me a lot. [Liza Danger Gardner](https://twitter.com/lyzadanger)
19 | describes a situation we all have been in: someone talks about that
20 | *new feature you never heard of* and you have no idea what they are talking about. Web
21 | development being a profession that develops so fast and in so many directions
22 | at the same time <strike>sometimes</strike> makes it hard to follow along.
23 | 
24 | <figure> <img src='/images/fear.gif' > </figure>
25 | 
26 | > There’s no defined lesson plan or standardized test for the many branches of
27 | > real-world applied web development, and the whole profession is a moving target.
28 | > So if we can’t possibly know everything, all the time, what things do we need to
29 | > know?
30 | 
31 | She then attempts to answer this question by answering the opposite question “What do we
32 | not need to know?” which is fine but doesn't exactly provide you with a list of
33 | things that are *required*. Probably this list is hard to create but I'm curious
34 | what you would put on it. What are the high level concepts in web development
35 | everyone should know about? Is it “How the DOM works” or is it “What Prototypes are in Javascript”
36 | or is it ...?
37 | 
38 | I'd love to hear what you think is “required knowledge” on Twitter ([@martinklepsch](https://twitter.com/martinklepsch))
39 | or elsewhere.
```

content/posts/2014-01-06-Code-Simplicity-Review.md
```
1 | ---
2 | date-published: 2014-01-06T00:00:00Z
3 | title: Code Simplicity
4 | uuid: 798632b6-60b7-4163-b1cc-ef3f49bbbc7b
5 | permalink: /posts/code-simplicity-review.html
6 | og-image: /images/selfies/2.jpg
7 | hidden: true
8 | type: post
9 | ---
10 | When O'Reilly promoted their cyber monday I somehow really got into a shopping
11 | spree.
12 | 
13 | First book I finished now is [Code Simplicity](http://shop.oreilly.com/product/0636920022251.do) by
14 | [Max Kanat Alexander](http://max.kanat-alexander.com/). The book doesn't hold on to the
15 | scientific approach that the blurb promises but once I made my peace with that
16 | it became a good read.   
17 | Some of the things I liked most:
18 | 
19 | - It provides good reasoning why an iterative process usually provides better
20 |   results than the old *Plan & Implement* workflow.
21 | - It has a strong argumentation for testing that can help you the next time
22 |   you have to explain why it is important or understand it yourself if you didn't
23 |   so far.
24 | - It gives you some great new angles on how to prioritize features.
25 | 
26 | Overall I'd probably recommend the book but you shouldn't expect to much
27 | scientific proof. Probably that's not even possible in that area but the
28 | blurb said so and that caused
29 | [some](https://readmill.com/christoffer/reads/code-simplicity)
30 | [bad](https://readmill.com/chdorner/reads/code-simplicity) reviews so take it
31 | with a grain of salt.
32 | 
33 | You can find all of my personal highlights [on Readmill](https://readmill.com/mklappstuhl/reads/code-simplicity)
```

content/posts/2014-01-08-Running-A-Marathon-or-Not.md
```
1 | ---
2 | FBimage: /images/tough-mudder.jpg
3 | date-published: 2014-01-08T00:00:00Z
4 | title: Running a Marathon, Or Not
5 | uuid: a4f45de9-5915-46ed-a41a-b10f4560a177
6 | permalink: /posts/running-a-marathon-or-not.html
7 | hidden: true
8 | og-image: /images/selfies/2.jpg
9 | type: post
10 | ---
11 | Earlier this year I came to the “very” surprising conclusion that I'm not doing enough
12 | sports in relation to the hours I spend in front of a computer every day.
13 | [I decided to run a marathon this September](/2013/03/10/From-Zero-To-Marathon-In-Six-Months/).
14 | I didn't. I did something else though that was fun and kind of sporty as well.
15 | 
16 | After starting runnning in March it was pretty amazing to see how quickly your
17 | stamina improves and longer distances become easier and less painful to run.
18 | After that initial success though improvements got smaller and motivation
19 | tanked quickly. Also the marathon I wanted to run was already booked
20 | out apparently.
21 | 
22 | - **March** 60.3km
23 | - **April** 33.5km
24 | - **May** 18.3km
25 | - **June** 5.5km
26 | - **July** 5.1km
27 | - **August** 0.0km
28 | - **September** 20km
29 | - **October** 4.8km
30 | 
31 | From a friend who also inspired me to go running I heard about **Tough Mudder**.
32 | Since I already noticed my lack of a goal I almost immediately registered in the
33 | beginning of June. The description on Wikipedia sounded exciting for sure:
34 | 
35 | > **Tough Mudder** is an endurance event series in which participants attempt 10-12
36 | > mile long military-style obstacle courses. Designed by British Special Forces to
37 | > test mental as well as physical strength, obstacles often play on common human
38 | > fears, such as fire, water, electricity and heights.
39 | > <cite><a href='http://en.wikipedia.org/wiki/Tough_Mudder'>Wikipedia</a></cite>
40 | 
41 | 
42 | ## <strike>Preparation</strike>
43 | 
44 | After seeing the (surprisingly) fast improvements when training I was certain
45 | I could get ready for a 20km run in a month or less. So I decided to start
46 | training a month before the event on 19th October:
47 | 
48 | - **01.06 - 23.09** 0.0km
49 | - **24.09** 5.7km
50 | - **28.09** 9km
51 | - **30.09** 6.3km
52 | - **06.10** 4.8km
53 | - **19.10** Tough Mudder
54 | 
55 | Somehow I wasn't quite persistent and stopped shortly after starting again.
56 | I guess mostly because it was such a pain to build up stamina **again**.
57 | 
58 | ## Oh Shit! It's tomorrow!
59 | 
60 | The days immediately before the event were full of uncertainty and doubt whether
61 | I could make it or not. 20km without any training? Is that even remotely
62 | possible? I never ran 20km before.
63 | Sometimes excuses not to go popped up in my mind but the hefty price tag of
64 | around 100€ and the social pressure of running in a group didn't really leave
65 | the option of not going.
66 | 
67 | **Surprisingly it was possible**. The around 15 obstacles usually took some time to pass
68 | and equally gave some to rest. The hardest part (stamina-wise)
69 | was a 2-4km trail up and down a hill over and over again. The whole course took
70 | us around 3 hours to complete.  
71 | We got a bit dirty on the way. I say **we** because completing the course on
72 | your own is almost impossible and we certainly have been a great team:
73 | 
74 | <figure>
75 | <img src='/images/tough-mudder.jpg'>
76 | </figure>
```

content/posts/2014-01-09-Telegram.md
```
1 | ---
2 | resource: http://telegram.org
3 | FBimage: /images/telegram-logo.png
4 | date-published: 2014-01-09T00:00:00Z
5 | title: Sending You a Telegram
6 | uuid: 28bbfb38-f744-443c-808e-ad3f1ebd808f
7 | hidden: true
8 | permalink: /posts/telegram.html
9 | og-image: /images/selfies/3.jpg
10 | type: post
11 | ---
12 | [Telegram](https://telegram.org) is a new messenger built by Pavel and Nikolai
13 | Durov.  They previously founded vk.com, Europe's second largest social network
14 | after Facebook (especially popular in Russia).
15 | 
16 | ## A Few Reasons Why It's Great
17 | 
18 | 1. Unlike most other messaging apps their [clients (Android, iOS) source code and the underlying protocols are released with an open source license](https://telegram.org/source) (GPL v2)
19 | 2. The founders note that they have no intention of [making money with Telegram](https://telegram.org/faq#q-how-are-you-going-to-make-money-out-of-this) If they ever run out of money they want to finance it with donations and non-essential goodies.
20 | 3. There is an [API](https://core.telegram.org/api) that allows anyone to hook
21 |    into Telegram and build clients, bots and additional tools.
22 | 4. The open API has led to the creation of 
23 |    [Mac](https://itunes.apple.com/de/app/messenger-for-telegram/id747648890),
24 |    [Windows](http://tdesktop.com/) and various other clients
25 |    (eg. [commandline](https://github.com/vysheng/tg)) that allow you to use Telegram
26 |    on multiple devices (with proper syncing).
27 | 5. Groups can hold up to 200 users (compared to WhatsApp that allows you to
28 |    create 50 groups à 50 participants max.)
29 | 6. All communication in Telegram [is encrypted](https://telegram.org/faq#security) and there is even an option to
30 |    create “[secret chats](https://telegram.org/faq#secret-chats)” that: won’t be
31 |    stored on Telegram’s servers, can’t be fowarded and will delete themself after some
32 |    time.
33 | 
34 | Currently available for
35 | [iOS](https://itunes.apple.com/us/app/telegram-messenger/id686449807),
36 | [Android](https://play.google.com/store/apps/details?id=org.telegram.messenger),
37 | [Mac](https://itunes.apple.com/de/app/messenger-for-telegram/id747648890)
38 | & [Windows](http://tdesktop.com) there is really no reason not to make the
39 | switch.
40 | 
41 | **Send me a message how you like it:** <a href="tel:+4917664718250">+49176 64718250</a>.  
42 | (It’s easy: just tap the number on your phone, save me as contact and I’ll appear
43 | in your Telegram contacts.)
44 | 
45 | **Edit:** Although it's not a completely decentralized service (as communication services
46 | should eventually be in the future) it seems to be a big step in the right direction.
47 | 
```

content/posts/2014-02-05-living-small.md
```
1 | ---
2 | FBimage: /images/dome-cropped.jpg
3 | date-published: 2014-02-05T00:00:00Z
4 | title: Living Small
5 | uuid: 24f91dd6-6b9e-4bca-9ae3-6f0a1465b4aa
6 | permalink: /posts/living-small.html
7 | og-image: /images/selfies/3.jpg
8 | type: post
9 | ---
10 | Living in a world where consumerism appears to be the predominant behaviour it seems more and more exciting to me to live a less materialistic lifestyle. What follows are some of the things that stimulated my thinking — maybe they do the same for you.
11 | 
12 | After stumbling upon Bruce Hauman’s blog while trying to figure out some Clojure stuff I discovered another post on his blog where he talks about [building a geodesic dome](http://rigsomelight.com/2013/09/09/frameless-geodesic-dome.html) in which he’s now (partly?) living for 3.5 years. It’s an amazingly analytical post about building something and simplifying it to it’s purest, most functional form. Motivated by spending less money on rent he iterated on his idea resulting in this construction:
13 | 
14 | <figure>
15 | <img src='/images/dome-cropped.jpg'>
16 | <figcaption>In case you were wondering what this “geodesic dome” thing looks like</figcaption>
17 | </figure>
18 | 
19 | In his post he also mentions the [Tiny Housing Movement](http://thetinylife.com/what-is-the-tiny-house-movement/) through which I found a TED talk that nicely illustrates the pitfalls of the idea of living “a good life” once you have your own income. The key line in the talk is: **What does freedom mean to you?** I wanted to give a small summary here but, as it is with TED talks, [the talk itself](http://www.youtube.com/watch?v=9XRPbFIN4lk) does it’s job pretty well.
20 | 
21 | I believe as thoughtful members of our society we should **rethink our relationship to stuff**. How can we, as a society, make better use of the things we have at our disposal? What do I really need to **own** to live my life? Ideas like [the share economy](http://en.wikipedia.org/wiki/Sharing_economy) seem like a logical step with the increasing ease of sharing/routing information.
22 | 
23 | Purposely living with less is obviously not a new idea and so it happens to be part of things like [the 100 Things Challenge](http://zenhabits.net/minimalist-fun-the-100-things-challenge/).
24 | Coming across the 100 Things Challenge again and again I want to try it myself. In February I'm going to create an inventory of the things I own. Let’s see if the number of things I own is going to be above or below 100.
25 | 
26 | > Stuff has gotten a lot cheaper, but our attitudes toward it haven't changed correspondingly. We overvalue stuff.
27 | > <cite><a href='http://paulgraham.com/stuff.html'>Stuff</a> by Paul Graham</cite>
```

content/posts/2014-02-07-early-adopters-and-inverted-social-proof.md
```
1 | ---
2 | date-published: 2014-02-07T00:00:00Z
3 | resource: http://ideolalia.com/early-adopters-and-inverted-social-proof/index.html
4 | title: Early Adopters And Inverted Social Proof
5 | uuid: c1881d68-6011-4888-a698-25339be6114f
6 | permalink: /posts/early-adopters-and-inverted-social-proof.html
7 | og-image: /images/selfies/2.jpg
8 | type: post
9 | ---
10 | > Hirschman credits the stability of early America with the fact that
11 | > discontents could simply travel west until they felt sufficiently freed from
12 | > its rules and restrictions. [...] <br>
13 | > There’s no limit on the number of possible subreddits, stack exchange sites,
14 | > or wikipedia pages that can be made, so a user can always keep traveling west
15 | > until they find something that’s worth sticking around to defend.
16 | 
17 | [A great analysis](http://ideolalia.com/early-adopters-and-inverted-social-proof/index.html) of why people are early adopters and how the constantly changing face
18 | of a community can affect their loyality to a product. One of those timeless
19 | reads you should probably re-read every now and then.
```

content/posts/2014-02-19-woodworking-masterclasses.md
```
1 | ---
2 | resource: http://woodworkingmasterclasses.com/
3 | date-published: 2014-02-19T00:00:00Z
4 | title: Woodworking Masterclasses
5 | uuid: 103f391e-8ccb-46ab-ac3b-7051d646a5fb
6 | hidden: true
7 | permalink: /posts/woodworking-masterclasses.html
8 | og-image: /images/selfies/3.jpg
9 | type: post
10 | ---
11 | Back when I lived at home my dad used to make fun of my mechanical skills.
12 | He said if everyone just sits in front of computers the whole day no one will
13 | know how to drill a hole at some point. He would like what follows.
14 | 
15 | [Woodworking Masterclasses](http://woodworkingmasterclasses.com/) is an online
16 | course to woodworking. It's the first time that I've seen such high quality
17 | material about learning a craft. They offer a simple monthly subscription to
18 | their courses with a new video being released every week. The videos are
19 | **top-notch**. Kind of like you would expect it by a company like
20 | Treehouse but not by people who do woodworking and are probably not too
21 | familiar with cutting videos and this type of stuff. Take a look:
22 | 
23 | <figure>
24 | <div class="responsive-embed"><iframe src="//player.vimeo.com/video/52801444?title=0&amp;byline=0&amp;portrait=0&amp;color=81c79b"
25 | width="500" height="281" frameborder="0" webkitallowfullscreen
26 | mozallowfullscreen allowfullscreen></iframe></div>
27 | <figcaption>
28 | <p><a href="http://vimeo.com/52801444">Clock Episode 1</a> from <a href="http://vimeo.com/woodworking">woodworking</a> on <a href="https://vimeo.com">Vimeo</a>.</p>
29 | </figcaption>
30 | </figure>
31 | 
32 | I like how the internet supports the ongoing evolvement of crafts and
33 | woodworking has always fascinated me. These videos make me want to be at
34 | a farm with a huge workbench.
```

content/posts/2014-03-12-setting-up-your-own-little-heroku-with-dokku-and-digitalocean.md
```
1 | ---
2 | date-published: 2014-03-12T00:00:00Z
3 | title: Heroku-like Deployment With Dokku And DigitalOcean
4 | uuid: 2818d220-0d76-4797-b967-b6fa9e5018e0
5 | permalink: /posts/setting-up-your-own-little-heroku-with-dokku-and-digitalocean.html
6 | hidden: true
7 | og-image: /images/selfies/2.jpg
8 | type: post
9 | ---
10 | Over the weekend I sat down to play around with Docker/Dokku and was able to
11 | set up a small machine on DigitalOcean that basically offers Heroku-like
12 | deployment. It was surprisingly simple so here is some rough outline that should
13 | get you going.
14 | 
15 | <aside>This guide is slightly opinionated and things can be done differently.
16 | I decided to settle for the way thats closest to Heroku to keep things short.</aside>
17 | 
18 | ## Create a Droplet at Digitalocean
19 | 
20 | Go to [DigitalOcean](https://www.digitalocean.com/?refcode=fb6eb9b8b286) and
21 | create a droplet (note the comments below the screenshots):
22 | 
23 | <figure>
24 | <img class='bordered' src='/images/hostname.png'>
25 | <figcaption>Make sure the hostname is a fully qualified domain name, as it will
26 | be the git remote you'll push to to deploy.</figcaption>
27 | </figure>
28 | 
29 | <figure>
30 | <img class='bordered' src='/images/image.png'>
31 | <figcaption>When selecting the image, go to Applications and select the Dokku
32 | one.</figcaption>
33 | </figure>
34 | 
35 | There are some unresolved problems with Dokku on Ubuntu 13+ so if you are not
36 | just playing around you might want to setup Dokku manually.  After that you're
37 | ready to hit the create button and a droplet will be created within the next
38 | minute. The last bit of server-setup that’s required is Dokku’s.
39 | 
40 | ## Setting up Dokku
41 | 
42 | To get to Dokku’s setup screen just steer your browser to the IP shown in the
43 | droplet’s detail view:
44 | 
45 | <figure>
46 | <img class='bordered' src='/images/droplet-ip.png'>
47 | </figure>
48 | 
49 | What you’ll see next is Dokku’s setup screen:
50 | 
51 | <figure>
52 | <img class='bordered' src='/images/dokku-setup.png'>
53 | <figcaption>Add an SSH key, tick the virtualhost checkbox, and make
54 | sure the hostname is correct.</figcaption>
55 | </figure>
56 | 
57 | ## DNS Setup
58 | 
59 | To get the hostname you chose earlier actually point to your machine running
60 | Dokku you need to add two A records to the zonefile of your domain.
61 | This strongly varies between domain/DNS providers so I can’t exactly say how
62 | it’d be done for yours. Whats important is that your provider supports wildcard
63 | entries. Also the value of an A record should be only the subdomain part of the
64 | hostname you set earlier, not the complete domain.
65 | 
66 |     A       <subdomain-of-hostname>      <droplet-ip>
67 |     A       *.<subdomain-of-hostname>    <droplet-ip>
68 | 
69 |     # in a zonefile it’d look like this:
70 |     *.apps 10800 IN A 107.170.35.171
71 |     apps 10800 IN A 107.170.35.171
72 | 
73 | ## Deploying
74 | 
75 | After you’ve waited three hours for DNS servers to propagate the changes you
76 | should be able to do something like the following:
77 | 
78 |     git clone git@github.com:heroku/node-js-sample.git
79 |     cd node-js-sample
80 |     git remote add digital-ocean dokku@apps.example.com:nodeapp
81 |     git push digital-ocean master
82 | 
83 | Now going to `nodeapp.<dokku-hostname>` should bring up “Hello World” from the app
84 | we just cloned and pushed.
85 | 
86 | If you want to add have a custom domain point to your app you'll need to either
87 | push to a remote like `dokku@apps.example.com:example.com` or edit the
88 | nginx.conf that comes with Dokku’s nginx plugin.
89 | 
90 | Thanks to Dokku’s [Buildstep](https://github.com/progrium/buildstep) that
91 | utilizes Heroku’s opensource buildpacks you can now deploy almost every application
92 | you can deploy to Heroku to Dokku as well.
93 | 
94 | **Have fun!**
```

content/posts/2014-07-22-emacs-and-vim.md
```
1 | ---
2 | date-published: 2014-07-22T00:00:00Z
3 | title: Emacs & Vim
4 | uuid: 6d204755-b8a0-4c43-98b2-b592e22970b7
5 | permalink: /posts/emacs-and-vim.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | 
10 | After using Vim for more than four years my recent contacts with Lisp
11 | encouraged me to take another look at Emacs. I used to make jokes
12 | about Emacs just as Emacs users about Vim but actually it seems to be
13 | a pretty decent piece of software.
14 | 
15 | Being a Vim user in the Clojure community sometimes feels weird. You
16 | are happy with Vim. Running Clojure code with right from the editor
17 | works well these days. Still you wonder why all those people you
18 | consider smart seem to be so committed to Emacs. So I decided to try
19 | it once again.
20 | 
21 | ## Keybindings
22 | 
23 | Let's start with a slight rant: I completely do not understand how
24 | anyone can use Emacs' default keybindings.  Being a Vim user I
25 | obviously have a thing for mode-based editing but Emacs' keybindings
26 | are beyond my understanding. Some simple movement commands to
27 | illustrate this:
28 | 
29 | - Move cursor down one line
30 |   - Emacs: <code>Ctrl-n</code>
31 |   - Vim: <code>j</code>
32 | 
33 | - Move cursor up one line
34 |   - Emacs: <code>Ctrl-p</code>
35 |   - Vim: <code>k</code>
36 | 
37 | - Move cursor left one character
38 |   - Emacs: <code>Ctrl-b</code>
39 |   - Vim: <code>h</code>
40 | 
41 | - Move cursor right one character
42 |   - Emacs: <code>Ctrl-f</code>
43 |   - Vim: <code>l</code>
44 | 
45 | These are the commands recommended in the Emacs tutorial (which you
46 | open with `Ctrl-h t`). They are mnemonic, what makes them easy to
47 | learn--but is that really the most important factor to consider for
48 | commands you will use hundreds of times a day? I don't think so. I
49 | tried to convince myself that it might be worth to get used to Emacs'
50 | default keybindings but after some time I gave up and installed
51 | `evil-mode`.
52 | 
53 | ## Mode-based Editing with Evil Mode
54 | 
55 | In my memory `evil-mode` sucked. I was delightfully surprised that it
56 | doesn't (anymore?). Evil brings well-done mode based editing to
57 | Emacs. As you continue to evolve your Emacs configuration you will
58 | most likely install additional packages that bring weird Emacs-style
59 | keybindings with them. Since you now have a mode-based editor you can
60 | use shorter, easier to remember keybindings to call functions provided
61 | by these packages. A useful helper that fits a sweet spot in my
62 | Vim-brain is `evil-leader` which allows you to setup `<leader>` based
63 | keybindings, just like you can do it in Vim:
64 | 
65 |     (evil-leader/set-leader ",")
66 |     (evil-leader/set-key
67 |       "," 'projectile-find-file)
68 | 
69 | With this I can open a small panel that finds files in my project in a
70 | fuzzy way (think Ctrl-p for Vim) hitting `,` two times instead of
71 | `Ctrl-c p f`.
72 | 
73 | ## Batteries Included
74 | 
75 | What I really enjoyed with Emacs was the fact that a package manager
76 | comes right with it. After adding a community maintained package
77 | repository to your configuration you have access to some 2000 packages
78 | covering Git integration, syntax and spell checking, interactive
79 | execution of Clojure code and more. This has been added in a the last
80 | major update (`v24`) after being a community project for some years.
81 | 
82 | ## Conclusion
83 | 
84 | Vim's lack of support for async execution of code has always bugged me
85 | and although there are some projects to change this I can't see it
86 | being properly fixed at least until NeoVim becomes the go-to Vim
87 | implementation. Emacs allows me to kick off commands and do other
88 | things until they return. In addition to that it nicely embeds Vim's
89 | most notable idea, mode-based editing, very well, allowing me to
90 | productively edit text while having a solid base to extend and to
91 | interactively write programs.
92 | 
93 | If you are interested in seeing how all that comes together in my
94 | Emacs configuration you can find it
95 | [on Github](https://github.com/martinklepsch/dotfiles/blob/master/emacs.d/init.el).
```

content/posts/2014-09-04-using-coreasync-and-transducers-for-direct-s3-upload.md
```
1 | ---
2 | date-published: 2014-09-04T00:00:00Z
3 | title: Using core.async and Transducers to upload files from the browser to S3
4 | uuid: a0c71769-4de2-476d-9453-2a00ccd527a5
5 | permalink: /posts/using-coreasync-and-transducers-for-direct-s3-upload.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | In a project I'm working on we needed to enable users to upload media
11 | content. In many scenarios it makes sense to upload to S3 directly
12 | from the browser instead of routing it through a server. If you're
13 | hosting on Heroku you need to do this anyways. After digging a bit
14 | into [`core.async`](https://github.com/clojure/core.async) this seemed
15 | like a neat little excuse to give Clojure's new transducers a go.
16 | 
17 | ## The Problem
18 | 
19 | To upload files directly to S3 without any server in between you need
20 | to do a couple of things:
21 | 
22 | 1. Enable Cross-Origin Resource Sharing (CORS) on your bucket
23 | 2. Provide special parameters in the request that authorize the upload
24 | 
25 | Enabling CORS is fairly straightforward, just follow
26 | [the documentation](http://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html)
27 | provided by AWS. The aforementioned special parameters are based on
28 | your AWS credentials, the key you want to save the file to, it's
29 | content-type and [a few other things](http://aws.amazon.com/articles/1434/).
30 | Because you don't want to store your credentials in client-side code
31 | the parameters need to be computed on a server.
32 | 
33 | We end up with the following procedure to upload a file to S3:
34 | 
35 | 1. Get a Javascript File object from the user
36 | 2. Retrieve special parameters for post request from server
37 | 3. Post **directly from the browser** to S3
38 | 
39 | ## Server-side code
40 | 
41 | I won't go into detail here, but here's
42 | [some rough Clojure code](https://gist.github.com/martinklepsch/0c6b40f45a415046f0fe)
43 | illustrating the construction of the special parameters and how
44 | they're sent to the client.
45 | 
46 | ## Client-side: Transducers and core.async
47 | 
48 | As we see the process involves multiple asynchronous steps:
49 | ![](/images/s3-direct.png)
50 | 
51 | To wrap all that up into a useful minimal API that hides all the
52 | complex back and forth happening until a file is uploaded core.async
53 | channels and transducers turned out very useful:
54 | 
55 |     (defn s3-upload [report-chan]
56 |       (let [upload-files (map #(upload-file % report-chan))
57 |             upload-chan  (chan 10 upload-files)
58 |             sign-files   (map #(sign-file % upload-chan))
59 |             signing-chan (chan 10 sign-files)]
60 | 
61 |         (go (while true
62 |               (let [[v ch] (alts! [signing-chan upload-chan])]
63 |                 ; that's not really required but has been useful
64 |                 (log v))))
65 |         signing-chan))
66 | 
67 | This function takes one channel as argument where it will `put!` the
68 | result of the S3 request. You can take a look at the `upload-file` and
69 | `sign-file` functions
70 | [in this gist](https://gist.github.com/martinklepsch/96e548d9595e111d70ce).
71 | 
72 | **So what's happening here?** We use a channel for each step of the
73 | process: `signing-chan` and `upload-chan`. Both of those channels have
74 | an associated transducer. In this case you can think best of a
75 | transducer as a function that's applied to each item in a channel on
76 | it's way through the channel. I initially trapped upon the fact that
77 | the transducing function is only applied when the element is being
78 | taken from the channel as well. Just putting things into a channel
79 | doesn't trigger the execution of the transducing function.
80 | 
81 | `signing-chan`'s transducer initiates the request to sign the File
82 | object that has been put into the channel. The second argument to the
83 | `sign-file` function is a channel where the AJAX callback will put
84 | it's result. Similary `upload-chan`'s transducer initiates the upload
85 | to S3 based on information that has been put into the channel. A
86 | callback will then put S3's response into the supplied `report-chan`.
87 | 
88 | The last line returns the channel that can be used to initiate a new upload.
89 | 
90 | ## Using this
91 | 
92 | Putting this into a library and opening it up for other people to use
93 | isn't overly complicated, the exposed API is actually very simple.
94 | Imagine an [Om](https://github.com/swannodette/om) component `upload-form`:
95 | 
96 |     (defn queue-file [e owner {:keys [upload-queue]}]
97 |       (put! upload-queue (first (array-seq (.. e -target -files)))))
98 | 
99 |     (defcomponent upload-form [text owner]
100 |       (init-state [_]
101 |         (let [rc (chan 10)]
102 |           {:upload-queue (s3-upload rc)
103 |            :report-chan rc}))
104 |       (did-mount [_]
105 |         (let [{:keys [report-chan]} (om/get-state owner)]
106 |           (go (while true (log (<! report-chan))))))
107 |       (render-state [this state]
108 |         (dom/form
109 |          (dom/input {:type "file" :name "file"
110 |                      :on-change #(queue-file % owner state)} nil))))
111 | 
112 | I really like how simple this is. You put a file into a channel and
113 | whenever it's done you take the result from another
114 | channel. `s3-upload` could take additional options like logging
115 | functions or a custom URL to retrieve the special parameters required
116 | to authorize the request to S3.
117 | 
118 | This has been the first time I've been doing something useful with
119 | core.async and, probably less surprisingly, the first time I played
120 | with transducers. I assume many things can be done better and I still
121 | need to look into some things like how to properly shut down the `go`
122 | blocks. **Any feedback is welcome!** [Tweet](https://twitter.com/martinklepsch) or
123 | [mail](mailto://martinklepsch@googlemail.com) me!
124 | 
125 | **Thanks** to Dave Liepmann who let me peek into some code
126 | he wrote that did similar things and to Kevin Downey (*hiredman*)
127 | who helped me understand core.async and transducers by answering
128 | my stupid questions in `#clojure` on Freenode.
```

content/posts/2014-09-11-running-a-clojure-uberjar-inside-docker.md
```
1 | ---
2 | date-published: 2014-09-11T00:00:00Z
3 | title: Running a Clojure Uberjar inside Docker
4 | uuid: 1f571433-ffa3-4d3b-9c5d-6220cf9ebe54
5 | permalink: /posts/running-a-clojure-uberjar-inside-docker.html
6 | hidden: true
7 | og-image: /images/selfies/2.jpg
8 | type: post
9 | ---
10 | 
11 | For a sideproject I wanted to deploy a Clojure uberjar on a remote server
12 | using Docker. I imagined that to be fairly straight foward but there are some
13 | caveats you need to be aware of.
14 | 
15 | Naively my first attempt looked somewhat like this:
16 | 
17 |     FROM dockerfile/java
18 |     ADD https://example.com/app-standalone.jar /
19 |     EXPOSE 8080
20 |     ENTRYPOINT [ "java", "-verbose", "-jar", "/app-standalone.jar" ]
21 | 
22 | I expected this to work. But it didn't. Instead it just printed the following:
23 | 
24 |     [Opened /usr/lib/jvm/java-7-oracle/jre/lib/rt.jar]
25 |     # this can vary depending on what JRE you're using
26 | 
27 | 
28 | And that has only been printed because I added `-verbose` when starting the jar.
29 | So if you're not running the jar verbosely it'll fail without any output.
30 | Took me quite some time to figure that out.
31 | 
32 | As it turns out the `dockerfile/java` image contains a `WORKDIR`
33 | command that somehow breaks my `java` invocation, even though it is
34 | using absolute paths everywhere.
35 | 
36 | ## What worked for me
37 | 
38 | I ended up splitting the procedure into two files in a way that allowed
39 | me to always get the most recent jar when starting the docker container.
40 | 
41 | The `Dockerfile` basically just adds a small script to the container that
42 | downloads and starts a jar it downloads from somewhere (S3 in my case).
43 | 
44 |     FROM dockerfile/java
45 |     ADD fetch-and-run.sh /
46 |     EXPOSE 42042
47 |     EXPOSE 3000
48 |     CMD ["/bin/sh", "/fetch-and-run.sh"]
49 | 
50 | And here is `fetch-and-run.sh`:
51 | 
52 |     #! /bin/sh
53 |     wget https://s3.amazonaws.com/example/yo-standalone.jar -O /yo-standalone.jar;
54 |     java -verbose -jar /yo-standalone.jar
55 | 
56 | Now when you build a new image from that Dockerfile it adds the
57 | `fetch-and-run.sh` script to the image's filesystem. Note that the
58 | jar is not part of the image but that it will be downloaded whenever
59 | a new container is being started from the image. That way a simple restart
60 | will always fetch the most recent version of the jar. In some
61 | scenarios it might become confusing to not have precise deployment
62 | tracking but in my case it turned out much more convenient than going
63 | through the process of destroying the container, deleting the image,
64 | creating a new image and starting up a new container.
```

content/posts/2014-10-02-patalyze-an-experiment-exploring-patent-data.md
```
1 | ---
2 | date-published: 2014-10-02T00:00:00Z
3 | title: Patalyze &mdash; An Experiment Exploring Publicly Available Patent Data
4 | uuid: 15184838-af2e-4ea1-b9a5-57f1ed52cf77
5 | permalink: /posts/patalyze-an-experiment-exploring-patent-data.html
6 | hidden: true
7 | og-image: /images/selfies/3.jpg
8 | type: post
9 | ---
10 | For a few months now I've been working on and off on a little
11 | "data-project" analyzing patents published by the US Patent &
12 | Trademark Office. Looking at the time I spent on this until now I
13 | think I should start talking about it instead of just hacking away
14 | evening after evening.
15 | 
16 | It started with a simple observation: there are companies like
17 | Apple that sometimes collaborate with smaller companies building a
18 | small part of Apple's next device. A contract like this usually gives
19 | the stock of the small company a significant boost. What if you could
20 | foresee those relationships by finding patents that employees from
21 | Apple and from the small company filed?
22 | 
23 | ## An API for patent data?
24 | 
25 | Obviously this isn't going to change the world for the better but just
26 | the possibility that such predictions or at least indications are
27 | possible kept me curious to look out for APIs offering patent data. I
28 | did not find much. So thinking about something small that could be
29 | "delivered" I thought a patent API would be great. To build the
30 | dataset I'd parse the archives provided on Google's
31 | [USPTO Bulk downloads](http://www.google.com/googlebooks/uspto-patents.html)
32 | page.
33 | 
34 | I later found out about [Enigma](http://enigma.io) and some offerings
35 | by [Thomson Reuters](http://ip.thomsonreuters.com). The prices are
36 | high and the sort of analysis we wanted to do would have been hard
37 | with inflexible query APIs.
38 | 
39 | For what we wanted to do we only required a small subset of the data a
40 | patent contains. We needed the organization, it's authors, the title
41 | and description, filing- and publication dates and some identifiers.
42 | With such a reduced amount of data that's almost only useful in
43 | combination with the complete data set I discarded the plan to build
44 | an API. Maybe it will make sense to publish reduced and more easily
45 | parseable versions of the archives Google provides at some point.
46 | Let me know if you would be interested in that.
47 | 
48 | ## What's next
49 | 
50 | So far I've built up a system to parse, store and query some 4 million patents
51 | that have been filed at the USPTO since beginning of 2001. While it
52 | sure would be great to make some money off of the work I've done so
53 | far I'm not sure what product could be built from the technology I created
54 | so far. Maybe I could sell the dataset but the number of potential
55 | customers is probably small and in general I'd much more prefer to
56 | make it public. I'll continue to explore the possibilities with regards
57 | to that.
58 | 
59 | For now I want to explore the data and share the results of this
60 | exploration. I setup a small site that I'd like to use as home for any
61 | further work on this. By now it only has a newsletter signup form
62 | (just like any other landing page) but I hope to share some
63 | interesting analysis with the subscribers to the list every now and
64 | then in the near future. Check it out at
65 | **[patalyze.co](http://www.patalyze.co)**.  There even is a small
66 | chart showing some data.
```

content/posts/2014-10-21-s3-beam-direct-upload-to-s3-with-clojure-and-clojurescript.md
```
1 | ---
2 | date-published: 2014-10-21T00:00:00Z
3 | title: S3-Beam — Direct Upload to S3 with Clojure & Clojurescript
4 | uuid: 8b07ff10-d213-41b5-b388-5cc9dbc17bfd
5 | permalink: /posts/s3-beam-direct-upload-to-s3-with-clojure-and-clojurescript.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | In a
11 | [previous post](http://www.martinklepsch.org/posts/using-coreasync-and-transducers-for-direct-s3-upload.html)
12 | I described how to upload files from the browser directly to S3 using
13 | Clojure and Clojurescript. I now packaged this up into a small (tiny,
14 | actually) library:
15 | [s3-beam](https://github.com/martinklepsch/s3-beam).
16 | 
17 | An interesting note on what changed to the process described in the
18 | earlier post: the code now uses `pipeline-async` instead of
19 | transducers. After some discussion with Timothy Baldridge this seemed
20 | more appropriate even though there are some aspects about the
21 | transducer approach that I liked but didn't get to explore further.
22 | 
23 | Maybe in an upcoming version it will make sense to reevaluate that
24 | decision. If you have any questions, feedback or suggestions I'm happy
25 | to hear them!
```

content/posts/2014-11-06-why-boot-is-relevant-for-the-clojure-ecosystem.md
```
1 | ---
2 | date-published: 2014-11-06T00:00:00Z
3 | title: Why Boot is Relevant For The Clojure Ecosystem
4 | uuid: ae776455-1de4-4ac8-bdda-d84680ed4b6b
5 | permalink: /posts/why-boot-is-relevant-for-the-clojure-ecosystem.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | Boot is a build system for Clojure projects. It roughly competes
10 | in the same area as Leiningen but Boot's new version brings some
11 | interesting features to the table that make it an alternative
12 | worth assessing.
13 | 
14 | <aside>
15 | If you don't know what Boot is I recommend reading this post by one of Boot's authors first:
16 | <a href="http://adzerk.com/blog/2014/11/clojurescript-builds-rebooted/">Clojurescript Builds, Rebooted</a>.
17 | </aside>
18 | 
19 | ## Compose Build Steps
20 | 
21 | If you've used Leiningen for more than packaging jars and uberjars
22 | you likely came across plugins like `lein-cljsbuild` or
23 | `lein-garden`, both compile your stuff into a target format (i.e. JS, CSS).
24 | Now if you want to run both of these tasks at the same time — which
25 | you probably want during development — you have two options: either
26 | you open two terminals and start them separately or you fall back to
27 | something like below that you run in a `dev` profile (this is how it's
28 | done in [Chestnut](https://github.com/plexus/chestnut)):
29 | 
30 | ```clojure
31 | (defn start-garden []
32 | (future
33 |   (print "Starting Garden.\n")
34 |   (lein/-main ["garden" "auto"])))
35 | ```
36 | 
37 | Now there are issues with both of these options in my opinion. Opening
38 | two terminals to initiate your development environment is just not
39 | very user friendly and putting code related to building the project
40 | into your codebase is boilerplate that unnecessarily can cause trouble
41 | by getting outdated.
42 | 
43 | What Boot allows developers to do is to write small composable tasks.
44 | These work somewhat similar to stateful transducers and ring middleware
45 | in that you can just combine them with regular function composition.
46 | 
47 | ### A Quick Example
48 | 
49 | Playing around with Boot, I tried to write a task. To test this task
50 | in an actual project I needed to install it into my local repository
51 | (in Leiningen: `lein install`).  Knowing that I'd need to reinstall
52 | the task constantly as I change it I was looking for something like
53 | Leiningen's Checkouts so I don't have to re-install after every
54 | change.
55 | 
56 | Turns out Boot can solve this problem in a very different way
57 | that illustrates the composing mechanism nicely. Boot defines a
58 | bunch of
59 | [built-in tasks](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj)
60 | that help with packaging and installing a jar: `pom`, `add-src`, `jar`
61 | & `install`.
62 | 
63 | We could call all of these these on the command line as follows:
64 | 
65 |     boot pom add-src jar install
66 | 
67 | Because we're lazy we'll define it as a task in our project's
68 | `build.boot` file. (Command-line task and their arguments are
69 | symmetric to their Clojure counterparts.)
70 | 
71 | ```clojure
72 | (require '[boot.core          :refer [deftask]]
73 |             '[boot.task.built-in :refer [pom add-src jar install]])
74 | 
75 | (deftask build-jar
76 |   "Build jar and install to local repo."
77 |   []
78 |   (comp (pom) (add-src) (jar) (install)))
79 | ```
80 | 
81 | Now `boot build-jar` is roughly equivalent to `lein install`. To have
82 | any changes directly reflected on our classpath we can just compose
83 | our newly written `build-jar` task with another task from the
84 | repertoire of built-in tasks: `watch`. The `watch`-task observes the
85 | file system for changes and initiates a new build cycle when they
86 | occur:
87 | 
88 |     boot watch build-jar
89 | 
90 | With that command we just composed our already composed task with
91 | another task. **Look at that cohesion!**
92 | 
93 | <aside>I'm not familiar enough with Leiningen Checkouts to say with
94 | confidence if this is identical behavior but for the majority of cases it'll
95 | probably work.</aside>
96 | 
97 | ## There Are Side-Effects Everwhere!
98 | 
99 | Is one concern that has been raised about Boot. Leiningen is
100 | beautifully declarative. It's one immutable map that describes your
101 | whole project. Boot on the other hand looks a bit different.  A usual
102 | boot file might contain a bunch of side-effectful functions and in
103 | general it's much more a program than it is data.
104 | 
105 | I understand that this might seem like a step back at first sight, in
106 | fact I looked at it with confusion as well. There are some problems
107 | with Leiningen though that are probably hard to work out in
108 | Leiningen's declarative manner (think back to
109 | [running multiple `lein X auto` commands](https://github.com/technomancy/leiningen/issues/1752).
110 | 
111 | Looking at Boot's code it becomes apparent that the authors spent a
112 | great deal of time on isolating the side effects that might occur in
113 | various build steps. I recommend reading the
114 | [comments on this Hacker News thread](https://news.ycombinator.com/item?id=8553189)
115 | for more information on that.
116 | 
117 | ## When To Use Boot, When To Use Leiningen
118 | 
119 | Boot is a build tool. That said it's task composition features only
120 | get to shine when multiple build steps are involved. If you're
121 | developing a library I'm really not going to try to convince you to
122 | switch to Boot.  Leiningen works great for that and is, I'd assume,
123 | more stable than Boot.
124 | 
125 | If you however develop an application that requires various build
126 | steps (like Clojurescript, Garden, live reloading, browser-repl) you
127 | should totally check out Boot. There are tasks for all of the above
128 | mentioned: [Clojurescript](https://github.com/adzerk/boot-cljs),
129 | [Clojurescript REPL](https://github.com/adzerk/boot-cljs-repl),
130 | [Garden](https://github.com/martinklepsch/boot-garden),
131 | [live reloading](https://github.com/adzerk/boot-reload). I wrote the
132 | Garden task and writing tasks is not hard once you have a basic
133 | understanding of Boot.
134 | 
135 | If you need help or have questions join the
136 | [#hoplon channel on freenode IRC](http://webchat.freenode.net/?channels=hoplon).
137 | I'll try to help and if I can't Alan or Micha, the authors of Boot,
138 | probably can.
```

content/posts/2015-01-05-cljsjs-use-javascript-libraries-in-clojurescript.md
```
1 | ---
2 | date-published: 2015-01-05T00:00:00Z
3 | title: CLJSJS - Use Javascript Libraries in Clojurescript With Ease
4 | uuid: 99353e70-0080-454a-825d-bb85f8398ae4
5 | permalink: /posts/cljsjs-use-javascript-libraries-in-clojurescript.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | In Clojure, Java interoperability or “interop” is a core feature. In
10 | Clojurescript, interop with Javascript libraries does not work
11 | out-of-the-box across optimization modes. Extern files or “externs”
12 | required for advanced optimizations are often hard to find.
13 | 
14 | To fix this a few newly found friends and I created **[CLJSJS](http://cljsjs.github.io)**.
15 | CLJSJS is an effort to package Javascript libraries with their respective
16 | extern files and provide tools to integrate them into your project.
17 | 
18 | My personal hope is that this will make it easier for newcomers to get
19 | started with Clojurescript.
20 | 
21 | Also existing solutions like `deps.clj` ([more here](https://groups.google.com/forum/#!msg/clojurescript/LtFMDxc5D00/hMR6BcfMMAMJ)) only
22 | address the problem of Javascript dependencies partially. Maybe CLJSJS
23 | can serve as a vehicle to find some "pseudo-standard" for this kind of
24 | stuff.
25 | 
26 | Thanks to Juho Teperi, Micha Niskin & Alan Dipert for their
27 | contributions and ideas so far. **Now go and check out the [project homepage](http://cljsjs.github.io) or jump straight into the [packages repo](https://github.com/cljsjs/packages) and learn how you can contribute.**
28 | 
29 | *Announcement post and discussion on the [Clojurescript mailinglist](https://groups.google.com/forum/#!topic/clojurescript/qhFNVEeNCbc)*
```

content/posts/2015-02-05-lisp-keymap.md
```
1 | ---
2 | date-published: 2015-02-05T00:00:00Z
3 | title: (lisp keymap)
4 | uuid: 92c021b4-d645-48fd-aeb5-333305edfdb5
5 | permalink: /posts/lisp-keymap.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | A while back I wanted to setup hotkeys for the various apps I use.
10 | Mostly because I was annoyed by the cognitive effort you need to
11 | make to figure out how often you need to press `Alt + Tab` exactly
12 | to get to the app you want.
13 | 
14 | It seemed like a good idea to use Capslock as a modifier key.
15 | This way I could be sure I wouldn't override any other keybindings.
16 | Figuring out how to do this I stumpled upon an excellent
17 | post by Steve Losh ["A Modern Space Cadet"](http://stevelosh.com/blog/2012/10/a-modern-space-cadet/). It's
18 | described in detail how to set Capslock to `Hyper` - a fifth modifier
19 | key. I then created bindings like `Hyper + S` which will focus Safari
20 | etc. Exactly what I was looking for.
21 | 
22 | 
23 | **Then I found something in his post I wasn't looking for**:
24 | instructions to map my shift keys to parentheses. It sounded crazy at
25 | first but doing mostly LISP-y stuff these days I tried it anyways.
26 | 
27 | Now I wouldn't want to live without it anymore. It's just so much easier
28 | than `Shift + {9,0}`. Also the Shift keys still work as they do usually
29 | when pressed in combination with other keys.
30 | 
31 | A few days ago I was typing some stuff at a collegues computer and
32 | it immediately felt cumbersome when having to type a parenthesis.
33 | 
34 | <aside> PS. Here are Steve Losh's original <a
35 | href="http://stevelosh.com/blog/2012/10/a-modern-space-cadet/#shift-parentheses">OS
36 | X Instructions</a>.  (What's KeyRemap4MacBook in this post is now <a
37 | href="https://pqrs.org/osx/karabiner/index.html.en">Karabiner</a>.)
38 | </aside>
```

content/posts/2015-04-10-formal-methods-at-amazon.md
```
1 | ---
2 | date-published: 2015-04-10T00:00:00Z
3 | title: Formal Methods at Amazon
4 | uuid: 915cc2e8-d190-4d54-b746-0e1c34dff835
5 | permalink: /posts/formal-methods-at-amazon.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | 
10 | I saw this paper being mentioned again and again in my Twitter
11 | feed. Basically not even knowing what "formal methods" really means I
12 | was intrigued by the claim that it's easy to read. And it has been.
13 | 
14 | The paper describes how Amazon used a specification language to
15 | describe designs of complex concurrent fault tolerant systems finding
16 | bugs and verifying changes in the process.
17 | 
18 | The specification language (TLA+) is not focus of the paper, rather
19 | the authors concentrate on describing benefits, problems and the path
20 | of adopting formal specification of system designs in an engineering
21 | organization.
22 | 
23 | TLA+, stands for *Temporal Logic of Actions* and ["is especially well suited for writing high-level specifications of concurrent and distributed systems."](http://research.microsoft.com/en-us/um/people/lamport/tla/tla-intro.html)
24 | 
25 | Reading how they use it at Amazon I'm under the impression that it
26 | works very similar to [generative testing](http://blog.8thlight.com/connor-mendenhall/2013/10/31/check-your-work.html)
27 | dumping a ton of basically random (according to some rules) data into a system
28 | and checking if the desired properties are maintained. Often the term
29 | *"model checker"* is used.
30 | 
31 | Download the [original paper](http://research.microsoft.com/en-us/um/people/lamport/tla/amazon.html) or a copy of it [with some passages highlighted](/images/formal-methods-amazon.pdf) that I found particulary interesting.
```

content/posts/2015-06-03-managing-local-and-project-wide-development-parameters-in-leiningen.md
```
1 | ---
2 | date-published: 2015-06-03T00:00:00Z
3 | title: Managing Local and Project-wide Development Parameters in Leiningen
4 | uuid: 47b430b8-3e76-48c5-a68e-a12fe88b2e4e
5 | permalink: /posts/managing-local-and-project-wide-development-parameters-in-leiningen.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | 
10 | Little tip. Long headline.
11 | 
12 | In any project there are often settings that are specific to the
13 | context the project is run in (think of an `environment` parameter)
14 | and then there are parameters that are specifc to the
15 | developer/workstation they're run on. This is a guide to separate
16 | these two things nicely in Leiningen-based Clojure projects.
17 | 
18 | So you have a project setup that uses
19 | [environ](https://github.com/weavejester/environ) to determine the
20 | context the project is run in (`development` vs. `production`).
21 | 
22 | ```clojure
23 | ; in project.clj:
24 | (defproject your-app "0.1.0-SNAPSHOT"
25 |   ; ...
26 |   :profiles {:dev {:env {:environment "development"}}})
27 | ```
28 | 
29 | Now you also want to use environment variables (or anything else thats
30 | supported by environ) to store AWS credentials to access Amazon
31 | S3. You don't want to commit these credentials into version control,
32 | therefore you can't add them to `project.clj`. The way to go is to
33 | create a file `profiles.clj` in your project to store workstation
34 | specific information. Naively this could look something like this
35 | 
36 | ```clojure
37 | {:dev {:env {:aws-access-key "abc"
38 |              :aws-secret-key "xyz"
39 |              :s3-bucket "mybucket"}}}
40 | ```
41 | 
42 | If you run your project with this `profiles.clj` you will be able to
43 | access your AWS credentials. You might also notice that `(environ/env
44 | :environment)` is nil. **That wasn't intended.**
45 | 
46 | The problem here is that Leiningen will override keys in profiles
47 | defined in `project.clj` if **the same profile** has also been defined
48 | in `profiles.clj`.  To recursively merge Leiningen profiles combine them like so:
49 | 
50 | ```clojure
51 | ;; in project.clj:
52 | (defproject your-app "0.1.0-SNAPSHOT"
53 |   ;; ...
54 |   :profiles {:dev [:project/dev :local/dev]
55 |              :project/dev {:env {:environment "development"}}})
56 | 
57 | ;; in profiles.clj
58 | {:local/dev {:env {:secret-key "xyz"}}}
59 | ```
60 | 
61 | Now both, `:envrionment` and `:secret-key` should be defined when you
62 | retrieve them using environ.
63 | 
64 | *This is largely inspired by James Reeves' [duct](https://github.com/weavejester/duct) Leiningen template.*
```

content/posts/2015-07-24-clojurebridge-berlin.md
```
1 | ---
2 | date-published: 2015-07-24T00:00:00Z
3 | title: ClojureBridge Berlin
4 | uuid: 3264b651-0aac-4e62-8751-77b88828f856
5 | permalink: /posts/clojurebridge-berlin.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | About two weeks ago something awesome happened: the very first
11 | ClojureBridge workshop in Berlin. After months of planning things
12 | finally got real.
13 | 
14 | ![ClojureBridge Berlin in it's entirety.](/images/clojurebridge-group-picture.jpg)
15 | 
16 | > ClojureBridge aims to increase diversity within the Clojure
17 | > community by offering free, beginner-friendly Clojure programming
18 | > workshops for women.
19 | 
20 | Many of you probably got the *"news"*: there's a lack of diversity in
21 | programming communities. Many communities acknowledge this and have
22 | created initiatives to fix it. The Ruby community has RailsBridge (and
23 | more) and other communties equally do their part in improving our
24 | industries diversity situation. Inspired by RailsBridge the Clojure
25 | community established ClojureBridge and has organized more than 20
26 | workshops worldwide since.
27 | 
28 | ### Why Diversity?
29 | 
30 | There are
31 | [endless amounts](http://www.ncwit.org/sites/default/files/resources/impactgenderdiversitytechbusinessperformance_print.pdf)
32 | [of research](http://newsoffice.mit.edu/2014/workplace-diversity-can-help-bottom-line-1007)
33 | why diversity is desirable but one of the reasons that seems most
34 | intuitive to me is that software is, after all, for humans. If we want
35 | to make great software for everyone then it can only be made by all of
36 | us and not by one priviliged monoculture.
37 | 
38 | ### ClojureBridge Berlin
39 | 
40 | ClojureBridge workshops consist of one evening installing required
41 | software (Friday) and a full day of actually learning things
42 | (Saturday). Besides some problems with our pizza delivery both days
43 | went really well. We had great vegan and vegetarian lunch on Saturday,
44 | fun ClojureBridge cupcakes and after the coffee machine broke on Friday
45 | people brought lots of coffee making equipment to the event on Saturday.
46 | You could say we had a little
47 | [third wave coffee](https://en.wikipedia.org/wiki/Third_wave_of_coffee)
48 | workshop as well.
49 | 
50 | ![We got some sweet cupcakes!](/images/clojurebridge-cupcakes.jpg)
51 | 
52 | On Saturday we initially had 2-3 coaches that "didn't have a job" and
53 | we were afraid they might feel superflous but the need for some
54 | additional help quickly arised when some learners got ahead of the
55 | rest of their group. In the end we were very happy that we had the
56 | flexibility of not having assigned all coaches to groups. (We still
57 | had teaching assistants.)
58 | 
59 | ### Results
60 | 
61 | At the end of the event we had a fantastic demo time. A great amount
62 | of learners showed their Quil creations, from an Santa Claus to
63 | stroboscopic rainbow animations. It was great to see how in the
64 | beginning everyone was shy to show their work but as more people did
65 | others felt encouraged to do the same.
66 | 
67 | ### Takeaways
68 | 
69 | This was the first time we organized such workshop in Berlin. We were
70 | lucky to be a big team of organizers (six people) which allowed us to
71 | distribute the work.
72 | 
73 | The feedback we got during and after the workshop has been very
74 | positive. About a third of the attendees have registered interest in
75 | joining project groups to keep learning. Obviously the more the better
76 | but even ten people is a nice outcome overall.
77 | 
78 | ### Thanks
79 | 
80 | I'd like to take the opportunity to thank all of our coaches: Nils,
81 | Sean, Paulus, Jan, Johannes, Ben, Franziska, Luca, Txus, Kofi,
82 | Torsten, Tibor, Thomas, Stephan, Oskar, Kai & Matt thank you so much
83 | for being part of this. **None of it would have happened without you!**
84 | 
85 | Also I'd like to thank my fellow organizers for pushing through the
86 | finish line together and for just being an overall awesome
87 | bunch. Thanks Bettina, Malwine, Arne, Jelle & Nicola.
88 | 
89 | Last but not least I'd like to thank the companies that enabled
90 | ClojureBridge Berlin: Wunderlist, SoundCloud, GitHub, InnoQ,
91 | TicketSolve, Babbel & DaWanda. A special thank you in this regard to
92 | Andrei, who has done an exceptional job at hosting the event at
93 | Wunderlist!
94 | 
95 | ![ClojureBridge Berlin T-Shirts](/images/clojurebridge-shirts.jpg)
96 | 
97 | If you'd like to be informed about upcoming workshops, follow
98 | [@ClojureBerlin](https://twitter.com/clojureberlin) on Twitter. If you
99 | don't have Twitter you can also
100 |  [send me an email](mailto:martinklepsch@googlemail.com) and I'll make
101 |  sure you'll be notified
102 | :-)
103 | 
104 | <aside>
105 | <p>Image credits (in order):
106 |  <a href="https://twitter.com/bumbledebee">@bumbledebee</a>,
107 |  <a href="https://twitter.com/codebeige/status/619804392284422144">@codebeige</a>,
108 |  <a href="https://twitter.com/ClojureBerlin/status/619890238584963072">@malwine</a>.</p>
109 | </aside>
```

content/posts/2015-08-11-parameterizing-clojurescript-builds.md
```
1 | ---
2 | date-published: 2015-08-11T00:00:00Z
3 | title: Parameterizing ClojureScript Builds
4 | uuid: a658e901-04ec-4ff4-a2d5-c8c72221e3e7
5 | permalink: /posts/parameterizing-clojurescript-builds.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | Just like with most server side software we often want to make minor
10 | changes to the behaviour of the code depending on the environment it's
11 | run in. This post highlights language and compiler features of ClojureScript
12 | making parameterized builds easy peasy.
13 | 
14 | On servers environment variables are a go-to solution to set things
15 | like a database URI. In ClojureScript we don't have access to those.
16 | You can work around that with macros and emit code based on environment
17 | variables but this requires additional code and separate tools.
18 | 
19 | With ClojureScript 1.7.48 (<strong>Update:</strong> There was a bug in 1.7.48
20 | `goog-define`. Use 1.7.107 instead.) a new macro `goog-define` has
21 | been added which allows build customization at compile time using
22 | plain compiler options. Let's walk through an example:
23 | 
24 |     (ns your.app)
25 |     (goog-define api-uri "http://your.api.com")
26 | 
27 | `goog-define` emits code that looks something like this:
28 | 
29 |     /** @define {string} */
30 |     goog.define("your.app.api_uri","http://your.api.com");
31 | 
32 | The `goog.define` function from Closure's standard library plus the JSDoc
33 | `@define` annotation tell the Closure compiler that `your.app.api_uri`
34 | is a constant that can be overridden at compile time.  To do so you
35 | just need to pass the appropriate `:closure-defines` compiler option:
36 | 
37 |     :closure-defines {'your.app/api-uri "http://your-dev.api.com"}
38 | 
39 | **Note:** When using Leinigen quoting is implicit so there is no quote
40 |   necessary before the symbol.
41 | 
42 | **Note:** Sometimes for debugging you may want to pass the Closure
43 | define as a string. If you decide to do so make sure it matches the
44 | string in the `goog.define` call in your emitted Javascript
45 | (i.e. account for name mangling).
46 | 
47 | <aside>
48 | Prior to 1.7.48 you could annotate things with <code>@define</code> but without
49 | using <code>goog.define</code> overriding those defines is not possible when
50 | using optimizations <code>:none</code> effectively making them much less useful.
51 | </aside>
52 | 
53 | ### Under the hood
54 | 
55 | When compiling with `:advanced` optimizations the Closure compiler will
56 | automatically replace all occurrences of your defined constants with their
57 | respective values. If this leads to unreachable branches in your code they
58 | will be removed as [dead code](https://developers.google.com/closure/compiler/docs/compilation_levels?hl=en#advanced_optimizations)
59 | by the Closure compiler. Very handy to elide things like logging!
60 | 
61 | Without any optimizations (`:none`) `goog.define` makes sure the right
62 | value is used. There are two global variables it takes into account
63 | for that: `CLOSURE_UNCOMPILED_DEFINES` and `CLOSURE_DEFINES`. When you
64 | override the default value using `:closure-defines` the ClojureScript
65 | compiler prepends `CLOSURE_UNCOMPILED_DEFINES` with your overridden
66 | define to your build causing `goog.define` to use the value in there
67 | instead of the default value you defined in your source files.
68 | 
69 | For details see
70 | [the source of goog.define](https://github.com/google/closure-library/blob/master/closure/goog/base.js#L147-L172).
```

content/posts/2015-11-03-om-next-reading-list.md
```
1 | ---
2 | date-published: 2015-11-03T00:00:00Z
3 | title: Om/Next Reading List
4 | uuid: e2aa0cae-c4ce-42c0-9052-f1b001e51c0e
5 | permalink: /posts/om-next-reading-list.html
6 | hidden: true
7 | og-image: /images/selfies/1.jpg
8 | type: post
9 | ---
10 | 
11 | A small dump of things I read to learn more about Om/Next. Most of these
12 | I stumbled upon while lurking in #om on the [Clojurians Slack](http://clojurians.net/).
13 | 
14 | ### [Thinking in Relay](https://facebook.github.io/relay/docs/thinking-in-relay.html)
15 | 
16 | This is Facebook's high level overview for Relay. It explains the
17 | reasoning for colocating queries and how data masking allows
18 | developers to write components that are not coupled to their location
19 | in the UI tree.
20 | 
21 | ### [Om/Next Quick Start](https://github.com/omcljs/om/wiki/Quick-Start-%28om.next%29)
22 | 
23 | This is the official Om/Next quick start tutorial. It guides you
24 | through building a basic application with Om/Next and introduces the
25 | basic API for queries and mutations. After reading this you should
26 | have a rough idea what's being talked about in the next two reads.
27 | 
28 | ### [Om/Next The Reconciler](https://medium.com/@kovasb/om-next-the-reconciler-af26f02a6fb4)
29 | 
30 | Kovas Boguta who previously gave an Om/Next workshop with David Nolen
31 | wrote this introduction to the Om/Next reconciler. It covers the
32 | architectural role of the reconciler managing application state and
33 | communicating it to components. The reconciler also acts as an indexer
34 | of all components and, using their queries to build a depdency graph,
35 | knows when to update which components.
36 | 
37 | ### [Om/Next Overview](https://github.com/awkay/om/wiki/Om-Next-Overview)
38 | 
39 | Written by Tony Kay this overview covers many practical aspects of
40 | writing queries and mutations. Before it goes into the nitty gritty
41 | details howvever there is another short *Problem → Solution* section
42 | that nicely describes the concepts in Relay and Om/Next in prose.
43 | 
44 | Now put all those links into Instapaper/Pocket & enjoy reading!
```

content/posts/2016-05-19-props-children-and-component-lifecycle-in-reagent.md
```
1 | ---
2 | date-published: 2016-05-19T00:00:00Z
3 | title: Props, Children & Component Lifecycle in Reagent
4 | uuid: bdabdf1d-742c-468f-89bb-032986a9d99f
5 | permalink: /posts/props-children-and-component-lifecycle-in-reagent.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | Every now and then I come across the situation that I need to compare
11 | previous and next props passed to a Reagent component. Every time
12 | again I fail to find some docs and figure it out by trial and error.
13 | 
14 | ## Props vs. Children
15 | 
16 | In React **everything** passed to a component is called `props`. Children passed to components are passed as `props.children`. In Reagent things are a bit different and Reagent’s hiccup syntax doesn’t explicitly separate the two:
17 | 
18 | ```clojure
19 | ;; configuration and one child
20 | [popup {:style :alert} [delete-confirmation]]
21 | ;; two children
22 | [popup [alert-icon] [delete-confirmation]]
23 | ```
24 | 
25 | ```xml
26 | <Popup style="alert"><DeleteConfirmation></Popup>
27 | ```
28 | 
29 | In React it is well-defined where you can access the `style` parameter (`props.style`) and how you can access the passed children (`props.children`). 
30 | 
31 | In Reagent things are a bit different: you have a function definition which takes a number of arguments which you can just refer to in the same way you can refer to any other function parameter. This makes thinking in functions a lot easier but also overshadows some of the underlying React behaviour. 
32 | 
33 | In a lifecycle handler like `:component-did-update` accessing component arguments via the symbol they’ve been given in the functions argument vector doesn’t work:
34 | 
35 | The moment you define components that are not simple render functions (remember those [Form-2 and Form-3](https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components) components?) all updates will pass their arguments to the components render function. 
36 | 
37 | The moment you render a component that has been created via `reagent.core/create-class` all updates will pass their arguments to the `:reagent-render` function, potentially triggering a re-render. The function that returned the result of `create-class` is only ever called once at the time of mounting the component — your top-level `defn` returns a component instead of being a render function itself. This is also [why you need to repeat the arguments in the `:reagent-render` arguments](https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components#form-2--a-function-returning-a-function).
38 | 
39 | ## Props in Lifecycle Handlers
40 | 
41 | Now how do we get access to these props in a lifecycle handler? The quick answer is, we use `reagent.core/props` — obvious, huh?
42 | 
43 | One peculiarity about the `props` function is that it expects the `props` data to be the first argument to your function. Also it **has to be a map** (if it’s not `props` returns `nil`).
44 | 
45 | If the first argument to your component is not a map all arguments are interpreted as children and can be retrieved via `reagent.core/children`.
46 | 
47 | So now we have the props for the current render, how do we access the previous ones? All previously passed arguments are passed to the lifecycle handler. Not as you might think though.
48 | 
49 | If you have a component that has a signature like this:
50 | 
51 | ```clojure
52 | (defn my-comp [my-props more] …)
53 | ```
54 | 
55 | You can access it’s previously passed arguments like this:
56 | 
57 | ```clojure
58 | :component-did-update (fn [comp [_ prev-props prev-more]] …))
59 | ```
60 | 
61 | `comp` is a reference to the current component. The second argument which is being destructured here contains what we’re looking for. As far as I understood the first item is the component's constructor. The rest are the previously rendered inputs (again in React they’re all `props`, in Reagent they’re `props` and `children`).
62 | 
63 | As you can see you can inspect all previous arguments to a component. The way you access them differs from the default React lifecycle method signatures so hopefully this post helps to clear up some confusion about this stuff. :)
64 | 
65 | <aside>Thanks to Jonas Enlund for reading a draft of this post and to Mike Thompson for his excellent Re-frame/Reagent docs.</aside>
```

content/posts/2016-11-25-just-in-time-script-loading-with-react-and-clojuresript.md
```
1 | ---
2 | date-published: 2016-11-25T00:00:00Z
3 | title: Just-in-Time Script Loading With React And ClojureScript
4 | uuid: 21eecbc4-1be6-4930-89ae-9c36c69e0a16
5 | permalink: /posts/just-in-time-script-loading-with-react-and-clojuresript.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | In the last projects I've been working on I've come accross the situation that I needed to load some external script (Stripe, Google Maps, ...) at a certain point and then do something with the features exposed by this newly imported library. Some times you might be able to circumvent loading a library at runtime by bundling it with your main application but even then you might want to consider splitting it into a separate module and loading it when it's actually needed.
11 | 
12 | We won't talk about module splitting and loading in this blog post though and instead focus on loading things like Stripe and Google Maps that just can't be bundled with your application.
13 | 
14 | The easy way to load these would be using a simple script tag:
15 | 
16 | ```html
17 | <script type="text/javascript" src="https://js.stripe.com/v2/"></script>
18 | ```
19 | 
20 | With this approach however you load the script for every user even though they may never, or already went through, your payment flow. A better way would be to load it when the user actually wants to pay you. I've heard fast loading apps make that more likely as well ;) Also you might say that these scripts could be cached, but even if they are: you still pay for the parsing and execution time.
21 | 
22 | So how can we go about that? What follows is one pattern that I think is fairly simple and elegant and also a nice use of React's lifecycle features and higher-order components:
23 | 
24 | ```clojure
25 | (ns your-app.lib.reagent
26 |   (:require [reagent.core :as reagent]
27 |             [goog.net.jsloader :as jsl]))
28 | 
29 | (defn filter-loaded [scripts]
30 |   (reduce (fn [acc [loaded? src]]
31 |             (if (loaded?) acc (conj acc src)))
32 |           []
33 |           scripts))
34 | 
35 | (defn js-loader
36 |   "Load a supplied list of Javascript files and render a component
37 |    during loading and another component as soon as every script is
38 |    loaded.
39 | 
40 |    Arg map: {:scripts {loaded-test-fn src}
41 |              :loading component
42 |              :loaded component}"
43 |   [{:keys [scripts loading loaded]}]
44 |   (let [loaded? (reagent/atom false)]
45 |     (reagent/create-class
46 |      {:component-did-mount (fn [_]
47 |                              (let [not-loaded (clj->js (filter-loaded scripts))]
48 |                                (.then (jsl/loadMany not-loaded)
49 |                                       #(do (js/console.info "Loaded:" not-loaded)
50 |                                            (reset! loaded? true)))))
51 |       :reagent-render (fn [{:keys [scripts loading loaded]}]
52 |                         (if @loaded? loaded loading))})))
53 | ```
54 | 
55 | And here's how you can use it:
56 | 
57 | ```clojure
58 | ;; payment-form can expect `js/Stripe` to be defined
59 | [js-loader {:scripts {#(exists? js/Stripe) "https://js.stripe.com/v2/"}
60 |             :loading [:div "Loading..."]
61 |             :loaded [payment-form]}]
62 | ```
63 | 
64 | So, what can we take away from this besides the specific snippets above?
65 | 
66 | - Higher order components can be very useful to hide away side effects needed for your views to function
67 | - They also are perfectly reusable
68 | - You can of course also use higher order components to pass things into child components, we don't do that here but if you create some stateful object this may come in handy
69 | 
70 | Hope this is helpful — let me know if you have any thoughts or suggestions :)
```

content/posts/2017-03-25-making-remote-work.md
```
1 | ---
2 | date-published: 2017-03-25T00:00:00Z
3 | title: Making Remote Work
4 | uuid: a658e901-04ec-4ff4-a2d5-c8c72231e3e7
5 | permalink: /posts/making-remote-work.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | 
10 | I've been working remotely for a bit over a year now. Most projects went pretty well. Some not so much. I've worked with fully distributed teams and as a satellite worker with on-site teams. I'm not an expert—but I've learned some things.
11 | 
12 | What follows are some basic ideas to make remote teams work. I hope these help companies and remote workers set things up for success.
13 | 
14 | ### Focus on Self Sufficiency 
15 | 
16 | In an office you walk up to somebody (read: interrupt their work) and discuss stuff. In a remote environment you may need to schedule, then wait, then deal with connectivity issues, then have a discussion. Sometimes the waiting part alone can take the better part of a day.
17 | 
18 | Synchronous communication, including chat, is great and necessary but the more your work routine relies on it, the more you might unknowingly slow everyone down.
19 | 
20 | With that in mind, start thinking of communication as a cost factor. Being slowed down costs time, momentum and motivation. These are the things you usually want to preserve at all cost. And that's where self-sufficiency comes in.
21 | 
22 | Being self sufficient in work means knowing what to do in order to advance the project and having the means to execute. The easiest way to make people understand what is worth working on is making them understand the business. Sometimes more specific goals will also do the job. But generally the broader the better.
23 | 
24 | Once everyone is able to develop ideas based on their understanding of the business all that is missing is The Feedback Loop™. Feedback is often a critical part of UI design but that does not mean it isn't applicable to other domains. Enable your team to see the impact of their work. Define relevant metrics and measure, measure, measure, graph, graph, graph. Share and celebrate accomplishments.
25 | 
26 | 
27 | ### Document With Rigour 
28 | 
29 | Again, communication has a cost. It's fun and important too. You want to spend that valuable face-time with your colleagues chatting about last weekends hike or that tricky problem you're banging your head against. You don't want to spend it figuring out how to run migrations or update your environment so that things work again with those latest changes.
30 | 
31 | Have a `README`. Make sure to add a table of contents. This is reference material, not an essay. Maybe try Asciidoc if Markdown feels too restrictive. Ensure that pull requests are reviewed with documentation changes in mind. With GitHub's pull request templates it's easy to remind team members to update documentation.
32 | 
33 | That hike last week was nice by the way:
34 | 
35 | ![](/images/hike.jpg)
36 | 
37 | <div class="pa2 tc f3 blue">·  ·  ·</div>
38 | 
39 | I realize the up-front cost of making someone understand the intricacies of a business might be higher than just telling them to "do this". There may be smaller projects where deep understanding is not necessary. It seems reasonable to think that everyone does a better job if they do understand the business though.
40 | 
41 | Now all these suggestions are probably applicable to any team, not only remote ones. However they are of particular importance for remote teams.
42 | 
43 | <p class="f6 ba br2 b--blue pa3 mt4">Distributed teams face special challenges. <a href="https://goo.gl/forms/BTteLFXQFopWOXxt1">I would love to hear yours</a> and see if there might be something that could be built to help.</p>
```

content/posts/2017-04-16-simple-debouncing-in-clojurescript.md
```
1 | ---
2 | date-published: 2017-04-16T00:00:00Z
3 | title: Simple Debouncing in ClojureScript
4 | uuid: 271f273e-8587-42ce-be1a-6efe22a78d2e
5 | permalink: /posts/simple-debouncing-in-clojurescript.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | This is a short post on a problem that eventually occurs in any
11 | Javascript app: debouncing. While there are various approaches to
12 | this problem I want to focus on one that relies on nothing else than
13 | the [Closure Library](https://developers.google.com/closure/library/).
14 | 
15 | ## Why Debounce
16 | 
17 | Debouncing is a technique to limit the rate of an action. Usually this
18 | rate is specified as an interval in which the action may be executed
19 | at most once. If execution of the action is requested multiple times
20 | in one interval it is important that the most recently supplied
21 | arguments are used when eventually executing the action.
22 | 
23 | (If you only care about the rate limiting and using the latest
24 | arguments isn't a requirement that's called throttling.)
25 | 
26 | Use cases for debouncing are plentiful. Auto-saving something the user
27 | is typing, fetching completions or triggering server side validations
28 | are some examples that come to mind.
29 | 
30 | ## Closure Library Facilities
31 | 
32 | I've long been a fan of the Closure Library that comes with
33 | ClojureScript.  Many common problems are solved in elegant and
34 | efficient
35 | ways, [the documentation](https://google.github.io/closure-library/)
36 | gives a good overview of what's in the box and the code and tests are
37 | highly readable.
38 | 
39 | For the problem of debouncing Closure provides a construct [goog.async.Debouncer](https://google.github.io/closure-library/api/goog.async.Debouncer.html)
40 | that allows you to debounce arbitrary functions. A short, very basic example in Javascript:
41 | 
42 | ```js
43 | var debouncer = new goog.async.Debouncer(function(x) {alert(x)}, 500);
44 | debouncer.fire("Hello World!")
45 | ```
46 | 
47 | This will create an alert saying "Hello World!" 500ms after the
48 | `fire()` function has been called. Now let's translate this to
49 | ClojureScript and generalize it slightly. In the end we want to be
50 | able to debounce any function.
51 | 
52 | ```clojure
53 | (ns app.debounce
54 |   (:import [goog.async Debouncer]))
55 | 
56 | (defn save-input! [input]
57 |   (js/console.log "Saving input" input))
58 | 
59 | (defn debounce [f interval]
60 |   (let [dbnc (Debouncer. f interval)]
61 |     ;; We use apply here to support functions of various arities
62 |     (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))
63 | 
64 | ;; note how we use def instead of defn
65 | (def save-input-debounced!
66 |   (debounce save-input! 1000))
67 | ```
68 | 
69 | What the `debounce` function does is basically returning a new
70 | function wrapped in a `goog.async.Debouncer`. When and how you create
71 | those debounced functions is up to you. You can create them at
72 | application startup using a simple `def` (as in the example) or you
73 | might also dynamically create them as part of your
74 | component/application lifecycle. (If you create them dynamically you
75 | might want to learn about `goog.Disposable`.)
76 | 
77 | There's one caveat with our `debounce` implementation above you should
78 | also be aware of: because we use Javascript's `apply` here we don't
79 | get any warnings if we end up calling the function with the wrong
80 | number of arguments. I'm sure this could be improved with a macro but
81 | that's not part of this article.
82 | 
83 | Also small disclaimer on the code: I mostly tested it
84 | with [Lumo](https://github.com/anmonteiro/lumo) in a REPL but I'm
85 | confident that it will work fine in a browser too.
86 | 
87 | ## Debounce Away
88 | 
89 | I hope this helps and shows that there's much useful stuff to be found
90 | in Closure Library. To this day it's a treasure trove that has rarely
91 | dissappointed me. Sometimes things are a bit confusing (I still don't
92 | understand `goog.i18n`) but there are many truly simple gems to be
93 | found. *Maybe I should do a post about my favorites some day...*
94 | 
95 | The [documentation site](https://google.github.io/closure-library) has
96 | a search feature and a tree view of all the namespaces of the library;
97 | use it next time when you're about to add yet another Javascript
98 | dependency to your project.
99 | 
100 | Also not a big surprise I guess but all of the Closure Library's code
101 | is Closure Compiler compatible just like your ClojureScript code. This
102 | means any functions, constants etc. that are never used will be
103 | removed by the compiler's Dead Code Elimination feature. Yeah!
104 | 
105 | **Update 2017-05-12** — Multiple people have noted that there also
106 | is a function [`goog.functions.debounce`](https://google.github.io/closure-library/api/goog.functions.html#debounce). For many basic cases this
107 | might result in simpler, more concise code.
```

content/posts/2017-05-12-requiring-closure-namespaces.md
```
1 | ---
2 | date-published: 2017-05-11T00:00:00Z
3 | title: Requiring Closure Namespaces
4 | uuid: 461f273b-8587-42ce-be1a-6efe22a78d2e
5 | permalink: /posts/requiring-closure-namespaces.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | Yet another post on properly using
11 | the [Closure Library](https://developers.google.com/closure/library/)
12 | from within ClojureScript. This time we'll discuss how to require
13 | different namespaces from Closure and the edge-cases that may not
14 | be immediately intuitive.
15 | 
16 | ## Namespaces, Constructors, Constants
17 | 
18 | When requiring things from Closure you mostly deal with its namespaces.
19 | Most namespaces have functions defined in them, some also contain constructors or constants.
20 | Functions are camelCased. Constructors are Capitalized. Constants are ALL_CAPS.
21 | The line between namespaces and constructors gets a bit blurry sometimes as you'll see shortly.
22 | 
23 | Let's take `goog.Timer` as an example. As per the previous paragraph you can infer that `Timer`
24 | is a constructor. Just like in Clojure we use `:import` to make constructors available:
25 | 
26 | ```
27 | (ns my.app
28 |   (:import [goog Timer]))
29 | ```
30 | 
31 | Now we may use the `Timer` constructor as follows:
32 | 
33 | ```
34 | (def our-timer (Timer. interval))
35 | ```
36 | 
37 | Great. We have a timer. Now we'll want to do something whenever it
38 | "ticks". The `Timer` instance emits events which we can listen
39 | to. Listening to events can be done with the function
40 | `goog.events.listen`. As you can see, this function is not part of any
41 | class instance - it just exists in the `goog.events` namespace.
42 | To make the `listen` function accessible you need to require the
43 | namespace containing it. This is similar to how we require regular
44 | ClojureScript namespaces:
45 | 
46 | ```
47 | (ns my.app
48 |   (:require [goog.events :as events])
49 |   (:import [goog Timer]))
50 | ```
51 | 
52 | We can refer to the function as `events/listen` now. To listen to
53 | specific kinds of events we need to pass an event type to this function. Many
54 | Closure namespaces define constants that you can use to refer to
55 | those event types. Internally they're often just strings or numbers but
56 | this level of indirection shields you from some otherwise breaking changes to
57 | a namespace's implementation.
58 | 
59 | Looking at the [Timer](https://google.github.io/closure-library/api/goog.Timer.html)
60 | docs you can find a constant `TICK`. Now we required the constructor
61 | and are able to use that but the constructor itself does not allow us
62 | to access other parts of the namespace. So let's require the namespace.
63 | 
64 | ```
65 | (ns my.app
66 |   (:require [goog.events :as events]
67 |             [goog.Timer :as timer]) ; <-- new
68 |   (:import [goog Timer]))
69 | 
70 | (def our-timer (Timer. interval))
71 | 
72 | (events/listen our-timer timer/TICK (fn [e] (js/console.log e)))
73 | ```
74 | 
75 | Remember the blurry line mentioned earlier? We just required the `goog.Timer` namespace
76 | both as a constructor and as a namespace. While this example works
77 | fine now, there are two more edge cases worth pointing out.
78 | 
79 | ## Deeper Property Access
80 | 
81 | Closure comes with a handy namespace for keyboard shortcuts, aptly named [`KeyboardShortcutHandler`](https://google.github.io/closure-library/api/goog.ui.KeyboardShortcutHandler.html).
82 | As you can guess, `KeyboardShortcutHandler` is a constructor that we can use via `:import`.
83 | Since it emits events, the namespace also provides an enum of events that we can use to listen for specific ones.
84 | In contrast to the timer's `TICK`, this enumeration is "wrapped" in `goog.ui.KeyBoardShortcutHandler.EventType`.
85 | 
86 | The `EventType` property contains `SHORTCUT_PREFIX` and `SHORTCUT_TRIGGERED`. So far we've only imported the constructor.
87 | At this point you might try this:
88 | 
89 | ```
90 | (:require [goog.ui.KeyBoardShortcutHandler.EventType :as event-types])
91 | ```
92 | 
93 | **But that won't work**. The `EventType` is not a namespace but an enum provided by
94 | the `KeyboardShortcutHandler` namespace. To access the enum you need to access it through the
95 | namespace providing it. In the end this will look like this:
96 | 
97 | ```
98 | (:require [goog.ui.KeyBoardShortcutHandler :as shortcut])
99 | 
100 | (events/listen a-shortcut-handler shortcut/EventType.SHORTCUT_TRIGGERED ,,,)
101 | ```
102 | 
103 | Note how the slash always comes directly after the namespace alias.
104 | 
105 | ## goog.string.format
106 | 
107 | Last but not least another weird one. `goog.string.format` is a namespace
108 | that
109 | [seems to](https://google.github.io/closure-library/api/goog.string.format.html) contain
110 | a single function called `format`. If you require the format namespace
111 | however, it turns out to contain no function of that name:
112 | 
113 | ```
114 | (:require [goog.string.format :as format])
115 | 
116 | (format/format ,,,) ; TypeError: goog.string.format.format is not a function
117 | ```
118 | 
119 | Now in cases like this it often helps to look at [the source code](https://github.com/google/closure-library/blob/master/closure/goog/string/stringformat.js)
120 | directly. Usually Closure Library code is very readable. The format function is defined as follows:
121 | 
122 | ```
123 | goog.string.format = function(formatString, var_args) {
124 | ```
125 | 
126 | As you can see it's defined as a property of `goog.string`, so we can
127 | access it via `goog.string/format` (or an alias you might have chosen
128 | when requiring `goog.string`).  In that sense `goog.string.format` is
129 | not a real namespace but rather something you require for its side
130 | effects — in this case the definition of another function in `goog.string`.
131 | I have no idea why they chose to split things up in that way.
132 | ¯\\_(ツ)_/¯
133 | 
134 | ## For Reference
135 | 
136 | I scratched my head many times about one or the other aspect of this
137 | and usually ended up looking at old code. Next time I'll look at the handy list below 🙂
138 | 
139 | - Require Google Closure **namespaces** just as you'd require ClojureScript namespaces
140 |     - `(:require [goog.events :as events])`
141 | - The **base `goog` namespace** is autmatically required as if you'd have
142 |   `[goog :as goog]` in your list of required namespaces.
143 |     - This implies that you can refer to `goog.DEBUG` as `goog/DEBUG`. Never refer to `goog` through the global Javascript namespace as in `js/goog.DEBUG`. ([CLJS-2023](https://dev.clojure.org/jira/browse/CLJS-2023))
144 | - Require **constructors** using one of the two forms. In either case you may use `Timer.` to construct new objects.
145 |     - `(:import [goog Timer])`
146 |     - `(:import goog.Timer)`
147 |     - There's an outstanding ticket about [imports with the same name shadowing each other](https://dev.clojure.org/jira/browse/CLJS-1734).
148 | - Only access **non-constructor parts** of a namespace through a namespace that has been `:require`d
149 | - Always use slash after the namespace alias, use dot for deeper property access.
150 | - Requiring `goog.string.format` will define a function `format` in the `goog.string` namespace.
151 | 
152 | ## Enjoy
153 | 
154 | For many of the things described here there are alternative ways to do
155 | them. We still build on Javascript after all. The ones I've chosen here are the ones
156 | that seem most idiomatic from a Clojurescript perspective.
157 | 
158 | Thanks to [Paulus Esterhazy](https://twitter.com/pesterhazy) and [António Monteiro](https://twitter.com/anmonteiro90) for proof-reading this post and offering their suggestions.
159 | 
160 | If you feel like reading more about utilizing the Closure Library and
161 | compiler in ClojureScript I have a few more posts on those:
162 | 
163 | - [Simple Debouncing in ClojureScript](/posts/simple-debouncing-in-clojurescript.html), showing how to build a simple debouncing mechanism with the facilities provided by the Closure Library.
164 | - [Parameterizing ClojureScript Builds](/posts/parameterizing-clojurescript-builds.html), outlining ways to modify ClojureScript builds using the Closure compiler's ability to customize constants at compile-time.
165 | - [Just-in-Time Script Loading](/posts/just-in-time-script-loading-with-react-and-clojuresript.html), describing how to load 3rd party scripts like Stripe using React components and Closure's script loader.
```

content/posts/2017-06-04-maven-snapshots.md
```
1 | ---
2 | date-published: 2017-06-04T00:00:00Z
3 | title: Maven Snapshots
4 | uuid: 443e70e4-c49d-4391-ac18-bf478b8e2955
5 | permalink: /posts/maven-snapshots.html
6 | og-image: /images/selfies/3.jpg
7 | type: post
8 | ---
9 | 
10 | ## Or: How to use Maven snapshots without setting your hair on fire.
11 | 
12 | Ever depended on a Clojure library with a version that ended in
13 | `-SNAPSHOT`? That's what's called a Maven snapshot.
14 | 
15 | Maven snapshots are a handy tool to provide pre-release builds to
16 | those who are interested. In contrast to proper releases a SNAPSHOT
17 | release can be "updated". And that's where the trouble comes in.
18 | 
19 | Let's say you depend on a snapshot because it contains a fix you
20 | recently contributed to your favorite open source project. A week
21 | later another fix is added and released under the same
22 | `0.1.0-SNAPSHOT` version.
23 | 
24 | Now it turns out that second fix contained a minor bug. No big deal,
25 | it's a pre-release after all. The problem with all this however is
26 | that you (Maven) will automatically use the new SNAPSHOT, no action required.
27 | A dependency you use in your project **changes without you being aware**
28 | of it. Suddenly stuff breaks. You wonder what happened. Did you change
29 | anything? No? Frustration ensues.
30 | 
31 | Because of this for a long time I thought SNAPSHOTS are evil and
32 | instead of using them library authors should release development
33 | builds with a qualifier like `0.1.0-alpha1`.
34 | I still think this is a good practice and try to adhere to it myself
35 | as much as possible.
36 | 
37 | ## In the meantime there is another way to safely depend on Maven snapshots though.
38 | 
39 | Whenever you push a SNAPSHOT version to a Maven repository (like
40 | Clojars) it does not actually overwrite the previously uploaded jar
41 | but creates a separate jar with a version like this:
42 | `0.1.0-20170301.173959-4`. Once the upload is complete it merely
43 | changes the SNAPSHOT version to point to that release. All previous
44 | releases are still available (by default Maven repos only keep the latest
45 | SNAPSHOT version but Clojars keeps them all).
46 | 
47 | This means instead of depending on a **mutable** version you can now
48 | depend on an **immutable** version. Oh do we love immutability.
49 | 
50 | ```
51 | [group-id/project-id "0.1.0-20170301.173959-4"]
52 | ```
53 | 
54 | Finding these version identifiers isn't the easiest thing but basically:
55 | 
56 | 1. you go to the page of a jar on Clojars, e.g. [adzerk/boot-cljs](https://clojars.org/adzerk/boot-cljs/)
57 | 2. in the sidebar that lists recent versions, click "Show All Versions"
58 | 3. [versions page](https://clojars.org/adzerk/boot-cljs/versions)
59 |   you can find a note at the bottom that leads you to the [Maven repository](https://repo.clojars.org/adzerk/boot-cljs/)
60 | 4. if you click on a SNAPSHOT version there you get to a page that [lists all the stable identifiers for that version](https://repo.clojars.org/adzerk/boot-cljs/2.0.0-SNAPSHOT/)
61 | 
62 | To get to the Maven repo page directly you can also just put a `repo.` subdomain in front of a given Clojars project url:
63 | 
64 | ```
65 | https://clojars.org/adzerk/boot-cljs/
66 | https://repo.clojars.org/adzerk/boot-cljs/
67 |         ^^^^
68 | ```
69 | 
70 | And they depend on SNAPSHOTs happily ever after.
71 | 
72 | 
```

content/posts/2018-01-19-sustainable-open-source-current-efforts.md
```
1 | ---
2 | date-published: 2018-01-19T00:00:00Z
3 | title: 'Sustainable Open Source: Current Efforts'
4 | uuid: 229c2b5e-1cdd-4904-9387-8c0491dc1382
5 | permalink: /posts/sustainable-open-source-current-efforts.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | 
10 | The recent appearance of [Clojurists Together](http://clojuriststogether.org/),
11 | friends working on [OpenBounty](https://openbounty.status.im/), as well as recently
12 | finding a lot of energy to work on a documentation platform for the Clojure
13 | ecosystem stirred some thoughts about sustainable OpenSource.
14 | 
15 | Let's say one thing right out of the gate: sustainable OpenSource
16 | isn't really a goal on it's own. What the community (users as well as
17 | maintainers) strives for is reliability and well-maintained ecosystem
18 | components. Businesses and individuals alike depend on those
19 | properties — not the fact that work done to achieve them is
20 | sustainable. That said I don't see any ways to achieve those without sustainability.
21 | 
22 | ### Current Efforts
23 | 
24 | There are various platforms trying to improve sustainability of open source
25 | efforts that have continued momentum. Many of them with their own ideas how
26 | the situation can be improved.
27 | 
28 | - [**OpenCollective**](https://opencollective.com) collects payments from
29 | individuals and companies and stores funds for organizations. People may then
30 | "invoice" the organization. This can be for stickers and labor time alike.
31 | 
32 | - [**OpenBounty**](https://openbounty.status.im/) is a bounty platform used
33 | with cryptocurrencies. Contributors may work on specific issues and get paid
34 | a bounty which has been defined in advance.
35 | 
36 | - [**Clojurists Together**](https://clojuriststogether.org/) collects
37 | money from companies and community members to fund open source
38 | projects benefitting the overall Clojure ecosystem. People may apply
39 | with a project they want to work on and get funding (depending on
40 | overall availability) for a duration of three months.
41 | 
42 | All platforms take care of collecting money and have mechanisms for
43 | redistributing it. With OpenCollective a community will need to agree on
44 | processes to request and distribute funds. Clojurists Together collects money
45 | in similar ways to OpenCollective but has a predefined process for
46 | how funds are allocated.
47 | 
48 | Projects like webpack [have embraced](https://opencollective.com/webpack/expenses) 
49 | OpenCollective with people getting reimbursed for expenses but also
50 | regular labor invoices for time worked on the project.
51 | 
52 | OpenBounty also provides some of these processes by assigning bounties
53 | to specific tasks. OpenBounty is used in
54 | [Status.im](https://status.im/)'s development process and while I
55 | don't believe bounties are the answer to everything I'm excited to
56 | watch this space in the future.
57 | 
58 | ### Sustainable Incentives
59 | 
60 | Contributing to OpenSource is about incentives. As far as I can judge these often are
61 | 
62 | - fun & community,
63 | - fixing a problem one encountered,
64 | - recognition & better job opportunities.
65 | 
66 | Nothing is wrong with this list but they are not sustainable on their
67 | own. Just working for the fuzzy feeling of giving back to a community
68 | doesn't pay your bills.
69 | As soon as you can no longer afford to work for the fun of it the stability
70 | and momentum of projects you contributed to will suffer.
71 | 
72 | I believe there are two kinds of participants required to achieve reliability, stability and so on:
73 | 
74 | - There need to be some people contributing on a regular basis. They provide
75 | overall direction, deal with reported issues and incoming contributions.
76 | Often projects refer to this as "core". In my experience stability of an open
77 | source project suffers with fluctuations in the set of "core" people working on it.
78 | - There need to be occasional/new contributors. Life will happen
79 | (kids, work, etc.) to regular contributors causing a natural
80 | decline. New contributors discovering they enjoy working on the
81 | project can fill up those gaps.
82 | 
83 | In my opinion the incentives listed above are not sufficient —
84 | especially for long-term regular contributors. If work is unpaid it
85 | will eventually become stressful to juggle with other responsibilities
86 | and people will be forced to step back. New contributors are just as
87 | important and I believe there are improvements to be done there as
88 | well but ultimately nothing works without a "core" set of people.
89 | 
90 | I have some further thoughts on how such incentives could be
91 | structured which I will explore in a later blog post.
```

content/posts/2019-01-28-writing-awesome-docstrings.md
```
1 | ---
2 | date-published: 2019-01-28T00:00:00Z
3 | title: 4 Small Steps Towards Awesome Clojure Docstrings
4 | uuid: b728ab75-373e-46b5-ba68-b01d5918cd70
5 | permalink: /posts/writing-awesome-docstrings.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | 
10 | Through my work on [cljdoc](https://cljdoc.org) I spent a lot of time looking at documentation
11 | and implementing code to render documentation. This made me more aware of the various
12 | facilities in Clojure documentation generators (codox, cljdoc, ...) and I would like to use
13 | this post to share that awareness with the wider Clojure community.
14 | 
15 | ## 1. Backtick-Quote Function Arguments & Special Keywords
16 | 
17 | Whenever referring to an argument or special keywords, quote them using Markdown style
18 | \`backticks\`. This makes them stand out more when reading the docstring, making it easier to
19 | visually parse and skim. Emacs also nicely highlights this (possibly others too).
20 | 
21 | ```
22 | (defn conj!
23 |   [coll x]
24 |   "Adds `x` to the transient collection, and return `coll`. The 'addition'
25 |    may happen at different 'places' depending on the concrete type."
26 |   ,,,)
27 | ```
28 | 
29 | ## 2. Link To Other Functions Using [[Wikilink]] Syntax
30 | 
31 | Functions call each other and sometimes it can be useful to link to other functions.
32 | In Codox and cljdoc you can do this by wrapping the var name in wikilink-style double brackets:
33 | 
34 | ```
35 | (defn unlisten!
36 |   "Removes registered listener from connection. See also [[listen!]]."
37 |   [conn key]
38 |   (swap! (:listeners (meta conn)) dissoc key))
39 | ```
40 | 
41 | Featured here: [`datascript.core/unlisten!`](https://cljdoc.org/d/datascript/datascript/0.17.1/api/datascript.core#unlisten!).
42 | To link to vars in other namespaces, fully qualify the symbol in the brackets, e.g. `[[datascript.core/listen!]]`.
43 | 
44 | ## 3. Include Small Examples
45 | 
46 | On cljdoc all docstrings are interpreted as Markdown. With Codox this can be achived with a
47 | small configuration tweak. This means you have access to all the text formatting facilities
48 | that Markdown provides including code blocks. Code blocks can be fantastic when trying to show
49 | how a function is used in a bigger context, as very nicely shown in the [Keechma Toolbox documentation](https://cljdoc.org/d/keechma/toolbox/0.1.23/api/keechma.toolbox.dataloader.controller#register):
50 | 
51 | [![keechma register](/images/keechma-register.png)](https://cljdoc.org/d/keechma/toolbox/0.1.23/api/keechma.toolbox.dataloader.controller#register)
52 | 
53 | See [the source](https://github.com/keechma/keechma-toolbox/blob/176c96a7f8b97a7d67f0d54d1351c23db052d71c/src/cljs/keechma/toolbox/dataloader/controller.cljs#L71-L85) of this majestic docstring.
54 | 
55 | ## 4. Use Tables To Describe Complex Options Maps
56 | 
57 | cljdoc's Markdown implementation supports tables as well. Those can be very useful when having a function that receives a map of options, like [`reitit.core/router`](https://cljdoc.org/d/metosin/reitit-core/0.2.13/api/reitit.core#router):
58 | 
59 | [![reitit core router](/images/reitit-router.png)](https://cljdoc.org/d/metosin/reitit-core/0.2.13/api/reitit.core#router)
60 | 
61 | See [the source](https://github.com/metosin/reitit/blob/0.2.13/modules/reitit-core/src/reitit/core.cljc#L417) of this beautiful docstring.
62 | 
63 | ## Closing
64 | 
65 | These trivial to implement improvements can make your docstrings 1000x times nicer to read
66 | (scientific studies have shown). Also they will just look plain awesome on [cljdoc](https://cljdoc.org). Check out
67 | some examplary docstring work done by Nikita Prokopov here:
68 | 
69 | - [Rum](https://cljdoc.org/d/rum/rum/0.11.3/api/rum.core)
70 | - [Datascript](https://cljdoc.org/d/datascript/datascript/0.17.1/api/datascript.core)
71 | 
72 | And **please tell me** about other projects with exceptional documentation or even more ways to
73 | make docstrings awesome.
```

content/posts/2019-09-06-using-cljs-bean-to-wrap-firebase-documents.md
```
1 | ---
2 | date-published: 2019-09-09T00:00:00Z
3 | title: Working with Firebase Documents in ClojureScript
4 | uuid: 3e616db0-a417-4bc4-93d0-b2a24256ab86
5 | permalink: /posts/using-cljs-bean-to-wrap-firebase-documents.html
6 | og-image: /images/selfies/1.jpg
7 | type: post
8 | ---
9 | 
10 | In a project [I’m currently working on](https://icebreaker.video) we’re making use of Google's [Firebase](https://firebase.google.com) to store domain data and run cloud functions.
11 | 
12 | In Firestore, which is Firebase’s database offering, every document is essentially a Javascript object. While interop in ClojureScript is pretty good we ended up converting the raw data of these documents to ClojureScript data structures using `js->clj`. This also meant we’d need to convert them back to JS objects before writing them to Firestore.
13 | 
14 | Because IDs are technically not part of the document the project adopted a pattern of representing documents as tuples:
15 | 
16 | ```clj
17 | [id (js->clj firestore-data)]
18 | ```
19 | 
20 | This works but isn’t particularly extensible. What if we also wanted to retain the “Firestore Reference” specifying a documents location inside the database? (Firestore stores data in a tree-like structure.)
21 | 
22 | It also leads to some funky gymnastics when working with collections of documents:
23 | 
24 | ```clj
25 | (sort-by (comp :join_dt second) list-of-document-tuples)
26 | ```
27 | 
28 | Could be worse... but also could be better.
29 | 
30 | This blogpost will compare various approaches approach to address the problems above using [cljs-bean](https://github.com/mfikes/cljs-bean), basic ClojureScript data structures, custom protocols and `:extend-via-metadata`.
31 | 
32 | ## cljs-bean
33 | With the recent release of [cljs-bean](https://github.com/mfikes/cljs-bean) we have an interesting alternative to `js->clj`. Instead of eagerly walking the structure and converting all values to their ClojureScript counterparts (i.e. persistent data structures) the original object is wrapped in a thin layer that allows us to use it as if it were a ClojureScript-native data structure:
34 | 
35 | ```clj
36 | (require '[cljs-bean.core :as cljs-bean])
37 | 
38 | (-> (cljs-bean/bean #js {"some_data" 1, :b 2})
39 |     (get :some_data)) ; => 1
40 | ```
41 | 
42 | Given a Firestore [QueryDocumentSnapshot](https://firebase.google.com/docs/reference/js/firebase.firestore.QueryDocumentSnapshot) we can make the JS object representing the data easily accessible from ClojureScript:
43 | 
44 | ```clj
45 | (-> (cljs-bean/->clj (.data query-document-snapshot))
46 |     (get :some_field))
47 | 
48 | ;; (cljs-bean/->clj data) is roughly the same as
49 | ;; (cljs-bean/bean data :recursive true)
50 | ```
51 | 
52 | The bean is immutable and can be used in client side app-state as if it is one of ClojureScript’s persistent data structures.
53 | 
54 | **Caveat:** Updating a bean using `assoc` or similar will create a copy of the object (Copy-on-Write). This is less performant and more GC intensive than with persistent data structures. Given that the data is usually quite small and that the document representations in our app state mostly aren’t written to directly this is probably ok ([cljs-bean #72](https://github.com/mfikes/cljs-bean/issues/72)).
55 | 
56 | Whenever we want to use the raw object to update data in Firestore we can simply call `->js` on the bean. Conveniently this will fall back to `clj->js` when called on ClojureScript data structures.
57 | 
58 | ```clj
59 | (.set some-ref (cljs-bean/->js our-bean))
60 | ```
61 | 
62 | Arguably the differences to using plain `clj->js` aren’t monumental but working with a database representing data as JS objects it is nice to retain those original objects.
63 | 
64 | ## Integrating Firestore Metadata
65 | 
66 | Now we got beans. But they still don’t contain the document ID or reference. In most places we don’t care about a documents ID or reference. So how could we enable the code below while retaining ID and reference?
67 | 
68 | ```clj
69 | (sort-by :join_dt participants)
70 | ```
71 | 
72 | Let’s compare the various options we have.
73 | 
74 | 
75 | ### Tuples and Nesting
76 | I already described the tuple-based approach above. Another, similar, approach achieves the same by nesting the data in another map. Both fall short on the requirement to make document fields directly accessible.
77 | 
78 | ```clj
79 | ;; structure
80 | {:id "some-id", :ref "/events/some-id", :data document-data}
81 | ;; usage (including gymnastics)
82 | (sort-by (comp :join_dt :data) participants)
83 | ```
84 | 
85 | I’m not too fond of either approach since they both expose a specific implementation detail, that the actual document data is nested, at the call site. In a way my critique of this approach is similar to why [Eric Normand advocated for getters in his IN/Clojure ’19 talk](https://youtu.be/Sjb6y19YIWg) — as far as I understand anyways.
86 | 
87 | ### Addition of a Special Key
88 | 
89 | Another approach could be to add metadata directly to the document data.
90 | 
91 | ```clj
92 | (defn doc [query-doc-snapshot]
93 |   (-> (cljs-bean/->clj (.data query-doc-snapshot))
94 |       (assoc ::meta {:id (.-id query-doc-snapshot
95 |                      :ref (.-ref query-doc-snapshot})))
96 | ```
97 | 
98 | This is reasonable and makes document fields directly accessible. However it also requires us to separate document fields and metadata before passing the data to any function writing to Firestore.
99 | 
100 | ```clj
101 | ;; before writing we need to remove ::meta
102 | (.set some-ref (cljs-bean/->js (dissoc document-data ::meta))
103 | ```
104 | 
105 | I think this is a reasonable solution that improves upon some of the issues with the tuple and nesting approach. I realize that this isn’t a huge change but this inversion of how things are nested does give us that direct field access that the nesting approach did not.
106 | 
107 | ### Protocols and `:extend-via-metadata`
108 | 
109 | An approach I’ve found particularly interesting to play with makes use of a protocol that can be implemented via metadata, as enabled by the new `:extend-via-metadata` option. This capability was added in [Clojure 1.10](https://clojure.org/reference/protocols#_extend_via_metadata) and subsequently added to ClojureScript with the [1.10.516 release](https://clojurescript.org/news/2019-01-31-release):
110 | 
111 | ```clj
112 | (defprotocol IFirestoreDocument
113 |   :extend-via-metadata true
114 |   (id [_] "Return the ID (string) of this document")
115 |   (ref [_] "Return the Firestore Reference object"))
116 | 
117 | (defn doc [query-doc-snapshot]
118 |   (with-meta
119 |     (cljs-bean/->clj (.data query-doc-snapshot))
120 |     {`id (fn [_] (.-id query-doc-snapshot))
121 |      `ref (fn [_] (.-ref query-doc-snapshot))}))
122 | ```
123 | 
124 | Using `with-meta` we extend a specific instance of a bean to implement the `IFirestoreDocument` protocol. This allows direct access to document properties while retaining important metadata:
125 | 
126 | ```clj
127 | (:name participant) ; => "Martin"
128 | (firebase/id participant) ; => "some-firebase-id"
129 | ```
130 | 
131 | At call sites we use a well-defined API (defined by the protocol) instead of reaching into nested maps whose structure may need to change as our program evolves. This arguably could also be achieved with plain functions.
132 | 
133 | **Sidenote:** A previous iteration of this used `specify!`. Specify modifies the bean instance however, meaning that whenever we’d update a bean the protocol implementation got lost. In contrast metadata is carried over across updates.
134 | 
135 | ## Summary
136 | Using [cljs-bean](https://github.com/mfikes/cljs-bean) we’ve enabled idiomatic property access for JS data structures without walking the entire document and converting it to a persistent data structure. We also retain the original Javascript object making it easy to use for Firestore API calls.
137 | 
138 | We’ve compared different ways of attaching additional metadata to those documents using compound structures as well as  the new and shiny `:extend-via-metadata`. Using it we’ve extended instances of beans to support a custom protocol allowing open ended extension without hindering the ergonomics of direct property access.
139 | 
140 | While I really enjoyed figuring out how to extend beans using `:extend-via-metadata` it turned out that any approach storing data in “unusual places” (i.e. metadata) causes notable complexity when also wanting to serialize the data.
141 | 
142 | Serializing metadata is something that [has been added to Transit quite some time ago](https://gist.github.com/mfikes/3a160a1504debd31e5771736256ca022) but compared to the plug and play serialization we get when working with plain maps it did not seem worth it. Even if set up properly the protocol implementations, which are functions, are impossible to serialize.
143 | 
144 | Ultimately we ended up with plain beans and storing metadata under a well known key that is removed before writing the data to Firestore again:
145 | 
146 | ```clj
147 | (defn doc [query-doc-snapshot]
148 |   (-> (cljs-bean/->clj (.data query-doc-snapshot))
149 |       (assoc ::meta {:id (.-id query-doc-snapshot)
150 |                      :ref (.-ref query-doc-snapshot)})))
151 | 
152 | (defn id [doc]
153 |   (-> doc ::meta :id))
154 | 
155 | (defn ref [doc]
156 |   (-> doc ::meta :ref))
157 | 
158 | (defn data [doc]
159 |   (cljs-bean/->js (dissoc doc ::meta)))
160 | ```
161 | 
162 | If you're using Firebase or comparable systems, I'd be curious to [hear if you do something similar on ClojureVerse](https://clojureverse.org/t/working-with-firebase-documents-in-clojurescript/4813).
163 | 
164 | *Thanks to Matt Huebert and Mike Fikes for their feedback & ideas.*
```

content/posts/2020-05-10-static-blogging-lessons-learned.md
```
1 | ---
2 | title: Static Blogging, Some Lessons Learned
3 | date-published: 2020-05-10T12:13:00.303Z
4 | uuid: 731b58c0-836e-4448-b225-67123f69d9af
5 | permalink: /posts/static-blogging-lessons-learned.html
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | 
10 | I've been running this blog for more than eight years now. Over these years it went through multiple rewrites, occasionally satisfying my urge to play with new toys. Now I'm in the middle of the next rewrite and I'm realizing some things that I'd love to have done from the start.
11 | 
12 | ### UUIDs For Every Post
13 | 
14 | Eventually the time will come where you want a unique identifier for a piece of content. Maybe it is to feed it into another system, maybe it is an ID for RSS feeds. No matter what it is it never hurts to have some identifiers for your content. I am now putting UUIDs into the frontmatter (YAML) section of every Markdown file I add. 
15 | 
16 | ### Static Permalinks
17 | 
18 | Some static site generators will define the permalink of a post by running code over some of the post's information like the title, slug, date, etc. I have found that a permalink should be permanent and thus there is no point in defining it in code. Just put the entire link into your post's metadata and whatever site generator you end up switching to, you'll know where that piece of content should be available in the end.
19 | 
20 | This means that maybe sometimes my URL schema isn't perfectly consistent but at the same time it also means I don't have to deal with redirects that would need to be configured in some external system system (e.g. websever/S3/Cloudfront).
21 | 
22 | Because I don't want to type out a UUID and permalink everytime I create a new post I created a little GitHub action that adds these fields to posts that don't already have it. 
23 | 
24 | ### Commit Everything
25 | 
26 | Committing generated files is one of these things that intuitively sounds wrong but my blog went through so many design iterations and changes and I would love to be able to just go back through those for a good trip down memory lane. In theory the source code is still there but in reality I'm rarely in the mood to get some code working again that I used to use five years ago.
27 | 
28 | I'm now commiting all files that you can see here in the `_site` directory of the repository backing this blog. 
29 | 
30 | ### Automation
31 | 
32 | I didn't intend to touch on this but one thing that I'm leaning into a lot for this rewrite is automating all kinds of things. As I write this post on [prose.io](https://prose.io) the frontmatter only has a title. As soon as I commit it an action will run to add `uuid`, `permalink`, `date-published`. This being easy to setup is a somewhat recent development, I guess last time around this wouldn't have been as easy as it is now. But it is exciting to me because it means I can just focus on writing and don't have to switch to a terminal to run `lumo -e '(random-uuid)'` or a deploy script.
33 | 
34 | This post is the first one that is being published using this automated setup so wish me luck as I hit the save & commit button. 
```

content/posts/2020-05-16-cljdoc-supports-foreign-libs.md
```
1 | ---
2 | title: Improved Support for Foreign Libs in cljdoc
3 | date-published: 2020-05-16T11:16:36.824Z
4 | uuid: 8fb953cf-e43f-4482-9fb2-f5e641a59cdf
5 | og-image: /images/selfies/2.jpg
6 | permalink: /posts/cljdoc-supports-foreign-libs.html
7 | type: post
8 | ---
9 | 
10 | Foreign libraries of ClojureScript libraries have always been a bit of an issue in cljdoc. With a namespace like the one below cljdoc would try to require `"react"` and then fail because `"react"` isn't a namespace it could find on the classpath. 
11 | 
12 | ```clojure
13 | (ns foo.bar
14 |   (:require ["react" :as react]))
15 | ```
16 | 
17 | No more! After some recent work by [Fabien Rozar](https://github.com/frozar) the analyzer will now walk all files packaged with a library for `:require` forms like the one above and stub them out so that the ClojureScript analyzer thinks they exist.
18 | 
19 | In the end the [implementation](https://github.com/cljdoc/cljdoc-analyzer/pull/20/files) was less complex than I thought it would be. Which I guess is a testament to the thoughtfulness of the people contributing to the ClojureScript compiler.
20 | 
21 | ### cljdoc-analyzer
22 | 
23 | All of this work builds on some long standing work by [Lee Read](https://github.com/lread) to provide a standalone analyzer to extract API information from Clojure & ClojureScript libraries. In many ways this is similar to the fantastic clj-kondo, except that it's focused more on full support of Clojure rather than speed. The analyzer cljdoc uses will actually load all your code so that even programatically created vars (often via macros) are returned properly.
24 | 
25 | [cljdoc-analyzer](https://github.com/cljdoc/cljdoc-analyzer) itself is a continuation of the work started by [codox](https://github.com/weavejester/codox) with some added bells and whistles, like more consistent output between Clojure and ClojureScript analysis results and automatic classpath construction based on a library's dependencies. The goal is that you can just run it on any library and get some API information (as EDN) in return. 
26 | 
27 | Thanks to Fabien and Lee for their work that made this all possible. I continue to be amazed by the people that come around to contribute to cljdoc. Fabien is from France, Lee is from Canada and unbeknownst to each other they basically shipped this together. Thank you!
28 | 
29 | 
30 | 
```

content/posts/2020-05-24-working-with-promises-in-clojurescript-repls.md
```
1 | ---
2 | title: Promises in a ClojureScript REPL
3 | date-published: 2020-05-24T11:09:30.032Z
4 | uuid: 7e85ecb2-3033-493b-81f5-8b27cef7574c
5 | og-image: /images/selfies/3.jpg
6 | permalink: /posts/working-with-promises-in-clojurescript-repls.html
7 | published: true
8 | type: post
9 | ---
10 | 
11 | [Roman](https://twitter.com/roman01la) wrote a nice post on working inside ClojureScript REPLs, also touching on [how to deal with promises](https://gist.github.com/roman01la/b939e4f2341fc2f931e34a941aba4e15#repl--asynchrony). If you're unfamiliar, the problem is that in Javascript many operations return promises and unlike in Clojure you cannot block until the promise is resolved. Instead you _have to_ handle the resulting value asynchronously. So if you for instance use `fetch` that could look something like this:
12 | 
13 |     (.then (js/fetch "https://jsonip.com/") prn)
14 | 
15 | This will use `prn` to print the value of the resolved promise. Sometimes you don't just want to print things though, the real power of a REPL lies in reusing values and successively building up just the shape of data you need.
16 | 
17 | One nice trick I learned from Sean Grove years ago is that you can just use `def`. This isn't something you'd do in production code but it's zero-ceremony and very handy to capture values.
18 | 
19 |     (.then (js/fetch "https://jsonip.com/") #(def -r %))
20 | 
21 | After this you can evaluate the `-r` symbol in your REPL and it will give you the value of the `fetch` promise. Alternatively to `def` we could also use an `atom` to store the return value.
22 | 
23 |     (def s (atom nil))
24 |     (.then (js/fetch "https://jsonip.com/") #(reset! s %))
25 | 
26 | ### Ergonomics
27 | 
28 | Now that we know how we can access the resulting value of a promise, let's make it convenient. For convenience I basically want two things:
29 | 
30 | - Make it easy to wrap any promise-returning form to capture it's return value
31 | - Make it easy to access the return values of multiple promises
32 | 
33 | What I came up with is a function I just named `t` which can be used like this:
34 | 
35 |     (let [s (atom {})]
36 |       (defn t
37 |         ([kw] (get @s kw))
38 |         ([p kw] (.then p (fn [r] (swap! s assoc kw r) r)))))
39 |         
40 |     (-> (js/fetch "https://jsonip.com/")
41 |         (t :jsonip))
42 | 
43 | When `t` receives two arguments it will consider the first argument a promise storing the resulting value in an atom under the key provided as the second argument, `:jsonip` in this case. 
44 | 
45 | This API is particularly nice when you consider that most editor integrations provide the ability to evaluate the form around your cursor. If I place my cursor within `(t :jsonip)` and evaluate this form I can look at the value the promise returned without changing any of the code. I can also just continue chaining with `then` since `t` returns the original promise. 
46 | 
47 | Another nice feature is that I can reuse the values for future REPL evaluations by referring to them using forms like `(t :jsonip)`.
48 | 
49 | Obviously **this is just one way** but I liked how that simple 4 line function made working with promises in a REPL a lot more enjoyable.
```

content/posts/2021-02-25-clojure-macros-creating-vars-from-a-map.md
```
1 | ---
2 | title: 'Clojure Macros: Creating vars from a map'
3 | permalink: /posts/clojure-macro-magic-vars-from-map.html
4 | date-published: 2021-02-25T12:18:42.591Z
5 | uuid: b2865f91-de5b-41f5-9632-6a3055b8e93d
6 | og-image: /images/selfies/2.jpg
7 | type: post
8 | ---
9 | The other day I was looking for a way to turn a map into a bunch of vars. I know a macro is what will get the job done but I write macros so rarely that it always takes me a while to figure it out. In the end I ended up with something like this:
10 | 
11 | ```
12 | (defmacro def-all [m]
13 |   (->> (for [[n v] m]
14 |          `(def ~(symbol n) ~v))
15 |        (into [])))
16 | ```
17 | 
18 | Using `macroexpand` you can see that this translates to the a bunch of `def` calls in a vector:
19 | 
20 | ```
21 | user=> (macroexpand '(def-all {:a 1 :b 2}))
22 | [(def a 1) (def b 2)]
23 | ```
24 | 
25 | Like myself, you may wonder why the vector is needed. The issue is that `for` will return a list and that would result in the macro emitting the following Clojure code:
26 | 
27 | ```
28 | ((def a 1) (def b 2))
29 | ```
30 | 
31 | After evaluating the two inner `def` forms, this will result in another function call where the return value of the first `def` is used as a function. Depending on what you are defining this may fail or lead to unexpected behavior.
32 | 
33 | After sharing my solution using `(into [])` in the [Clojurians Slack](https://clojurians.net/) I was made aware that instead if turning the thing into a vector you can also just prepend a do into that list, resulting in code that feels slightly more aligned with my intention:
34 | 
35 | ```
36 | (defmacro def-all [m]
37 |   (->> (for [[n v] m]
38 |          `(def ~(symbol n) ~v))
39 |        (cons 'do)))
40 | 
41 | (macroexpand '(def-all {:a 1 :b 2}))
42 | ; returns
43 | (do (def a 1) (def b 2))
44 | ```
45 | 
46 | I realize this is a super basic macro but I can totally see how that might be useful to people starting to write their own macros. If you're looking for a more full-fledged guide, [Clojure for the Brave and True](https://www.braveclojure.com/writing-macros/) got you covered.
47 | 
48 | Thanks to Justin Smith for sharing his experience on Slack with me so many times.
```

content/posts/2021-05-04-localizing-a-ghost-theme.md
```
1 | ---
2 | title: Localizing a Ghost Theme
3 | permalink: /posts/localizing-a-ghost-theme.html
4 | date-published: 2021-05-04T16:25:56.005Z
5 | og-image: /images/selfies/2.jpg
6 | hidden: true
7 | uuid: 542795e9-4a9c-4542-b57d-0bbe4e3e332c
8 | type: post
9 | ---
10 | Lately I've spent some time setting up a blog for a friend. [Ghost](https://ghost.org/) is where it's at these days when it comes to blogging — unless you're a nerd like me who loves to mess around with static site generators every now and then. 😅
11 | 
12 | Ghost provides many beautiful themes out of the box but most of them don't seem to support localization, which would be a nice thing to have for my friends blog. So I did some digging and essentially it comes down to:
13 | 
14 | 1. Using the `{{t}}` helper for any strings that should be localized ([docs](https://ghost.org/docs/themes/helpers/translate/))
15 | 2. Providing a `locales/de.json` file with mappings to localized strings
16 | 
17 | The [Dawn theme](https://github.com/TryGhost/Dawn) that we were using was pretty light on strings that needed localization so with a little bit of [vim-sandwich](https://github.com/machakann/vim-sandwich) magic and a custom mapping I was able to update it to use the `{{t}}` helper in maybe half an hour.
18 | 
19 | ```
20 | xmap <Leader>t sai{{t "<CR>"}}<CR>
21 | ```
22 | 
23 | With the visual mapping above all I needed to do is select the text that I want to localize and hit `<space>t`.
24 | 
25 | ![selecting some text and using vim-sandwich to wrap it in {{t}}](/images/uploads/sandwich-magic.gif "vim sandwich wrapping with custom head/tail")
26 | 
27 | Now the last step was to create the initial `locales/en.json` file. Later on I will use the English one as a template to create a German localization.
28 | 
29 | Since typing out the *more than a dozen* strings manually would have been boring I instead wrote a [babashka](https://babashka.org/) script to generate the English locales file for me.
30 | 
31 | ```clojure
32 | #!/usr/bin/env bb
33 | (require '[clojure.java.io :as io]
34 |          '[cheshire.core :as json]
35 |          '[babashka.fs :as fs])
36 | 
37 | (def entries
38 |   (->> (fs/glob "." "**/*.hbs")
39 |        (map (fn [p] (slurp (io/file (str p)))))
40 |        (mapcat (fn [file-contents]
41 |                  (map second (re-seq #"\{\{t \"(.*)\"\}\}" file-contents))))
42 |        (set)
43 |        (map (fn [s] [s s]))
44 |        (into {})))
45 | 
46 | (println (json/generate-string entries {:pretty true}))
47 | ```
48 | 
49 | This script essentially finds all usages of the `{{t}}` helper and spits out a JSON object where the keys are identical to the values (i.e. if the theme was English, that would be the `locales/en.json` file).
50 | 
51 | Babashka makes figuring this stuff out such a breeze because I can just incrementally build this out in a connected babashka nREPL session instead of changing the file and running the script as a whole on every change. REPLs for the win! 
52 | 
53 | In the end I created [this little PR to the theme](https://github.com/TryGhost/Dawn/pull/38).
```

content/posts/2021-05-09-homoiconicity-and-feature-flags.md
```
1 | ---
2 | title: Homoiconicity & Feature Flags
3 | date-published: 2021-05-09T22:09:30.032Z
4 | permalink: /posts/homoiconicity-and-feature-flags.html
5 | og-image: /images/selfies/10.jpg
6 | uuid: a9a77281-eb62-472c-a08b-1e9935c8a9c2
7 | published: true
8 | type: post
9 | ---
10 | At work we've been using feature flags to roll out various changes of the product. Most recently the rebrand from Icebreaker to [Gatheround](https://gatheround.com). This allowed us to continuously ship small pieces and review and improve these on their own pace without creating two vastly different branches of changes.
11 | 
12 | With the rebrand work in particular there were lots of places where we needed relatively small, local differentiations between the old and the new appearance. Oftentimes just applying a different set of classes to a DOM element. Less often, up to swapping entire components.
13 | 
14 | Overall this approach seemed to work really well and we shipped the rebrand without significant delays and at a level of quality that made everyone happy.
15 | What we're left with now is some 250+ conditionals involving our `use-new-brand?` feature flag.
16 | 
17 | *This tells the story of how we got rid of those.*
18 | 
19 | ## Introducing Homoiconicity
20 | 
21 | If you're well familiar with homoiconicity this may not be entirely new but for those who aren't: homoiconicity is the fancy word for when you can read your program as data. Among many other lisp/scheme languages Clojure is homoiconic:
22 | 
23 | ```clojure
24 | (doseq [n (range 10)]
25 |   (println n))
26 | ```
27 | 
28 | The program above can be run but it can also be read as multiple nested lists:
29 | 
30 | ```clojure
31 | [doseq     [n [range 10]]    [println n]]
32 | ```
33 | 
34 | Now, if you know what I'm talking about you will see that I skipped over a small detail here, namely that the code above uses two types of parenthesis and that information got lost in this simplified array representation.
35 | 
36 | When doing it right (by differentiating between the two types of lists) we would end up with exactly the same representation as in the first code sample. And that is homoiconicity.
37 | 
38 | ## Homoiconicity & Feature Flags
39 | 
40 | With this basic understanding of homoiconicity, lets take a look at what those feature flags looked like in practice:
41 | 
42 | ```clojure
43 | [:div
44 |  {:class (if (config/use-new-brand?)
45 |            "bg-new-brand typo-body"
46 |            "bg-old-brand typo-large")}]
47 | ```
48 | 
49 | ```clojure
50 | (when (config/use-new-brand?)
51 |   (icon/Icon {:name "conversation-color"
52 |               :class "prxxs h3"}))
53 | ```
54 | 
55 | And so on. Now we have 250+ of those in our codebase but don't really plan on reversing that change any time soon... so we got to get rid of them. Fortunately Clojure is homoiconic and doing this is possible in a fashion that really tickles my brain in a nice way. 
56 | 
57 | ## Code Rewriting
58 | 
59 | ... isn't new of course, CircleCI famously [rewrote 14.000 lines of test code to use a new testing framework](https://circleci.com/blog/rewriting-your-test-suite-in-clojure-in-24-hours/). I'm sure many others have done similar stuff and this general idea also isn't limited to Clojure. Code rewriting tools exist in many language ecosystems. **But how easily you can do it in Clojure felt very empowering.** 
60 | 
61 | The next two sections will be about some 30 lines of code that got us there about 90% of the way.
62 | 
63 | ## Babashka + rewrite-clj
64 | 
65 | [Babashka](https://babashka.org/) is a "fast, native Clojure scripting runtime". With Babashka you can work with the filesystem with shell-like abstractions, make http requests and much more. You can't use every Clojure library from Babashka but many useful ones are included right out of the box. 
66 | 
67 | One of the libraries that is included is [rewrite-clj](https://github.com/clj-commons/rewrite-clj). And, you guessed it, rewrite-clj helps you 🥁 ... rewrite Clojure/Script code. 
68 | 
69 | I hadn't used rewrite-clj before much am still a bit unfamiliar with it's API but after asking some questions on Slack [@borkdude](https://twitter.com/borkdude) (who also created Babashka) helped me out with an example of transforming conditionals that I then adapted for my specific situation.
70 | 
71 | I will not go into the code in detail here but if you're interested, I recorded [a short 4 minute video explaining it at a surface level and demonstrating my workflow](https://www.loom.com/share/70c1d3c45d9f45e9833344b5bd076813).
72 | 
73 | The rewriting logic showed in the video ignores many edge cases and isn't an attempt at an holistic tool to remove dead code branches but in our case this basic tool removed about 95% of the feature flag usages, leaving a mere 12 cases behind that used things like `cond->` or conjunctions.
74 | 
75 | Of the more than 230 feature flags that have been removed only about ten needed additional adjustments for indentation. This happened mostly when a feature-flag-using conditional wrapped multiple lines of code. Due to the locality of our changes that (fortunately) was relatively uncommon. If we had set up an automatic formatter for our code this also wouldn't have required any extra work.
76 | 
77 | ## Onward
78 | 
79 | This has been an extremely satisfying project, if you can even call those 30 lines a "project". I hope you also learned something or found it helpful in other ways!
80 | 
81 | Thanks to [Michiel "borkdude" Borkent](https://github.com/sponsors/borkdude) for all his work on Babashka. The interactive development workflow shown in [the video](https://www.loom.com/share/70c1d3c45d9f45e9833344b5bd076813) paired with blazing startup times and a rich ecosystem makes it feel like there is a lot of potential still to be uncovered.
82 | 
83 | I'd also like to thank [Lee Read](https://github.com/lread), who has done such an amazing job making rewrite-clj ready for more platforms like ClojureScript and Babashka as well as making sure it's future-proof by adding more tests and fixing many long standing bugs.
84 | 
85 | After writing this blog post and detailing the beginnings of this idea I also took a bit more time **[to clean up the code and put it on GitHub](https://github.com/martinklepsch/prune-feature-flags.clj)**.
86 | 
87 | If you thought this was interesting, consider [following me on Twitter](https://twitter.com/martinklepsch)!
```

content/posts/2023-08-29-better-clojurescript-node-repl-defaults.md
```
1 | ---
2 | title: Better ClojureScript Node REPL Defaults
3 | date-published: 2023-08-29T11:34:31.066Z
4 | uuid: a5e97c9e-46a4-4a55-914b-d63af6f698bb
5 | og-image: /images/selfies/2.jpg
6 | type: post
7 | permalink: /posts/better-clojurescript-node-repl-defaults.html
8 | ---
9 | 
10 | Hi there! Welcome back — to you as much as to me. It's been a while that I've
11 | published anything but here we go: **a little quality of life improvement for anyone driving a Node.js REPL from ClojureScript.**
12 | 
13 | ### The Problems
14 | 
15 | There's two issues I often run into when working with ClojureScript and Node.js REPLs:
16 | 
17 | 1. many values are async, resulting in a `<#Promise>` return value
18 | 2. uncaught errors will cause the Node.js process to exit
19 | 
20 | The first applies to any kind of ClojureScript REPL while the second is a more Node-specific problem. Losing your REPL state whenever something fails is annoying. This behavior makes sense when you run Node.js in production but for a REPL... not ideal.
21 | 
22 | ### A Workaround
23 | 
24 | Fortunately a bandaid solution is pretty trivial. To solve 1) we can make use of the excellent [portal](https://github.com/djblue/portal) tool. For 2) we can install a handler for `unhandledRejection` events, catching the error and reporting it in whatever way we like.
25 | 
26 | Below is a namespace that can be added to your [`:preloads`](https://shadow-cljs.github.io/docs/UsersGuide.html#_preloads) or just required when you start a new REPL session.
27 | 
28 | ```clj
29 | (ns acme.node-repl-preloads
30 |   (:require [portal.api :as portal]))
31 | 
32 | (js/process.on "unhandledRejection"
33 |                (fn [err]
34 |                  (js/console.log "unhandledRejection" err)
35 |                  (tap> {:unhandledRejection err})))
36 | 
37 | (when (.-ACME_DEV js/process.env)
38 |   (portal/open)
39 |   (add-tap portal/submit))
40 | ```
41 | 
42 | I add this to my `node-repl` helper like this:
43 | 
44 | ```clj
45 |   (shadow/node-repl
46 |     {:config-merge [{:devtools {:preloads '[acme.node-repl-preloads]}}]})
47 | ```
48 | 
49 | Now, with `ACME_DEV` set, we'll get a Portal window whenever we start a Node
50 | REPL, allowing us to chain promises into `tap>` and inspecting their value that
51 | way.
52 | 
53 | In addition to that any errors will also be logged to the console and to the
54 | Portal window — without crashing the process 🙂 From where I stand this would
55 | be a good default behavior but messing with error handling obviously comes with
56 | it's own tradeoffs.
57 | 
58 | Adding another handler for `unhandledException` is probably a good idea.
59 | 
60 | Anyways, nice to be back. I hope this is a slight improvement to someone's setup 🤗
```

content/posts/2024-12-13-gatheround-has-been-acquired.md
```
1 | ---
2 | title: Gatheround Got Acquired
3 | date-published: 2024-12-13T00:00:00Z
4 | type: post
5 | permalink: /posts/gatheround-got-acquired.html
6 | uuid: 643387bc-e58a-480d-9047-42f0b81399c9
7 | og-image: /images/selfies/7.jpg
8 | ---
9 | 
10 | 
11 | When I joined [Gatheround](https://gatheround.com/) I had a clear vision that for my next job *I wanted to be part of a team again.* To share the ups and downs, work more closely with product and design folks and to learn more about building software products.
12 | 
13 | The years of consulting, albeit full of variety, left me longing for depth.
14 | 
15 | Thanks to a friend I got introduced to and ended up joining Lisa, Perry and Alexander on their quest to build a digital product to foster human connection.
16 | 
17 | And while I'm no longer working at Gatheround **this somehow still feels like a milestone worth sharing**: Gatheround [has been acquired by Donut](https://www.donut.com/blog/donut-acquires-gatheround/)!
18 | 
19 | <figure>
20 | <img src='/images/Gatheround-First-Retreat.jpg' />
21 | <figcaption>Lisa, Perry, Alexander, Nick & myself in Oakland (February 2020)</figcaption>
22 | </figure>
23 | 
24 | **Community building has always been close to my heart**; be it within the Clojure community or my extended group of friends in Berlin.
25 | 
26 | From their roots in grassroots activisim we had some hypothesis to test around human connection in online spaces. And when COVID hit, the proverbial fans started spinning. The need for connection was greater than ever and Gatheround helped organizations, communities and companies to keep things human with smart breakout matching and fun-to-serious conversation prompts.
27 | 
28 | With promising traction [we raised a Series A from Homebrew](https://homebrew.co/blog/2023/11/06/gatheround-raises-usd8-million-series-a-for-software-that-powers-high-impact-video-meetings), Slack Fund and others. We also shifted focus towards more business use cases like all-hands meetings, onboarding sessions and other experiences.
29 | 
30 | While I left earlier in 2024 to pursue other projects it's been a fascinating experience to be a part of Gatheround's evolution over the years.
31 | 
32 | Here's to its future 🥂
```

content/posts/2025-01-15-pdf-to-csv-with-gemini-and-claude.md
```
1 | ---
2 | title: Converting a 700 Page PDF to Excel
3 | type: post
4 | permalink: /posts/pdf-to-csv-with-gemini-and-claude.html
5 | og-image: /images/selfies/10.jpg
6 | date-published: 2025-01-15T20:20:00Z
7 | uuid: 0a5c60c6-a252-4075-a9d1-23fc2ccf402e
8 | ---
9 | 
10 | The other day, my flatmate mentioned that a colleague had a 700 page PDF with a bunch of tables that would be nice to have in a spreadsheet. 
11 | 
12 | With the understanding that those problems are a thing of the past and poor impulse control, we went ahead and started playing with Gemini Advanced and other LLMs trying to extract the data. This blogpost details some of the steps that were necessary to do that reliably.
13 | 
14 | ## Model Choice
15 | 
16 | While I’ve recently been toying with Gemini Flash 2.0 it didn’t take long to bump against its limits. No matter how many things I called out in the prompt, I couldn’t get it to put empty cells into the CSV as an empty string `""`. Instead, it would simply omit the cell, making every cell slide left and corrupting the entire CSV.
17 | 
18 | For fun I even tried adding more vertical cell separation by quickly drawing in some black lines but that didn’t help much. Not that it would’ve been a feasible approach anyways.
19 | 
20 | GPT 4o didn’t do much better either.
21 | 
22 | In the end I tried Claude and was surprised by just how much better it was. Even with a reduced prompt that didn’t list out the columns it got things right fairly reliably. While my verification isn’t perfect it looks like only a few rows got messed up. 
23 | 
24 | The full prompt I used:
25 | 
26 | ```md
27 | Extract the tabular data on this page as csv. Respond with only the content of the csv file without any additional text.
28 | 
29 | Ignore any linebreaks in the table cells. Make sure to quote every cell value.
30 | When encountering empty cells, provide an empty string as value.
31 | 
32 | You must extract ALL the information available in the entire document.
33 | ```
34 | 
35 | ## 700 pages, one at a time
36 | 
37 | The great thing about extracting tables is that each page is independent, making it much easier to verify your approach at a more granular level. `pdfseparate` made it trivial to split up the PDF:
38 | 
39 | ```
40 | pdfseparate netzausbauplan-2024\ _kurz.pdf pages/%03d.pdf
41 | ```
42 | 
43 | After that, I used [Simon Willison](https://simonwillison.net/)’s excellent [`llm`](https://github.com/simonw/llm) command line tool and wrote a small script that processes a range of pages. 
44 | 
45 | ```
46 | llm -m claude-3-5-sonnet-latest -o temperature 0 -a pages/123.pdf "$(cat prompt.md)"
47 | ```
48 | 
49 | Setting the temperature to 0 wasn’t something that was necessary from my testing, but it seemed appropriate to ask for as-deterministic-as-possible model behavior, given the nature of the task.
50 | 
51 | 
52 | ## Anthropic Batches API
53 | 
54 | Now processing all 700 pages is the kind of workload where you get in the range of 2.5M input tokens and 1.4M output tokens, *amounting to about $30*. Perhaps cheap for what it does **but we are cheaper**. 
55 | 
56 | Doing the math on what it would cost to process the entire PDF I learned about [Anthropic’s Batches API](https://docs.anthropic.com/en/docs/build-with-claude/message-batches#pricing). A variable-latency API that is meant for workloads that don’t require an immediate response. 
57 | 
58 | Now the `llm` CLI tool does not support the Batches API. But that didn’t stop us. I copied my existing script and some links to Claude’s API docs into Cursor and within 20 minutes I had a script with the same command line API, but using the Batches API. 
59 | 
60 | Was this necessary? Probably not. Was it fun to make Cursor rewrite my code to use a different API and programming language? **Definitely.**
61 | 
62 | ## Who puts 700 pages full of tables in a PDF? 
63 | 
64 | Good question. [The PDF](https://downloads.ctfassets.net/xytfb1vrn7of/2upzYS0EhiuuAU4yOYGKwi/18b3903c9a72da15bf914ebb70a52e33/netzausbauplan-2024.pdf) contains a list of planned and ongoing grid development projects across all of Germany. By law (§ 14d Energiewirtschaftsgesetz), all grid operators with more than 100.000 customers are required to publish these plans.
65 | 
66 | The law seemingly did not state anything about the format in which those plans should be published. So, the publishers, Netze BW GmbH / EnBw, decided: PDF it is!
67 | 
68 | For those who might have use for this data, you can download it here: [Google Sheet Netzbauplan](https://ggl.link/netzbauplan).
```

content/posts/2025-02-06-one-shot-babashka-scripts.md
```
1 | ---
2 | title: One-Shot Babashka CLI Scripts
3 | type: post
4 | permalink: /posts/one-shot-babashka-cli-scripts.html
5 | og-image: /images/selfies/10.jpg
6 | date-published: 2025-02-05T23:18:12.514Z
7 | uuid: 813135fb-d709-4fbd-8fbd-a3ce33091b69
8 | bluesky-post-id: 3lhhrisefmk2t
9 | ---
10 | Like everyone I've been exploring AI tools and reading Simon Willisons excellent blog I discovered [how he uses LLMs to generate one-off Python tools](https://simonwillison.net/2024/Dec/19/one-shot-python-tools/).
11 | 
12 | In this post I'm gonna share a bit more about how I generate Babashka scripts in similar ways.
13 | 
14 | ## It's all in the context
15 | 
16 | While Claude is pretty good at Clojure already it often generated code that didn't quite work. One particular case that kept occurring was that Claude kept thinking that `babashka.fs/glob` returns files (something that `slurp` could read) when in reality it returns a path. Globs can match directories after all.
17 | 
18 | So I started to copy together some pieces of documentation into a snippet that I'd always provide as context. Among the documentation I also included some guidelines for how I want things to be done.
19 | 
20 | ```md
21 | # general tips for writing babashka code
22 | 
23 | 1. When using `p/process` and `p/shell` a variable list of strings is expected at the end. When creating the command using a vector or similar, be sure to use `apply` so that the vector is unwrapped
24 |   1. Example: `(apply p/process {} ["echo" "123"])`
25 | 
26 | 2. Some useful flags for file processing scripts
27 |   1. `--dry-run` only print actions, don’t execute
28 |   2. `--verbose` log additional input
29 | 
30 | 3. When creating namespaces and functions using the babashka.cli APIs, it is useful to alias them into your `bb.edn` file so that they can used as a shorter command
31 |   -  e.g. `{:tasks {prune some.ns/prune}}`
32 | ```
33 | 
34 | Maintaining a list of prompts and context windows seems like a useful thing to do! *There's a link to this one and a few others at the end.*
35 | 
36 | ## Generating a script
37 | 
38 | Let's say I have a directory of markdown files and I want to add some frontmatter to each of them, say `type: post`.
39 | 
40 | With the provided context I'd write a short prompt
41 | 
42 | > Please write a babashka CLI script that transforms markdown files by adding `type: post` to their YAML frontmatter. It should be possible to specify individual files as well as a glob pattern.
43 | 
44 | The result will be a script like this one. It's not the prettiest and if you look closely there's definitely a few interesting idiosyncrasies but overall it's pretty good for something I didn't have 30 seconds ago!
45 | 
46 | - There's CLI options for verbose and dry-run modes
47 | - Files to process can be specified via files or pattern options
48 | - Emojis are used in progress messages 
49 | - It's readable!
50 | 
51 | (If you properly want to read this code, [this Gist](https://gist.github.com/martinklepsch/b534f6be88cd48bf9aad4076dc2ccbfa) will display it better.)
52 | 
53 | ```clojure
54 | #!/usr/bin/env bb
55 | 
56 | (require '[babashka.cli :as cli]
57 |          '[babashka.fs :as fs]
58 |          '[clojure.string :as str]
59 |          '[clj-yaml.core :as yaml])
60 | 
61 | (def cli-opts
62 |   {:spec {:files {:desc "Individual markdown files to process"
63 |                   :coerce []}
64 |           :pattern {:desc "Regex pattern to match markdown files (e.g. \"posts/*.md\")"
65 |                    :alias :p}
66 |           :dry-run {:desc "Print what would be changed without making changes"
67 |                    :coerce :boolean}
68 |           :verbose {:desc "Print additional information during processing"
69 |                    :coerce :boolean}}})
70 | 
71 | (defn extract-frontmatter
72 |   "Extracts YAML frontmatter from markdown content.
73 |    Returns [frontmatter remaining-content] or nil if no frontmatter found."
74 |   [content]
75 |   (when (str/starts-with? content "---\n")
76 |     (when-let [end-idx (str/index-of content "\n---\n" 4)]
77 |       (let [frontmatter (subs content 4 end-idx)
78 |             remaining (subs content (+ end-idx 5))]
79 |         [frontmatter remaining]))))
80 | 
81 | (defn update-frontmatter
82 |   "Updates the frontmatter by adding type: post if not present"
83 |   [markdown-str]
84 |   (if-let [[frontmatter content] (extract-frontmatter markdown-str)]
85 |     (let [data (yaml/parse-string frontmatter)
86 |           updated-data (cond-> data
87 |                         (not (:type data)) (assoc :type "post"))
88 |           new-frontmatter (yaml/generate-string updated-data :dumper-options {:flow-style :block})]
89 |       (str "---\n" new-frontmatter "---\n" content))
90 |     markdown-str))
91 | 
92 | (defn process-file
93 |   "Process a single markdown file, updating its frontmatter"
94 |   [file {:keys [dry-run verbose]}]
95 |   (let [content (slurp file)
96 |         updated-content (update-frontmatter content)]
97 |     (when verbose
98 |       (println "📝 Processing" (str file)))
99 |     (if (= content updated-content)
100 |       (when verbose
101 |         (println "⏭️ No changes needed for" (str file)))
102 |       (do
103 |         (when verbose
104 |           (println "🔄 Updating frontmatter in" (str file)))
105 |         (when-not dry-run
106 |           (spit file updated-content))))))
107 | 
108 | (defn process-files
109 |   "Process multiple markdown files based on CLI options"
110 |   [{:keys [files pattern] :as opts}]
111 |   (let [pattern-files (when pattern
112 |                        (->> (fs/glob "." pattern)
113 |                             (map fs/file)
114 |                             (filter #(str/ends-with? (str %) ".md"))))
115 |         all-files (concat (map fs/file files) pattern-files)]
116 |     (if (seq all-files)
117 |       (do
118 |         (when (:verbose opts)
119 |           (println "🔍 Found" (count all-files) "files to process"))
120 |         (doseq [file all-files]
121 |           (process-file file opts))
122 |         (println "✨ Processing complete!"))
123 |       (println "⚠️ No markdown files found to process"))))
124 | 
125 | (defn -main [& args]
126 |   (let [opts (cli/parse-opts args cli-opts)]
127 |     (if (:help opts)
128 |       (println (cli/format-opts cli-opts))
129 |       (process-files opts))))
130 | 
131 | (when (= *file* (System/getProperty "babashka.file"))
132 |   (apply -main *command-line-args*))
133 | ```
134 | 
135 | ## Context windows
136 | 
137 | Using a prepared context window/prompt I can describe something in a generic way and turn it into something concrete in a twist.
138 | 
139 | Here's two context windows that might be helpful to you:
140 | 
141 | 1. [**The Babashka one**](https://github.com/ctxs-ai/ctxs.ai/blob/main/contexts/martinklepsch/babashka.md) previewed here
142 | 1. [**A JS/ClojureScript conversion helper**](https://github.com/ctxs-ai/ctxs.ai/blob/main/contexts/martinklepsch/js-cljs-conv.md) that understands Uix, Reagent & React and lets you translate code between those.
143 | 
144 | PRs to improve these are welcome!
145 | 
146 | I'm curious to make a full REPL loop that runs code that was generated this way. Natural language in, Clojure forms out, confirm to run. Could be fun!
```

content/onehundred/2021-02-15-writing-100-things.md
```
1 | ---
2 | title: Writing 100 Things
3 | date-published: 2021-02-15T19:06:42.148Z
4 | uuid: e0ebd76e-e415-4623-a9a1-8f2d69e19cec
5 | og-image: /images/selfies/2.jpg
6 | type: onehundred
7 | permalink: /100/writing-100-things.html
8 | ---
9 | In order to write more I'm going to embark on a little project: publishing 100 pieces of writing. This is inspired by a bunch of Twitter strangers putting things like "(22/100 Youtube videos)" next to their display names and by [Mike](https://critter.blog/) who has been blogging every day for the last 100+ days, producing a wonderful array of short but insightful articles.
10 | 
11 | Doing anything a hundred times should give me a good understanding of what I like or dislike about it. It'll also require that I keep an open eye for topics and themes to write about. Being a more attentive observer. Reviewing notes and bookmarks on the hunt for the next bit. 
12 | 
13 | I've also observed that a lot of writing I enjoy has a certain conviction to it. Writers like [Derek Sivers](https://sive.rs/) and [Visakan](https://twitter.com/visakanv) come to mind. They share their ideas without much introduction or "proof", and yet a lot of their writing really resonates with me as being true to my subjective experience. 
14 | 
15 | In the process of writing more regularly I will also write shorter posts, share more unfinished ideas, mid-flight thoughts and random ramblings. Part of this experiment is to get rid of the filter I've created for myself for posting on my [main blog](https://martinklepsch.org) and just write more for the sake of writing — and sharing. 
16 | 
17 | If there's things you'd like to see me write about, let me know!
```

content/onehundred/2021-02-16-how-do-you-get-a-remote-clojure-job.md
```
1 | ---
2 | title: How do you get a remote Clojure job?
3 | date-published: 2021-02-16T17:01:26.181Z
4 | uuid: e3aefa0b-1858-4fe8-9553-534b842ad209
5 | og-image: /images/selfies/2.jpg
6 | type: onehundred
7 | permalink: /100/how-do-you-get-a-remote-clojure-job.html
8 | ---
9 | I don't think there's a significant difference to getting any other remote job. In the end Clojure is a programming language and the ways you show you're qualified feel largely similar (with some aspects being more top-of-mind in individual communities). Also keep in mind that many roads lead to Rome and that my perspective on this isn't going to be an absolute truth but rather something that grew from my experiences. Yours might be different and the path of least resistance is always to find out what works best for you.
10 | 
11 | 📝  Write code, put it out there and allow people to trace your decision making. Architecture Decision Records (ADRs) are something that I found helpful in this regard. Often people do this in the form of libraries but I don't think it really matters if others use the code in the end. Maybe the code you're most excited about writing is a little application to scratch a personal itch. 
12 | 
13 | 👩🏽‍💻 Network. I hate this term personally and it has never felt like what I've been doing but I've been active in local meetups, conferences and online communities, which has made me many friends and access to several opportunities that were never listed on a job board.
14 | 
15 | 👀 Nonetheless, keep an eye out for remote job listings. Identify themes in what companies are looking for, reflect on which opportunities excite you and which don't. Invest in skills that you feel you might be lacking for the exciting ones. 
16 | 
17 | 💪 If something looks exciting but you don't have the requested experience/skills, reach out anyways. Likely you'll learn something from that interaction and often people are willing to take chances with you if they can sense your excitement. At the very least you've put yourself on the map for them.
18 | 
19 | 💰 Start cheap. This kind of depends on your other experience obviously but if you want to transition (e.g. to being remote or writing Clojure), getting the first job locked in is the hardest. Reducing your rate can make this easier and you can always renegotiate or try to find a second job, that is willing to pay better. I once read that people who change jobs more often tend to earn more money, which seems logical to me given the competitive dynamics around hiring + the incentive to keep operating expenses low. (For context I should say that I've worked as a freelancer for a long time.)
20 | 
21 | ✍🏻 Write and get better at it. Remote work tends to be much more asynchronous and writing is a key skill to be an efficient collaborator. People hiring for remote roles will want to judge your writing skills. If you're not a native English speaker don't let that deter you. While grammatical correctness is nice it is not the most important thing as long as things are understandable. What is important is making the complex simple ;) Often the goal is building consensus or transferring knowledge (a.k.a. documentation). Identify what goal you have with a piece of writing and find the right structure to achieve it. A blog can be a great venue to practice and showcase your writing. (A blog can be a plain GitHub repo.)
22 | 
23 | Hope this helps! 
24 | 
25 | PS. I realize that a lot of these things require time and not everyone may have the luxury of a lot of free time. If you're a member of an underrepresented group and feel like a bit of cash would go a long way in terms of making time for the things above, send me a DM.
26 | 
27 | *Note: This was originally posted [on rep.ly](https://rep.ly/answer/5f92959f17801d0018370de4).*
```

content/onehundred/2021-02-16-mobile-first-constraints.md
```
1 | ---
2 | title: Mobile First Constraints
3 | date-published: 2021-02-16T08:27:11.989Z
4 | uuid: d80a3ffa-30de-40c8-84a5-b0a9857100c4
5 | og-image: /images/selfies/2.jpg
6 | type: onehundred
7 | permalink: /100/mobile-first-constraints.html
8 | ---
9 | I've been revisiting a book on [Mobile First](https://www.lukew.com/resources/mobile_first.asp) over the last couple of days. It's a very short and sweet book, like many others by [A Book Apart](https://abookapart.com/). The book argues that web experiences should be designed for mobile first for two main reasons: 
10 | 
11 | 1. mobile is growing like hell and 
12 | 2. starting with a more constrained design leads to a better understanding of the core of what is being built. 
13 | 
14 | The case for mobile growing is supported by engagement and screen time metrics with a kind of blissful ignorance for more recent addiction concerns that you can probably only find in a 2015 book.
15 | 
16 | Anyways, that second point is what stayed with me and what I've been thinking about a lot in the context of my own work. Designing for mobile first, forces a product team to discover the essence of a feature that is being developed. It becomes about identifying the smallest useful thing to ship. 
17 | 
18 | If you think about it there aren't any quite as easy-to-articulate constraints to be a forcing function. Most of the reduction happening in the product development process is based on intuition, subjective experience and user interviews. Designing for a small screen first on the other hand is an incredibly specific guideline.
19 | 
20 | It also reinforces all the things I want when working on product stuff: learning from experiments as early as possible and shipping fast / building momentum.
21 | 
22 | To end this, here's a nice quote from [Kate Aronowitz](https://twitter.com/katearonowitz), back then Facebook’s Director of Design:
23 | 
24 | > We’re just now starting to get into mobile first and then web second for a lot of our products. What we’re finding is that the designers on mobile are really embracing the constraints \[and] that it’s actually teaching us a lot about how to design back to the desktop,
```

content/onehundred/2021-02-21-cljdoc-origins-1-n.md
```
1 | ---
2 | title: cljdoc origins 1/n
3 | date-published: 2021-02-21T09:57:29.870Z
4 | uuid: 7d40d366-4fb0-44b0-a09e-baedb1ee9165
5 | og-image: /images/selfies/2.jpg
6 | type: onehundred
7 | permalink: /100/cljdoc-origins-1-n.html
8 | ---
9 | It was one of these cold and dark December evenings in 2018. Like every month [Ben](https://twitter.com/socksy) and [Paulus](https://twitter.com/pesterhazy) hosted the Clojure meetup on the second Wednesday of the month. This time it was at Thoughtworks, and I believe there was pizza. Some 50 people were about to attend the meetup that ignited a spark, which eventually led to cljdoc. 
10 | 
11 | First, [Oli](https://www.eidel.io/talks) gave a talk on targeting WebGL from a re-frame app. At work he had been working on this quite complex UI to annotate radiology scans so that it could be tested if the resulting data could be used to aid doctors in their diagnostic work. It was his first talk ever and everyone was pumped. 
12 | 
13 | A couple of pizza slices later Daniel showed off a new \`unrepl\` based Emacs integration that allowed [multimedia objects](https://twitter.com/volrath/status/941342874372894720) and a bunch of other cool things that at the time made people quite hyped about the broader "REPL renaissance" at the time. 
14 | 
15 | So it was a good meetup. Plenty of good vibes and people seemed to enjoy what Ben and Paulus had organized. As every so often there was a third slot that was still up for grabs and often someone would step up last minute and share something they felt was worth sharing. On this day Arne took the opportunity. 
16 | 
17 | Arne presented a little script he wrote, called \`autodoc\`. At the time the primary way library authors published API documentation for their Clojure libraries was \`codox\`, but the way most people used it required manual updating and often the documentation would lag behind the most recent release. Arne's script was aiming at simplifying that process and helping people to keep their docs in sync. But besides all this Arne also showed some cool stuff from the Elixir community, namely [hexdocs.pm](https://hexdocs.pm/). Hexdocs is a hosted platform for Elixir library documentation, much like what cljdoc is now. 
18 | 
19 | Chatting with a bunch of people after Arne's talk I more and more thought "wow, this would be so amazing to have for Clojure". 
20 | 
21 | A few days later Oli initiated a [conversation on ClojureVerse](https://clojureverse.org/t/creating-a-central-documentation-repository-website-codox-complications/1287/4) and the ball started rolling. I became aware of related projects like Reid's [grimoire](https://github.com/clojure-grimoire/grimoire) and started to work on some early prototypes. Grimoire ended up becoming the storage layer for early versions of cljdoc. 
22 | 
23 | A month later, on the second Wednesday of January, the Berlin Clojure community gathered again. This time at Acrolynx and this time it was only lightning talks. Excited about the prospect of a "hexdocs for Clojure" I prepared a [tiny presentation](https://clojureverse.org/uploads/default/original/1X/3ecba3a15c85783f14044da7b8be57020304ecce.pdf) painting in broad strokes what I hoped we could build.
```

content/onehundred/2021-02-21-cljdoc-origins-2-n.md
```
1 | ---
2 | title: cljdoc origins 2/n
3 | date-published: 2021-02-21T12:17:05.266Z
4 | uuid: 183d3761-d354-4361-b903-e4e4d9ee9717
5 | og-image: /images/selfies/1.jpg
6 | type: onehundred
7 | permalink: /100/cljdoc-origins-2-n.html
8 | ---
9 | Okay, surfing [is done](https://twitter.com/martinklepsch/status/1363428911422521346?s=20). Back to January 2018. As [I said](https://martinklepsch.org/100/cljdoc-origins-1-n.html) I created a [small presentation](https://clojureverse.org/uploads/default/original/1X/3ecba3a15c85783f14044da7b8be57020304ecce.pdf) and shared it, full of excitement, with the attendees of that January Clojure Berlin meetup. At the time I had a prototype that analyzed a jar, and created a super basic page listing vars in the available namespaces.
10 | 
11 | ![The very first namespace with some vars being listed ](https://martinklepsch.org/images/uploads/screen-shot-2018-01-11-at-22.31.01.png "cljdoc v0")
12 | 
13 | At that time I had maybe sunk a dozen hours or so into this project. I was so excited. So excited to just share the vision of the project but also quite excited to potentially recruit some people to join me.
14 | 
15 | I don't remember all the interactions I had after that meetup but I do remember one.
16 | 
17 | One of the core problems of this project was always to analyze Clojure code without opening up a bunch of security holes. Anything can happen when requiring a Clojure namespace. There's no guarantee that it won't start deleting random files or mining bitcoin.
18 | 
19 | At that January meetup I didn't yet have a good answer for this problem and basically asked the room if they had ideas or might even be able to help with this directly.
20 | 
21 | And then there was this one person. I was being informed how very difficult this is and that there's companies whose whole business builds around detecting abuse like this. What I was trying to do was basically a futile effort. Not the greatest vibe when you're sharing an early idea but — surprisingly — it did kind of work out for me in the end.
22 | 
23 | In the coming days I was all about just finding a way to be able to say: "Look, the impossible just happened!"
24 | 
25 | A twisted part of me just wanted to prove to that person that they're not going to shoot this down. And you know, probably it wasn't even their intention.
26 | 
27 | As you might guess I didn't magically solve all the problems. But I found a kind of hack that just avoided the problem all together. I used CircleCI's build infrastructure, queued jobs with different parameters via their API and collected the results as a file that was stored with the build as a build artifact. [40.000+ builds later](https://cljdoc.org/builds) this is still going strong as ever.
28 | 
29 | After figuring this out it felt like the rest was within the boundaries of what I've done in the past. Even though I've only been hacking on this for a couple of weeks it felt like the rest would be like riding down a hill. It turned out to be a pretty long slope but I think I didn't quite realize that at this point.
30 | 
31 | Anyways, time to call it a night and get some food. Take care of yourselves ☺️
```

content/onehundred/2021-02-23-we-need-more-gifs.md
```
1 | ---
2 | title: We need more GIFs
3 | date-published: 2021-02-23T12:17:05.252Z
4 | uuid: b85d4060-0b46-41f3-a489-f668498d59bc
5 | og-image: /images/selfies/3.jpg
6 | type: onehundred
7 | permalink: /100/we-need-more-gifs.html
8 | ---
9 | The other day a new colleague joined the team at Icebreaker and he started doing something that I always liked as a fun norm of sorts but never really quite stuck with me as something I would do: posting GIFs alongside PR reviews.
10 | 
11 | Inspired by the Family Guy "CSS Window Blinds" classic I quickly found this super easy [GIFs for GitHub](https://chrome.google.com/webstore/detail/gifs-for-github/dkgjnpbipbdaoaadbdhpiokaemhlphep) Chrome extension. It integrates Giphy's search into GitHub comment forms and makes it a breeze to sprinkle whatever feedback you may have with something that might make your colleagues smile.
12 | 
13 | I guess the next level is maintaining your own repository of GIFs and having them ready at the blink of an eye. Like back in the golden days of IRC.
```

content/onehundred/2021-03-21-different-beaches.md
```
1 | ---
2 | title: Different Beaches
3 | date-published: 2021-03-21T12:04:13.015Z
4 | uuid: 982b40f7-3d46-446d-a09d-d88b051452b6
5 | og-image: /images/selfies/3.jpg
6 | type: onehundred
7 | permalink: /100/different-beaches.html
8 | ---
9 | The last weeks haven't been very productive when it comes to this writing project, as I was mostly busy enjoying the last stretch of my South Africa trip that started at the end of 2020. Now I'm back in Berlin and under quarantine so in theory that should create lots of space to write.  
10 | 
11 | Anyways, I've been learning to surf. The last time I tried to surf was in Dulan (Taiwan) a couple of years ago. In retrospect the waves then now seem surprisingly tiny to learn how to surf but as it turns out some of the technique still felt familiar when I got back into it in Muizenberg. For the first couple of days we were in South Africa the beaches were still open. Most of January they were closed as part of South Africa's COVID measures. 
12 | 
13 | I took one or two lessons during these couple of days to get back into it and had a great coach, Jaydon. After refreshing my pop-up (surfer speak for the moment when you switch from laying on the board to standing) I got into the water and — with some helpful pushes — was able to catch some waves after only a few tries. 
14 | 
15 | Maybe a dozen sessions later I'm slowly feeling an urge to surf elsewhere, try different waves. I think at first I mostly thought about it in terms of "bigger", probably because that was the only dimension I understood at that time. 
16 | 
17 | Eventually I ended up surfing another bigger [beach break](https://en.wikipedia.org/wiki/Surf_break#Beach_break) at Plettenberg Bay and a [reef break](https://en.wikipedia.org/wiki/Surf_break#Reef_break) at Buffels Bay. I think I didn't surf a single wave at Plettenberg. The waves were just too high and too steep compared to anything I've surfed before and so I just got wrecked by most of the waves. I still had a great time though. Even if I didn't really succeed, I felt challenged and the prospect of succeeding made me paddle back out again and again. 
18 | 
19 | The dolphins that came by just as the sun began to set really put a cherry on top of that whole afternoon. Quite the magical sight. 
20 | 
21 | A few weeks later I listened to some podcast about speaking and one of the points being made was that your ability to speak freely is very contextual and being able to speak freely in a professional setting does not imply you're able to do the same when hanging out with your partner's friends or similar. They also drew a comparison to Brazilian Jiu-Jitsu and how competing is just such a different context that it requires equal exposure, familiarity and practice as just training in a gym.
22 | 
23 | Listening to this made me realize that surfing in different places, different "breaks" will also make a huge difference to my surfing abilities. I don't want to become a surfer or anything but who doesn't enjoy the feeling of improving and eventually being reasonably good at something. It certainly maps to my bouldering experience. 
24 | 
25 | Cheers to Jaydon for showing me this stuff again. In particular during our last lesson — after having surfed a in a few more places — I understood a lot more about how my position affects things. If a wave isn't very strong shifting your body more to the front will make you go faster and will make it easier to catch the wave. If a wave is fast and strong being further back is important to avoid the nose-dive (of which I did plenty). And sometimes it makes sense to shift the weight around a bit even while you're on the wave. 
26 | 
27 | Anyways, let me know if you wanna surf some time! 😁
```

content/onehundred/2021-04-18-rocket-league.md
```
1 | ---
2 | title: Rocket League
3 | date-published: 2021-04-18T18:47:44.556Z
4 | uuid: d659580c-f51d-42a3-a930-6f14ec470d1d
5 | og-image: /images/selfies/3.jpg
6 | type: onehundred
7 | permalink: /100/rocket-league.html
8 | ---
9 | I've you've talked to me about computer/console games I've probably mentioned Rocket League. 
10 | 
11 | Gaming has been kind of an on-and-off hobby for me since my youth. Sometimes I just didn't have access to a computer that would allow me to play games. Yes, those were the days when I was using Linux. A Mac isn't much better though so I eventually bought a Playstation 4 a few years ago. I played many games I liked but one that I'm still playing on a somewhat regular basis is Rocket League. 
12 | 
13 | I first played what was to become Rocket League on a Playstation 3. Then it was called *Supersonic Acrobatic Rocket-Powered Battle-Cars*. It's essentially soccer with 1-3 rocket powered cars per team. To me it's the perfect adaptation of an old idea (⚽️) to a new medium, online multiplayer games. It can be highly cooperative if you're not playing 1v1 and even if the game mechanics aren't very complex the skill ceiling is high.
14 | 
15 | I have a few friends who I play with every now and then, we log into Discord and on a good day we'll let each other know where we are on the field, when we're going for the ball etc. On a bad day we bump into each other. 😅
16 | 
17 | With one game lasting between 5-8 minutes it can be a nice break-time activity. It's also nice that you can play it cross-platform (Playstation, Xbox, Windows). Anyways, this is my Rocket League appreciation post 😁 Hit me up if you want to play some time!
```

content/onehundred/2021-04-29-making-music-with-a-launchpad.md
```
1 | ---
2 | title: making music with a launchpad
3 | date-published: 2021-04-29T08:46:55.469Z
4 | uuid: 1d7b346d-8ee4-49f1-9e5c-c988c12944b6
5 | og-image: /images/selfies/2.jpg
6 | type: onehundred
7 | permalink: /100/making-music-with-a-launchpad.html
8 | ---
9 | Some time in the beginning of 2020 I got to play on synthesizer of a friend and realized that things can sound pretty cool when you just follow simple instructions like  "press three keys with one key in between each of them". Synthesizers make this fun, all the lack of musical knowledge you can make up for with knowledge of twisting knobs.
10 | 
11 | I bought a used microKORG and had some fun with that, started to try to find my way through the piano keys. Sitting down to practice doesn't come very easy for me though. My best learning "journeys" happen when I'm chasing new knowledge and immediately apply it. Practicing is kind of the opposite, or at least it feels that way to me. 
12 | 
13 | And because of that I never really got comfortable on piano keys. 
14 | 
15 | My most recent attempt to be able to make music is a Launchpad X. These midi controllers have an 8x8 grid and a layout with which you can play notes using that grid. What's great about this layout is that it is "isomorphic": 
16 | 
17 | > 'Isomorphic' controllers are designed so that scales and chords use the same shapes in any key. 
18 | 
19 | Having the ability to switch between scales and modes without losing all of your muscle memory seems great. I also find it easier to move my hands over the square than over the width of a piano. 
20 | 
21 | Sure, it's not the same but do I really care? I just want to make something that sounds nice.
22 | 
23 | I've also been taking some lessons with [Jamie Blake](https://www.youtube.com/channel/UCwDrJn9Jzqaz8fy20EenQFA), who just has a [beautiful way](https://www.youtube.com/watch?v=S9cRau38tu4) of playing his Ableton Push 2 (which is similar to my Launchpad in that it is an isomorphic controller). 
24 | 
25 | Anyone else playing around with this?
```

content/onehundred/2021-05-04-gatheround.md
```
1 | ---
2 | title: Gatheround
3 | date-published: 2021-05-04T18:01:34.293Z
4 | uuid: 6510e162-2a88-47d8-bdea-a60894ab999d
5 | og-image: /images/selfies/10.jpg
6 | type: onehundred
7 | permalink: /100/gatheround.html
8 | ---
9 | Until recently I worked for a company called Icebreaker. Now I work for a company called [Gatheround](https://gatheround.com). After months of behind the scenes work we finally changed our name to something a little more open. Something that better reflects the ambitions behind the product. 
10 | 
11 | Some time last year we started with a list of a dozen or so names. And here we are today.
12 | 
13 | [![screenshot of the Gatheround homepage](/images/uploads/gatheround-homepage-screenshot.png "Gatheround")](https://gatheround.com)
14 | 
15 | [Brenna](https://www.brennajmarketello.com/) came up with a wonderful design system which I got to pour in code. Prior to this rebrand we didn't have much of a design system so having one now is already paying huge dividends in terms of shipping more consistent interfaces.
16 | 
17 | **The migration** was also a fun challenge to tackle. In the end we back-ported some of our new design system to the old Icebreaker brand and used a feature flag whenever that didn't feel appropriate. This approach meant we didn't need to feature-freeze at all and that we would preserve the Git history of our UI code. 
18 | 
19 | We also allowed enabling the feature flag via a URL param which meant that others on the team could review the new brand in preview environments.
20 | 
21 | Last but not least I had quite a bit of fun getting into [react-spring](https://react-spring.io/) for some **animations** on the [homepage](https://gatheround.com). It's been a few years that I seriously looked at React animation stuff and it seems to have come a long way since then. The hooks-based react-spring API is pretty straightforward and since everything happens inside your program (as opposed to CSS transitions) you have much more direct control over sequencing and overall behavior.
22 | 
23 | CSS-in-JS was also something that might have made some things easier (e.g. parameterizing styles, avoiding conflicts) but ultimately it felt a little too risky given that no-one on the team had significant experience using it. 
24 | 
25 | Shout out to everyone who helped make this happen including the many open source projects that we rely on. (Gatheround is in the process of becoming a [ClojuristsTogether](https://www.clojuriststogether.org/) member. 🎉)
```

content/onehundred/2021-05-06-deep-frozen-pizza.md
```
1 | ---
2 | title: Deep frozen pizza
3 | date-published: 2021-05-06T08:35:26.914Z
4 | uuid: ccf1ae3a-254f-4976-9081-60656d4f5214
5 | og-image: /images/selfies/1.jpg
6 | type: onehundred
7 | permalink: /100/deep-frozen-pizza.html
8 | ---
9 | Every now and then I'd order pizza. Maybe it's Sunday, the fridge is empty and you're not really feeling like Sushi or Indian. You're craving the carbs of a nice pizza. Unfortunately almost all the time I'd be disappointed by my decision. Pizza that is in a delivery box for anything between 10-30 minutes just can't be the same as one that's fresh out of the oven. You all know what I'm talking about. 
10 | 
11 | So, the thoughtleader that I am, I hereby proclaim that you shall not order pizza but instead buy frozen pizza. Don't buy anything other than the ones by [Gustavo Gusto](https://gustavo-gusto.de/pizzen/). They are gooood. 🍕 👌
12 | 
13 | <iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/UnAbszcy3bs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
```

content/onehundred/2021-05-20-tools.md
```
1 | ---
2 | title: Tools
3 | date-published: 2021-05-20T23:12:36.395Z
4 | uuid: febe047c-d0b7-4b54-b986-e3f69583dda9
5 | og-image: /images/selfies/6.jpg
6 | type: onehundred
7 | permalink: /100/tools.html
8 | ---
9 | I tend to care a lot about the tools I'm using. Sometimes that results in choices that might be hard to understand from the outside. Clojure is a programming language that even many programmers I meet haven't heard of. Vim is a text editor that requires years of building muscle memory to really feel that extra power it gets you.
10 | 
11 | With both of these there are fundamental differences to their alternatives that seemed worthwhile at the time and still do. Clojure provides a feedback loop like no other language I've used. Vim just helps me looking like "a guy who knows things" in front of my coworkers. We all know it matters.
12 | 
13 | Anyways, I care about tools.
14 | 
15 | I believe the tools we use shape how we work in subtle, yet significant ways. I hope you agree that the logical conclusion to this is that we should always question the tools we use. Do they serve us well? Are they a reflection of how we want to work? How we want to collaborate? Do they feel like a sharp knife or more like a dull one?
16 | 
17 | I've been thinking about this in the context of my own tool-use. I'm writing this in Roam, an app that feels a bit like Vim. It almost looks broken the first few times you look at it but there's something at a lower level that I agree with and for which I'm willing to feel lost every now and then.
18 | 
19 | As a software engineer I use plenty of "tools" every day. Some of my own choosing, some chosen by the group of people I work with. There are certain defaults. Google Docs, Trello, GitHub, Asana &c.
20 | 
21 | I'm writing this because I think it is important to question those defaults. Try new things. Find tools that spark joy and don't underestimate the second order effects of having sharp knives in your kitchen.
22 | 
23 | [“We shape our tools and our tools shape us.”](https://martinklepsch.org/posts/when-we-build-stuff.html) — Marshall McLuhan
```

content/onehundred/2021-07-06-drawing-sixes.md
```
1 | ---
2 | title: drawing sixes
3 | date-published: 2021-07-06T10:12:57.048Z
4 | uuid: 58e4bedf-8f2a-416d-aba4-2c606416c41e
5 | og-image: /images/selfies/14.jpg
6 | type: onehundred
7 | permalink: /100/drawing-sixes.html
8 | ---
9 | There's a guy in my neighborhood following a strange art project. He paints the digit '6' on all kinds of crap that you can find on the street. He paints it pretty much exclusively on things that will disappear again. Trash, poster ads, giveaway boxes, you name it. 
10 | 
11 | I've been a spectator of this for quite some time now, spotting a new six here and there. For a long time I've been very curious about who is behind this. Sometimes I'd see a new six and the paint would still be wet. Then, one time, I even saw him paint a six just a few hundred meters from my home — but I was rushed and didn't stop. 
12 | 
13 | Ultimately there were some other hints and I found out who he was.
14 | 
15 | There's a **14 year old video** [on Vimeo](https://www.are.na/block/12120615) where he talks about his art. Part of the reason for choosing a simple shape like '6' was so that he could draw it while riding his bike. 
16 | 
17 | ![guy painting a 6 from the bike](/images/uploads/bike-6.jpg "guy painting a 6 from the bike")
18 | 
19 | He also makes strange software for keyboard shortcuts on Windows that I don't understand at all.
20 | 
21 | And here I am, struggling to write 100 things. He probably drew tens of thousands of sixes over the years. Even without understanding the motivation, the persistence behind this project is inspiring. 
22 | 
23 | I compiled a [collection of images and links on Are.na](https://www.are.na/martin-klepsch/nk6).
```

content/onehundred/2021-08-01-leave-a-note.md
```
1 | ---
2 | title: leave a note
3 | permalink: /100/leave-a-note.html
4 | date-published: 2021-08-01T17:26:10.111Z
5 | uuid: 40337570-916f-4e0e-bdb6-8af83f8b01dd
6 | og-image: /images/selfies/4.jpg
7 | type: post
8 | ---
9 | My little "write a 100 things" project isn't really coming along all that well. Or maybe it's coming along well, but slowly. I could go full-on introspective now and carefully analyze what I'd need to do to produce more of these little random pieces but I also just came home from camping at a lake for 2 days so I'll defer that and take a hot shower instead. 
10 | 
11 | One thing I'm really enjoying though is the reactions I've been getting. Sometimes it's a conversation with a friend from the past, sometimes it's something new I haven't thought about. Oftentimes this happens on Twitter but, knowing myself, maybe there's more people who are willing to "reply" than those who chose do to so publicly on Twitter. 
12 | 
13 | So before I took off for the lake on Friday I did hack a little bit on *The Most Basic Comment Form* imaginable. You type, you hit enter. Done. The comment is then sent to me via [Pushover](https://pushover.net/). It's not stored and no one else will be able to see it. It's anonymous unless you add your name to the message.
14 | 
15 | It's an expansion of the existing feedback loop and I'm just very curious to see what will happen. Is the internet civil enough for an anonymous comment form? 🙃
```

content/onehundred/2021-08-02-interactive-design-system-docs.md
```
1 | ---
2 | title: interactive design system docs
3 | date-published: 2021-08-02T23:58:51.578Z
4 | uuid: 10080765-1b49-42c1-8a65-0047a1b4cb42
5 | og-image: /images/selfies/14.jpg
6 | type: onehundred
7 | permalink: /100/interactive-design-system-docs.html
8 | ---
9 | Having [worked on design systems](https://martinklepsch.org/100/gatheround.html) for a good amount of time I've also been thinking about design system documentation quite a bit. Storybook is great but it's also [so intertwined with build tools](https://storybook.js.org/tutorials/intro-to-storybook/react/en/get-started/) that it's not really fun to target from ClojureScript. I know some people are doing it but they didn't seem too excited about it. 
10 | 
11 | We ended up just making yet another component which shows all our components. It's basic but it's good enough for our current needs. 
12 | 
13 | Now recently there's been quite a lot of interesting things happening around the [small clojure interpreter (sci)](https://github.com/borkdude/sci), which made me wonder if maybe it could also be used as tool to provide interactive design system / component documentation. 
14 | 
15 | And... it looks like it could be quite nice. `sci` makes it very easy to access any function in your ClojureScript code and after that you don't need much more than a small textarea with an example of using the component and a place to render it.
16 | 
17 | With a bit more work you could probably colocate the examples with the component via macros or metadata. Another macro could be used to fully expose specific namespaces.
18 | 
19 | Seems nice? 
20 | 
21 | ```
22 | (rum/defc Button < rum/static
23 |   [attrs label]
24 |   [:button attrs label])
25 | 
26 | (rum/defc ComponentViewer < rum/static
27 |   []
28 |   (let [[input set-input!] (rum/use-state "(ui/Button {:on-click #(js/alert :hello)} \"hello\")")
29 |         [c set-c!] (rum/use-state nil)]
30 |     (rum/use-effect!
31 |      (fn []
32 |        (set-c!
33 |         (sci/eval-string input
34 |                          {:classes {'js goog/global :allow :all}
35 |                           ;; make the Button component available
36 |                           :namespaces {'ui {'Button Button}}})))
37 |      [input])
38 |     [:div
39 |      [:div c]
40 |      [:textarea {:on-change #(set-input! (.. % -target -value))
41 |                  :default-value input
42 |                  :style {:font-family "monospace"}}]]))
43 | ```
44 | 
45 | <blockquote class="twitter-tweet"><p lang="en" dir="ltr">Tiny demo GIF of rendering a ClojureScript React component via `sci` and updating it from the same page. <a href="https://t.co/RZuKQjlZ1F">pic.twitter.com/RZuKQjlZ1F</a></p>&mdash; Martin Klepsch (14/100 posts) (@martinklepsch) <a href="https://twitter.com/martinklepsch/status/1422473867663192121?ref_src=twsrc%5Etfw">August 3, 2021</a></blockquote> <script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>
```

content/onehundred/2021-08-20-the-cljdoc-contributions-strategy.md
```
1 | ---
2 | title: the cljdoc contributions strategy
3 | date-published: 2021-08-20T14:04:06.667Z
4 | uuid: 4f639b1f-7e97-467c-be84-73b5589f80e5
5 | og-image: /images/selfies/10.jpg
6 | type: onehundred
7 | permalink: /100/the-cljdoc-contributions-strategy.html
8 | ---
9 | Since [cljdocs](https://martinklepsch.org/100/cljdoc-origins-1-n.html) [inception](https://martinklepsch.org/100/cljdoc-origins-2-n.html) I've always been very eager to make it easy for folks to contribute. I wrote almost [two dozen Architecture Decision Records](https://github.com/cljdoc/cljdoc/tree/master/doc/adr) (ADR) hoping it would help future contributors navigate the codebase. I encouraged people to contribute and tried to spec out smaller "good first issue" type contributions so folks could start with something small. 
10 | 
11 | Once people started contributing I of course wanted to encourage them to stick around and help maintain the project. Since there's so many parts to maintaining a project like this even small non-code contributions like participating in a discussion or reviewing a PR can be tremendously helpful. 
12 | 
13 | To give a sense of ownership I added anyone who contributed as a contributor on GitHub and gave them push access to the repository. The understanding was that we'd still use PRs for most contributions but it did remove me as a primary gatekeeper. Any previous contributor is empowered merge a PR. I think the first time I read about this approach as in Felix Geisendörfers [Pull Request Hack](https://felixge.de/2013/03/11/the-pull-request-hack/) article. 
14 | 
15 | As the project grew we added continuous deployment. Any commits to the main branch are automatically deployed. It might seem a bit wild to just let anyone deploy to cljdoc.org but it works for this project.
16 | 
17 | cljdoc isn't a library. It doesn't have an API. We can just let people improve it in ways they see fit. Sure maintainability is still a concern, but that is much less challenging than maintaining backwards compatible APIs or just designing a good API in the first place. Historically I think most contributions have actually improved the codebase.
18 | 
19 | I'm really happy I'm not maintaining a popular library with lots of contributors. I'm helping maintain a thing where people can play around and explore their ideas. Where it's ok to make a mistake. We're all here to learn. I learned so much from creating cljdoc and I couldn't be happier that others can do the same. 
20 | 
21 | Shoutout to Lee Read who prompted these reflections. 🙌
```

content/onehundred/2021-08-25-a-blockchain-usecase.md
```
1 | ---
2 | title: a blockchain usecase
3 | date-published: 2021-08-25T23:44:42.623Z
4 | uuid: 983fcaac-c4c5-489c-a73f-c7247519942c
5 | og-image: /images/selfies/14.jpg
6 | type: onehundred
7 | permalink: /100/a-blockchain-usecase.html
8 | ---
9 | yes, I know — but bear with me here for a second...
10 | 
11 | Yesterday I was at a small birthday gathering. We all had some chili and talked about this and that. One of my friends works at a private art collection that had recently gifted a large chunk of it to another institution. The process of gifting all these art pieces **took five years**. 
12 | 
13 | A few months ago I watched a documentary on Netflix about one of the biggest cases of art forgery in history, called *Made You Look*. Whenever high-end art is sold it goes through a process in which the various owners and locations of the piece are verified, which is also referred to as provenance. For many art pieces there already is a good record. In the case depicted in the movie someone fabricated that record and convinced a gallery owner that it is valid, causing some $80m in damages.
14 | 
15 | Now, have a guess why the process of gifting the collection took so long. 
16 | 
17 | It took so long because proving provenance is hard. These art pieces haven't changed hands in decades, the paperwork proving such transactions is in a binder somewhere down in the archive or in some database used by the collections stewards. Every time a piece changes hands this information needs to be gathered and packaged in a way so that it can then be imported into someone else's database alongside the various documents proving the legitimacy of the transaction. 
18 | 
19 | The longer the time between transactions the harder it will be to verify a paintings records due to database changes, lost files and people dying. If you write it into a blockchain and verify a transactions validity at the time of the transaction it would be trivial to verify it at any time in the future. It's verified forever basically. 
20 | 
21 | Add on top that large private collections, museums and even private collectors would operate on the same database making the exchange of information significantly easier in situations like the large gifting my friend helped with.
22 | 
23 | I don't have a horse in this race really but I think it's undeniable that there's *some innovation* in the basic concept of a blockchain. Plus, it's just fun to think about an application like that. It's not going to magically solve all problems in this space but it does seem to fix some of the more annoying ones.
24 | 
25 | Oh, and of course there's a platform doing just this: <https://www.artory.com/>.
```

content/onehundred/2021-08-25-luma-gets-it.md
```
1 | ---
2 | title: luma gets it
3 | date-published: 2021-08-25T11:01:48.696Z
4 | uuid: a163c594-b1a7-4204-b03f-c23b42a841d6
5 | og-image: /images/selfies/4.jpg
6 | type: onehundred
7 | permalink: /100/luma-gets-it.html
8 | ---
9 | I really love [Luma](https://lu.ma/). It fits so well into my internal narrative about how to build a business/audience/community.
10 | 
11 | ### Email is king
12 | 
13 | Twitter is all fun and games but if you want to build something more sustainable (a community or even a business) building an email list is the way to go. Substack rode that wave for quite some time now and yeah, there's not much else to say. Email is king.
14 | 
15 | ###  Selling something from the start
16 | 
17 | Again, if you want to build up something as a sustainable part of your live a good way to support that is to figure out a way to make money from it. Luma lets you sell event tickets from the start and supports flexible prices (i.e. suggested/minimum). The integration with Stripe is so well done, it took me like 10 minutes max. 
18 | 
19 | ### A slick package
20 | 
21 | Besides the above it supports a bunch more functionality that just makes it a "great first website" type thing. You can publish updates, add links to other websites (a la linktree), you get some basic analytics about how many people visit and subscribe to updates. 
22 | 
23 | The visual design just feels great and you can tell they want it to be fun to use.
24 | 
25 | ### The store
26 | 
27 | Right now the whole product is really built around running events but I wonder if that's their "end goal". There's a store that you can set up. The only thing you can sell is credit packs which is basically discounted event tickets. That makes sense in their event-focused model but it's not far off to just turn this into a basic store for digital goods a la Gumroad as well. 
28 | 
29 | \--- 
30 | 
31 | All these things in such a tight, easy to use package. You start with the email list, you run some free events and if there's more interest it's trivial to involve money. Their pricing makes so much sense too, free with 5% platform fee and some limits or $39 with no platform fee. It all kind of adds up for me 😅
```

content/onehundred/2021-09-17-luma-newsletter-bouquet.md
```
1 | ---
2 | title: Luma Newsletter No. 1
3 | date-published: 2021-11-15T09:51:54.998Z
4 | uuid: 4781b69d-da73-42a3-be8f-d1e6d1e33d4f
5 | og-image: /images/selfies/9.jpg
6 | type: onehundred
7 | permalink: /100/luma-newsletter-bouquet.html
8 | ---
9 | 
10 | > I'm not really sure why but in the spirit of exploration I started a kind of irregular newsletter. You can read [the first edition here](https://lu.ma/p/MnWMR5rLJHDcWL5/Martin-says-Welcome) and it's also in full below. Since I started this project here of writing 100 things I decided to be generous with myself and I'll also count any newsletters I'm sending.
11 | 
12 | Hello and welcome dear friends,
13 | 
14 | you probably subscribed to this list about a month ago when [I mentioned on Twitter](https://twitter.com/martinklepsch/status/1428333611992993795) that I'd like to do a stream during which I try to build and ship a small ClojureScript app including a backend and database. Time has come: I'll stream this on Twitch next Tuesday 11AM Berlin time. You can register for it [here](https://lu.ma/hackers-cljs).
15 | 
16 | What follows is a random assortment of things™
17 | 
18 | Subscription layers (and time)
19 | -------------------------------
20 | 
21 | I've been thinking about subscription layers in frontend apps quite a bit. In an earlier library of mine ([derivatives](https://github.com/martinklepsch/derivatives)) I essentially followed a "watch-based approach", meaning a "derivative" would recompute whenever any of it's inputs change. Now in the projects I work on the changes or often much more granular and suitable for a more reduce-based approach that processes individual change events.
22 | 
23 | This reduce-based approach is something I'm intending to explore more.
24 | 
25 | Another interesting challenge I've come across is that sometimes I have an entity with a state like `active?` that really depends on the current time. Most subscription layers don't really expose anything to ensure that the subscription is recomputed the moment `active? `should change. The best idea I have right now is to schedule a "touch" event that forces a rebuild of the subscription but I haven't really explored an implementation of that yet.
26 | 
27 | Cycle time
28 | -----------
29 | 
30 | Yesterday I listened to [The REPL with Paulus Esterhazy](https://www.therepl.net/episodes/40/) and it made me once again aware of the importance of cycle time in software development. One of the core metrics Paulus mentions is the time between first commit on a feature branch to production deploy.
31 | 
32 | A lot of the conversation reminded me of [Theory of Constraints](https://www.notion.so/rostnl/Systems-Thinking-Notes-Resources-2871c456284b47388b7a76d47521038c#2c3b8ac9282848f9b5506134c38202d9) (some notes by Robert Stuttaford) and how to optimize for throughput you have to tackle bottlenecks first.
33 | 
34 | I'm hoping that over the next months I can explore this some more and put some metrics in place for the product team at [Gatheround](https://gatheround.com/).
35 | 
36 | Retrospectives
37 | ---------------
38 | 
39 | Through the above podcast I also discovered the podcast by GeePaw Hill and in particular the one about how for retrospectives, [variety is key](https://www.geepawhill.org/2020/08/04/retrospectives-variety-is-key/). I've been running [POPCORN Flow](https://www.youtube.com/watch?v=cqtxMy58kz8)-inspired retros with our engineering team for a while now but I'm occasionally missing a bit of energy in the room (zoom) and so this podcast was a welcome invitation to experiment with more different format and cycle hosts.
40 | 
41 | > The goal here: decenter ourselves just the right amount for dynamic, creative discourse about who & how we were --- and who & how we wish to be.
42 | 
43 | Can trees talk?
44 | ----------------
45 | 
46 | Last but not least a video by the amazing Real Science YouTube channel about [how trees communicate using fungal networks](https://www.youtube.com/watch?v=9HiADisBfQ0). It goes into the details behind various experiments that were used to prove that some information flow is happening between trees.
47 | 
48 | In one of the experiments one of three plants was infected with a pest and the two other started producing enzymes to defend against the pest within 6 hours. But check it out yourself, it's pretty cool!
49 | 
50 | Ok, that's it. Happy Friday y'all.
```
