(import '(java.util UUID)
        '(java.time Instant)
        '(java.util Date))

(defn slug [file]
  (let [path (.getPath file)
        filename (.getName file)]
    (cond
      (.contains path "posts")
      (->> (str/split filename #"[-\.]")
           (drop 3)
           drop-last
           (str/join "-")
           str/lower-case)

      (.pages path "pages")
      (->> (str/split filename #"[-\.]")
           drop-last
           (str/join "-")
           str/lower-case)

      :else (throw (ex-info "Could not build slug" {:file file})))))

(defn permalink-fn [file]
  (if (.startsWith (.getPath file) "content/posts")
    (str "/posts/" (slug file) ".html")
    (str "/" (slug file) ".html")))

(defn day-str [d]
  (println d)
  (subs (.format java.time.format.DateTimeFormatter/ISO_INSTANT (.toInstant d)) 0 10))

(def yaml-head
  #"---\r?\n")

(defn file-contents [f]
  (let [[_ yml content] (str/split (slurp f) yaml-head 3)]
    [yml content]))

(defn update-frontmatter! [f]
  (let [[yml content] (file-contents f)
        frontmatter (yaml/parse-string yml)
        updated (cond-> frontmatter
                  (and (nil? (:date-published frontmatter))
                       (not (:draft frontmatter)))
                  (assoc :date-published (Date.))

                  (nil? (:uuid frontmatter))
                  (assoc :uuid (str (UUID/randomUUID)))

                  (nil? (:permalink frontmatter))
                  (assoc :permalink (permalink-fn f)))]
    (when (not= frontmatter updated)
      (println "Frontmatter changed" updated)
      (spit f
            (str "---\n"
                 (yaml/generate-string updated :dumper-options {:flow-style :block})
                 "---\n"
                 content)))))

(comment
  (def f (io/file "content/posts/2012-01-28-fix-broken-decoding-of-mail-subjects-in-exim.markdown"))

  (str/split (slurp f) #"---\r?\n" 3)
  (update-frontmatter! f)
  )

(doseq [f *input*]
  (println f)
  (update-frontmatter! (io/file f)))
