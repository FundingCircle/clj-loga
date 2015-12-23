(ns clj-loga.core-test
  (:require [cheshire.core :refer [parse-string]]
            [clj-loga.core :refer :all]
            [clojure.test :refer :all]
            [clj-loga.support.logging-helpers :refer :all]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- log-to-atom [f]
  (init-logging)
  (timbre/merge-config! atom-appender)
  (f))

(def expected-default-tags [:timestamp :level :message :namespace])

(defn contains-expected-tags? [event]
  (every? #(get (parse-string event) (name %)) expected-default-tags))

(deftest init-logging-test
  (testing "contains desired defaut tags"
    (info "A log event")
    (is (true? (contains-expected-tags? @log-event))))
  (testing "appends tag to the log"
    (let [tag "the-tag"]
      (set-tag tag
               (info "A tagged dummy event"))
      (is (= tag (get (parse-string @log-event) "tag"))))))

(use-fixtures :each log-to-atom)
