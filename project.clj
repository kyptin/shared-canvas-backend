(defproject tech.jeffterrell.draw "0.1.0-SNAPSHOT"
  :description "Demo backend for COMP 523 spring 2019"
  :url "http://www.cs.unc.edu/~stotts/COMP523-s19/"
  :license {:name "The MIT License", :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring "1.7.0"]
                 [ring/ring-codec "1.1.2"]]
  :main tech.jeffterrell.draw.main
  :uberjar-name "comp523-backend-standalone.jar"
  :repl-options {:init-ns tech.jeffterrell.draw.main})
