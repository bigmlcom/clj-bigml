(defproject bigml/clj-bigml "0.3.0"
  :description "Clojure bindings for the BigML.io API"
  :url "https://github.com/bigmlcom/clj-bigml"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :aliases {"lint" ["do" "check," "eastwood"]
            "distcheck" ["do" "clean," "lint," "test"]}
  :profiles {:dev {:plugins [[jonase/eastwood "0.2.3"]]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.7.1"]
                 [org.clojure/data.csv "0.1.4"]
                 [clj-http "3.6.1"]
                 [commons-validator "1.6"]])
