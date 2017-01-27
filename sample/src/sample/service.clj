(ns sample.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as i]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [sample.views]
            [pedestal.views :as views]
            [stencil.core :as stencil]))

(defn about-page
  [request]
  {:view            :sample.views/about
   :clojure-version (clojure-version)
   :url             (route/url-for ::about-page)})

(defn home-page-enlive
  [request]
  {:view :sample.views/enlive
   :text "bar"
   :body "Hello, world!"})

(defn home-page-stencil
  [request]
  {:view :sample.views/stencil
   :text "bar"
   :body "Hello, world!"})

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
