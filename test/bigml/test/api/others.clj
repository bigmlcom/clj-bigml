;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.others
  "Contains unit tests for various functions."
  (:require (bigml.api [utils :as utils]
                       [core :as api]))
  (:use clojure.test))

(deftest normalized-resource-test-1
  (let [r {:resource "sample/uuid"}]
    (is (thrown-with-msg? Exception #"404"
                          (utils/normalized-resource r false)))))

(deftest normalized-resource-test-2
  (let [r {:resource "sample/uuid" :input_fields ""}]
    (is (= (utils/normalized-resource r false) r))))

(deftest normalized-resource-test-3
  (let [r {:resource "sample/uuid"  :input_fields ""}]
    (is (thrown-with-msg? Exception #"404"
                          (utils/normalized-resource r true)))))

(deftest normalized-resource-test-4
  (let [r {:resource "sample/uuid"  :input_fields "" :sample {:fields ""}}]
    (is (= (utils/normalized-resource r true) r))))

(deftest normalized-resource-test-5
  (let [r {:resource "sample/uuid"  :input_fields "" :sample {:fields ""}}]
    (is (= (utils/normalized-resource r false) r))))

(deftest normalized-resource-test-6
  (let [r {:resource "sample/uuid"  :input_fields "" :samplee {:fields ""}}]
    (is (thrown-with-msg? Exception #"404"
                          (utils/normalized-resource r true) r))))

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

(deftest get-keypath-test-1
  (let [r {:0 "0" :1 { :2 "2" :3 "3"}}]
      (is (= (#'utils/get-keypath r [:0]) r))))

(deftest get-keypath-test-2
  (let [r {:0 "0" :1 { :2 "2" :3 { :4 "4" }}}]
      (is (= (#'utils/get-keypath r [:1 :3 :4]) r))))

(deftest get-keypath-test-3
  (let [r {:0 "0" :1 { :2 "2" :3 "3"}}]
      (is (= (#'utils/get-keypath r [:5]) nil))))

(deftest get-keypath-test-4
  (is (= (#'utils/get-keypath {} [:0]) nil)))

(deftest get-keypath-test-5
  (is (= (#'utils/get-keypath "" [:5]) nil)))
