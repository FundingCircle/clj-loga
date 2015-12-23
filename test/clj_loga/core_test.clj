(ns clj-loga.core-test
  (:require [cheshire.core :refer [parse-string]]
            [clj-loga.core :refer :all]
            [clojure.test :refer :all]
            [clj-loga.support.logging-helpers :refer :all]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- log-to-atom [f]
  (setup-loga)
  (timbre/merge-config! atom-appender)
  (f))

(def expected-default-tags [:timestamp :level :message :namespace])

(defn contains-expected-tags? [event]
  (every? #(get (parse-string event) (name %)) expected-default-tags))

(deftest setup-loga-test
  (testing "formats log event to contain desired default tags"
    (info "A log event")
    (is (true? (contains-expected-tags? @log-event)))))

(deftest set-tag-test
  (testing "appends tag to the log event"
    (let [tag "the-tag"]
      (set-tag tag
               (info "A tagged dummy event"))
      (is (= tag (get (parse-string @log-event) "tag"))))))

(use-fixtures :each log-to-atom)
