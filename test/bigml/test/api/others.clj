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

(deftest field-name-mapping-test-1
  (let [r {:resource "model/uuid"
           :model {:fields
                   { "01" { :name "a" }
                     "02" { :name "b" }}}}]
    (is (.equals (#'bigml.api.utils/field-name-mapping r)
                  {"a" "01" "b" "02"}))))

(deftest field-name-mapping-test-2
  (let [r {:resource "ensemble/uuid"
           :ensemble {:fields
                   { "01" { :name "a" }
                     "02" { :name "b" }}}}]
    (is (.equals (#'bigml.api.utils/field-name-mapping r)
                  {"a" "01" "b" "02"}))))

(deftest field-name-mapping-test-3
  (let [r {:resource "model/uuid"}]
    (is (.equals (#'bigml.api.utils/field-name-mapping r) {}))))

(deftest field-name-mapping-test-4
  (let [r {}]
    (is (.equals (#'bigml.api.utils/field-name-mapping r) {}))))

(deftest resource-type-test-1
  (is (= (utils/resource-type "model/12345") "model")))

(deftest resource-type-test-2
  (is (= (utils/resource-type {:resource "model/12345"}) "model")))

(deftest resource-type-test-3
  (is (= (utils/resource-type {}) "{}")))

(deftest resource-type-test-4
  (is (= (utils/resource-type "") "")))

(deftest if-contains-test-1
  (let [r {:0 "0" :1 { :2 "2" :3 "3"}}]
      (is (= (#'utils/if-contains r [:0]) r))))

(deftest if-contains-test-2
  (let [r {:0 "0" :1 { :2 "2" :3 { :4 "4" }}}]
      (is (= (#'utils/if-contains r [:1 :3 :4]) r))))

(deftest if-contains-test-3
  (let [r {:0 "0" :1 { :2 "2" :3 "3"}}]
      (is (= (#'utils/if-contains r [:5]) nil))))

(deftest if-contains-test-4
  (is (= (#'utils/if-contains {} [:0]) nil)))

(deftest if-contains-test-5
  (is (= (#'utils/if-contains "" [:5]) nil)))
