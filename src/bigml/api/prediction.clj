;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.prediction
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a prediction."
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
  "Retrieves a list of predictions."
  [& params]
  (apply api/list :prediction params))

(defn output
  "Returns the prediction output."
  [prediction]
  (let [output-fn #(first (vals (:prediction %)))]
    (or (output-fn prediction)
        (output-fn (api/get-final prediction)))))
