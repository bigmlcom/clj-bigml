;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.integration
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [cluster :as cluster]
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
        cluster (api/get-final (cluster/create dataset))]
    (doall (pmap api/delete [source dataset model pred eval cluster]))
    (is (== 10 (predictor [8 2]) (predictor [5 5])))
    (is source)
    (is dataset)
    (is model)
    (is pred)
    (is eval)
    (is cluster)))

(deftest integration
  (test-with-generated-data))

(deftest dev-mode-integration
  (api/with-dev-mode
    (test-with-generated-data)))
