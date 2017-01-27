(ns com.cognitect.pedestal.views-test
  (:require [clojure.test :refer :all]
            [io.pedestal.interceptor.chain :as chain]
            [com.cognitect.pedestal.views :refer :all])
  (:import [clojure.lang ExceptionInfo]))

(defn run-interceptor
  ([i]     (run-interceptor {} i))
  ([ctx i] (chain/execute (chain/enqueue* ctx i))))

(defn ctx-with-renderer
  [r]
  {:response {:view r}})

(defn rendered-body
  [ctx]
  (-> (run-interceptor ctx renderer) :response :body))

(defn rendered-status
  [ctx]
  (-> (run-interceptor ctx renderer) :response :status))

(defn render-with
  [r]
  (rendered-body (ctx-with-renderer r)))

(defn hello-fn
  [_]
  "cake")

(deftest rendering
  (are [expected renderer] (= expected (render-with renderer))
    "hello!" (constantly "hello!")
    "cake"   hello-fn
    "cake"   #'hello-fn
    "cake"   :com.cognitect.pedestal.views-test/hello-fn))

(deftest passthrough-when-no-view-given
  (is (= "cake" (rendered-body {:response {:body "cake"}}))))

(deftest missing-render-fn
  (are [renderer] (thrown-with-msg? ExceptionInfo #"Missing render function"
                                    (render-with renderer))
    :no.such.fn/exists
    'no.such/symbol
    (clojure.lang.Var/create)))

(deftest status-not-changed-if-set
  (is (= 202
         (rendered-status {:response {:status 202 :view hello-fn}}))))

(defn boom [_] (throw (ex-info "Boom!" {})))

(deftest render-fn-throws
  (is (thrown-with-msg? ExceptionInfo #"Boom!" (render-with boom))))

(defmacro exceptions
  [f]
  `(try
    ~f
    (assert false "exception not thrown.")
    (catch Throwable t#
      (take-while boolean (iterate #(.getCause %) t#)))))

(deftest invalid-view-selectors
  (are [renderer] (= [ExceptionInfo AssertionError]
                     (map type (exceptions (render-with renderer))))
    "foobar"
    nil))


(run-tests)
