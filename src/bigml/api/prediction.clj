;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.prediction
  (:require (bigml.api [resource :as resource]))
  (:refer-clojure :exclude [list]))

(defn- convert-inputs [model inputs]
  (let [input-fields (or (:input_fields model)
                         (:input_fields (resource/get-final model))
                         (throw (Exception. "Unaccessable model")))]
    (apply hash-map (flatten (map clojure.core/list input-fields inputs)))))

(defn create
  "Creates a prediction."
  [model inputs & params]
  (let [inputs (if (and (not (map? inputs)) (coll? inputs))
                 (convert-inputs model inputs)
                 inputs)
        params (apply resource/query-params params)
        form-params (assoc (dissoc params :username :api_key)
                      :model (resource/location model)
                      :input_data inputs)
        auth-params (select-keys params [:username :api_key])]
    (resource/create :prediction
                     {:content-type :json
                      :form-params form-params
                      :query-params auth-params})))

(defn list
  "Retrieves a list of predictions."
  [& params]
  (apply resource/list :prediction params))

(defn output
  "Returns the prediction output."
  [prediction]
  (let [output-fn #(first (vals (:prediction %)))
        output (output-fn prediction)]
    (if output output (output-fn (resource/get-final prediction)))))
