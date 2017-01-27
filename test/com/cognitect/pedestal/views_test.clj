(ns pedestal.views-test
  (:require [clojure.test :refer :all]
            [pedestal.views :refer :all]
            []))

(defn run-interceptor
  ([i]     (run-interceptor {} i))
  ([ctx i] (chain/execute (chain/enqueue* ctx i))))

(deftest rendering
  (testing "fn in response")
  (testing "symbol in response")
  (testing "var in response")
  (testing "keyword in response"))

(deftest passthrough-when-no-view-given)

(deftest missing-render-fn)

(deftest status-not-changed-if-set)

(deftest render-fn-throws)

(deftest invalid-view-selectors
  (testing "string")
  (testing "integer")
  (testing "nil"))

(run-tests)
