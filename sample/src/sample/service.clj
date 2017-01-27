(ns sample.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [com.cognitect.pedestal.views :as views]))

(defn ok
  [body]
  {:status 200
   :body   body})

(defn about-page
  [request]
  {:view            :page
   :clojure-version (clojure-version)
   :url             (route/url-for ::about-page)})

(defn home-page
  [request]
  (ok "Hello, world!"))

(def common-interceptors [(body-params/body-params) http/html-body (views/render)])

(def routes #{["/"      :get (conj common-interceptors `home-page)]
              ["/about" :get (conj common-interceptors `about-page)]})


(def service {::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})
