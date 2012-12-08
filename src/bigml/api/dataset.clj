;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.dataset
  (:require (bigml.api [resource :as resource]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a dataset."
  [source & params]
  (let [params (apply resource/query-params params)
        form-params (assoc (dissoc params :username :api_key)
                      :source (resource/location source))
        auth-params (select-keys params [:username :api_key])]
    (resource/create :dataset
                     {:content-type :json
                      :form-params form-params
                      :query-params auth-params})))

(defn list
  "Retrieves a list of datasets."
  [& params]
  (apply resource/list :dataset params))
