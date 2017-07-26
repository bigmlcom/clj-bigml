;; Copyright 2012-2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.integration
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [cluster :as cluster]
                       [centroid :as centroid]
                       [prediction :as prediction]
                       [evaluation :as evaluation]))
  (:use clojure.test))

(defn test-with-generated-data []
  (let [data (for [i (range 10) j (range 10)]
               [i j (+ i j)])
        source (api/get-final (source/create data))
        dataset (api/get-final (dataset/create source))
        model (api/get-final (model/create dataset))
        pred (prediction/create model (first data))
        eval (api/get-final (evaluation/create model dataset))
        predictor (prediction/predictor model)
        cluster (api/get-final (cluster/create dataset))
        centroid (centroid/create cluster (first data))]
    (dorun (pmap api/delete
                 [source dataset model pred eval cluster centroid]))
    (is (== 10 (predictor [8 2]) (predictor [5 5])))
    (is source)
    (is dataset)
    (is model)
    (is pred)
    (is eval)
    (is cluster)
    (is centroid)))

(deftest integration
  (test-with-generated-data))

(deftest alt-integration
  (api/with-dev-mode false
    (test-with-generated-data)))

(deftest dev-mode-integration
  (api/with-dev-mode true
    (test-with-generated-data)))
