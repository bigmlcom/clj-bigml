;; Copyright 2012, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.t01
  "Contains a few examples that exercise BigML's API"
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [prediction :as prediction]
                       [evaluation :as evaluation])
            (bigml.test.api [utils :as test])
            (clojure [data :as data]))
  (:use clojure.test))

(deftest ts01
  (api/with-mode :dev
    (let [prd (test/create
               ["source" "dataset" "model" "prediction"]
               "test/data/iris.csv.gz"
               {:prediction {"petal width" 0.5}})]
      (is (= "Iris-setosa" (first (vals (:prediction prd))))))))
