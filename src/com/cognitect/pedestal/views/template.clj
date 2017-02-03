(ns com.cognitect.pedestal.views.template
  (:require [com.cognitect.pedestal.views :as views]
            [clojure.spec :as s]))

(defn- kw->str [k]
  (subs (str k) 1))

(defn- append-suffix
  [name suffix]
  (if suffix (str name "." suffix) name))

(defn- template-resolver
  [template-renderer opts]
  (fn [ctx]
    (assoc-in ctx [:response :render-fn]
              (partial template-renderer (some-> ctx
                                                 :response
                                                 :view
                                                 kw->str
                                                 (append-suffix (:file-suffix opts)))))))

(defn renderer
  "Returns a template-driven renderer interceptor using render-impl.
   opts are key-value pairs from:

   :file-suffix     The file suffix to append to the template name."
  [render-impl & opts]
  (let [opts (apply hash-map opts)]
    (views/make-renderer (template-resolver render-impl opts))))
