(ns clj-loga.core
  (:require [cheshire.core :refer [generate-string]]
            [clojure.string :refer [join upper-case]]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre :refer [errorf info merge-config!]]))

(def ^:private ^:dynamic _tag nil)

(defmacro set-log-tag
  "Sets a tag, which is appended to the log event."
  [tag* & body]
  `(binding [_tag ~tag*]
     ~@body))

(defmacro wrap-operation-with-log
  [m & body]
  `(let [operation# (:operation ~m)
         pre-log-msg# (:pre-log-msg ~m)
         post-log-msg# (:post-log-msg ~m)]
    (info (str (or pre-log-msg# "started: ") operation#))
    (let [result# ~@body]
      (info (str (or post-log-msg# "finished: ") operation#))
      result#)))

(defmacro operation-log-wrapper
  "Wrap function body with log before and after its execution.
  Tag is applied if present.
  - required keys: operation
  - optional keys: pre-log-msg, post-log-msg and tag"
  [m & body]
  `(if-let [tag# (:tag ~m)]
     (set-log-tag tag# (wrap-operation-with-log ~m ~@body))
     (wrap-operation-with-log ~m ~@body)))

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
  (setup-loga)
  (timbre/info "Log it out.")
  (set-log-tag "smart-tag"
           (timbre/info "Log it tagged."))
  (timbre/error (Exception. "Something went wrong"))
  (operation-log-wrapper {:operation "processing message" :tag "some-tag"}
                         (do (prn "all the work happening now") "return value"))
  )
