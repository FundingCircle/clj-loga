(ns clj-loga.core
  (:require [taoensso.timbre :refer [errorf merge-config!] :as timbre]
            [dire.core :refer [with-wrap-hook! with-pre-hook! with-post-hook!]]
            [clojure.string :refer [upper-case join]]
            [cheshire.core :refer [generate-string]]
            [clj-time.format :as f]
            [environ.core :refer [env]]))

(def ^:private ^:dynamic _tag nil)

(defmacro set-tag
  "Sets a tag, which is appended to the log event."
  [tag* & body]
  `(binding [_tag ~tag*] ~@body))

(defn- get-tag []
  "Gets current _tag"
  _tag)

(defn- append-tag [m]
  (if _tag (assoc m :tag _tag) m))

(defn- format-stacktrace [err opts]
  (format (join "\n " (map str (.getStackTrace err)))))

(defn- append-stacktrace* [{:keys [?err_ opts]} m]
  (if-not (:no-stacktrace? opts)
    (when-let [err (force ?err_)]
             (assoc m :stacktrace (str (format-stacktrace err opts))))))

(defn- append-stacktrace [data m]
  "If stacktrace is present in data, returns log event with stacktrace
   based on the options opts"
  (if-let [log-with-stack-trace (append-stacktrace* data m)]
    log-with-stack-trace
    m))

(defn- format-log-event
  [{:keys [level ?err_ vargs_ msg_ ?ns-str hostname_ timestamp_ ?line]}]
  {:timestamp @timestamp_
   :level (upper-case (name level))
   :message @msg_
   :namespace ?ns-str})

(def ^:private iso-timestamp-opts
  "Controls (:timestamp_ data) to return ISO 8601 formatted time"
  {:pattern "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" ;1997-07-16T19:20:30.45
   :locale (java.util.Locale. "en")
   :timezone (java.util.TimeZone/getDefault)})

(defn- exception-handler [throwable ^Thread thread]
  (errorf throwable "Uncaught exception on thread: %s"
          (.getName thread)))

(defn- output-fn
  ([data] (output-fn nil data))
  ([opts data]
   (->> data
        format-log-event
        (append-stacktrace data)
        append-tag
        generate-string)))

(def ^:private loga-enabled?
  (= (env :enable-loga) "true"))

(defn setup-loga []
  "Initialize formatted logging."
  (if loga-enabled?
    (do (timbre/handle-uncaught-jvm-exceptions!)
        (merge-config! {:output-fn output-fn
                        :timestamp-opts iso-timestamp-opts}))
    (timbre/info "Skipping custom log formatter.")))

(comment
  (init-logging)
  (timbre/info "Log it out.")
  (set-tag "smart-tag"
           (timbre/info "Log it tagged."))
  (timbre/error (Exception. "Something went wrong")))
