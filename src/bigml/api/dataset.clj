;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.dataset
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a dataset given a source. Accepts the optional creation
   parameters defined in the BigML API docs:
      https://bigml.com/developers/datasets#d_create"
  [source & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :source (api/location source))
        auth-params (select-keys params api/auth-params)]
    (api/create :dataset
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defn list
  "Retrieves a list of datasets. Optional parameters are supported
   for pagination and filtering. Details are available here:
      https://bigml.com/developers/datasets#d_list"
  [& params]
  (apply api/list :dataset params))
