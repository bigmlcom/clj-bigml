;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.utils
  "Offers functions specific for BigML clusters.
     https://bigml.com/developers/clusters"
  (:require (bigml.api [core :as api])))

(defn normalized-inputs
  "Converts inputs to their map normal form if provided as list,
   in which case they are associated to input fields in the order
   the latter appeared during training."
  [resource inputs]
  (let [input-fields (or (:input_fields resource)
                         (:input_fields (api/get-final resource))
                         (throw (Exception. "Inaccessible/wrong resource")))]
    (apply hash-map (flatten (map list input-fields inputs)))))

(defn create
  "Create a resource from another.

   The `target` parameter identifies the type of resource to create,
   `origin` is either a tuple containing the type of the resource used
   to create `target` and its id string, or a map containing the resource
   as returned by `get` or `list`. E.g.:

     (create :target :model :origin [:dataset '123123']
     (create :target :prediction :origin [:model '123123'] :inputs {...}"
  [& {:keys [target origin params inputs]}]
  (let [origin-id (api/resource-id (peek origin))
        inputs (if (and inputs (not (map? inputs)) (coll? inputs))
                 (normalized-inputs origin-id inputs)
                 inputs)
        params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      (first origin) origin-id)
        form-params (if inputs
                      (assoc form-params :input_data inputs)
                      form-params)
        auth-params (select-keys params api/auth-params)]
    (api/create target
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))
