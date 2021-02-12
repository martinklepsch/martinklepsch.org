(ns mkl.transform
  (:require [mkl.pods]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [pod.retrogradeorbit.bootleg.utils :as utils]
            [pod.retrogradeorbit.bootleg.markdown :as markdown]
            [pod.retrogradeorbit.net.cgrand.enlive-html :as enlive]
            [pod.retrogradeorbit.bootleg.html :as html]
            [pod.retrogradeorbit.bootleg.glob :as glob]))

(defn transform [doc {:keys [output-format transform]}]
  )

(def test-f "test-post.pretty.html")

(defn as-hiccup-seq
  "Convert any input, including filenames `s` to :hiccup-seq"
  [s]
  (if (and (string? s)
           (.exists (io/file s)))
    (html/html s) ;path
    (utils/convert-to s :hiccup-seq)));other input

(defn update-test-file [hiccup-seq]
  (let [prev-f (str test-f ".prev")]
    (spit prev-f (slurp test-f))
    (spit test-f (utils/convert-to hiccup-seq :html))
    (shell/sh "npx" "prettier" "--write" test-f)
    (prettier-diff)
    (if (= (slurp test-f) (slurp prev-f))
      (println "Files seem identical")
      (println "FILE CHANGED"))))

;; FIXME currently `transform` doesn't seem to be working
;; https://github.com/retrogradeorbit/bootleg/pull/68
(defn enlive-at* [node-or-nodes rules]
  (reduce (fn [nodes [s t]]
            (prn s t)
            (enlive/transform nodes (if (enlive/static-selector? s) (enlive/cacheable s) s) t))
          (enlive/as-nodes node-or-nodes)
          rules))

(enlive/at hs [:title] nil)
(enlive-at* hs [[[:title] (fn [n] (prn n) nil)]])
(do
  (def s [:title])
  (def t nil)
  (def hs (as-hiccup-seq test-f))
  (=
   (-> hs
       (enlive/as-nodes)
       (enlive/transform (if (enlive/static-selector? s) (enlive/cacheable s) s) t))
   (enlive-at* hs [[s t]])
   (enlive/at hs s t))

  )
