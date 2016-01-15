(ns clj-loga.hooks-test
  (:require [clj-loga.hooks :as sut]
            [clojure.test :refer :all]))

(def expected-ns-data
  (map create-ns ['expected-ns.core 'expected-ns.test 'expected-ns.loga]))

(def unexpected-ns-data
  (map create-ns ['other-ns.core 'non-expected-ns.core]))

(def all-ns-mock
  (concat expected-ns-data unexpected-ns-data))

(defn- extract-ns-names [ns-list]
  (sort (map ns-name ns-list)))

(deftest get-namespaces-from-list-test
  (testing "expands pattern list"
    (with-redefs [all-ns (constantly all-ns-mock)]
      (is (= (extract-ns-names (sut/get-namespaces-from-list ["expected-ns.*"])) (extract-ns-names expected-ns-data)))))
  (testing "defaults to all loaded namespaces"
    (with-redefs [all-ns (constantly all-ns-mock)]
      (is (= (extract-ns-names (sut/get-namespaces-from-list [:all])) (extract-ns-names all-ns-mock))))))

(def expected-fn-data
  (doall
   (map (fn [x] (let [fn (with-meta 'fn {::sut/operation "operation test" ::sut/tag [1]})]
                  (intern x fn (fn [] nil)))) expected-ns-data)))

(deftest target-functions-from-namespaces
  (testing "gets only functions with loga metadata"
    (let [add-noise-fn (fn [] (doseq [ns expected-ns-data]
                                (intern ns 'other-fn (fn [] nil))))]
      (add-noise-fn)
      (is (= (sut/target-functions-from-namespaces expected-ns-data) expected-fn-data)))))

(deftest select-loga-keys-test
  (testing "renames ns prepended loga keys"
    (is (= (sut/select-loga-keys {::sut/tag [1] ::sut/operation "operation"}) {:tag [1] :operation "operation"})))
  (testing "selects only loga keys"
    (is (= (sut/select-loga-keys {::sut/tag [1] ::sut/operation "op" :other false}) {:tag [1] :operation "op"}))))
