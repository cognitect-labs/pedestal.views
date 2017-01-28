(ns sample.views
  (:require [net.cgrand.enlive-html :as html :refer [deftemplate defsnippet]]
            [stencil.core :as stencil]))

(defsnippet title "layout.html" [:#title]
  [title]
  [:h1] (html/content title))

(deftemplate enlive "layout.html"
  [{:keys [text body url]}]
  [:#title]   (html/substitute (title text))
  [:#wrapper] (html/content body)
  [:a]        (html/set-attr :href url))

(defn stencil
  [data]
  (stencil/render-file "layout-stencil" data))
