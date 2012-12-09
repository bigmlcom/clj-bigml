;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.resource
  (:require (clj-http [client :as client]))
  (:refer-clojure :exclude [get list]))

(def ^:private api-version "andromeda")
(def ^:private api-base "https://bigml.io")

(defn location
  "Returns the resource location."
  [resource]
  (if (map? resource) (:resource resource) resource))

(defn- resource-type-url [resource-type]
  (str api-base "/" api-version "/" (name resource-type)))

(defn- resource-url [resource]
  (str api-base "/" api-version "/" (location resource)))

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
  (let [{:keys [status body]}
        (client/put (resource-url resource)
                    {:query-params (apply query-params params)
                     :form-params updates
                     :content-type :json
                     :as :json})]
    (with-meta body {:http-status status})))

(defn delete
  "Deletes the specified resource.  Returns nil upon success."
  [resource & params]
  (when (client/delete (resource-url resource)
                       {:query-params (apply query-params params)})))

(defn get
  "Retrieves a resource."
  [resource & params]
  (let [{:keys [status body]}
        (client/get (resource-url resource)
                    {:query-params (apply query-params params)
                     :as :json})]
    (with-meta body {:http-status status})))

(defn status-code
  "Return the status code of the resource as a keyword."
  [resource]
  ({0 :waiting 1 :queued 2 :started 3 :in-progress 4 :summarized
    5 :finished -1 :faulty -2 :unknown -3 :runnable}
   (:code (:status resource))))

(defn finished?
  "Returns true if the resource's status is finished."
  [resource]
  (= :finished (status-code resource)))

(defn final?
  "Returns true if the resource is final, meaning either finished or
  an error state (faulty, unknown, and runnable)."
  [resource]
  (#{:finished :faulty :unknown :runnable} (status-code resource)))

(def ^:private decay-rate 1.618)

(defn get-final
  "Retries GETs to the resource until it is finished."
  [resource & params]
  (loop [sleep-time 500]
    (let [result (apply get resource params)]
      (if (final? result)
        result
        (do (Thread/sleep (long sleep-time))
            (recur (min (* decay-rate sleep-time) 120000)))))))
