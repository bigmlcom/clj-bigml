;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.utils
  "Offers functions specific for BigML clusters.
     https://bigml.com/developers/clusters"
  (:require (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn create
  "Create a resource from another.

   The origin-tag identifies the type of resource to create, e.g. :source;
   origin is either a string or a map."
  [target-tag origin-tag origin params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params)
                      origin-tag (api/resource-id origin))
        auth-params (select-keys params api/auth-params)]
    (api/create target-tag
                (:dev_mode params)
                {:content-type :json
                 :throw-exceptions (:throw-exceptions params true)
                 :form-params (dissoc form-params :throw-exceptions)
                 :query-params auth-params})))
