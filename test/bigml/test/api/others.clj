;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.others
  "Contains unit tests for various functions."
  (:require (bigml.api [utils :as utils]))
  (:require (bigml.api [core :as api]))
  (:use clojure.test))

(defn normalized-resource-test 
  "Tests for utils/normalized-resource. Each test sample shall contain:
    - the resource to normalize (a map);
    - the by-name flag (true of false);
    - the expected outcome (true or false)."
  [resource by-name]
  (try
    (utils/normalized-resource resource
                               by-name)
    (catch Exception e (if (.contains (.getMessage e) "404")
                         "exception"
                         (throw e)))))

(deftest normalized-resource-test-1
  (let [r {:resource "sample/uuid"}]
    (is (= (normalized-resource-test r false) "exception"))))

(deftest normalized-resource-test-2
  (let [r {:resource "sample/uuid" :input_fields ""}]
    (is (= (normalized-resource-test r false) r))))

(deftest normalized-resource-test-3
  (let [r {:resource "sample/uuid"  :input_fields ""}]
    (is (= (normalized-resource-test r true) "exception"))))

(deftest normalized-resource-test-4
  (let [r {:resource "sample/uuid"  :input_fields "" :fields ""}]
    (is (= (normalized-resource-test r true) r))))
