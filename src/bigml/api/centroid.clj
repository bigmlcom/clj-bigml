;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.centroid
  "Offers functions specific for BigML centroids.
     https://bigml.com/developers/centroids."
  (:require (bigml.api [core :as api] [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a centroid from a cluster. A cluster can be represented through its
  id (e.g., `cluster/123123`), or a map as returned by `get` or `list`.

   The inputs may either be a map (field ids to values), or a
   sequence of the inputs fields in the order they appeared during
   training.

  Accepts the optional creation parameters described in the BigML API docs:
     https://bigml.com/developers/centroids#ct_centroid_arguments

  HTTP response information is attached as meta data. Exceptions are thrown
  on failure unless :throw-exceptions is set to false (default is true),
  in which case the HTTP response details are returned as a map on failure."
  [cluster inputs & params]
  (utils/create :target :centroid
                :origin [:cluster cluster]
                :params params
                :inputs inputs))

(defn list
  "Retrieves a list of centroids. The optional parameters can include
  pagination and filtering options detailed here:
     https://bigml.com/developers/centroids#ct_listing_centroids

  Pagination details are returned as meta data attached to the list, along
  with the HTTP response information. Exceptions are thrown on failure unless
  :throw-exceptions is set to false (default is true), in which case the
  HTTP response details are returned in a map on failure."
  [& params]
  (apply api/list :centroid params))
