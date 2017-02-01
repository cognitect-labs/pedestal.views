(ns com.cognitect.pedestal.views
  (:require [io.pedestal.log :as log]
            [clojure.spec :as s])
  (:import  [javax.servlet.http HttpServletResponse]
            [java.nio.charset Charset]
            [java.nio ByteBuffer]))

(defn- assert-render-fn!
  [ctx]
  (assert
   (-> ctx :response :render-fn)
   (str "Missing render function for view "
        (-> ctx :response :view)))
  ctx)

(defn- too-long?
  [body limit]
  (< limit (count body)))

(defn- render
  [response async-limit content-type wrapper]
  (let [contents       ((:render-fn response) response)
        contents       (if (coll? contents)
                         (apply str contents)
                         contents)
        content-length (count contents)
        headers        (or (:headers response) {})
        headers        (assoc headers
                              "Content-Length" (str content-length)
                              "Content-Type"   content-type)
        body           (if (< async-limit content-length)
                         (wrapper contents)
                         contents)
        status         (or (:status response) 200)]
    (assoc response
           :body    body
           :headers headers
           :status  status)))

(defn- needs-rendering?
  [ctx]
  (contains? (:response ctx) :view))

(def utf-8 (Charset/forName "UTF-8"))

(defn- wrap-byte-buffer
  [body charset]
  (ByteBuffer/wrap (.getBytes body charset)))

(defn- async-cutoff
  [ctx]
  (if-let [servlet-response (get-in ctx [:response :servlet-response])]
    (.getBufferSize ^HttpServletResponse servlet-response)
    ;; let's play it safe and assume 1500 MTU
    1460))

(defn make-renderer
  [render-fn-resolver]
  {:name ::renderer
   :leave
   (fn [ctx]
     (let [content-type (str "text/html;charset=" (.name utf-8))
           wrapper      #(wrap-byte-buffer % utf-8)
           async-limit  (async-cutoff ctx)]
       (if (needs-rendering? ctx)
         (-> ctx
             render-fn-resolver
             assert-render-fn!
             (update :response render async-limit content-type wrapper))
         ctx)))})

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
