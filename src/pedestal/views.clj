(ns pedestal.views
  (:require [io.pedestal.log :as log]))

(defn- kw->var [k]
  (resolve (symbol (namespace k) (name k))))

(defn- locate-render-fn
  [selector]
  (cond
    (fn? selector)      selector
    (symbol? selector)  (locate-render-fn (resolve selector))
    (var? selector)     (locate-render-fn (var-get selector))
    (keyword? selector) (locate-render-fn (kw->var selector))))

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
