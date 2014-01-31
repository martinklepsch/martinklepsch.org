(defproject mkl "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["_src"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["_src"]
              :compiler {
                :output-to "cljs-dev/main.js"
                :output-dir "cljs-dev"
                :optimizations :none
                :source-map true}}

             {:id "production"
              :source-paths ["_src"]
              :compiler {
                :output-to "cljs-production/main.js"
                :output-dir "cljs-production"
                :optimizations :advanced }}]})
