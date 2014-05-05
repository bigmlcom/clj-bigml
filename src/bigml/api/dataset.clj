;; Copyright 2012, 2014 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.dataset
  "Offers functions specific for BigML datasets.
      https://bigml.com/developers/datasets"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn- create* [origin-tag origin params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      origin-tag (api/resource-id origin))
        auth-params (select-keys params api/auth-params)]
    (api/create :dataset
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))

(defn create
  "Creates a dataset given a source. The source may be either a string
   representing the source id (`source/123123`), or a map with either
   the full source (as returned with `get`) or a partial source (as
   returned with `list`).

   Accepts the optional creation parameters defined in the BigML API
   docs:
      https://bigml.com/developers/datasets#d_create

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [source & params]
  (create* :source source params))

(defn clone
  "Clones a given dataset, represented either as a full resource (as
   returned by, e.g., `get`, or as string id.

   The optional parameters map can contain any of the options described
   in https://bigml.com/developers/transformations#t_cloning

   Error handling is analogous to that of `create`, which see."
  [dataset & params]
  (create* :origin_dataset dataset params))

(defn list
  "Retrieves a list of datasets. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/datasets#s_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :dataset params))
