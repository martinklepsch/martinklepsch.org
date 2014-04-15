(ns mkl.core
  (:import [goog.dom query])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as s]
            [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(extend-type js/NodeList
    ISeqable
    (-seq [nl] (array-seq nl 0)))

(extend-type js/HTMLCollection
    ISeqable
    (-seq [nl] (array-seq nl 0)))

(defn transform-to-new-format [link]
  (let [path   (s/split link #"/")
        slug   (last path)
        title  (s/lower-case slug)
        target (str "/posts/" title ".html")]
    target))

(defn add-find-link! []
  (let [a (aget (query "[data-behave='find'") 0)
        path (aget js/window "location" "pathname")]
    (when a (set! (.-href a) (transform-to-new-format path)))))

(add-find-link!)

(defn listen [tagsel ev]
  (let [out (chan)
        els  (.getElementsByTagName js/document tagsel)]
    (doseq
      [el els]
      (events/listen el ev
        (fn [e] (put! out e))))
    out))

;; Link coloring magic

(defn random-color []
  (str \# (.toString (rand-int 16rFFFFFF) 16)))

(defn colorize [el]
  (let [styles (.-style el)
        color  (random-color)]
    (set! (.-color styles) color)
    (set! (.-borderColor styles) color)))

(let [hovers (listen "a" "mouseover")
      leaves (listen "a" "mouseleave")]
  (go (while true
        (colorize (.-target (<! hovers)))
        (.removeAttribute (.-target (<! leaves)) "style"))))
