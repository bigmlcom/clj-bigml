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

(defn- do-verb
  "this method is a sort of universal wrappers around individual create
   functions that reside in specific namespaces. it enables the syntax:
      (create source file-path options)
   in place of the more cumbersome
      (bigml.api.source/create file-path options)"
  [verb res-type res-uuid & params]
  (api/get-final (apply
                  (ns-resolve
                   (symbol (clojure.string/join
                            ["bigml.api." res-type]))
                   (symbol verb))
                  res-uuid params)))

(defn create
  "This method creates either a single resource, or a sequence of resources
   res-type is the resource type(s) to create, either a string or an array
   res-uuid is the uuid of the resource which is used to create the requested
         resource or the first of the sequence; it can be a file path
   params is either a list of option for the single-resource case, or
       a map from a resource type (represented by a keyword) and
       another map representing the options to use for that resource type"
  [res-type res-uuid & params]
  (if (string? res-type)
    (apply do-verb "create" res-type res-uuid params)
    (api/get-final (reduce
                    #(:resource
                      (apply create %2 %1 ((keyword %2) (first params))))
                    res-uuid
                    res-type))))
