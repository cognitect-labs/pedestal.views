(ns sample.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as i]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [sample.views]
            [com.cognitect.pedestal.views :as views]
            [com.cognitect.pedestal.views.stencil :as stencil]
            [com.cognitect.pedestal.views.selmer :as selmer]))

(defn about-page
  [request]
  {:view :sample.views/home-page-with-enlive
   :text "About"
   :body (clojure-version)
   :url  (route/url-for ::about-page)})

(defn home-page-enlive
  [request]
  {:view :sample.views/home-page-with-enlive
   :text "Enlive"
   :body "Hello, world!"
   :url  (route/url-for ::home-page-enlive)})

(defn home-page-stencil
  [request]
  {:view (if (contains? (:params request) :error) :abby-normal :normal)
   :text "Stencil"
   :body "Hello, world!"
   :url  (route/url-for ::home-page-stencil)})

(defn home-page-selmer
  [request]
  {:view :selmer-home
   :text "Selmer"
   :body "Hello, world!"
   :url  (route/url-for ::home-page-selmer)})

(def enlive-interceptors  [(body-params/body-params) http/html-body views/renderer])
(def stencil-interceptors [(body-params/body-params) http/html-body stencil/renderer])
(def selmer-interceptors [(body-params/body-params) http/html-body selmer/renderer])

(def routes #{["/enlive"  :get (conj enlive-interceptors `home-page-enlive)]
              ["/about"   :get (conj enlive-interceptors `about-page)]
              ["/stencil" :get (conj stencil-interceptors `home-page-stencil)]
              ["/selmer"  :get (conj selmer-interceptors `home-page-selmer)]})


(def service {::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})
