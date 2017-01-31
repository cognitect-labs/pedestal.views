(ns com.cognitect.pedestal.views
  (:require [io.pedestal.log :as log]
            [clojure.spec :as s]))

(defn- assert-render-fn!
  [ctx]
  (assert
   (-> ctx :response :render-fn)
   (str "Missing render function for view "
        (-> ctx :response :view)))
  ctx)

(defn- render
  [response]
  (let [contents ((:render-fn response) response)
        status   (or (:status response) 200)]
    (assoc response
           :body (if (coll? contents)
                   (apply str contents)
                   contents)
           :status status)))

(defn- needs-rendering?
  [ctx]
  (contains? (:response ctx) :view))

(defn make-renderer
  [render-fn-resolver]
  {:name ::renderer
   :leave
   (fn [ctx]
     (if (needs-rendering? ctx)
       (-> ctx
           render-fn-resolver
           assert-render-fn!
           (update :response render))
       ctx))})

(defn- kw->sym [k]
  (symbol (namespace k) (name k)))

(s/def ::view-selector
  (s/or :fn fn? :symbol symbol? :keyword keyword? :var var?))

(defn- var-get-if-bound
  [^clojure.lang.Var x]
  (when (and x (.isBound x))
    (var-get x)))

(defn- locate-render-fn
  [selector]
  {:pre [(s/valid? ::view-selector selector)]}
  (cond
    (fn?      selector) selector
    (var?     selector) (some-> selector var-get-if-bound)
    (symbol?  selector) (some-> selector resolve var-get-if-bound)
    (keyword? selector) (some-> selector kw->sym resolve var-get-if-bound)))

(defn view-key-resolver
  [ctx]
  (assoc-in ctx [:response :render-fn]
            (locate-render-fn (some-> ctx :response :view))))

(def renderer (make-renderer view-key-resolver))
