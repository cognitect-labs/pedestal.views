(ns com.cognitect.pedestal.views.stencil
  (:require [stencil.core :as stencil]
            [com.cognitect.pedestal.views :as views]))

(defn- kw->str [k]
  (subs (str k) 1))

(defn- stenciler
  [template]
  (fn [response]
    (stencil/render-file template response)))

(defn- stencil-template-resolver
  [ctx]
  (assoc-in ctx [:response :render-fn]
            (stenciler (kw->str (some-> ctx :response :view)))))

(def renderer (views/make-renderer stencil-template-resolver))
