;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.prediction
  "Offers functions specific for BigML predictions.
      https://bigml.com/developers/predictions"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a prediction given a model and the field inputs.

   The inputs may either be a map (field ids to values), or a
   sequence of the inputs fields in the order they appeared during
   training.

   This function also accepts the optional creation parameters defined
   in the BigML API docs:
      https://bigml.com/developers/predictions#p_create"
  [model inputs & params]
  (let [inputs (if (and (not (map? inputs)) (coll? inputs))
                 (api/convert-inputs model inputs)
                 inputs)
        params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      :model (api/location model)
                      :input_data inputs)
        auth-params (select-keys params api/auth-params)]
    (api/create :prediction
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defn list
  "Retrieves a list of predictions. Optional parameters are supported
   for pagination and filtering.  Details are available here:
      https://bigml.com/developers/predictions#p_list"
  [& params]
  (apply api/list :prediction params))

(defn output
  "Returns the prediction output."
  [prediction]
  (let [output-fn #(first (vals (:prediction %)))]
    (or (output-fn prediction)
        (output-fn (api/get-final prediction)))))
