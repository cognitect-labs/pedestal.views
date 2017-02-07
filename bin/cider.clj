(ns cider)

(println "Loading ns")

(println "Compiling nREPL")
(require 'clojure.tools.nrepl.server)
(println "Compiling cider-nrepl")
(require 'cider.nrepl)
(println "Starting CIDER nREPL on port 7888")
(spit    ".nrepl-port" "7888")
(eval '(clojure.tools.nrepl.server/start-server :port 7888 :handler cider.nrepl/cider-nrepl-handler))
