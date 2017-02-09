(ns com.cognitect.pedestal.literals-test
  (:require  [clojure.test :refer :all]
             [com.cognitect.pedestal.views :as views]
             [com.cognitect.pedestal.views-test :refer [run-interceptor]]
             [io.pedestal.interceptor :as i]))

(deftest render-action-read
  (testing "acceptable literals"
    (are [x] (read-string x)
      "#views/render{:view :a-view-key}"
      "#views/render{:name :keyword :view :a-view-key}"
      "#views/render{:name :keyword :view :a-view-key :from :keyword}"
      "#views/render{:view :a-view-key :extra-data 123545}"
      ))

  (testing "required vals enforcement"
    (are [x] (thrown? AssertionError (read-string x))
      "#views/render{}"
      "#views/render{:view \"string-not-allowed\"}"
      "#views/render{:view :key :name \"string-not-allowed\"}"
      "#views/render{:view :key :name :keyword :from \"string-not-allowed\"}"))

  (testing "it's really an interceptor"
    (is (= {:view :a-view :from :query-data}
           (-> "#views/render{:view :a-view :from :query-data}"
               read-string
               i/-interceptor
               run-interceptor
               :response
               (select-keys [:view :from]))))))

(run-tests)
