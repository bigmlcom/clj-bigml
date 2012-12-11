;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.examples
  "Contains a few examples that excercise BigML's API"
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [prediction :as prediction]
                       [evaluation :as evaluation])))

(def wine-quality-examples
  [{:input [6.4 0.22 0.38 9.1 0.044 35 127 0.99326 2.97 0.3 11] :quality 7}
   {:input [6.9 0.75 0.13 6.3 0.036 19 50 0.99312 3.09 0.25 11.1] :quality 4}
   {:input [6.3 0.26 0.25 7.8 0.058 44 166 0.9961 3.24 0.41 9] :quality 5}
   {:input [6.3 0.41 0.33 4.7 0.023 28 110 0.991 3.3 0.38 12.5] :quality 7}])

(defn forest-example []
  ;; This example builds a small random forest on UCI's wine-quality
  ;; dataset, transforms the models into Clojure fns, and then
  ;; evaluates the forest on a few examples.

  (binding [api/*username* nil ;; You're BigML username and API-key go here
            api/*api-key* nil]
    (let [wine-quality-url "http://goo.gl/UyDmy"
          _ (println "Building the source and dataset from UCI's wine
                      quality dataset")
          source (api/get-final (source/create wine-quality-url))
          dataset (api/get-final (dataset/create source))

          _ (println "Beginning to construct the forest")
          models (vec (repeatedly 20 #(model/create dataset
                                                    :sample_rate 0.6
                                                    :randomize true
                                                    :stat_pruning false)))

          _ (println "Waiting for the trees to finish")
          models (mapv api/get-final models)

          _ (println "Building prediction fns for each tree")
          predictors (mapv model/predictor models)

          _ (println "Combining tree predictors into a forest predicton fn")
          f-predictor #(/ (reduce + ((apply juxt predictors) %))
                          (count predictors))]

      (println "Make a few predictions using the forest:")
      (doseq [{:keys [input quality]} wine-quality-examples]
        (println "\tActual Quality:" quality " - "
                 "\tPredicted Quality:" (f-predictor input)))

      (println "Cleaning up by deleting the resources from BigML")
      (api/delete source)
      (api/delete dataset)
      (mapv api/delete models))))


(defn- train-and-test-example []
  ;; This example uses the well-known iris dataset
  ;; (http://en.wikipedia.org/wiki/Iris_flower_data_set). We use the
  ;; optional sampling parameters to train the model on 2/3 of the
  ;; data and evaluate the results on the remaining 1/3.

  (binding [api/*username* nil ;; You're BigML username and API-key go here
            api/*api-key* nil]
    (let [_ (println "Building the source and dataset for iris")
          source (api/get-final (source/create "test/data/iris.csv.gz"))
          dataset (api/get-final (dataset/create source))

          _ (println "Training a pruned tree using 2/3 of the data")
          model (api/get-final (model/create dataset
                                             :sample_rate 0.66
                                             :seed "iris-test"
                                             :stat_pruning true))

          _ (println "Evaluating the model on the remaining 1/3 of the data")
          evaluation (api/get-final (evaluation/create model dataset
                                                       :sample_rate 0.66
                                                       :seed "iris-test"
                                                       :out_of_bag true))]
      (println "Test set accuracy:"
               (:accuracy (:model (:result evaluation))))

      (println "Cleaning up by deleting the resources from BigML")
      (mapv api/delete [source dataset model evaluation]))))
