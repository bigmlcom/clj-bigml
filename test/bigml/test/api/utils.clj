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
  (apply (ns-resolve
          (symbol (clojure.string/join ["bigml.api." res-type]))
          (symbol verb))
         res-uuid params))

(defn create
  "This function creates either a single resource, or a sequence of resources.
   It returns an array of the UUID of the resource created or an array thereof.
   res-type is the resource type(s) to create, either a string or an array
   res-uuid is the uuid of the resource which is used to create the requested
         resource or the first one of the sequence; it can be a file path/url
   params is either a list of options for the single-resource case, or
       a map from a resource type (represented by a keyword) and
       another map representing the options to use for that resource type"
  [res-type res-uuid & params]
  (if (string? res-type)
    (api/get-final (apply do-verb "create" res-type res-uuid params))
    (reduce
     #(conj %1 (:resource
                (apply create %2 (last %1) ((keyword %2) (first params)))))
     [res-uuid]
     res-type)))

(defn create-get-cleanup
  "This function wraps create so it does a GET of the last resource
   returned by create and returns it; additionally, it deletes remotely
   all created resources."
  [res-type res-uuid & params]
  (let [rs (apply create res-type res-uuid params)
        result (api/get-final (last rs))]
    (doall (pmap api/delete (drop 1 rs)))
    result))
