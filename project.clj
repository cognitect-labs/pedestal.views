(defproject pedestal.views "0.1.0-SNAPSHOT"
  :description "Library of interceptors to make server-side rendering easy again"
  :url "https://github.com/cognitect-labs/pedestal.views"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[javax.servlet/javax.servlet-api "3.1.0" :scope "provided"]
                                       [org.clojure/clojure "1.9.0-alpha14"     :scope "provided"]]}
             :dev      {:dependencies [[javax.servlet/javax.servlet-api "3.1.0" :scope "provided"]
                                       [org.clojure/clojure "1.9.0-alpha14"     :scope "provided"]]}}
  :dependencies [[io.pedestal/pedestal.interceptor "0.5.2"]
                 [io.pedestal/pedestal.log "0.5.2"]
                 [stencil "0.5.0"]])
