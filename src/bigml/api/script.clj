;; Copyright 2017 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.api.script
  "Offers functions specific for BigML datasets.
      https://bigml.com/developers/datasets"
  (:require (bigml.api [core :as api]
                       [utils :as utils]))
  (:refer-clojure :exclude [list]))

(defn create
  "Creates a script given either a string containing the source code
   or an inputstream from which to read the source code.

   Accepts the optional creation parameters defined in the BigML API
   docs:
      https://bigml.com/developers/scripts#ws_creating_a_script

   HTTP response information is attached as meta data. Exceptions are
   thrown on failure unless :throw-exceptions is set as false (default
   is true), in which case the HTTP response details are returned as
   a map on failure."
  [source-code & params]
  (let [source-code (if (string? source-code) source-code (slurp source-code))
        params (concat params [:source_code source-code])]
    (utils/create :target :script :params params)))

(defn list
  "Retrieves a list of scripts. The optional parameters can include
   pagination and filtering options detailed here:
      https://bigml.com/api/scripts#ws_listing_scripts

   Pagination details are returned as meta data attached to the list,
   along with the HTTP response information.  Exceptions are thrown on
   failure unless :throw-exceptions is set as false (default is true),
   in which case the HTTP response details are returned as a map on
   failure."
  [& params]
  (apply api/list :script params))
