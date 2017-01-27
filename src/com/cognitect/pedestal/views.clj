(ns com.cognitect.pedestal.views
  (:require [io.pedestal.log :as log]
            [clojure.spec :as s]))

(defn- kw->sym [k]
  (symbol (namespace k) (name k)))

(s/def ::view-selector
  (s/or :fn fn? :symbol symbol? :keyword keyword? :var var?))

(defn- var-get-if-bound
  [x]
  (when (.isBound x)
    (var-get x)))

(defn- locate-render-fn
  [selector]
  {:pre [(s/valid? ::view-selector selector)]}
  (cond
    (fn?      selector) selector
    (var?     selector) (some-> selector var-get-if-bound)
    (symbol?  selector) (some-> selector resolve var-get-if-bound)
    (keyword? selector) (some-> selector kw->sym resolve var-get-if-bound)))

(defn- render
  [response]
  (let [render-fn (locate-render-fn (:view response))]
    (assert render-fn (str "Missing render function for view" (:view response)))
    (let [contents (render-fn response)
          contents (if (coll? contents) (apply str contents) contents)
          status   (or (:status response) 200)]
      (assoc response
             :body contents
             :status status))))

(defn- needs-rendering?
  [ctx]
  (contains? (:response ctx) :view))

(def renderer
  {:name ::renderer
   :leave
   (fn [ctx]
     (if (needs-rendering? ctx)
       (update ctx :response render)
       ctx))})
