(ns tech.jeffterrell.draw.main
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [ring.adapter.jetty :as jetty]
            [tech.jeffterrell.draw.canvas :as canvas]))

(defonce canvas (canvas/new-canvas))

(defmacro with-verified-edn-body [request [edn-data-name] & body]
  `(let [body-string# (-> ~request :body slurp)
         ~edn-data-name (try
                          (edn/read-string body-string#)
                          (catch Throwable _# nil))]
     (if-not ~edn-data-name
       {:status 400,
        :body (str "Could not read input string '" body-string# "' as edn.\n"
                   "Try something like this instead:\n"
                   "[50 100 10 30 [0.95 0.5 0.1]]\n"
                   "(i.e. x, y, width, height, and RGB color\n)")}
       (do ~@body))))

(s/def ::color-component (s/and float? #(<= 0.0 % 1.0)))
(s/def ::color (s/tuple ::color-component ::color-component ::color-component))
(s/def ::rect (s/tuple nat-int? nat-int? nat-int? nat-int? ::color))

(defmacro with-valid-rect-data [data & body]
  `(let [result# (s/conform ::rect ~data)]
     (if (= ::s/invalid result#)
       {:status 400
        :body (str "could not read request body as rect data; "
                   "spec output follows:\n"
                   (s/explain-str ::rect ~data)
                   \newline
                   (pr-str (s/explain-data ::rect ~data)))}
       (do ~@body))))

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
