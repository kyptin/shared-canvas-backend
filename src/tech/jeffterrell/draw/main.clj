(ns tech.jeffterrell.draw.main
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [ring.adapter.jetty :as jetty])
  (:import javax.imageio.ImageIO
           [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.awt Color Rectangle]
           [java.awt.image BufferedImage]))

(def width 800)
(def height 600)

(defonce canvas
  (BufferedImage. width height BufferedImage/TYPE_INT_RGB))

(s/def ::color-component (s/and float? #(<= 0.0 % 1.0)))
(s/def ::color (s/tuple ::color-component ::color-component ::color-component))
(s/def ::rect (s/tuple nat-int? nat-int? nat-int? nat-int? ::color))

(defn draw-rect [request]
  (let [body (try
               (-> request :body slurp edn/read-string)
               (catch Throwable _ nil))]
    (if-not body
      {:status 400, :body "could not read request body as edn"}
      (let [rect-data (s/conform ::rect body)]
        (if (= ::s/invalid rect-data)
          {:status 400
           :body (str "could not read request body as rect data; "
                      "spec output follows:\n"
                      (s/explain-str ::rect body)
                      \newline
                      (pr-str (s/explain-data ::rect body)))}

          (let [[x y width height color] rect-data
                [red green blue] color]
            (doto (.createGraphics canvas)
              (.setPaint (Color. (float red) (float green) (float blue)))
              (.draw (Rectangle. x y width height)))
            {:status 200}))))))

(defn image-body []
  (let [buffer (ByteArrayOutputStream.)]
    (ImageIO/write canvas "PNG" buffer)
    (ByteArrayInputStream. (.toByteArray buffer))))

(defn handler [request]
  (let [method (:request-method request)
        path (:uri request)]
    (cond
      (and (= method :get) (= path "/image"))
      {:status 200, :headers {"Content-Type" "image/png"}, :body (image-body)}

      (and (= method :post) (= path "/rect"))
      (draw-rect request)

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




















;; simple counter example actually used in class
;; (ns tech.jeffterrell.draw-backend
;;   (:gen-class)
;;   (:require [clojure.edn :as edn]
;;             [clojure.java.io :as io]
;;             [clojure.spec.alpha :as s]
;;             [ring.adapter.jetty :as jetty]
;;             [clojure.java.io :as io]
;;             [clojure.pprint :refer [pprint]]))

;; (def counter (atom 0))

;; (defn handler [request]
;;   (let [path (:uri request)
;;         method (:request-method request)]

;;     (cond
;;       (and (= method :get) (= path "/value"))
;;       {:status 200
;;        :headers {"Content-Type" "application/edn"}
;;        :body (pr-str {:counter @counter})}

;;       (and (= method :put) (= path "/value"))
;;       (let [new-value (-> request :body slurp Integer/parseInt)]
;;         (reset! counter new-value)
;;         {:status 200
;;          :headers {"Content-Type" "application/edn"}
;;          :body (pr-str {:counter @counter})})

;;       (= path "/inc")
;;       {:status 200
;;        :headers {"Content-Type" "application/edn"}
;;        :body (do
;;                (swap! counter inc)
;;                (pr-str {:counter @counter}))}

;;       :else {:status 404})))

;; (defn start-server!
;;   ([] (start-server! false))
;;   ([join?]
;;    (def server
;;      (jetty/run-jetty handler {:port (or (System/getenv "PORT") 4002)
;;                                :join? join?}))))

;; (defn stop-server! []
;;   (.stop server)
;;   (def server nil))

;; (defn -main
;;   [& args]
;;   (start-server! true))
