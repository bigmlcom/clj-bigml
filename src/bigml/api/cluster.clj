;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.cluster
  "Offers functions specific for BigML clusters.
     https://bigml.com/developers/clusters"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a cluster from a dataset. A dataset can be represented through its
  id (e.g., `cluster/123123`), or a map as returned by `get` or `list`.
  
  Accepts the optional creation parameters described in the BigML API docs:
     https://bigml.com/developers/clusters#cl_cluster_arguments

  HTTP response information is attached as meta data. Exceptions are thrown
  on failure unless :throw-exceptions is set to false (default is true),
  in which case the HTTP response details are returned as a map on failure."
  [dataset & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :dataset (api/resource-id dataset))
        auth-params (select-keys params api/auth-params)]
    (api/create :cluster
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))

(defn list
  "Retrieves a list of clusters. The optional parameters can include
  pagination and filtering options detailed here:
     https://bigml.com/developers/clusters#cl_listing_clusters

  Pagination details are returned as meta data attached to the list, along
  with the HTTP response information. Exceptions are thrown on failure unless
  :throw-exceptions is set to false (default is true), in which case the
  HTTP response details are returned in a map on failure."
  [& params]
  (apply api/list :cluster params))
