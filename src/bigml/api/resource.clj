;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.resource
  (:require (clj-http [client :as client]))
  (:refer-clojure :exclude [get list]))

(def ^:private api-version "andromeda")
(def ^:private api-base "https://bigml.io")

(defn- resource-type-url [resource-type]
  (str api-base "/" api-version "/" (name resource-type)))

(defn- resource-url [resource]
  (str api-base "/" api-version "/" resource))

(defn query-params [& {:keys [username api_key] :as params}]
  (let [env (System/getenv)
        username (if username username (clojure.core/get env "BIGML_USERNAME"))
        api_key (if api_key api_key (clojure.core/get env "BIGML_API_KEY"))]
    (if (and username api_key)
      (assoc params :username username :api_key api_key)
      (throw (Exception. "No authentication defined.")))))

(defn create
  "Create a resource."
  [resource-type params]
  (let [params (assoc params :as :json)
        {:keys [body status]}
        (client/post (resource-type-url resource-type) params)]
    (with-meta body {:http-status status})))

(defn list
  "Retrieves a list of the desired resource type."
  [resource-type & params]
  (let [{:keys [status body]}
        (client/get (resource-type-url resource-type)
                    {:query-params (apply query-params params)
                     :as :json})]
    (let [{:keys [meta objects]} body]
      (with-meta objects (assoc meta :http-status status)))))

(defn update
  "Updates the specified resource.  Returns the updated resource upon
   success."
  [resource updates & params]
  (let [resource (if (map? resource) (:resource resource) resource)
        {:keys [status body]}
        (client/put (resource-url resource)
                    {:query-params (apply query-params params)
                     :form-params updates
                     :content-type :json
                     :as :json})]
    (with-meta body {:http-status status})))

(defn delete
  "Deletes the specified resource.  Returns nil upon success."
  [resource & params]
  (let [resource (if (map? resource) (:resource resource) resource)]
    (when (client/delete (resource-url resource)
                         {:query-params (apply query-params params)}))))

(defn get
  "Retrieves a resource."
  [resource & params]
  (let [resource (if (map? resource) (:resource resource) resource)
        {:keys [status body]}
        (client/get (resource-url resource)
                    {:query-params (apply query-params params)
                     :as :json})]
    (with-meta body {:http-status status})))
