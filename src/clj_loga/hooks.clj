(ns clj-loga.hooks
  (:require [clojure.set :refer [rename-keys]]
            [taoensso.timbre :refer [info]]))

(defn- filter-namespace
  [item]
  (let [formatted-item (format "^%s" item)]
    (filter #(re-find (re-pattern formatted-item) (str %)) (all-ns))))

(defn get-namespaces-from-list
  "Gets an expanded list of namespaces from a list

   (get-namespaces-from-list ['clj-loga.*])
   => #{clj-loga.support.functions clj-loga.core clj-loga.test}
  "
  [namespaces-list]
  (if (= (first namespaces-list) :all)
    (all-ns)
    (seq (reduce
      (fn [acum item]
        (reduce conj acum (filter-namespace item)))
      #{}
      namespaces-list))))

(defn target-functions-from-namespaces
  "Gets a list of all the functions that contains loga's related metadata"
  [namespaces]
  (let [loga-meta-keys #{::operation ::tag ::pre-log-msg ::post-log-msg}
        all-functions (mapcat #(-> % ns-interns vals) namespaces)]
    (filter #(some loga-meta-keys (keys (meta %))) all-functions)))

(defn select-loga-keys [meta]
  (-> meta (rename-keys {::tag :tag
                         ::operation :operation
                         ::pre-log-msg :pre-log-msg
                         ::post-log-msg :post-log-msg})
      (select-keys [:operation :tag :pre-log-msg :post-log-msg])))

(defn format-pre-log-msg [meta args]
  (if-let [pre-log-msg (:pre-log-msg meta)]
    (assoc meta :pre-log-msg (fn [] (info pre-log-msg (into [] args))))
    meta))
