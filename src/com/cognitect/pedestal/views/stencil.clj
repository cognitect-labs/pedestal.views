(ns com.cognitect.pedestal.views.stencil
  (:require [com.cognitect.pedestal.views :as views]
            [stencil.core :as stencil]))

(def renderer (views/make-template-renderer stencil/render-file))
