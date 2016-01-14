(ns clj-loga.core-test
  (:require [cheshire.core :refer [parse-string]]
            [clj-loga.core :refer :all]
            [clj-loga.hooks :as h]
            [clj-loga.support.logging-helpers :refer :all]
            [clojure.test :refer :all]
            [taoensso.timbre :as timbre :refer [info]]))

(def expected-default-tags [:timestamp :level :message :namespace])

(def obfuscated-key :password)

(defn- log-to-atom [f]
  (setup-loga :obfuscate [obfuscated-key])
  (timbre/merge-config! atom-appender)
  (f))

(defn- contains-expected-tags? [event]
  (every? #(get (parse-string event) (name %)) expected-default-tags))

(deftest setup-loga-test
  (testing "formats log event to contain desired default tags"
    (reset-log-events)
    (info "dummy log")
    (is (true? (contains-expected-tags? (latest-log-event)))))
  (testing "obfuscates specific keys"
    (reset-log-events)
    (info {:bar "baz" :password "secret"})
    (is (= anonym-string (:password (read-string (get-log-element (latest-log-event) "message")))))))

(deftest set-log-tag-test
  (testing "appends tag to the log event"
    (reset-log-events)
    (let [tag "the-tag"]
      (set-log-tag tag (info "A tagged dummy event"))
      (is (= tag (get-log-element (latest-log-event) "tag"))))))

(deftest log-wrapper-test
  (testing "logs 2 messages around the operation"
    (reset-log-events)
    (log-wrapper {:operation "a-operation"} "result")
    (is (= 2 (count @log-events))))
  (testing "tags log messages"
    (reset-log-events)
    (let [tag "the-tag"]
      (log-wrapper {:operation "a-operation" :tag tag} "result")
      (is (every? #(= tag %) (map #(get-log-element % "tag")  @log-events)))))
  (testing "applies custom pre-log message"
    (reset-log-events)
    (let [msg "dummy log message"]
      (log-wrapper {:operation "a-operation" :pre-log-msg msg} "result")
      (is (.contains (get-log-element (earliest-log-event) "message") msg))))
  (testing "applies custom post-log message"
    (reset-log-events)
    (let [msg "dummy message"]
      (log-wrapper {:operation "a-operation" :post-log-msg msg} "result")
      (is (.contains (get-log-element (latest-log-event) "message") msg))))
  (testing "returns result of wrapped body"
    (reset-log-events)
    (let [expected-result "result"
          result (log-wrapper {:operation "a-operation"} expected-result)]
      (is (= result expected-result)))))

(defn create-loga-decorated-function! [ns-name]
  (let [decorated-fn (with-meta 'decor {::h/tag [1] ::h/pre-log-msg "start" ::h/operation "processing..."})
        ns-name-symbol (symbol ns-name)]
    (create-ns ns-name-symbol)
    (intern ns-name-symbol decorated-fn (fn [ & args] (prn "test hook")))))

(deftest set-loga-hooks-test
  (testing "sets hooks in decorated functions with loga metadata"
    (reset-log-events)
    (create-loga-decorated-function! "clj-loga.ephemeral")
    (set-loga-hooks ["clj-loga.ephemeral"])
    (apply (resolve 'clj-loga.ephemeral/decor) {:a 1 :password "secret"})

    (is (= (get-log-element (latest-log-event) "tag") [1]))
    (is (.contains (get-log-element (earliest-log-event) "message") "FILTERED"))
    (is (.contains (get-log-element (latest-log-event) "message") "processing..."))))

(use-fixtures :each log-to-atom)
