(ns tech.jeffterrell.draw.parse-rectangle
  "This namespace contains functions to parse and verify rectangle data attached
  to a ring request. The functions in this namespace are aware of ring and
  consume and may return ring request/response maps."
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [ring.util.codec :refer [form-decode]]))

(defn verify-edn-body-then
  "Verify that the body of the given request is valid EDN. If not, return a 400
  (bad request) ring response with a helpful error message in the body.
  Otherwise, call the given body function with the data parsed from the EDN
  string as the only argument and return whatever it returns."
  [request body-fn]
  (let [body-string (-> request :body slurp)
        parsed-data (try
                      (edn/read-string body-string)
                      (catch Throwable _ nil))]
    (if-not parsed-data
      {:status 400,
       :body (str "Could not read input string '" body-string "' as edn.\n"
                  "Try something like this instead:\n"
                  "[50 100 10 30 [0.95 0.5 0.1]]\n"
                  "(i.e. x, y, width, height, and RGB color\n)")}
      (body-fn parsed-data))))

(s/def ::color-component (s/and float? #(<= 0.0 % 1.0)))
(s/def ::color (s/tuple ::color-component ::color-component ::color-component))
(s/def ::rect (s/tuple nat-int? nat-int? nat-int? nat-int? ::color))

(defn verify-rectangle-data-then
  "Verify that the parsed data from a request is valid rectangle data. If not,
  return a 400 (bad request) ring response with an explanatory error message in
  the body. Otherwise, make a rectangle map from the given data and call the
  given body function with the rectangle as the only argument, returning
  whatever it returns."
  [data body-fn]
  (let [result (s/conform ::rect data)]
    (if (= ::s/invalid result)
      {:status 400
       :body (str "Could not read request body as rect data; "
                  "spec output follows:\n"
                  (s/explain-str ::rect data)
                  \newline
                  (pr-str (s/explain-data ::rect data)))}
      (let [[x y width height color] data
            [r g b] color
            color {:red r, :green g, :blue b}
            rect {:x x, :y y, :width width, :height height, :color color}]
        (body-fn rect)))))

(defn parse-edn-rectangle-then
  "Given alleged rectangle data in EDN format, verify the format and structure
  of the data, then call the given function with the rectangle map as the only
  argument."
  [request body-fn]
  (verify-edn-body-then request
    (fn [data]
      (verify-rectangle-data-then data body-fn))))

(defn parse-form-rectangle-then
  "Given alleged rectangle data in form-encoded format (e.g. from an HTML form),
  verify the format and structure of the data, then call the given function with
  the rectangle map as the only argument."
  [request body-fn]
  (try
    (let [params (form-decode (-> request :body slurp))
          {:strs [x y width height red green blue]} params
          f #(Float/parseFloat %)
          color {:red (f red), :green (f green), :blue (f blue)}
          rect {:x (f x), :y (f y), :width (f width), :height (f height)
                :color color}]
      (body-fn rect))
    (catch NumberFormatException _
      {:status 400
       :body "Number format exception. Ensure all values are numeric."})))

(defn parse-rectangle-then
  "Try to parse the given request body as rectangle data in either EDN format or
  form-encoded format. If unsuccessful, return an informative ring response map.
  If successful, call thi given function with the rectangle map as the only
  argument. The rectangle map has keys :x, :y, :width, :height, and :color. All
  but the :color value is numeric, and the :color value is a nested map with
  keys :red, :green, and :blue and numeric values."
  [request body-fn]
  (let [content-type (get-in request [:headers "content-type"])]
    (cond
      (= content-type "application/x-www-form-urlencoded")
      (parse-form-rectangle-then request body-fn)

      (= content-type "application/edn")
      (parse-edn-rectangle-then request body-fn)

      :else
      {:status 415
       :body "Use a content type of either application/edn or application/x-www-form-urlencoded"})))
