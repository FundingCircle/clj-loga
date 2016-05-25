(ns clj-loga.core-test
  (:require [cheshire.core :refer [parse-string]]
            [clj-loga.core :as loga :refer :all]
            [clj-loga.support.logging-helpers :refer :all]
            [clojure.test :refer :all]
            [clojure.string :as s]
            [taoensso.timbre :as timbre]))

(def expected-default-tags [:timestamp :level :message :namespace])

(def obfuscated-key :password)

(defn- log-to-atom [f]
  (setup-loga :obfuscate [obfuscated-key] :level :trace)
  (timbre/merge-config! atom-appender)
  (f))

(defn- contains-expected-tags? [event]
  (every? #(get (parse-string event) (name %)) expected-default-tags))

(deftest setup-loga-test
  (testing "formats log event to contain desired default tags"
    (reset-log-events)
    (timbre/info "dummy log")
    (is (true? (contains-expected-tags? (latest-log-event)))))
  (testing "obfuscates specific keys"
    (reset-log-events)
    (timbre/info {:bar "baz" :password "secret"})
    (is (= anonym-string (:password (read-string (get-log-element (latest-log-event) "message")))))))

(deftest set-log-tag-test
  (testing "appends tag to the log event"
    (reset-log-events)
    (let [tag (uuid)]
      (set-log-tag tag (timbre/info "A tagged dummy event"))
      (is (= tag (get-log-element (latest-log-event) "tag"))))))

(deftest log-wrapper-test
  (testing "logs 2 messages around the operation"
    (reset-log-events)
    (log-wrapper {:operation "a-operation"} "result")
    (is (= 2 (count @log-events))))
  (testing "tags log messages"
    (reset-log-events)
    (let [tag (uuid)]
      (log-wrapper {:operation "a-operation" :tag tag} "result")
      (is (every? #(= tag %) (map #(get-log-element % "tag")  @log-events)))))
  (testing "applies custom pre-log message"
    (reset-log-events)
    (let [msg (uuid)]
      (log-wrapper {:operation "a-operation" :pre-log-msg msg} "result")
      (is (.contains (get-log-element (earliest-log-event) "message") msg))))
  (testing "applies custom post-log message"
    (reset-log-events)
    (let [msg (uuid)]
      (log-wrapper {:operation "a-operation" :post-log-msg msg} "result")
      (is (.contains (get-log-element (latest-log-event) "message") msg))))
  (testing "returns result of wrapped body"
    (reset-log-events)
    (let [expected-result (uuid)
          result (log-wrapper {:operation "a-operation"} expected-result)]
      (is (= result expected-result)))))

(deftest append-stacktrace-test
  (reset-log-events)
  (let [ex-message (uuid)
        ex-data {:cause (uuid)}
        message (uuid)
        e (ex-info ex-message ex-data)
        _ (loga/error e message)]
    (testing "logs stacktrace"
      (is (some? (:stacktrace (latest-log-event-map)))))
    (testing "logs message"
      (is (= message (:message (latest-log-event-map)))))
    (testing "logs ex-message"
      (is (= (.toString e) (:exception-message (latest-log-event-map)))))
    (testing "logs ex-data"
      (is (= ex-data (:exception-data (latest-log-event-map)))))))

(deftest appends-error-message-to-message
  (reset-log-events)
  (let [_ (loga/error (Exception.))]
    (testing "logs message"
      (is (= "java.lang.Exception" (:message (latest-log-event-map)))))))

(deftest log-test
  (reset-log-events)
  (let [level :info
        message (uuid)
        _  (loga/log level message)]
    (testing "sets info log level"
      (is (= (:level (latest-log-event-map)) (-> level name s/upper-case))))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map)) message)))))

(deftest info-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/info message)]
    (testing "sets info log level"
      (is (= (:level (latest-log-event-map)) "INFO")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

(deftest trace-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/trace message)]
    (testing "sets trace log level"
      (is (= (:level (latest-log-event-map)) "TRACE")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

(deftest debug-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/debug message)]
    (testing "sets debug log level"
      (is (= (:level (latest-log-event-map)) "DEBUG")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

(deftest warn-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/warn message)]
    (testing "sets warn log level"
      (is (= (:level (latest-log-event-map)) "WARN")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

(deftest error-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/error message)]
    (testing "sets error log level"
      (is (= (:level (latest-log-event-map)) "ERROR")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

(deftest fatal-test
  (reset-log-events)
  (let [message (uuid)
        _ (loga/fatal message)]
    (testing "sets error log level"
      (is (= (:level (latest-log-event-map)) "FATAL")))
    (testing "emmit the log message"
      (is (= (:message (latest-log-event-map) message))))))

  (use-fixtures :each log-to-atom)
