;; Copyright 2012, 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.source
  "Offers functions specific for BigML sources.
      https://bigml.com/developers/sources"
  (:import (java.io ByteArrayInputStream File StringWriter)
           (org.apache.commons.validator.routines UrlValidator))
  (:require (clojure.java [io :as io])
            (clojure.data [csv :as csv])
            (cheshire [core :as json])
            (bigml.api [core :as api]
                       [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn- url? [url]
  (or
   (.isValid (UrlValidator.) url)
   (re-find (re-pattern "^s3://") url)))

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
      https://bigml.com/developers/sources#s_create

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  create-type)

(defmethod create :url [url & params]
  (utils/create :target :source :origin [:remote url] :params params))

(defmethod create :file [file & params]
  (let [file (io/file file)
        params (apply api/query-params params)
        form-params (utils/get-form-params-in params)
        auth-params (select-keys params api/auth-params)
        multipart (map (fn [[k v]] {:name (name k)
                                    :content (if (map? v)
                                               (json/generate-string v)
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
          form-params (assoc (utils/get-form-params-in params)
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
  "Retrieves a list of data sources. The optional parameters can
   include pagination and filtering options detailed here:
      https://bigml.com/developers/sources#s_list

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :source params))
