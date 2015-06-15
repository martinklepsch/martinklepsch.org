(set-env!
 :source-paths    #{"src" "_posts"}
 :resource-paths  #{"resources"}
 :dependencies '[[jeluard/boot-notify "0.1.1" :scope "test"]
                 [pandeiro/boot-http  "0.6.3-SNAPSHOT" :scope "test"]
                 [hiccup "1.0.5"]
                 [perun "0.1.0-SNAPSHOT"]])

(require '[jeluard.boot-notify :refer [notify]]
         '[pandeiro.boot-http  :refer [serve]]
         '[io.perun            :as    p]
         '[org.martinklepsch.blog :as blog])

(defn renderer [data] (:title data))

(deftask build
  "Build blog."
  []
  (comp (p/markdown)
        ;; (draft)
        ;; (ttr)
        (p/permalink)
        (p/render :renderer renderer)
        (p/collection :renderer blog/index-render :page "index.html")))
