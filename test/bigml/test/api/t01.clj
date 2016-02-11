;; Copyright 2012, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.t01
  "Contains a few examples that exercise BigML's API"
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [prediction :as prediction]
                       [evaluation :as evaluation])
            (clojure [data :as data]))
  (:use clojure.test))

;; this should go into utils
(defn- do-verb [verb res-type res-uuid & params]
  (let [resource (api/get-final (apply
                                (ns-resolve
                                 (symbol (clojure.string/join
                                          ["bigml.api." res-type]))
                                 (symbol verb))
                                res-uuid params))]
     (api/get (:resource resource))))

(defn- create [res-type res-uuid & params]
   (apply do-verb "create" res-type res-uuid params))

(deftest t01
  (api/with-mode :dev
    (let [src (api/get-final (source/create "test/data/iris.csv.gz"))
          dst (create "dataset" (:resource src))
          mdl (create "model" (:resource dst))
          prd (create "prediction" (:resource mdl) {"petal width" 0.5})]
      (is (= "Iris-setosa" (first (vals (:prediction prd))))))))
