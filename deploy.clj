(require '[confetti.s3-deploy :as s3]
         '[clojure.java.io :as io])

(defn sync! []
  (s3/sync!
    {:access-key (System/getenv "AWS_ACCESS_KEY")
     :secret-key (System/getenv "AWS_SECRET_KEY")}
    (System/getenv "S3_BUCKET_NAME")
    (s3/dir->file-maps (io/file "_site"))
    {:dry-run? true
     :report-fn (fn [{:keys [s3-key op]}]
                  #_(println op s3-key))}))

(prn (sync!))
