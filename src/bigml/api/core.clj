;; Copyright 2012, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.core
  "Offers generic functions for interacting with BigML resources."
  (:require (clj-http [client :as client]))
  (:refer-clojure :exclude [get list]))

(def ^:dynamic *connection* nil)

(def api-version "andromeda")
(def api-base "https://bigml.io")

(def auth-params
  "Parameters used for authentication with the BigML API."
  #{:username :api_key})

(def conn-params
  "Parameters used for connecting with the BigML API."
  (conj auth-params :dev_mode))

(defn make-connection
  "Creates a connection intended for 'with-connection'. Requires a
   username and api-key and optionally accepts a boolean for
   development mode."
  [username api-key & [dev-mode]]
  {:username username
   :api-key api-key
   :dev-mode dev-mode})

(defmacro with-connection
  "Executes the body given the connection information."
  [connection & body]
  `(binding [*connection* (merge *connection* ~connection)]
     ~@body))

(defmacro with-dev-mode
  "Executes the body with development mode enabled."
  [& body]
  `(binding [*connection* (merge *connection* {:dev-mode true})]
     ~@body))

(defmacro with-mode
  "Executes the body with development mode enabled or disabled
   according to the first argument."
  [dev & body]
  `(binding [*connection* (merge *connection* {:dev-mode (= ~dev :dev)})]
     ~@body))

(defn query-params
  "Transforms a list of parameters into a map of query parameters
   including the connection specific keys (:username, :api_key,
   and :dev_mode).  Throws an exception when no authentication
   information is found."
  [& {:keys [username api_key dev_mode] :as params}]
  (let [env (System/getenv)
        username (or username
                     (:username *connection*)
                     (clojure.core/get env "BIGML_USERNAME"))
        api_key (or api_key
                    (:api-key *connection*)
                    (clojure.core/get env "BIGML_API_KEY"))
        dev_mode (or dev_mode
                     (:dev-mode *connection*)
                     (Boolean/valueOf (clojure.core/get env "BIGML_DEV_MODE")))]
    (if (and username api_key)
      (assoc params :username username :api_key api_key :dev_mode dev_mode)
      (throw (Exception. "No authentication defined.")))))

(defn resource-id
  "Returns the resource id."
  [resource]
  (if (map? resource) (:resource resource) resource))

(defn- resource-base [dev-mode]
  (str api-base "/" (if dev-mode "dev/" "") api-version "/"))

(defn- resource-type-url [resource-type dev-mode]
  (str (resource-base dev-mode) (name resource-type)))

(defn- resource-url [resource dev-mode]
  (str (resource-base dev-mode) (resource-id resource)))

(defn create
  "Create a resource given a resouce-type, a boolean for development
   mode, and a map of parameters formatted for a clj-http POST.  It's
   recommended to use the more friendly create functions in the
   source, dataset, model, evaluation, and prediction namespaces.

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [resource-type dev-mode params]
  (let [params (assoc params :as :json)
        {:keys [body] :as response}
        (client/post (resource-type-url resource-type dev-mode) params)]
    (if (client/success? response)
      (with-meta body (dissoc response :body))
      response)))

(defn list
  "Retrieves a list of the desired resource type. The list will
   contain partial representations of the resource.  Optional
   parameters can include pagination and filtering options as detailed
   here:
      Sources     - https://bigml.com/developers/sources#s_list
      Datasets    - https://bigml.com/developers/datasets#d_list
      Models      - https://bigml.com/developers/models#m_list
      Predictions - https://bigml.com/developers/predictions#p_list
      Evaluations - https://bigml.com/developers/evaluations#e_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [resource-type & params]
  (let [params (apply query-params params)
        {:keys [body] :as response}
        (client/get (resource-type-url resource-type (:dev_mode params))
                    {:query-params (dissoc params :dev_mode :throw-exceptions)
                     :throw-exceptions (:throw-exceptions params true)
                     :as :json})]
    (if (client/success? response)
      (let [{:keys [meta objects]} body]
        (with-meta objects (merge meta (dissoc response :body))))
      response)))

(defn update
  "Updates the specified resource given a map of attributes to be
   updated. The resource may be either a string representing the
   resource id (`model/123123`), or a map with either the full
   resource (as returned with `get`) or a partial resource (as
   returned with `list`).

   Returns the updated resource upon success. The valid attributes for
   updating are listed in the BigML API docs according to resource
   type:
      Sources     - https://bigml.com/developers/sources#s_update
      Datasets    - https://bigml.com/developers/datasets#d_update
      Models      - https://bigml.com/developers/models#m_update
      Predictions - https://bigml.com/developers/predictions#p_update
      Evaluations - https://bigml.com/developers/evaluations#e_update

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [resource updates & params]
  (let [params (apply query-params params)
        {:keys [body] :as response}
        (client/put (resource-url resource (:dev_mode params))
                    {:query-params (dissoc params :dev_mode :throw-exceptions)
                     :throw-exceptions (:throw-exceptions params true)
                     :form-params updates
                     :content-type :json
                     :as :json})]
    (if (client/success? response)
      (with-meta body (dissoc response :body))
      response)))

(defn delete
  "Deletes the specified resource. The resource may be either a string
   representing the resource id (`model/123123`), or a map with either
   the full resource (as returned with `get`) or a partial
   resource (as returned with `list`). Returns true upon success.

   Exceptions are thrown on failure unless :throw-exceptions is set as
   false (default is true), in which case the HTTP response details
   are returned as a map on failure."
  [resource & params]
  (let [params (apply query-params params)
        {:keys [body] :as response}
        (client/delete (resource-url resource (:dev_mode params))
                       {:query-params (dissoc params :dev_mode :throw-exceptions)
                        :throw-exceptions (:throw-exceptions params true)})]
    (if (client/success? response)
      true
      response)))

(defn get
  "Retrieves the current resource. The resource may be either a string
   representing the resource id (`model/123123`), or a map with either
   the full resource (as returned with `get`) or a partial
   resource (as returned with `list`). The HTTP response information
   is attached as meta data.

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [resource & params]
  (let [params (apply query-params params)
        {:keys [body] :as response}
        (client/get (resource-url resource (:dev_mode params))
                    {:query-params (dissoc params :dev_mode :throw-exceptions)
                     :throw-exceptions (:throw-exceptions params true)
                     :as :json})]
    (if (client/success? response)
      (with-meta body (dissoc response :body))
      response)))

(defn status-code
  "Return the status code of the resource as a keyword.
   https://bigml.com/developers/status_codes"
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
(def ^:private init-period 500)
(def ^:private max-period 120000)

(defn get-final
  "Retries GETs to the resource until it is finalized.
   Accepts :max-wait as an optional parameter.  This limits the
   time (in milliseconds) that get-final will wait for a finalized
   resource. If a finalized resource hasn't occured by the limit,
   get-final will return nil."
  [resource & {:keys [max-wait] :as params}]
  (let [start (System/currentTimeMillis)]
    (loop [sleep-period init-period]
      (let [result (apply get resource (flatten (seq (dissoc params :max-wait))))]
        (cond (final? result) result
              (and max-wait (> (- (System/currentTimeMillis) start) max-wait)) nil
              :else (do (Thread/sleep (long sleep-period))
                        (recur (min max-period (* decay-rate sleep-period)))))))))
