;; Copyright 2012 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.source
  (:import (java.io ByteArrayInputStream File StringWriter)
           (org.apache.commons.validator.routines UrlValidator))
  (:require (clojure.java [io :as io])
            (clojure.data [json :as json]
                          [csv :as csv])
            (bigml.api [resource :as resource]))
  (:refer-clojure :exclude [get list]))

(defn- url? [url]
  (.isValid (UrlValidator.) url))

(defn- create-type [v & _]
  (cond (and (coll? v) (coll? (first v))) :collection
        (url? v) :url
        (instance? File v) :file
        (string? v) :file))

(defmulti create create-type)

(defmethod create :url [url & params]
  (let [params (apply resource/query-params params)
        form-params (assoc (dissoc params :username :api_key) :remote url)
        auth-params (select-keys params [:username :api_key])]
    (resource/create :source
                     {:content-type :json
                      :form-params form-params
                      :query-params auth-params})))

(defmethod create :file [file & params]
  (let [file (io/file file)
        params (apply resource/query-params params)
        form-params (dissoc params :username :api_key)
        auth-params (select-keys params [:username :api_key])
        multipart (map (fn [[k v]] {:name (name k)
                                    :content (if (map? v)
                                               (json/json-str v)
                                               (str v))})
                       form-params)
        multipart (conj multipart {:name "file" :content file})]
    (resource/create :source
                     {:multipart multipart
                      :query-params auth-params})))

(defmethod create :collection [coll & params]
  (with-open [writer (StringWriter.)]
    (csv/write-csv writer coll)
    (let [params (apply resource/query-params params)
          form-params (assoc (dissoc params :username :api_key)
                        :data (str writer))
          auth-params (select-keys params [:username :api_key])]
      (resource/create :source
                       {:content-type :json
                        :form-params form-params
                        :query-params auth-params}))))

(defmethod create :default [& _]
  (throw (Exception. "Unrecognized artifact for source creation.")))

(defn list
  "Retrieves a list of data sources."
  [& params]
  (apply resource/list :source params))

(defn update
  "Updates the specified data source.  Returns the updated source upon
   success."
  [source updates & params]
  (apply resource/update source updates params))

(defn get
  "Retrieves a data source."
  [source & params]
  (apply resource/get source params))

(defn delete
  "Deletes the specified data source.  Returns nil upon success."
  [source & params]
  (apply resource/delete source params))
