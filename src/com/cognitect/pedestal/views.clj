(ns com.cognitect.pedestal.views
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.walk :as walk]
            [io.pedestal.interceptor :as i]
            [io.pedestal.log :as log])
  (:import java.net.URLDecoder
           java.nio.ByteBuffer
           [java.nio.charset Charset StandardCharsets]
           javax.servlet.http.HttpServletResponse))

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
  [ctx async-limit content-type wrapper]
  (let [response       (:response ctx)
        from           (or (some-> response :from vector) [:response])
        contents       ((get-in ctx [:response :render-fn] identity) (get-in ctx from))
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
    (update ctx :response merge {:body    body
                                 :headers headers
                                 :status  status})))

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
  (i/interceptor
   {:name ::renderer
    :leave
    (fn [ctx]
      (let [^Charset charset StandardCharsets/UTF_8
            content-type     (str "text/html;charset=" (.name charset))
            wrapper          #(wrap-byte-buffer % charset)
            async-limit      (async-cutoff ctx)]
        (if (needs-rendering? ctx)
          (-> ctx
              render-fn-resolver
              assert-render-fn!
              (render async-limit content-type wrapper))
          ctx)))}))

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

(defn- attach-renderer
  [ctx f]
  (assoc-in ctx [:response :render-fn] f))

(defn- view-function-resolver
  "Resolves the :view in the ctx's response to a render fn and attaches it."
  [ctx]
  (attach-renderer ctx (locate-render-fn (some-> ctx :response :view))))

(defn- make-template-resolver
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

;; The several functions are copied from Vase
;;
;; It is duplication, but in this case preferrable to a new dependency.
(defn- map-vals
  [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) m m))

(defn- dynamic-interceptor
  "Build an interceptor/interceptor from a map of keys to
  expressions. The expressions will be evaluated and must evaluate to
  a function of 1 argument. At runtime the function will be called
  with a Pedestal context map."
  [name literal exprs]
  (with-meta
    (i/interceptor
     (merge
      {:name name}
      (map-vals eval exprs)))
    {:action-literal literal}))

(defn- decode-map
  "URL Decode the values of a Map
  This opens up the potential for non-sanitized input to be rendered."
  [m]
  (walk/postwalk
   #(cond-> %
      (string? %)
      (URLDecoder/decode %))
   m))

(defn merged-parameters
  [request]
  {:post [(map? %)]}
  (let [{:keys [path-params params json-params edn-params]} request]
    (merge
     (if (empty? path-params)
       {}
       (decode-map path-params))
     params
     json-params
     edn-params)))

(defn bind
  [param-syms]
  (let [param-keys (mapv #(if (vector? %) (first %) %) param-syms)
        param-defaults (into {} (filter vector? param-syms))]
    `{:keys ~(or param-keys [])
      :or ~param-defaults}))

;;
;; end of snitching from Vase
;;

(defn- render-action-exprs
  "Return code for a Pedestal interceptor that will respond with a
  canned response. The same `body`, `status`, and `headers` arguments
  are returned for every HTTP request."
  [params view from]
  `(fn [{~'request :request :as ~'context}]
     (let [~(bind params) (merged-parameters ~'request)]
       (update ~'context :response assoc :view ~view :from ~from))))

(defn- render-action
  "Return a Pedestal interceptor that attaches a view key to the
  response"
  [name params view from]
  (dynamic-interceptor
   name
   :respond
   {:enter (render-action-exprs params view from)
    :action-literal
    :views/render}))

(defrecord RenderAction [name params view from]
  i/IntoInterceptor
  (-interceptor [_]
    (render-action name params view from)))

(s/def ::name keyword?)
(s/def ::view keyword?)
(s/def ::from keyword?)
(s/def ::params seq?)
(s/def ::render-literal (s/keys :req-un [::view] :opt-un [::params ::name ::from]))

(defn render-literal
  [form]
  {:pre [(s/valid? ::render-literal form)]}
  (map->RenderAction form))

(defmethod print-method RenderAction [t ^java.io.Writer w]
  (.write w (str "#views/render" (into {} t))))
