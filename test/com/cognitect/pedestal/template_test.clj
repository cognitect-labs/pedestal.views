(ns com.cognitect.pedestal.template-test
  (:require  [com.cognitect.pedestal.views.template :refer :all]
             [com.cognitect.pedestal.views.test-util :refer [run-interceptor]]
             [stencil.core :as stencil]
             [selmer.parser :as selmer]
             [clojure.test :refer :all]))

(deftest template-renderer-tests
  (testing "Rendering with Stencil"
    (is (= "cake\n"
           (-> (run-interceptor {:response {:view :example-stencil :dessert "cake"}}
                                (renderer stencil/render-file))
               :response
               :body))))
  (testing "Rendering with Selmer"
    (is (= "cake\n"
           (-> (run-interceptor {:response {:view :example-selmer :dessert "cake"}}
                                (renderer selmer/render-file :file-suffix "html"))
               :response
               :body)))))
