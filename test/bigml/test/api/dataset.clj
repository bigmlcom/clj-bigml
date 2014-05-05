;; Copyright 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.dataset
  (:use clojure.test)
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset])
            (clojure [data :as data])))

(defn- clean-up [ds]
  (dissoc (update-in ds [:status] dissoc :elapsed) :resource :created :updated))

(defn- create-and-test [artifact & params]
  (let [initial (api/get-final (apply dataset/create artifact params))
        retrieved (api/get (:resource initial))]
    (is (and initial retrieved))
    (is (= initial retrieved) (data/diff initial retrieved))
    retrieved))

(def ^:dynamic iris-src nil)

(defn with-iris-src [f]
  (let [src (api/get-final (source/create "test/data/iris.csv.gz"))]
    (binding [iris-src src]
      (try (f) (finally (api/delete src))))))

(use-fixtures :once with-iris-src)

(deftest datasets
  (let [ds (create-and-test iris-src)
        ds2 (create-and-test (:resource iris-src))]
    (testing "Dataset creation from sources"
      (is (= (clean-up ds) (clean-up ds2)) (data/diff ds ds2))
      (is (api/delete ds2)))
    (testing "Dataset cloning"
      (let [clc #(dissoc (clean-up %)
                         :status :origin_dataset :download :range
                         :ranges :sample_rates :out_of_bag
                         :replacement :sample_rate :seed :seeds
                         :replacements :name)
            dsc (api/get-final (dataset/clone ds))
            dsc2 (api/get-final (dataset/clone (:resource dsc)))]
        (is (= (clc dsc) (clc ds)) (data/diff (clc dsc) (clc ds)))
        (is (= (clc dsc) (clc dsc2)) (data/diff (clc dsc) (clc dsc2)))
        (is (api/delete dsc))
        (is (api/delete dsc2))))
    (is (api/delete ds))))
