;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.source
  (:import (java.io ByteArrayInputStream File StringWriter)
           (org.apache.commons.validator.routines UrlValidator))
  (:require (clojure.java [io :as io])
            (clojure.data [json :as json]
                          [csv :as csv])
            (bigml.api [core :as api]))
  (:refer-clojure :exclude [list]))

(defn- url? [url]
  (.isValid (UrlValidator.) url))

(defn- create-type [v & _]
  (cond (and (coll? v) (coll? (first v))) :collection
        (url? v) :url
        (instance? File v) :file
        (string? v) :file))

(defmulti create
  "Creates a source given either a url, a file, or a sequence of
   sequences (each inner sequence representing a row, and intended for
   small amounts of data).  Accepts the optional creation parameters
   defined in the BigML API docs:
      https://bigml.com/developers/sources#s_create"
  create-type)

(defmethod create :url [url & params]
  (let [params (apply api/query-params params)
        form-params (assoc (apply dissoc params api/conn-params) :remote url)
        auth-params (select-keys params api/auth-params)]
    (api/create :source
                (:dev_mode params)
                {:content-type :json
                 :form-params form-params
                 :query-params auth-params})))

(defmethod create :file [file & params]
  (let [file (io/file file)
        params (apply api/query-params params)
        form-params (apply dissoc params api/conn-params)
        auth-params (select-keys params api/auth-params)
        multipart (map (fn [[k v]] {:name (name k)
                                    :content (if (map? v)
                                               (json/json-str v)
                                               (str v))})
                       form-params)
        multipart (conj multipart {:name "file" :content file})]
    (api/create :source
                (:dev_mode params)
                {:multipart multipart
                 :query-params auth-params})))

(defmethod create :collection [coll & params]
  (with-open [writer (StringWriter.)]
    (csv/write-csv writer coll)
    (let [params (apply api/query-params params)
          form-params (assoc (apply dissoc params api/conn-params)
                        :data (str writer))
          auth-params (select-keys params api/auth-params)]
      (api/create :source
                  (:dev_mode params)
                  {:content-type :json
                   :form-params form-params
                   :query-params auth-params}))))

(defmethod create :default [& _]
  (throw (Exception. "Unrecognized artifact for source creation.")))

(defn list
  "Retrieves a list of data sources. Optional parameters are supported
   for pagination and filtering. Details are available here:
      https://bigml.com/developers/sources#s_list"
  [& params]
  (apply api/list :source params))
