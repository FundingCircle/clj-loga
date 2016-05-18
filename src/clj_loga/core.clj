(ns clj-loga.core
  (:require [cheshire.core :refer [generate-string]]
            [clj-loga.hooks
             :refer
             [format-pre-log-msg
              get-namespaces-from-list
              select-loga-keys
              target-functions-from-namespaces]]
            [clojure
             [string :refer [join upper-case]]
             [walk :refer [postwalk]]]
            [environ.core :refer [env]]
            [robert.hooke :refer [add-hook]]
            [taoensso.timbre :as timbre :refer [merge-config!]]))

(def ^:private ^:dynamic _tag nil)

(def anonym-string "[FILTERED]")

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
     (if (fn? pre-log-msg#)
       (do (pre-log-msg#) (timbre/info (or operation# "")))
       (timbre/info (str (or pre-log-msg# "started: ") (or operation# ""))))
     (let [result# ~@body]
       (timbre/info (str (or post-log-msg# "finished: ") (or operation# "")))
       result#)))

(defmacro log-wrapper
  "Wrap function body with log before and after its execution.
  Tag is applied if present.
  - optional keys:
    - tag - to tag log events
    - pre-log-msg - custom log message before body execution
    - :post-log-msg - custom log message after body execution
    - :operation - descriptive name for the wrapped forms"
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

(defn- append-ex-data [m e]
  (if (instance? clojure.lang.ExceptionInfo e)
    (assoc m :exception-data (ex-data e))
    m))

(defn- append-ex-defaults [m e opts]
  (assoc m
         :stacktrace (str (format-stacktrace e opts))
         :exception-message (str (.toString e))))

(defn- append-stacktrace* [{:keys [?err_ opts]} m]
  (if-not (:no-stacktrace? opts)
    (when-let [err (force ?err_)]
      (-> m
       (append-ex-defaults err opts)
       (append-ex-data err)))))

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
  (timbre/errorf throwable "Uncaught exception on thread: %s"
          (.getName thread)))

(defn- output-fn
  ([data] (output-fn nil data))
  ([opts data]
   (let [formatted-log (format-log-event data)]
     (try
       (->> formatted-log
            (append-stacktrace data)
            append-tag
            generate-string)
       (catch Exception e
         (format "Unable to process log event due to error %s. Log: %s" (.getMessage e) formatted-log))))))

(defn- loga-enabled? []
  (= (env :enable-loga) "true"))

(defn obfuscate-key [m key-to-obfuscate]
  (if (contains? m key-to-obfuscate)
    (assoc m key-to-obfuscate anonym-string)
    m))

(defn obfuscate-vector-key [m key-to-obfuscate]
  (if (and (= (count m) 2) (= key-to-obfuscate (first m)))
    [key-to-obfuscate anonym-string]
    m))

(defn obfuscate-data [data keys-to-obfuscate]
  (postwalk
   (fn [element]
     (cond
       (map? element) (reduce obfuscate-key element keys-to-obfuscate)
       (vector? element) (reduce obfuscate-vector-key element keys-to-obfuscate)
       :else element))
   data))

(defn set-loga-hooks [namespaces]
  (doseq [function (-> namespaces
                       get-namespaces-from-list
                       target-functions-from-namespaces)]
    (add-hook function :loga-hook
              (fn [f & args]
                (let [fn-metada (meta function)
                      meta-args (-> fn-metada select-loga-keys (format-pre-log-msg args))]
                  (log-wrapper meta-args (apply f args)))))))

(defn setup-loga
  "Initialize formatted logging."
  [& {:keys [level namespaces obfuscate]
      :or {level :info namespaces [:all] obfuscate []}}]
  (if (loga-enabled?)
    (do (timbre/handle-uncaught-jvm-exceptions!)
        (set-loga-hooks namespaces)
        (merge-config! {:middleware [(fn [{:keys [vargs_] :as data}]
                                       (assoc data :vargs_ (delay (obfuscate-data @vargs_ obfuscate))))]
                        :output-fn output-fn
                        :timestamp-opts iso-timestamp-opts
                        :level level}))
    (timbre/info "Skipping custom log formatter.")))

(defmacro log
  [& args]
  `(timbre/log ~@args))

(defmacro trace
  [& args]
  `(timbre/trace ~@args))

(defmacro debug
  [& args]
  `(timbre/debug ~@args))

(defmacro info
  [& args]
  `(timbre/info ~@args))

(defmacro warn
  [& args]
  `(timbre/warn ~@args))

(defmacro error
  [& args]
  `(timbre/error ~@args))

(defmacro fatal
  [& args]
  `(timbre/fatal ~@args))

(comment
  (log :info "message")
  (trace "message")
  (setup-loga :obfuscate [:password] :level :debug)
  (setup-loga :level :debug)
  (timbre/info "Log event with params" {:password "secret" :bar "baz" :sub {:password "secret" :foo "bar"}})
  (set-log-tag
   "smart-tag"
   (timbre/info "Log it tagged.")
   (future (timbre/info "furure log")))
  (timbre/error (Exception. "Something went wrong") "error")
  (log-wrapper {:operation "processing message" :tag "some-tag"}
               (do (prn "all the work happening now") "return value"))
  (log-wrapper {:pre-log-msg "started processing kafka message" :post-log-msg "finished processing kafka message" :tag "message id"}
               (do (prn "all the work happening now") "return value")))
