(ns pedestal.views)

(defn renderer
  []
  {:name ::renderer
   :leave (fn [ctx] ctx)})
