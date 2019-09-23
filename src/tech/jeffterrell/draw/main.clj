(ns tech.jeffterrell.draw.main
  (:gen-class)
  (:require
   [ring.adapter.jetty :as jetty]
   [tech.jeffterrell.draw.handler :refer [handler-with-new-canvas]]))

(defn start-server!
  ([] (start-server! false))
  ([join?]
   (def server
     (jetty/run-jetty (handler-with-new-canvas)
                      {:port (try (Integer/parseInt (System/getenv "PORT"))
                                  (catch Throwable _ 4002))
                       :join? join?}))))

(defn stop-server! []
  (.stop server)
  (def server nil))

(defn -main
  [& args]
  (start-server! true))

(comment
  (start-server!)
  (stop-server!)
  )
