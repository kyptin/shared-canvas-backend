(ns tech.jeffterrell.draw.macros
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]))

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
