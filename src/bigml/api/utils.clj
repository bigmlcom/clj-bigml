;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.utils
  "Offers functions specific for BigML clusters.
     https://bigml.com/developers/clusters"
  (:require [clojure.set :as set])
  (:require (bigml.api [core :as api])))

(defn resource-type
  "Returns the resource type of the specified resource.
   The resource can be specified through:
    - its full UUID, e.g., 'model/12234567890';
    - a minimal map-representation, e.g., { :resource '...' };
    - a full JSON representation."
  [resource]
  (let [resource (or (:resource resource) (str resource))]
    (first (clojure.string/split resource #"/"))))

(defn- if-contains
  "Returns the passed map m if it contains the given keypath ks,
  otherwise nil. A keypath is a sequence of keys for recursive lookup."
  [m ks]
  (and (get-in m ks) m))

(defn normalized-resource
  "Makes sure the passed resource contains enough information for input
   normalization (i.e., :input_fields and additionally :restype/field
   when by-name is true. If it does not, then the resoure is fetched."
  [resource by-name]
  (let [ res-type (resource-type resource)]
   (or (and (if-contains resource [:input_fields])
            (or (if-contains resource [(keyword res-type) :fields])
                (and (not by-name) resource)))
       (api/get-final resource))))

(defn- field-name-mapping
  "Returns a map that associates the passed resource's field names to
  the respective field IDs. E.g.:
      {'petal width' '000001' 'sepal length '000002' }"
  [resource]
  (let [res-type (resource-type resource)]
    (reduce #(assoc %1 (:name (second %2)) (first %2))
            {}
            (seq (:fields ((keyword res-type) resource))))))

(defn normalized-inputs
  "Converts inputs to their map normal form if provided as list,
   in which case they are associated to input fields in the order
   the latter appeared during training."
  [resource inputs by-name]
  (let [resource (or (normalized-resource resource by-name)
                      (throw (Exception. "Inaccessible/wrong resource")))
        input-fields (:input_fields resource)]
    (if (and (map? inputs) by-name)
      (set/rename-keys inputs (field-name-mapping resource))
      (apply hash-map (flatten (map list input-fields inputs))))))

(defn get-form-params-in
  "Filters parameters that are meant to be sent as HTTP form fields."
  [params]
  (apply dissoc params (conj api/conn-params :dev_mode :by-field-name)))

(defn create
  "Creates a resource from another.

   The `target` parameter identifies the type of resource to create,
   `origin` is either a tuple containing the type of the resource used
   to create `target` and its id string, or a map containing the resource
   as returned by `get` or `list`. E.g.:

     (create :target :model :origin [:dataset '123123']
     (create :target :prediction :origin [:model '123123'] :inputs {...}"
  [& {:keys [target origin params inputs]}]
  (let [origin-id (api/resource-id (peek origin))
        by-name (if (coll? params)
                  (:by-field-name (apply hash-map params))
                  false)
        inputs (if (and inputs
                        (or (not (map? inputs)) by-name)
                        (coll? inputs))
                 (normalized-inputs origin-id inputs by-name)
                 inputs)
        params (apply api/query-params params)
        form-params (assoc (get-form-params-in params)
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
