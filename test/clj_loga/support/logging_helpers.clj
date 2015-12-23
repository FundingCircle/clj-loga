(ns clj-loga.support.logging-helpers
  (:require [taoensso.timbre :as timbre]))

(def log-event (atom nil))

(defn reset-log-events []
  (swap! log-event (fn [_] nil)))

(defn- store-event [event]
  (swap! log-event (fn [_] event)))

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


