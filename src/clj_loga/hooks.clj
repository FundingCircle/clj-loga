(ns clj-loga.hooks
  (:require [clojure.set :refer [rename-keys]]))

(defn- filter-namespace
  [item]
  (filter #(re-find (re-pattern item) (str %)) (all-ns)))

(defn get-namespaces-from-list
  "Gets an expanded list of namespaces from a list

   (get-namespaces-from-list ['clj-loga.*])
   => [clj-loga.support.functions clj-loga.core clj-loga.test]
  "
  [namespaces-list]
  (reduce
   (fn [acum item]
     (reduce conj acum (filter-namespace item)))
   #{}
   namespaces-list))

(defn target-functions-from-namespaces
  "Gets a list of all the functions that contains loga's related metadata"
  [namespaces]
  (let [loga-meta-keys #{:clj-loga/operation :clj-loga/tag :clj-loga/pre-log-msg :clj-loga/post-log-msg}
        all-functions (mapcat #(-> % ns-interns vals) namespaces)]
    (filter #(some loga-meta-keys (keys (meta %))) all-functions)))

(defn select-loga-keys [meta]
  (-> meta (rename-keys {:clj-loga/tag :tag
                         :clj-loga/operation :operation
                         :clj-loga/pre-log-msg :pre-log-msg
                         :clj-loga/post-log-msg :post-log-msg})
      (select-keys [:operation :tag :pre-log-msg :post-log-msg])))

(defn format-pre-log-msg [meta args]
  (assoc meta :pre-log-msg (apply format (:pre-log-msg meta) args)))
