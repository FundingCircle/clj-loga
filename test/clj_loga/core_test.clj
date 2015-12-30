(ns clj-loga.core-test
  (:require [cheshire.core :refer [parse-string]]
            [clj-loga.core :refer :all]
            [clj-loga.support.logging-helpers :refer :all]
            [clojure.test :refer :all]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- log-to-atom [f]
  (setup-loga)
  (timbre/merge-config! atom-appender)
  (f))

(def expected-default-tags [:timestamp :level :message :namespace])

(defn- contains-expected-tags? [event]
  (every? #(get (parse-string event) (name %)) expected-default-tags))

(deftest setup-loga-test
  (testing "formats log event to contain desired default tags"
    (reset-log-events)
    (info "dummy log")
    (is (true? (contains-expected-tags? (latest-log-event))))))

(deftest set-log-tag-test
  (testing "appends tag to the log event"
    (reset-log-events)
    (let [tag "the-tag"]
      (set-log-tag tag (info "A tagged dummy event"))
      (is (= tag (get-log-element (latest-log-event) "tag"))))))

(deftest operation-log-wrapper-test
  (testing "logs 2 messages around the operation"
    (reset-log-events)
    (operation-log-wrapper {:operation "a-operation"} "result")
    (is (= 2 (count @log-events))))
  (testing "tags log messages"
    (reset-log-events)
    (let [tag "the-tag"]
      (operation-log-wrapper {:operation "a-operation" :tag tag} "result")
      (is (every? #(= tag %) (map #(get-log-element % "tag")  @log-events)))))
  (testing "applies custom pre-log message"
    (reset-log-events)
    (let [msg "dummy log message"]
      (operation-log-wrapper {:operation "a-operation" :pre-log-msg msg} "result")
      (is (.contains (get-log-element (earliest-log-event) "message") msg))))
  (testing "applies custom post-log message"
    (reset-log-events)
    (let [msg "dummy message"]
      (operation-log-wrapper {:operation "a-operation" :post-log-msg msg} "result")
      (is (.contains (get-log-element (latest-log-event) "message") msg))))
  (testing "returns result of wrapped body"
    (reset-log-events)
    (let [expected-result "result"
          result (operation-log-wrapper {:operation "a-operation"} expected-result)]
      (is (= result expected-result)))))

(use-fixtures :each log-to-atom)
