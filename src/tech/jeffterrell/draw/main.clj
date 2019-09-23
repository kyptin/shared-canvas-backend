(ns tech.jeffterrell.draw.main
  "This namespace is the top-level namespace for the backend demo app. The
  `-main` function will be called by running `lein run` or otherwise using this
  namespace as the main class for a JVM process."
  (:gen-class)  ; needed for this to work as a JVM entry point
  (:require
   [ring.adapter.jetty :as jetty]
   [tech.jeffterrell.draw.handler :refer [handler-with-new-canvas]]))

(defn -main
  "Start a new HTTP server on port 4002 or whichever port is given in the $PORT
  environment variable, sending requests to the handler function returned by
  `(handler-with-a-new-canvas)`. Join the serving thread; that is, never
  return but wait for the program to be terminated. Tolerates and ignores any
  arguments."
  [& args]
  (jetty/run-jetty (handler-with-new-canvas)
                   {:port (try (Integer/parseInt (System/getenv "PORT"))
                               (catch Throwable _ 4002))
                    :join? true}))
