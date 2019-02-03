(ns tech.jeffterrell.draw.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [tech.jeffterrell.draw.canvas :as canvas]
            [tech.jeffterrell.draw.macros :refer [with-verified-edn-body
                                                  with-valid-rect-data]]))

(defonce canvas (canvas/new-canvas))

(defn handle-rect-request
  [request]
  (with-verified-edn-body request [body-data]
    (with-valid-rect-data body-data
      (let [[x y width height color] body-data
            [red green blue] color]
        (canvas/draw-rect canvas x y width height red green blue)
        {:status 200}))))

(defn handler [request]
  (let [method (:request-method request)
        path (:uri request)]
    (cond
      (and (= method :get) (= path "/image"))
      (canvas/canvas-as-png-response-map 200 canvas)

      (and (= method :post) (= path "/rect"))
      (handle-rect-request request)

      :else {:status 404})))

(defn start-server!
  ([] (start-server! false))
  ([join?]
   (def server
     (jetty/run-jetty handler {:port (or (System/getenv "PORT") 4002)
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
