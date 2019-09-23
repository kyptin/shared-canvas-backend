(ns tech.jeffterrell.draw.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [tech.jeffterrell.draw.canvas :as canvas]
            [tech.jeffterrell.draw.parse-request
             :refer [verify-edn-body-then verify-rectangle-data-then]]))

(defn handle-rect-request
  [canvas request]
  (verify-edn-body-then request
    (fn [data]
      (verify-rectangle-data-then data
        (fn [rect]
          (let [[x y width height color] rect
                [red green blue] color]
            (canvas/draw-rect canvas x y width height red green blue)
            {:status 200}))))))

(defn new-handler [canvas]
  (fn [request]
    (let [method (:request-method request)
          path (:uri request)]
      (cond
        (and (= method :get) (= path "/"))
        (canvas/canvas-as-png-response-map 200 canvas)

        (and (= method :post) (= path "/rect"))
        (handle-rect-request canvas request)

        :else {:status 404}))))

(defn start-server!
  ([] (start-server! false))
  ([join?]
   (def server
     (jetty/run-jetty (new-handler (canvas/new-canvas))
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
