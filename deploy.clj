(require '[confetti.s3-deploy :as s3]
         '[clojure.java.io :as io])

(def creds
  {:access-key (System/getenv "AWS_ACCESS_KEY")
   :secret-key (System/getenv "AWS_SECRET_KEY")})

(defn sync! []
  (s3/sync!
    creds
    (System/getenv "S3_BUCKET_NAME")
    (s3/dir->file-maps (io/file "_site"))
    {:dry-run? false
     :report-fn (fn [{:keys [s3-key op]}]
                  (binding [*out* *err*]
                    (println op s3-key)))}))

(let [sync-results (sync!)
      inv-paths (->> (mapcat sync-results [:uploaded :updated :deleted])
                     (map #(str "/" %)))]
  (when (seq inv-paths)
    (binding [*out* *err*]
      (println "Invalidating" inv-paths))
    (s3/cloudfront-invalidate! creds (System/getenv "CLOUDFRONT_ID") inv-paths))
  (prn (assoc sync-results :unchanged :hidden)))
