;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.evaluation
  "Offers functions specific for BigML evaluations.
      https://bigml.com/developers/evaluations"
  (:require (bigml.api [core :as api]
                       [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates an evaluation given a model and a dataset. The model and
   the dataset may be either a strings representing resource
   ids (`model/123123`), or maps with either the full resource (as
   returned with `get`) or a partial resource (as returned with
   `list`).

   Accepts the optional creation parameters defined in the BigML API
   docs:
      https://bigml.com/developers/evaluations#e_create

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [model dataset & params]
  (let [params (apply api/query-params params)
        form-params (assoc (utils/get-form-params-in params)
                      :model (api/resource-id model)
                      :dataset (api/resource-id dataset))
        auth-params (select-keys params api/auth-params)]
    (api/create :evaluation
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))

(defn list
  "Retrieves a list of evaluations. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/evaluations#s_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :evaluation params))
