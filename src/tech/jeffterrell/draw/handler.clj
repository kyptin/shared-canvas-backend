(ns tech.jeffterrell.draw.handler
  "This namespace is the heart of the simple backend in this repo. It contains
  the top-level ring request handler and route dispatcher (see
  `handler-with-new-canvas`) and request handlers for each of the two
  endpoints."
  (:require
   [tech.jeffterrell.draw.canvas :as canvas]
   [tech.jeffterrell.draw.parse-request :refer [verify-edn-body-then
                                                verify-rectangle-data-then]]))

(defn handle-get-canvas
  "Handle the get canvas request, and return a ring response map."
  [canvas]
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (canvas/canvas-as-png-data canvas)})

(defn handle-create-rect
  "Handle the create rectangle request, and return a ring response map."
  [canvas request]
  (verify-edn-body-then request
    (fn [data]
      (verify-rectangle-data-then data
        (fn [rect]
          (let [[x y width height color] rect
                [red green blue] color]
            (canvas/draw-rect canvas x y width height red green blue)
            {:status 200}))))))

(defn handler-with-new-canvas
  "Create a new stateful canvas and return a ring request handler that uses it."
  []
  (let [canvas (canvas/new-canvas)]
    (fn [request]
      (let [method (:request-method request)
            path (:uri request)]
        (cond
          (and (= method :get) (= path "/"))
          (handle-get-canvas canvas)

          (and (= method :post) (= path "/rect"))
          (handle-create-rect canvas request)

          :else {:status 404})))))
