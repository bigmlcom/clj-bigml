;; Copyright 2016 BigML
;; Licensed under the Apache License, Version 2.0
;; http://www.apache.org/licenses/LICENSE-2.0

(ns bigml.test.api.utils
  "Contains a few facilities especially meant tor testing"
  (:require (bigml.api [core :as api])))

(defn authenticated-connection
  "Returns a properly authenticated connection.
  Credentials shall be provided through the BIGML_USERNAME
  and BIGML_API_KEY environment variables"
  []
  (let [username (System/getenv "BIGML_USERNAME")
        api-key (System/getenv "BIGML_API_KEY")]
    (api/make-connection username api-key)))
