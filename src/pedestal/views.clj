(ns pedestal.views
  (:require [io.pedestal.log :as log]
            [clojure.spec :as s]))

(defn- kw->sym [k]
  (symbol (namespace k) (name k)))

(s/def ::view-selector
  (s/nilable
   (s/or :fn fn? :symbol symbol? :keyword keyword? :var var?)))

(defn- locate-render-fn
  [selector]
  {:pre [(s/valid? ::view-selector selector)]}
  (cond
    (fn? selector)      selector
    (var? selector)     (var-get selector)
    (symbol? selector)  (var-get (resolve selector))
    (keyword? selector) (var-get (resolve (kw->sym selector)))))

(defn- render
  [response]
  (if-let [render-fn (locate-render-fn (:view response))]
    (let [contents (render-fn response)
          contents (if (coll? contents) (apply str contents) contents)
          status   (or (:status response) 200)]
      (assoc response
             :body contents
             :status status))
    (do
      (log/warn :msg "Missing render function for view" :view (:view response))
      response)))

(defn- needs-rendering?
  [ctx]
  (get-in ctx [:response :view]))

(def renderer
  {:name ::renderer
   :leave
   (fn [ctx]
     (if (needs-rendering? ctx)
       (update ctx :response render)
       ctx))})
