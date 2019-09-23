(ns tech.jeffterrell.draw.parse-request
  "This namespace contains functions to parse and verify data attached to a ring
  request."
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]))

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
  the body. Otherwise, call the given body function with the given data as the
  only argument and return whatever it returns."
  [data body-fn]
  (let [result (s/conform ::rect data)]
    (if (= ::s/invalid result)
      {:status 400
       :body (str "Could not read request body as rect data; "
                  "spec output follows:\n"
                  (s/explain-str ::rect data)
                  \newline
                  (pr-str (s/explain-data ::rect data)))}
      (do
        (prn 'result result)
        (body-fn data)))))
