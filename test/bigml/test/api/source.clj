;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.source
  (:require (bigml.api [core :as api]
                       [source :as source]))
  (:use clojure.test))

(defn- create-and-test [artifact & params]
  (let [initial (api/get-final (apply source/create artifact params))
        source-name (str "test-source" (rand-int 1000))
        updated (api/update initial {:name source-name})
        retrieved (api/get updated)
        deleted (api/delete retrieved)]
    (is (and initial updated retrieved))
    (is (true? deleted))
    (is (thrown? Exception (api/get initial)))
    (is (= source-name (:name updated) (:name retrieved)))))

(deftest sources
  (testing "Source creation from file"
    (create-and-test "test/data/iris.csv.gz"))
  (testing "Source creation from url"
    (create-and-test "https://static.bigml.com/csv/iris.csv"))
  (testing "Source creation from collection"
    (create-and-test [["a" 1 4] ["b" 2 3] ["c" 3 2] ["d" 4 1]])))
