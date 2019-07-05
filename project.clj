(defproject com.cognitect/pedestal.views "0.1.0-SNAPSHOT"
  :description "Library of interceptors to make server-side rendering easy again"
  :url "https://github.com/cognitect-labs/pedestal.views"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"                :scope "provided"]
                                       [org.clojure/clojure "1.10.1"                           :scope "provided"]
                                       [stencil "0.5.0" :exclusions [[org.clojure/core.cache]] :scope "provided"]
                                       [selmer "1.12.12"                                       :scope "provided"]]}
             :dev      {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"                :scope "provided"]
                                       [org.clojure/clojure "1.10.1"                           :scope "provided"]
                                       [criterium "0.4.5"]]
                        :resource-paths ["test/resources"]
                        :global-vars {*warn-on-reflection* true}}}
  :dependencies [[io.pedestal/pedestal.interceptor "0.5.7"]
                 [io.pedestal/pedestal.log "0.5.7"]])
