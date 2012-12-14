;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.model
  "Offers functions specific for BigML models.
      https://bigml.com/developers/models"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a model given a dataset. Accepts the optional creation
   parameters defined in the BigML API docs:
      https://bigml.com/developers/models#m_create"
  [dataset & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :dataset (api/location dataset))
        auth-params (select-keys params api/auth-params)]
    (api/create :model
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defn list
  "Retrieves a list of models. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/developers/models#s_list

   Pagination details are returned as meta information attached to the
   list."
  [& params]
  (apply api/list :model params))
