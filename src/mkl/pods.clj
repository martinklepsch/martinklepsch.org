(ns mkl.pods
  "Side effecting namespace that mostly makes sure that we have loaded the
  bootleg pod."
  (:require [babashka.pods :as pods]))

(let [loaded? (volatile! false)]
  (when-not @loaded?
    (pods/load-pod "bootleg")
    (vreset! loaded? true)))
