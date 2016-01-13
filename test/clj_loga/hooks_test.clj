(ns clj-loga.hooks-test
  (:require [clj-loga.hooks :as sut]
            [clojure.test :refer :all]))

(def expected-ns-data
  (map create-ns ['expected-ns.core 'expected-ns.test 'expected-ns.loga]))

(def unexpected-ns-data
  (map create-ns ['other-ns.core 'non-expected-ns.core]))

(def all-ns-mock
  (concat expected-ns-data unexpected-ns-data))

(deftest get-namespaces-from-list-test
  (testing "expands pattern list"
    (with-redefs [all-ns (constantly all-ns-mock)]
      (is (= (sut/get-namespaces-from-list ["expected-ns.*"]) (into #{} expected-ns-data))))))

(def expected-fn-data
  (doall
   (map (fn [x] (let [fn (with-meta 'fn {:clj-loga/operation "operation test" :clj-loga/tag [1]})]
                  (intern x fn (fn [] nil)))) expected-ns-data)))

(deftest target-functions-from-namespaces
  (testing "gets only functions with loga metadata"
    (let [add-noise-fn (fn [] (doseq [ns expected-ns-data]
                                (intern ns 'other-fn (fn [] nil))))]
      (add-noise-fn)
      (is (= (sut/target-functions-from-namespaces expected-ns-data) expected-fn-data)))))

(deftest select-loga-keys-test
  (testing "renames ns prepended loga keys"
    (is (= (sut/select-loga-keys {:clj-loga/tag [1] :clj-loga/operation "operation"}) {:tag [1] :operation "operation"})))
  (testing "selects only loga keys"
    (is (= (sut/select-loga-keys {:clj-loga/tag [1] :clj-loga/operation "op" :other false}) {:tag [1] :operation "op"}))))
