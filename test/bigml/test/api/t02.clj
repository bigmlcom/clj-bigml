;; Copyright 2012, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.t02
  "Testing prediction creation in DEV mode"
  (:require (bigml.api [core :as api])
            (bigml.test.api [utils :as test]))
  (:use clojure.test))

(deftest ts01
  "Successfully creating a prediction in DEV mode"
  (api/with-dev-mode true
    (let [test-data ["test/data/iris.csv.gz"
                     {:prediction [1.44 0.54 2.2]}
                     "Iris-setosa"]
          prediction (test/create-get-cleanup
                      [:source :dataset :model :prediction] test-data)]
      (is (= (last test-data) (first (vals (:prediction prediction))))))))
