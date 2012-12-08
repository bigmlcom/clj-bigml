;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.model
  (:require (bigml.api [resource :as resource]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a model."
  [dataset & params]
  (let [params (apply resource/query-params params)
        form-params (assoc (dissoc params :username :api_key)
                      :dataset (resource/location dataset))
        auth-params (select-keys params [:username :api_key])]
    (resource/create :model
                     {:content-type :json
                      :form-params form-params
                      :query-params auth-params})))

(defn list
  "Retrieves a list of models."
  [& params]
  (apply resource/list :model params))
