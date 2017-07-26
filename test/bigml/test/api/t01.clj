;; Copyright 2012-2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.t01
  "Contains a few examples that exercise BigML's API"
  (:require (bigml.api [core :as api])
            (bigml.test.api [utils :as test])
            (clojure.data [csv :as csv])
            (clojure.java [io :as io]))
  (:use clojure.test))

(defn take-csv
  "Takes file name and reads data."
  [fname]
  (with-open [file (io/reader fname)]
    (doall (csv/read-csv (slurp file)))))

(defn- create-and-test [resources test-data test-fn]
  (let [result (test/create-get-cleanup resources test-data)]
        (is (= (last test-data) (test-fn result)))))

(defn- predicted-value [prediction-map]
  (first (vals (:prediction prediction-map))))

(deftest ts01
  ;; Creating predictions from sources of various kind
  (api/with-dev-mode true
    (doseq [test-data [["test/data/iris.csv.gz"
                        {:source {"category" "1"}
                         :prediction {:input_data
                                      { "000001" 0.25 "000003" 0.5}
                                      :options
                                      { :by-field-name false }}}
                        "Iris-setosa"]
                       ["test/data/iris.csv.gz"
                        {:source {"category" "1"}
                         :prediction {:input_data
                                      { "petal length" 2.4
                                        "sepal width" 3.9 }
                                      :options
                                      { :by-field-name true }}}
                        "Iris-setosa"]
                       ["test/data/iris-sp-chars.csv"
                        {:prediction {:input_data [1.44 0.54 2.2]}}
                        "Iris-setosa"]
                       ["test/data/iris-sp-chars.csv"
                        {:prediction {:input_data
                                      {"sépal.length" 0.25
                                       "pétal&width" 0.5}
                                      :options
                                      { :by-field-name true }}}
                        "Iris-setosa"]]]
      (create-and-test [:source :dataset :model :prediction]
                       test-data
                       predicted-value))))

(deftest ts02
  ;; Creating a prediction from remote source
  (api/with-dev-mode false
    (doseq [test-data [["https://static.bigml.com/csv/iris.csv"
                        {:prediction {:input_data [1.44 0.54 2.2]}}
                        "Iris-setosa"]
                       ["s3://bigml-public/csv/iris.csv"
                        {:prediction {:input_data [1.44 0.54 2.2]}}
                        "Iris-setosa"]]]
      (create-and-test [:source :dataset :model :prediction]
                       test-data
                       predicted-value))))

(deftest ts04
  ;; Creating a prediction from inline data source
  (doseq [test-data [[(take-csv "test/data/iris-sp-chars.csv")
                      {:prediction {:input_data [1.44 0.54 2.2]}}
                      "Iris-setosa"]]]
    (create-and-test [:source :dataset :model :prediction]
                     test-data
                     predicted-value)))

(deftest ts05
  ;; Creating a centroid and the associated dataset
  (api/with-dev-mode true
    (doseq [test-data [["test/data/diabetes.csv"
                 {:centroid {:input_data [0 118 84 47 230 45.8 0.551 31 "true"]}}
                 "Cluster 0"]]]
      (create-and-test [:source :dataset :cluster :centroid]
                       test-data
                       :centroid_name))))
