;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.integration
  (:require (bigml.api [resource :as resource]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]))
  (:use clojure.test))

(defn- create-final-model [artifact & params]
  (let [source (resource/get-final (apply source/create artifact params))
        dataset (resource/get-final (apply dataset/create source params))
        model (resource/get-final (apply model/create dataset params))]
    model))
