(ns com.cognitect.pedestal.views
  (:require [io.pedestal.log :as log]
            [clojure.spec :as s])
  (:import  [javax.servlet.http HttpServletResponse]
            [java.nio.charset Charset
                              StandardCharsets]
            [java.nio ByteBuffer]))

(defn- assert-render-fn!
  [ctx]
  (assert
   (-> ctx :response :render-fn)
   (str "Missing render function for view "
        (-> ctx :response :view)))
  ctx)

(defn- format-body
  [contents ^long content-length ^long limit wrapper]
  (if (< content-length limit)
    contents
    (wrapper contents)))

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
        body           (format-body contents content-length async-limit wrapper)
        status         (:status response 200)]
    (assoc response
           :body    body
           :headers headers
           :status  status)))

(defn- needs-rendering?
  [ctx]
  (contains? (:response ctx) :view))

(defn- wrap-byte-buffer
  [^String body ^Charset charset]
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
     (let [^Charset charset StandardCharsets/UTF_8
           content-type (str "text/html;charset=" (.name charset))
           wrapper      #(wrap-byte-buffer % charset)
           async-limit  (async-cutoff ctx)]
       (if (needs-rendering? ctx)
         (-> ctx
             render-fn-resolver
             assert-render-fn!
             (update :response render async-limit content-type wrapper))
         ctx)))})

(defn- kw->sym [k]
  (symbol (namespace k) (name k)))

(defn- kw->str [k]
  (subs (str k) 1))

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

(defn attach-renderer
  [ctx f]
  (assoc-in ctx [:response :render-fn] f))

(defn view-function-resolver
  "Resolves the :view in the ctx's response to a render fn and attaches it."
  [ctx]
  (attach-renderer ctx (locate-render-fn (some-> ctx :response :view))))

(defn make-template-resolver
  "Returns a view-template-resolver fn which resolves the :view
  in the ctx's response to a template-based render fn and attaches it."
  [template-render-fn]
  (fn [ctx]
    (attach-renderer ctx (partial template-render-fn
                                  (some-> ctx
                                          :response
                                          :view
                                          kw->str)))))

(defn make-template-renderer
  [template-render-fn]
  (make-renderer (make-template-resolver template-render-fn)))

(def renderer (make-renderer view-function-resolver))
