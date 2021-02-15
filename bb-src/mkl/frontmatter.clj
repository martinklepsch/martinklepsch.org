(ns mkl.frontmatter
  (:require [mkl.pods]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clj-yaml.core :as yaml]
            [pod.retrogradeorbit.bootleg.glob :as glob])
  (:import (java.util UUID)
           (java.time Instant)
           (java.util Date)))

(defn slug [file]
  (let [path (.getPath file)
        filename (.getName file)]
    (cond
     (or (.contains path "posts")
         (.contains path "onehundred"))
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
  (cond
   (.startsWith (.getPath file) "content/posts")
   (str "/posts/" (slug file) ".html")

   (.startsWith (.getPath file) "content/onehundred")
   (str "/100/" (slug file) ".html")

   :else
   (str "/" (slug file) ".html")))

(defn entry-type [file]
  (cond
   (.startsWith (.getPath file) "content/posts") "post"
   (.startsWith (.getPath file) "content/onehundred") "onehundred"))

(defn day-str [d]
  (subs (.format java.time.format.DateTimeFormatter/ISO_INSTANT (.toInstant d)) 0 10))

(def yaml-head
  #"---\h*\r?\n")

(defn file-contents [f]
  (let [[_ yml content] (str/split (slurp f) yaml-head 3)]
    [yml content]))

(def selfies
  (->> (file-seq (io/file "resources/public/images/selfies/"))
       (filter #(.isFile %))
       (map #(str/replace % #"^resources/public/" "/"))))

(defn get-frontmatter [f]
  (let [[yml _] (file-contents f)]
    (yaml/parse-string yml)))

(defn update-frontmatter! [f]
  (let [[yml content] (file-contents f)
        frontmatter (yaml/parse-string yml)
        updated (cond-> frontmatter
                  (and (nil? (:date-published frontmatter))
                       (not (:draft frontmatter)))
                  (assoc :date-published (Date.))

                  (or (nil? (:uuid frontmatter))
                      (str/blank? (:uuid frontmatter)))
                  (assoc :uuid (str (UUID/randomUUID)))

                  (nil? (:og-image frontmatter))
                  (assoc :og-image (rand-nth selfies))

                  (nil? (:type frontmatter))
                  (assoc :type (entry-type f))

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

(def post-files
  (shuffle
    (into (glob/glob "content/onehundred/*.md")
          (glob/glob "content/posts/*.md"))))

(defn -main []
  (doseq [f post-files]
    (println f)
    (update-frontmatter! (io/file f))))
