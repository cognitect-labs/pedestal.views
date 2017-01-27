(ns sample.views
  (:require [net.cgrand.enlive-html :as html :refer [deftemplate defsnippet]]
            [stencil.core :as stencil]))

(defsnippet title "layout.html" [:#title]
  [title]
  [:h1] (html/content title))

(deftemplate enlive "layout.html"
  [{:keys [text body]}]
  [:#title]   (html/substitute (title text))
  [:#wrapper] (html/content body))

(defn stencil
  [data]
  (stencil/render-file "layout-stencil" data))
