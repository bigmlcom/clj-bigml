;; Copyright 2016, 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.utils
  "Contains a few facilities especially meant tor testing"
  (:require (bigml.api [core :as api]
                       [source :as source]
                       [dataset :as dataset]
                       [model :as model]
                       [prediction :as prediction]
                       [evaluation :as evaluation]
                       [cluster :as cluster]
                       [centroid :as centroid])))

(defn authenticated-connection
  "Returns a properly authenticated connection.
  Credentials shall be provided through the BIGML_USERNAME
  and BIGML_API_KEY environment variables"
  []
  (let [username (System/getenv "BIGML_USERNAME")
        api-key (System/getenv "BIGML_API_KEY")]
    (api/make-connection username api-key)))

(def api-fns
  {:source {:create source/create}
   :dataset {:create dataset/create}
   :model {:create model/create}
   :cluster {:create cluster/create}
   :centroid {:create centroid/create}
   :evaluation {:create evaluation/create}
   :prediction {:create prediction/create}})

(defn- do-verb
  "This function is a sort of universal wrapper around individual create
   functions that reside in specific namespaces. it enables the syntax:
     (create source file-path options)
   in place of the less flexible
     (bigml.api.source/create file-path options)"
  [verb res-type res-uuid params]
  (let [inputs (:input_data params)
        params (:options params)]
   (if (nil? inputs)
     (apply (get-in api-fns [res-type verb])
           res-uuid
           (flatten (seq params)))
     (apply (get-in api-fns [res-type verb])
           res-uuid
           inputs
           (flatten (seq params))))))

(defn- create-arg-type
  "This is used to select the proper implementation of the create multimethod"
  [x & _]
  (cond (= (type x) (type :keyword)) :single
        (= (type x) (type [])) :sequence
        :default :error))

(defmulti create
  "This function creates either a single resource, or a sequence of resources.
   It returns the UUID of the resource created or an array thereof.
   - res-type: resource type(s) to create, a keyword or an array thereof
   - res-uuid: UUID of the resource which is used to create the requested
         resource or the first one of the sequence; it can also be a file path/url
   - params: a list of options for the single-resource case, or
       a map from a resource type (represented by a keyword) and
       another map representing the options to use for that resource type"
  create-arg-type)

(defmethod create :single
  [res-type res-uuid & params]
  (api/get-final (apply do-verb :create res-type res-uuid params)))

(defmethod create :sequence
  [res-type res-uuid & params]
  (reduce
    #(conj %1 (:resource
               (create %2 (last %1) (%2 (first params)))))
    [res-uuid]
    res-type))

(defn create-get-cleanup
  "This function wraps create so it does a GET of the last resource
   returned by create and returns it; additionally, it deletes
   all resources created remotely."
  [res-type [res-uuid params]]
  (let [resources (create res-type res-uuid params)
        result (api/get-final (last resources))]
    (dorun (pmap api/delete (drop 1 resources)))
    result))
