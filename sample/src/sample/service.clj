(ns sample.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as i]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [sample.views]
            [com.cognitect.pedestal.views :as views]
            [stencil.core :as stencil]))

(defn about-page
  [request]
  {:view :sample.views/enlive
   :text "About"
   :body (clojure-version)
   :url  (route/url-for ::about-page)})

(defn home-page-enlive
  [request]
  {:view :sample.views/enlive
   :text "Enlive"
   :body "Hello, world!"
   :url  (route/url-for ::home-page-enlive)})

(defn home-page-stencil
  [request]
  {:view :sample.views/stencil
   :text "Stencil"
   :body "Hello, world!"
   :url  (route/url-for ::home-page-stencil)})

(def common-interceptors [(body-params/body-params) http/html-body views/renderer])

(def routes #{["/enlive"  :get (conj common-interceptors `home-page-enlive)]
              ["/stencil" :get (conj common-interceptors `home-page-stencil)]
              ["/about"   :get (conj common-interceptors `about-page)]})


(def service {::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})
