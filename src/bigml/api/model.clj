;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.model
  "Offers functions specific for BigML models.
      https://bigml.com/developers/models"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a model given a dataset. The dataset may be either a string
   representing the dataset id (`dataset/123123`), or a map with
   either the full dataset (as returned with `get`) or a partial
   dataset (as returned with `list`).

   Accepts the optional creation parameters defined in the BigML API
   docs:
      https://bigml.com/developers/models#m_create

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as true (default
   is false), in which case the HTTP response details are returned as
   a map on failure."
  [dataset & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :dataset (api/resource-id dataset))
        auth-params (select-keys params api/auth-params)]
    (api/create :model
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))

(defn list
  "Retrieves a list of models. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/models#s_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as true (default is false),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :model params))
