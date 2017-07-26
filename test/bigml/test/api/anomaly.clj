;; Copyright 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.anomaly
  (:use clojure.test)
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [anomaly-detector :as anomaly-detector]
                       [anomaly-score :as anomaly-score])))

(defn- about= [x y]
  ;; Ignore small differences because we don't round results locally
  (> 0.0001 (Math/abs (- x y))))

(deftest iris
  (let [inputs [[5.2 3.5 1.5 0.2 "Iris-setosa"]
                [5.2 3.5 1.5 0.2 "Iris-virginica"]]
        src (api/get-final (source/create "test/data/iris.csv.gz"))
        ds (api/get-final (dataset/create src))
        detector (api/get-final (anomaly-detector/create ds))
        remote-scores (mapv #(anomaly-score/create detector %) inputs)
        local-detector (anomaly-score/detector detector)
        local-scores (mapv local-detector inputs)]
    (is (every? true?
                (map #(about= (:score %1) %2) remote-scores local-scores)))
    (dorun (map api/delete remote-scores))
    (api/delete detector)
    (api/delete ds)
    (api/delete src)))
