;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.integration
  (:require (bigml.api [resource :as resource]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [prediction :as prediction]
                       [evaluation :as evaluation]))
  (:use clojure.test))

(defn- generate-row [cols]
  (let [inputs (vec (repeatedly (dec cols) #(rand-int 100)))]
    (conj inputs (reduce + inputs))))

(defn- generate-data [rows cols]
  (repeatedly rows #(generate-row cols)))

(deftest integration
  (let [data (generate-data 100 3)
        source (resource/get-final (source/create data))
        dataset (resource/get-final (dataset/create source))
        model (resource/get-final (model/create dataset))
        pred (prediction/create model (first data))
        eval (resource/get-final (evaluation/create model dataset))]
    (doall (map resource/delete [source dataset model pred eval]))
    (is source)
    (is dataset)
    (is model)
    (is pred)
    (is eval)))
