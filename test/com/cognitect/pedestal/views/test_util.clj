(ns com.cognitect.pedestal.views.test-util
  (:require  [io.pedestal.interceptor.chain :as chain]))

(defn run-interceptor
  ([i]     (run-interceptor {} i))
  ([ctx i] (chain/execute (chain/enqueue* ctx i))))
