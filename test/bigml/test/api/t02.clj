;; Copyright 2012-2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.t02
  "Testing prediction creation in DEV mode"
  (:require (bigml.api [core :as api])
            (bigml.test.api [utils :as test]))
  (:use clojure.test))

(deftest ts01
  ;; Creating a prediction in DEV mode
  (api/with-dev-mode true
    (let [test-data ["test/data/iris.csv.gz"
                     {:prediction {:input_data [1.44 0.54 2.2]}}
                     "Iris-setosa"]
          prediction (test/create-get-cleanup
                      [:source :dataset :model :prediction] test-data)]
      (is (= (last test-data) (first (vals (:prediction prediction))))))))
