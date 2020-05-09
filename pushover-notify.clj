(def r "https://martinklepsch.org/")

(def msg
  (->> (for [op [:uploaded :updated :deleted]
             file (get *input* op)]
         (str (name op) " " r file))
       (str/join "\n")))

(curl/post "https://api.pushover.net/1/messages.json"
           {:form-params {"token" (System/getenv "PUSHOVER_TOKEN")
                          "user" (System/getenv "PUSHOVER_USER")
                          "message" (if (seq msg) msg "Deploy ran, no changes")}} )
