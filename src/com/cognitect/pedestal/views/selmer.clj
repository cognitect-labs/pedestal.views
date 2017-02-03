(ns com.cognitect.pedestal.views.selmer
  (:require [com.cognitect.pedestal.views :as views]
            [selmer.parser :as selmer]))

(defn- selmer-wrapper
  [t data]
  (selmer/render-file (str t ".html") data))

(def renderer (views/make-template-renderer selmer-wrapper))
