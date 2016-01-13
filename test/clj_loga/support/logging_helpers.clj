(ns clj-loga.support.logging-helpers
  (:require [cheshire.core :refer [parse-string]]))

(declare get-log-element)

(def log-events (atom ()))

(defn reset-log-events []
  (swap! log-events (fn [_] ())))

(defn- store-event [event]
  (swap! log-events (fn [logged-events] (conj logged-events event))))

(defn latest-log-event []
  (first @log-events))

(defn earliest-log-event []
  (last @log-events))

(def atom-appender
  {:appenders
   {:atom-appender
    {:enabled? true
     :async? false
     :min-level nil
     :rate-limit [[1 250] [10 5000]]
     :output-fn :inherit
     :fn
     (fn [data]
       (let [{:keys [output-fn]} data
             formatted-output-str (output-fn data)]
         (store-event formatted-output-str)))}}})

(defn get-log-element [log element]
  (get (parse-string log) element))

(comment
  @log-events
  (store-event "a-event")
  (reset-log-events))
